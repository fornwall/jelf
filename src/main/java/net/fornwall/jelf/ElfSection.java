package net.fornwall.jelf;

public class ElfSection {
    public final ElfSectionHeader header;
    protected final ElfParser parser;

    ElfSection(ElfParser parser, ElfSectionHeader header) {
        this.header = header;
        this.parser = parser;
    }

    /**
     * Get the bytes contained in this ELF section.
     */
    public byte[] getData() {
        if (header.sh_size == 0 || header.sh_type == ElfSectionHeader.SHT_NOBITS || header.sh_type == ElfSectionHeader.SHT_NULL) {
            return new byte[0];
        } else if (header.sh_size > (long) Integer.MAX_VALUE) {
            throw new ElfException("Too big section: " + header.sh_size);
        }

        byte[] result = new byte[(int) header.sh_size];
        parser.seek(header.sh_offset);
        parser.read(result);
        return result;
    }

}
