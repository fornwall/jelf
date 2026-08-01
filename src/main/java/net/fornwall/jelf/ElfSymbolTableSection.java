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

        int num_entries = (int) (header.sh_size / header.sh_entsize);
        symbols = new ElfSymbol[num_entries];
        for (int i = 0; i < num_entries; i++) {
            final long symbolOffset = header.sh_offset + (i * header.sh_entsize);
            symbols[i] = new ElfSymbol(parser, symbolOffset, header.sh_type);
        }
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
        final int linkIndex = header.sh_link;
        if (linkIndex <= 0 || linkIndex >= elfFile.e_shnum) {
            throw new ElfException("No symbol table linked from section of type 0x" + Long.toHexString(header.sh_type)
                    + " (sh_link=" + linkIndex + ", number of sections=" + elfFile.e_shnum + ")");
        }
        ElfSection linkedSection = elfFile.getSection(linkIndex);
        if (!(linkedSection instanceof ElfSymbolTableSection symbolTableSection)) {
            throw new ElfException("The section linked from section of type 0x" + Long.toHexString(header.sh_type)
                    + " is not a symbol table, but of type 0x" + Long.toHexString(linkedSection.header.sh_type)
                    + " (sh_link=" + linkIndex + ")");
        }
        return symbolTableSection;
    }
}
