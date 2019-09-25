package net.fornwall.jelf;

import java.io.IOException;

/**
 * String table sections hold null-terminated character sequences, commonly called strings.
 *
 * The object file uses these strings to represent symbol and section names.
 *
 * You reference a string as an index into the string table section.
 */
final class ElfStringTable extends ElfSection {

	/** The string table data. */
	private final byte[] data;
	public final int numStrings;

	/** Reads all the strings from [offset, length]. */
	ElfStringTable(ElfParser parser, long offset, int length, ElfSectionHeader header) throws ElfException, IOException {
		super(header);

		parser.seek(offset);
		data = new byte[length];
		int bytesRead = parser.read(data);
		if (bytesRead != length)
			throw new ElfException("Error reading string table (read " + bytesRead + "bytes - expected to " + "read " + data.length + "bytes)");

		int stringsCount = 0;
		for (byte datum : data) if (datum == '\0') stringsCount++;
		numStrings = stringsCount;
	}

	String get(int index) {
		int endPtr = index;
		while (data[endPtr] != '\0')
			endPtr++;
		return new String(data, index, endPtr - index);
	}
}
