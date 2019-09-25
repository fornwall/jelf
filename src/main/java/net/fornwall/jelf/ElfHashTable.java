package net.fornwall.jelf;

public class ElfHashTable extends ElfSection {

    ElfHashTable(ElfParser parser, ElfSectionHeader header) {
        super(header);

        parser.seek(header.section_offset);

        int num_buckets = parser.readInt();
        int num_chains = parser.readInt();

        // These could probably be memoized.
        int[] buckets = new int[num_buckets];
        int[] chains = new int[num_chains];
        // Read the bucket data.
        for (int i = 0; i < num_buckets; i++) {
            buckets[i] = parser.readInt();
        }

        // Read the chain data.
        for (int i = 0; i < num_chains; i++) {
            chains[i] = parser.readInt();
        }

        // Make sure that the amount of bytes we were supposed to read
        // was what we actually read.
        int actual = num_buckets * 4 + num_chains * 4 + 8;
        if (header.size != actual) {
            throw new ElfException("Error reading string table (read " + actual + "bytes, expected to read " + header.size + "bytes).");
        }
    }

}
