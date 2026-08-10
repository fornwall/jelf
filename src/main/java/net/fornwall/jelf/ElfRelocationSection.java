package net.fornwall.jelf;

public final class ElfRelocationSection extends ElfSection {
    public final ElfRelocation[] relocations;

    ElfRelocationSection(ElfParser parser, ElfSectionHeader header) {
        super(parser, header);

        if (header.sh_entsize <= 0) {
            throw new ElfException("Invalid relocation entry size: " + header.sh_entsize);
        }
        // A trailing partial entry is ignored instead of rejected, since only whole entries can be read.
        int numEntries = ElfFile.arraySize(header.sh_size / header.sh_entsize, "relocation count");
        relocations = new ElfRelocation[numEntries];
        for (int i = 0; i < numEntries; i++) {
            final long relOffset = header.sh_offset + (i * header.sh_entsize);
            relocations[i] = new ElfRelocation(parser, relOffset, header);
        }
    }

    /**
     * The symbol table that the {@link ElfRelocation#getSymbolIndex() symbol indexes} of the {@link #relocations}
     * refer to, as specified by the {@link ElfSectionHeader#sh_link} field of this section.
     *
     * @throws ElfException if this section does not link to a symbol table
     */
    public ElfSymbolTableSection getSymbolTableSection() throws ElfException {
        return ElfSymbolTableSection.linkedFrom(parser.elfFile, header);
    }
}
