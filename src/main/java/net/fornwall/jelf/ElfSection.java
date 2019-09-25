package net.fornwall.jelf;

public class ElfSection {
    public final ElfSectionHeader header;

    public ElfSection(ElfSectionHeader header) {
        this.header = header;
    }
}
