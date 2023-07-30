package net.fornwall.jelf;

public interface BackingFile {

    void seek(long offset);
    void skip(int bytesToSkip);
    short readUnsignedByte();
    int read(byte[] data);

}
