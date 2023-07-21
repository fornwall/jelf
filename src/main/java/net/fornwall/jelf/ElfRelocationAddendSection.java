package net.fornwall.jelf;

public final class ElfRelocationAddendSection extends ElfSection {
    public final ElfRelocationAddend[] relocations;

    public ElfRelocationAddendSection(ElfParser parser, ElfSectionHeader header) {
        super(header);

        int num_entries = (int) (header.sh_size / header.sh_entsize);
        relocations = new ElfRelocationAddend[num_entries];
        for (int i = 0; i < num_entries; i++) {
            final long relOffset = header.sh_offset + (i * header.sh_entsize);
            relocations[i] = new ElfRelocationAddend(parser, relOffset);
        }
    }

}
