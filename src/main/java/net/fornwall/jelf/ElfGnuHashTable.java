package net.fornwall.jelf;

/**
 * An ELF section containing a hash table for lookup of dynamic symbols.
 *
 * Has the section type {@link ElfSectionHeader#SHT_GNU_HASH}.
 *
 * Replaces {@link ElfHashTable} on almost all modern Linux systems.
 *
 * See https://flapenguin.me/2017/05/10/elf-lookup-dt-gnu-hash/
 */
public class ElfGnuHashTable extends ElfSection {

    private final int ELFCLASS_BITS;
    // The number of .dynsym symbols skipped.
    final long symoffset;
    final long bloom_shift;
    final long[] bloom;
    final long[] buckets;
    long[] chain;

    ElfGnuHashTable(ElfParser parser, ElfSectionHeader header) {
        super(parser, header);

        ELFCLASS_BITS = parser.elfFile.ei_class == ElfFile.CLASS_32 ? 32 : 64;

        parser.seek(header.sh_offset);
        long numberOfBucketsValue = parser.readUnsignedInt();
        symoffset = parser.readUnsignedInt();
        long bloomSizeValue = parser.readUnsignedInt();
        bloom_shift = parser.readUnsignedInt();
        int bloomWordSize = ELFCLASS_BITS / 8;
        if (header.sh_size < 16 || bloomSizeValue > (header.sh_size - 16) / bloomWordSize) {
            throw new ElfException("GNU hash bloom filter exceeds section size");
        }
        long remaining = header.sh_size - 16 - bloomSizeValue * bloomWordSize;
        if (numberOfBucketsValue > remaining / 4) {
            throw new ElfException("GNU hash buckets exceed section size");
        }
        int numberOfBuckets = ElfFile.arraySize(numberOfBucketsValue, "GNU hash bucket count");
        int bloomSize = ElfFile.arraySize(bloomSizeValue, "GNU hash bloom size");
        if (numberOfBuckets == 0 || bloomSize == 0) throw new ElfException("Invalid empty GNU hash table");
        bloom = new long[bloomSize];
        buckets = new long[numberOfBuckets];

        for (int i = 0; i < bloomSize; i++) {
            bloom[i] = parser.readUnsignedIntOrLong();
        }
        for (int i = 0; i < numberOfBuckets; i++) {
            buckets[i] = parser.readUnsignedInt();
        }
        // The chain is initialized on first use in lookupSymbol() due to it requiring .dynsym size.
    }

    ElfSymbol lookupSymbol(String symbolName, ElfSymbolTableSection symbolTable) {
        if (chain == null) {
            if (symoffset > symbolTable.symbols.length) {
                throw new ElfException("GNU hash symbol offset out of range: " + symoffset);
            }
            int chainSize = (int) (symbolTable.symbols.length - symoffset);
            chain = new long[chainSize];
            parser.seek(
                    header.sh_offset + 4 * 4 + (long) bloom.length * (ELFCLASS_BITS / 8) + (long) buckets.length * 4);
            for (int i = 0; i < chainSize; i++) {
                chain[i] = parser.readUnsignedInt();
            }
        }

        final int nameHash = gnuHash(symbolName);

        long word = bloom[(Integer.remainderUnsigned(Integer.divideUnsigned(nameHash, ELFCLASS_BITS), bloom.length))];
        long mask = 1L << (long) (Integer.remainderUnsigned(nameHash, ELFCLASS_BITS))
                | 1L << (long) (Integer.remainderUnsigned((nameHash >>> bloom_shift), ELFCLASS_BITS));

        if ((word & mask) != mask) {
            // If at least one bit is not set, a symbol is surely missing.
            return null;
        }

        long symix = buckets[Integer.remainderUnsigned(nameHash, buckets.length)];
        if (symix < symoffset) {
            return null;
        }

        while (true) {
            long chainIndex = symix - symoffset;
            if (chainIndex >= chain.length || symix >= symbolTable.symbols.length) {
                throw new ElfException("GNU hash symbol index out of range: " + symix);
            }
            long hash = chain[(int) chainIndex];

            if ((Integer.toUnsignedLong(nameHash) | 1L) == (hash | 1L)) {
                // The chain contains contiguous sequences of hashes for symbols hashing to the same index,
                // with the lowest bit discarded (used to signal end of chain).
                ElfSymbol symbol = symbolTable.symbols[(int) symix];
                if (symbolName.equals(symbol.getName())) return symbol;
            }

            if ((hash & 1) != 0) {
                // Chain ends with an element with the lowest bit set to 1.
                break;
            }

            symix++;
        }

        return null;
    }

    static int gnuHash(String name) {
        int h = 5381;
        int nameLength = name.length();
        for (int i = 0; i < nameLength; i++) {
            char c = name.charAt(i);
            h = (h << 5) + h + c;
        }
        return h;
    }
}
