package net.fornwall.jelf;

/** Package internal class used for parsing ELF files. */
class ElfParser {

	final ElfFile elfFile;
	private final BackingFile backingFile;

    ElfParser(ElfFile elfFile, BackingFile backingFile) {
        this.elfFile = elfFile;
		this.backingFile = backingFile;
	}

	public void seek(long offset) {
	    backingFile.seek(offset);
	}

	public void skip(int bytesToSkip) {
		backingFile.skip(bytesToSkip);
	}
	
	short readUnsignedByte() {
	    return backingFile.readUnsignedByte();
	}

	short readShort() throws ElfException {
		int ch1 = readUnsignedByte();
		int ch2 = readUnsignedByte();
		if (elfFile.ei_data == ElfFile.DATA_LSB) {
			return (short) (((short) ch2 & 0xff) << 8 | ((short) ch1 & 0xff));
		} else {
			return (short) (((short) ch1 & 0xff) << 8 | ((short) ch2 & 0xff));
		}
	}

	int readInt() throws ElfException {
		int ch1 = readUnsignedByte();
		int ch2 = readUnsignedByte();
		int ch3 = readUnsignedByte();
		int ch4 = readUnsignedByte();
		if (elfFile.ei_data == ElfFile.DATA_LSB) {
			return ((int) ch4 & 0xff) << 24 | ((int) ch3 & 0xff) << 16 | ((int) ch2 & 0xff) << 8 | ((int) ch1 & 0xff);
		} else {
			return ((int) ch1 & 0xff) << 24 | ((int) ch2 & 0xff) << 16 | ((int) ch3 & 0xff) << 8 | ((int) ch4 & 0xff);
		}
	}

	long readLong() {
		int ch1 = readUnsignedByte();
		int ch2 = readUnsignedByte();
		int ch3 = readUnsignedByte();
		int ch4 = readUnsignedByte();
		int ch5 = readUnsignedByte();
		int ch6 = readUnsignedByte();
		int ch7 = readUnsignedByte();
		int ch8 = readUnsignedByte();

		if (elfFile.ei_data == ElfFile.DATA_LSB) {
			return ((long) ch8 << 56) | ((long) ch7 & 0xff) << 48 | ((long) ch6 & 0xff) << 40
					| ((long) ch5 & 0xff) << 32 | ((long) ch4 & 0xff) << 24 | ((long) ch3 & 0xff) << 16
					| ((long) ch2 & 0xff) << 8 | ((long) ch1 & 0xff);
		} else {
			return ((long) ch1 << 56) | ((long) ch2 & 0xff) << 48 | ((long) ch3 & 0xff) << 40
					| ((long) ch4 & 0xff) << 32 | ((long) ch5 & 0xff) << 24 | ((long) ch6 & 0xff) << 16
					| ((long) ch7 & 0xff) << 8 | ((long) ch8 & 0xff);
		}
	}

	/** Read four-byte int or eight-byte long depending on if {@link ElfFile#ei_class}. */
	long readIntOrLong() {
		return elfFile.ei_class == ElfFile.CLASS_32 ? readInt() : readLong();
	}

	/** Returns a big-endian unsigned representation of the int. */
	long unsignedByte(int arg) {
		long val;
		if (arg >= 0) {
			val = arg;
		} else {
			val = (unsignedByte((short) (arg >>> 16)) << 16) | ((short) arg);
		}
		return val;
	}

	/**
	 * Find the file offset from a virtual address by looking up the {@link ElfSegment} segment containing the
	 * address and computing the resulting file offset.
	 */
	long virtualMemoryAddrToFileOffset(long address) {
		for (int i = 0; i < elfFile.e_phnum; i++) {
			ElfSegment ph = elfFile.getProgramHeader(i);
			if (address >= ph.p_vaddr && address < (ph.p_vaddr + ph.p_memsz)) {
				long relativeOffset = address - ph.p_vaddr;
				if (relativeOffset >= ph.p_filesz)
					throw new ElfException("Can not convert virtual memory address " + Long.toHexString(address) + " to file offset -" + " found segment " + ph
							+ " but address maps to memory outside file range");
				return ph.p_offset + relativeOffset;
			}
		}
		throw new ElfException("Cannot find segment for address " + Long.toHexString(address));
	}

	public int read(byte[] data) {
	    return backingFile.read(data);
	}

}
