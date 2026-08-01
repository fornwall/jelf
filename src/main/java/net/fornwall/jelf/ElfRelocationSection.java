package net.fornwall.jelf;

public final class ElfRelocationSection extends ElfSection {
    public final ElfRelocation[] relocations;

    ElfRelocationSection(ElfParser parser, ElfSectionHeader header) {
        super(parser, header);

        int num_entries = (int) (header.sh_size / header.sh_entsize);
        relocations = new ElfRelocation[num_entries];
        for (int i = 0; i < num_entries; i++) {
            final long relOffset = header.sh_offset + (i * header.sh_entsize);
            relocations[i] = new ElfRelocation(parser, relOffset, header);
        }
    }

    /**
     * The symbol table that the {@link ElfRelocation#getSymbolIndex() symbol indexes} of the {@link #relocations}
     * refer to, as specified by the {@link ElfSectionHeader#sh_link} field of this section.
     */
    public ElfSymbolTableSection getSymbolTableSection() throws ElfException {
        return ElfSymbolTableSection.linkedFrom(parser.elfFile, header);
    }
}
