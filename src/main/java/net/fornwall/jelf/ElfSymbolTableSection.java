package net.fornwall.jelf;

public class ElfSymbolTableSection extends ElfSection {

    public ElfSymbol[] symbols;

    public ElfSymbolTableSection(ElfParser parser, ElfSectionHeader header) {
        super(header);

        int num_entries = (int) (header.size / header.entry_size);
        symbols = new ElfSymbol[num_entries];
        for (int i = 0; i < num_entries; i++) {
            final long symbolOffset = header.section_offset + (i * header.entry_size);
            symbols[i] = new ElfSymbol(parser, symbolOffset, header.type);
        }
    }
}
