package net.fornwall.jelf;

public interface BackingFile {

    /**
     * The number of bytes available from this backing file, or {@code -1} if it is not known.
     */
    default long size() {
        return -1;
    }

    void seek(long offset);

    void skip(int bytesToSkip);

    short readUnsignedByte();

    int read(byte[] data);
}
