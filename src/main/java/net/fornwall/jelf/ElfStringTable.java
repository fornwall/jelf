package net.fornwall.jelf;

/**
 * String table sections hold null-terminated character sequences, commonly called strings.
 *
 * The object file uses these strings to represent symbol and section names.
 *
 * You reference a string as an index into the string table section.
 */
public final class ElfStringTable extends ElfSection {

    /** The string table data. */
    private final byte[] data;

    public final int numStrings;

    /** Reads all the strings from [offset, length]. */
    ElfStringTable(ElfParser parser, long offset, int length, ElfSectionHeader header) throws ElfException {
        super(parser, header);

        parser.seek(offset);
        data = new byte[length];
        int bytesRead = parser.read(data);
        if (bytesRead != length)
            throw new ElfException("Error reading string table (read " + bytesRead + "bytes - expected to " + "read "
                    + data.length + "bytes)");

        int stringsCount = 0;
        for (byte datum : data) if (datum == '\0') stringsCount++;
        numStrings = stringsCount;
    }

    static ElfStringTable linkedFrom(ElfFile elfFile, ElfSectionHeader header) throws ElfException {
        long linkIndex = header.sh_link;
        String linkDescription = " (sh_link=" + linkIndex + ")";
        if (linkIndex <= 0 || linkIndex >= elfFile.e_shnum) {
            throw new ElfException("No string table linked from the section of type 0x"
                    + Long.toHexString(header.sh_type) + " in a file with " + elfFile.e_shnum + " sections"
                    + linkDescription);
        }
        ElfSection linkedSection = elfFile.getSection((int) linkIndex);
        if (!(linkedSection instanceof ElfStringTable stringTable)) {
            throw new ElfException("The section linked from the section of type 0x"
                    + Long.toHexString(header.sh_type) + " is not a string table, but of type 0x"
                    + Long.toHexString(linkedSection.header.sh_type) + linkDescription);
        }
        return stringTable;
    }

    public String get(long index) {
        if (index < 0 || index >= data.length) throw new ElfException("String table index out of range: " + index);
        int start = (int) index;
        int end = start;
        while (end < data.length && data[end] != '\0') end++;
        if (end == data.length) throw new ElfException("Unterminated string at index " + index);
        return new String(data, start, end - start);
    }

    public String get(int index) {
        return get((long) index);
    }
}
