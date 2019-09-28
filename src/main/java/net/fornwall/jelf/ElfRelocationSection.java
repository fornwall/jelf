package net.fornwall.jelf;

public class ElfRelocationSection extends ElfSection {

    public ElfRelocationSection(ElfParser parser, ElfSectionHeader header) {
        super(header);

        int num_entries = (int) (header.size / header.entry_size);
    }

}
