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

    int readUnsignedShort() throws ElfException {
        int ch1 = readUnsignedByte();
        int ch2 = readUnsignedByte();
        if (elfFile.ei_data == ElfFile.DATA_LSB) {
            return (ch2 & 0xff) << 8 | (ch1 & 0xff);
        } else {
            return (ch1 & 0xff) << 8 | (ch2 & 0xff);
        }
    }

    int readInt() throws ElfException {
        int ch1 = readUnsignedByte();
        int ch2 = readUnsignedByte();
        int ch3 = readUnsignedByte();
        int ch4 = readUnsignedByte();
        if (elfFile.ei_data == ElfFile.DATA_LSB) {
            return (ch4 & 0xff) << 24 | (ch3 & 0xff) << 16 | (ch2 & 0xff) << 8 | (ch1 & 0xff);
        } else {
            return (ch1 & 0xff) << 24 | (ch2 & 0xff) << 16 | (ch3 & 0xff) << 8 | (ch4 & 0xff);
        }
    }

    long readUnsignedInt() throws ElfException {
        return Integer.toUnsignedLong(readInt());
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
            return ((long) ch8 << 56)
                    | ((long) ch7 & 0xff) << 48
                    | ((long) ch6 & 0xff) << 40
                    | ((long) ch5 & 0xff) << 32
                    | ((long) ch4 & 0xff) << 24
                    | ((long) ch3 & 0xff) << 16
                    | ((long) ch2 & 0xff) << 8
                    | ((long) ch1 & 0xff);
        } else {
            return ((long) ch1 << 56)
                    | ((long) ch2 & 0xff) << 48
                    | ((long) ch3 & 0xff) << 40
                    | ((long) ch4 & 0xff) << 32
                    | ((long) ch5 & 0xff) << 24
                    | ((long) ch6 & 0xff) << 16
                    | ((long) ch7 & 0xff) << 8
                    | ((long) ch8 & 0xff);
        }
    }

    long readUnsignedIntOrLong() {
        return elfFile.ei_class == ElfFile.CLASS_32 ? readUnsignedInt() : readLong();
    }

    long readSignedIntOrLong() {
        return elfFile.ei_class == ElfFile.CLASS_32 ? readInt() : readLong();
    }

    /**
     * Find the file offset from a virtual address by looking up the {@link ElfSegment} segment containing the
     * address and computing the resulting file offset.
     */
    long virtualMemoryAddrToFileOffset(long address) {
        for (int i = 0; i < elfFile.e_phnum; i++) {
            ElfSegment ph = elfFile.getProgramHeader(i);
            long relativeOffset = address - ph.p_vaddr;
            if (Long.compareUnsigned(address, ph.p_vaddr) >= 0
                    && Long.compareUnsigned(relativeOffset, ph.p_memsz) < 0) {
                if (Long.compareUnsigned(relativeOffset, ph.p_filesz) >= 0)
                    throw new ElfException(
                            "Can not convert virtual memory address " + Long.toHexString(address) + " to file offset -"
                                    + " found segment " + ph + " but address maps to memory outside file range");
                return ph.p_offset + relativeOffset;
            }
        }
        throw new ElfException("Cannot find segment for address " + Long.toHexString(address));
    }

    public int read(byte[] data) {
        return backingFile.read(data);
    }
}
