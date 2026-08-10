package net.fornwall.jelf;

/**
 * An ELF section containing a hash table for lookup of dynamic symbols.
 *
 * Note that this has been replaced with {@link ElfGnuHashTable} on modern Linux systems.
 *
 * See https://flapenguin.me/2017/04/24/elf-lookup-dt-hash/
 */
public class ElfHashTable extends ElfSection {

    private final long[] buckets;
    private final long[] chain;

    ElfHashTable(ElfParser parser, ElfSectionHeader header) {
        super(parser, header);

        parser.seek(header.sh_offset);

        long numBucketsValue = parser.readUnsignedInt();
        long numChainsValue = parser.readUnsignedInt();
        if (header.sh_size < 8 || numBucketsValue + numChainsValue > (header.sh_size - 8) / 4) {
            throw new ElfException("Hash table entries exceed section size");
        }
        int numBuckets = ElfFile.arraySize(numBucketsValue, "hash bucket count");
        int numChains = ElfFile.arraySize(numChainsValue, "hash chain count");
        if (numBuckets == 0) throw new ElfException("Invalid hash bucket count: 0");

        buckets = new long[numBuckets];
        for (int i = 0; i < numBuckets; i++) {
            buckets[i] = parser.readUnsignedInt();
        }

        chain = new long[numChains];
        for (int i = 0; i < numChains; i++) {
            chain[i] = parser.readUnsignedInt();
        }

        // Make sure that the amount of bytes we were supposed to read
        // was what we actually read.
        long actual = (long) numBuckets * 4 + (long) numChains * 4 + 8;
        if (header.sh_size != actual) {
            throw new ElfException("Error reading string table (read " + actual + "bytes, expected to read "
                    + header.sh_size + "bytes).");
        }
    }

    public ElfSymbol lookupSymbol(String name, ElfSymbolTableSection symbolTable) {
        long hashValue = elfHash(name);
        long index = buckets[(int) (hashValue % buckets.length)];
        while (true) {
            if (index == 0) return null;
            if (index >= symbolTable.symbols.length || index >= chain.length) {
                throw new ElfException("Hash symbol index out of range: " + index);
            }
            ElfSymbol symbol = symbolTable.symbols[(int) index];
            if (name.equals(symbol.getName())) return symbol;
            index = chain[(int) index];
        }
    }

    static long elfHash(String name) {
        long hash = 0;
        int nameLength = name.length();
        for (int i = 0; i < nameLength; i++) {
            hash = (hash << 4) + name.charAt(i);
            long x = hash & 0xF0000000L;
            if (x != 0) hash ^= (x >> 24);
            hash &= ~x;
        }
        return hash;
    }
}
