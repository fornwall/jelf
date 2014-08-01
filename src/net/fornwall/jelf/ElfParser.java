package net.fornwall.jelf;

import java.io.IOException;
import java.io.RandomAccessFile;

/** Package internal class used for parsing ELF files. */
class ElfParser {
	final ElfFile elfFile;
	final RandomAccessFile fsFile;

	ElfParser(ElfFile elfFile, RandomAccessFile fsFile) {
		this.elfFile = elfFile;
		this.fsFile = fsFile;
	}

	/**
	 * Signed byte utility functions used for converting from big-endian (MSB) to little-endian (LSB).
	 */
	short byteSwap(short arg) {
		return (short) ((arg << 8) | ((arg >>> 8) & 0xFF));
	}

	int byteSwap(int arg) {
		return ((byteSwap((short) arg)) << 16) | (((byteSwap((short) (arg >>> 16)))) & 0xFFFF);
	}

	long byteSwap(long arg) {
		return ((((long) byteSwap((int) arg)) << 32) | (((long) byteSwap((int) (arg >>> 32))) & 0xFFFFFFFF));
	}

	byte readByte() throws IOException {
		return fsFile.readByte();
	}

	short readShort() throws ElfException, IOException {
		short val;
		switch (elfFile.encoding) {
		case ElfFile.DATA_LSB:
			val = byteSwap(fsFile.readShort());
			break;
		case ElfFile.DATA_MSB:
			val = fsFile.readShort();
			break;
		default:
			throw new ElfException("Invalid encoding.");
		}
		return val;
	}

	int readInt() throws ElfException, IOException {
		int val;
		switch (elfFile.encoding) {
		case ElfFile.DATA_LSB:
			val = byteSwap(fsFile.readInt());
			break;
		case ElfFile.DATA_MSB:
			val = fsFile.readInt();
			break;
		default:
			throw new ElfException("Invalid encoding.");
		}
		return val;
	}

	long readLong() throws IOException {
		long val;
		switch (elfFile.encoding) {
		case ElfFile.DATA_LSB:
			val = byteSwap(fsFile.readLong());
			break;
		case ElfFile.DATA_MSB:
			val = fsFile.readLong();
			break;
		default:
			throw new ElfException("Invalid encoding.");
		}
		return val;
	}

	/** Read four-byte int or eight-byte long depending on if {@link ElfFile#objectSize}. */
	long readIntOrLong() throws IOException {
		return elfFile.objectSize == ElfFile.CLASS_32 ? readInt() : readLong();
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
	 * Find the file offset from a virtual address by looking up the {@link ElfProgramHeader} segment containing the
	 * address and computing the resulting file offset.
	 */
	long virtualMemoryAddrToFileOffset(long address) throws IOException {
		for (int i = 0; i < elfFile.num_ph; i++) {
			ElfProgramHeader ph = elfFile.getProgramHeader(i);
			if (address >= ph.virtual_address && address < (ph.virtual_address + ph.mem_size)) {
				long relativeOffset = address - ph.virtual_address;
				if (relativeOffset >= ph.file_size)
					throw new ElfException("Can not convert virtual memory address " + Long.toHexString(address) + " to file offset -" + " found segment " + ph
							+ " but address maps to memory outside file range");
				return ph.offset + relativeOffset;
			}
		}
		throw new ElfException("Cannot find segment for address " + Long.toHexString(address));
	}

}
