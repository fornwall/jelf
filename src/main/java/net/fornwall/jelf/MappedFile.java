package net.fornwall.jelf;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

public class MappedFile implements BackingFile {
    private final MappedByteBuffer mappedByteBuffer;

    public MappedFile(MappedByteBuffer mappedByteBuffer) {
        this.mappedByteBuffer = mappedByteBuffer;
        this.mappedByteBuffer.position((int) 0);
    }

    public void seek(long offset) {
        try {
            this.mappedByteBuffer.position((int)(offset)); // we may be limited to sub-4GB mapped files
        } catch (IllegalArgumentException e) {
            throw new ElfException("Seek out of range (offset=" + offset + ", limit=" + mappedByteBuffer.limit() + ")");
        }
    }

    public void skip(int bytesToSkip) {
        int target = mappedByteBuffer.position() + bytesToSkip;
        try {
            mappedByteBuffer.position(target);
        } catch (IllegalArgumentException e) {
            throw new ElfException("Skip out of range (target=" + target + ", limit=" + mappedByteBuffer.limit() + ")");
        }
    }

    public short readUnsignedByte() {
        if (!mappedByteBuffer.hasRemaining()) {
            throw new ElfException("Trying to read outside file");
        }

        return (short) (mappedByteBuffer.get() & 0xFF);
    }

    public int read(byte[] data) {
        int position = mappedByteBuffer.position();
        try {
            mappedByteBuffer.get(data);
        } catch (BufferUnderflowException e) {
            throw new ElfException("Error reading " + data.length + " bytes at position " + position + " (remaining=" + (mappedByteBuffer.limit() - position) + ")");
        }
        return data.length;
    }

    public byte get() {
        return mappedByteBuffer.get();
    }

    public int write(byte[] data) {
        mappedByteBuffer.put(data);
        return data.length;
    }

    public void put(byte data) {
        mappedByteBuffer.put(data);
    }

    public ByteBuffer getBuffer() {
        return mappedByteBuffer;
    }
}
