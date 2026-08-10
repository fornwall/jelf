package net.fornwall.jelf;

/**
 * An ELF section with symbol information.
 *
 * This class represents either of two section types:
 * <ul>
 *     <li>{@link ElfSectionHeader#SHT_DYNSYM}: For a minimal set of symbols adequate for dynamic linking. Can be stripped and has no runtime cost (is non-allocable). Normally named ".dynsym".</li>
 *     <li>{@link ElfSectionHeader#SHT_SYMTAB}: A complete symbol table typically used for link editing. Can not be stripped (is allocable). Normally named ".symtab".</li>
 * </ul>
 */
public class ElfSymbolTableSection extends ElfSection {

    public final ElfSymbol[] symbols;

    ElfSymbolTableSection(ElfParser parser, ElfSectionHeader header) {
        super(parser, header);

        if (header.sh_entsize <= 0 || header.sh_size % header.sh_entsize != 0) {
            throw new ElfException("Invalid symbol table entry size: " + header.sh_entsize);
        }
        int numEntries = ElfFile.arraySize(header.sh_size / header.sh_entsize, "symbol count");
        symbols = new ElfSymbol[numEntries];
        for (int i = 0; i < numEntries; i++) {
            final long symbolOffset = header.sh_offset + (i * header.sh_entsize);
            symbols[i] = new ElfSymbol(parser, symbolOffset, header);
        }
    }

    /**
     * The string table containing the names of this table's symbols.
     *
     * @throws ElfException if this symbol table does not link to a string table
     */
    public ElfStringTable getStringTableSection() throws ElfException {
        return ElfStringTable.linkedFrom(parser.elfFile, header);
    }

    /**
     * The symbol table section referred to by the {@link ElfSectionHeader#sh_link} field of the specified
     * section header, which for relocation sections is the symbol table that relocation entries index into.
     *
     * @throws ElfException if sh_link is not the index of a symbol table section
     */
    static ElfSymbolTableSection linkedFrom(ElfFile elfFile, ElfSectionHeader header) throws ElfException {
        // Note that the section header name is not used when describing the sections below, since looking
        // it up may fail for the same type of malformed files that make us end up here in the first place.
        final long linkIndex = header.sh_link;
        final String linkDescription = " (sh_link=" + linkIndex + ")";
        if (linkIndex <= 0 || linkIndex >= elfFile.e_shnum) {
            throw new ElfException("No symbol table linked from the section of type 0x"
                    + Long.toHexString(header.sh_type) + " in a file with " + elfFile.e_shnum + " sections"
                    + linkDescription);
        }
        ElfSection linkedSection = elfFile.getSection((int) linkIndex);
        if (!(linkedSection instanceof ElfSymbolTableSection symbolTableSection)) {
            throw new ElfException("The section linked from the section of type 0x"
                    + Long.toHexString(header.sh_type) + " is not a symbol table, but of type 0x"
                    + Long.toHexString(linkedSection.header.sh_type) + linkDescription);
        }
        return symbolTableSection;
    }
}
