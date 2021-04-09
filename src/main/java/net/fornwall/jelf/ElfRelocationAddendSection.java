package net.fornwall.jelf;

public class ElfRelocationAddendSection extends ElfSection {

    public ElfRelocationAddendSection(ElfParser parser, ElfSectionHeader header) {
        super(header);

        int num_entries = (int) (header.size / header.entry_size);
    }

}
