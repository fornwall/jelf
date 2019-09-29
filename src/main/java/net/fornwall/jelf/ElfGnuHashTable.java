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

    private final ElfParser parser;
    private final int ELFCLASS_BITS;
    // The number of .dynsym symbols skipped.
    int symbolOffset;
    int bloomShift;
    long[] bloomFilter;
    int[] buckets;
    int[] chain;

    ElfGnuHashTable(ElfParser parser, ElfSectionHeader header) {
        super(header);
        this.parser = parser;

        ELFCLASS_BITS = parser.elfFile.objectSize == ElfFile.CLASS_32 ? 32 : 64;

        parser.seek(header.section_offset);
        int numberOfBuckets = parser.readInt();
        symbolOffset = parser.readInt();
        int bloomSize = parser.readInt();
        bloomShift = parser.readInt();
        bloomFilter = new long[bloomSize];
        buckets = new int[numberOfBuckets];

        for (int i = 0; i < bloomSize; i++) {
            bloomFilter[i] = parser.readIntOrLong();
        }
        for (int i = 0; i < numberOfBuckets; i++) {
            buckets[i] = parser.readInt();
        }
        // The chain is initialized on first use in lookupSymbol() due to it requiring .dynsym size.
    }

    ElfSymbol lookupSymbol(String symbolName, ElfSymbolTableSection symbolTable) {
        if (chain == null) {
            int chainSize = ((ElfSymbolTableSection) parser.elfFile.firstSectionByType(ElfSectionHeader.SHT_DYNSYM)).symbols.length - symbolOffset;
            chain = new int[chainSize];
            parser.seek(header.section_offset + 4*4 + bloomFilter.length*(ELFCLASS_BITS/8) + buckets.length * 4);
            for (int i = 0; i < chainSize; i++) {
                chain[i] = parser.readInt();
            }
        }

        final int nameHash = gnuHash(symbolName);

        long word = bloomFilter[(Integer.remainderUnsigned(Integer.divideUnsigned(nameHash, ELFCLASS_BITS), bloomFilter.length))];
        long mask = 1L << (long) (Integer.remainderUnsigned(nameHash, ELFCLASS_BITS))
                | 1L << (long) (Integer.remainderUnsigned((nameHash >>> bloomShift), ELFCLASS_BITS));

        if ((word & mask) != mask) {
            // If at least one bit is not set, a symbol is surely missing.
            return null;
        }

        int symix = buckets[Integer.remainderUnsigned(nameHash, buckets.length)];
        if (symix < symbolOffset) {
            return null;
        }

        while (true) {
            int hash = chain[symix - symbolOffset];

            if ((((long) nameHash)|1L) == (((long) hash)|1L)) {
                // The chain contains contiguous sequences of hashes for symbols hashing to the same index,
                // with the lowest bit discarded (used to signal end of chain).
                ElfSymbol symbol = symbolTable.symbols[symix];
                if (symbolName.equals(symbol.getName())) return symbol;
            }
            ElfSymbol symbol = symbolTable.symbols[symix];

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
