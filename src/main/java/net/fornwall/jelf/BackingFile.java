package net.fornwall.jelf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.MappedByteBuffer;

interface BackingFile {

    public void seek(long offset);
    public void skip(int bytesToSkip);
    short readUnsignedByte();
    public int read(byte[] data);

}
