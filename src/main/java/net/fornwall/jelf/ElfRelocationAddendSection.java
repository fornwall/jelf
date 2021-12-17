package net.fornwall.jelf;

public class ElfRelocationAddendSection extends ElfSection {

    public ElfRelocationAddendSection(ElfParser parser, ElfSectionHeader header) {
        super(header);

        int num_entries = (int) (header.sh_size / header.sh_entsize);
    }

}
