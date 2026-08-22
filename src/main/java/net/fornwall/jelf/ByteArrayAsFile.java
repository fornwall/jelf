package net.fornwall.jelf;

import java.io.ByteArrayInputStream;
import java.io.IOException;

class ByteArrayAsFile implements BackingFile {
    private final ByteArrayInputStream byteArray;
    private final int size;

    public ByteArrayAsFile(byte[] buffer) {
        byteArray = new ByteArrayInputStream(buffer);
        size = buffer.length;
    }

    @Override
    public long size() {
        return size;
    }

    public void seek(long offset) {
        if (offset < 0) throw new ElfException("seeking outside file");
        byteArray.reset();
        if (byteArray.skip(offset) != offset) throw new ElfException("seeking outside file");
    }

    public void skip(int bytesToSkip) {
        long skipped = byteArray.skip(bytesToSkip);
        if (skipped != bytesToSkip) {
            throw new ElfException("Wanted to skip " + bytesToSkip + " bytes, but only able to skip " + skipped);
        }
    }

    public short readUnsignedByte() {
        int val = -1;
        val = byteArray.read();
        if (val < 0) throw new ElfException("Trying to read outside file");
        return (short) val;
    }

    public int read(byte[] data) {
        try {
            return byteArray.read(data);
        } catch (IOException e) {
            throw new RuntimeException("Error reading " + data.length + " bytes", e);
        }
    }
}
