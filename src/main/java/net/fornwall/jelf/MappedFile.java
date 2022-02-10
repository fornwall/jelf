package net.fornwall.jelf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.MappedByteBuffer;
import java.nio.ByteBuffer;

public class MappedFile implements BackingFile{
    private final MappedByteBuffer mappedByteBuffer;

    public MappedFile(MappedByteBuffer mappedByteBuffer) {
        this.mappedByteBuffer = mappedByteBuffer;
        this.mappedByteBuffer.position((int) 0);
    }

    public void seek(long offset) {
        this.mappedByteBuffer.position((int)(offset)); // we may be limited to sub-4GB mapped filess
    }

    public void skip(int bytesToSkip) {
        mappedByteBuffer.position(mappedByteBuffer.position() + bytesToSkip);
    }

    public short readUnsignedByte() {
        int val = -1;
        byte temp = mappedByteBuffer.get();
        val = temp & 0xFF; // bytes are signed in Java =_= so assigning them to a longer type risks sign extension.
        if (val < 0) throw new ElfException("Trying to read outside file");
        return (short) val;
    }

    public int read(byte[] data) {
        mappedByteBuffer.get(data);
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
