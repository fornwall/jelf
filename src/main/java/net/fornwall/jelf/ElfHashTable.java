package net.fornwall.jelf;

/**
 * An ELF section containing a hash table for lookup of dynamic symbols.
 *
 * Note that this has been replaced with {@link ElfGnuHashTable} on modern Linux systems.
 *
 * See https://flapenguin.me/2017/04/24/elf-lookup-dt-hash/
 */
public class ElfHashTable extends ElfSection {

    private final int[] buckets;
    private final int[] chain;

    ElfHashTable(ElfParser parser, ElfSectionHeader header) {
        super(header);

        parser.seek(header.sh_offset);

        int num_buckets = parser.readInt();
        int num_chains = parser.readInt();

        buckets = new int[num_buckets];
        for (int i = 0; i < num_buckets; i++) {
            buckets[i] = parser.readInt();
        }

        chain = new int[num_chains];
        for (int i = 0; i < num_chains; i++) {
            chain[i] = parser.readInt();
        }

        // Make sure that the amount of bytes we were supposed to read
        // was what we actually read.
        int actual = num_buckets * 4 + num_chains * 4 + 8;
        if (header.sh_size != actual) {
            throw new ElfException("Error reading string table (read " + actual + "bytes, expected to read " + header.sh_size + "bytes).");
        }
    }

    public ElfSymbol lookupSymbol(String name, ElfSymbolTableSection symbolTable) {
        long hashValue = elfHash(name);
        int index = buckets[(int) (hashValue % buckets.length)];
        while (true) {
            if (index == 0) return null;
            ElfSymbol symbol = symbolTable.symbols[index];
            if (name.equals(symbol.getName())) return symbol;
            index = chain[index];
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
