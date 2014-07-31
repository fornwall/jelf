package net.fornwall.jelf;

import java.io.IOException;

public class ElfSectionHeader {

	/** Undefined section header index. */
	public static final int NDX_UNDEFINED = 0;
	/** Lower bound section header index. */
	public static final int NDX_LORESERVE = 0xff00;
	/**
	 * Lower bound section header index reserved for processor specific semantics.
	 */
	public static final int NDX_LOPROC = 0xff00;
	/**
	 * Upper bound section header index reserved for processor specific semantics.
	 */
	public static final int NDX_HIPROC = 0xff1f;
	/**
	 * Absolute values for the corresponding reference. Symbols defined relative to section number NDX_ABS have absolute
	 * values and are not affected by relocation.
	 */
	public static final int NDX_ABS = 0xfff1;
	/**
	 * Symbols defined relative to this section are common symbols, such as FORTRAN, COMMON or unallocated C external
	 * variables.
	 */
	public static final int NDX_COMMON = 0xfff2;
	/** Upper bound section header index. */
	public static final int NDX_HIRESERVE = 0xffff;

	/** Section is inactive. */
	public static final int TYPE_NULL = 0;
	/** Section holds information defined by the program. */
	public static final int TYPE_PROGBITS = 1;
	/**
	 * Section holds symbol table information for link editing. It may also be used to store symbols for dynamic
	 * linking. Only 1 per ELF file.
	 */
	public static final int TYPE_SYMTBL = 2;
	/** Section holds string table information. */
	public static final int TYPE_STRTBL = 3;
	/** Section holds relocation entries with explicit addends. */
	public static final int TYPE_RELO_EXPLICIT = 4;
	/** Section holds symbol hash table. */
	public static final int TYPE_HASH = 5;
	/** Section holds information for dynamic linking. */
	public static final int TYPE_DYNAMIC = 6;
	/** Section holds information that marks the file. */
	public static final int TYPE_NOTE = 7;
	/** Section occupies no space but resembles TYPE_PROGBITS. */
	public static final int TYPE_NOBITS = 8;
	/** Section holds relocation entries without explicit addends. */
	public static final int TYPE_RELO = 9;
	/** Section is reserved but has unspecified semantics. */
	public static final int TYPE_SHLIB = 10;
	/** Section holds a minimum set of dynamic linking symbols. */
	public static final int TYPE_DYNSYM = 11;
	/** Lower bound section type that contains processor specific semantics. */
	public static final int TYPE_LOPROC = 0x70000000;
	/** Upper bound section type that contains processor specific semantics. */
	public static final int TYPE_HIPROC = 0x7fffffff;
	/** Lower bound of the range of indexes reserved for application programs. */
	public static final int TYPE_LOUSER = 0x80000000;
	/** Upper bound of the range of indexes reserved for application programs. */
	public static final int TYPE_HIUSER = 0xffffffff;

	/** Flag informing that this section contains data that should be writable during process execution. */
	public static final int FLAG_WRITE = 0x1;
	/** Flag informing that section occupies memory during process execution. */
	public static final int FLAG_ALLOC = 0x2;
	/** Flag informing that section contains executable machine instructions. */
	public static final int FLAG_EXEC_INSTR = 0x4;
	/** Flag informing that all the bits in the mask are reserved for processor specific semantics. */
	public static final int FLAG_MASK = 0xf0000000;

	/** Section header name identifying the section as a string table. */
	public static final String STRING_TABLE_NAME = ".strtab";
	/** Section header name identifying the section as a dynamic string table. */
	public static final String DYNAMIC_STRING_TABLE_NAME = ".dynstr";

	/** Index into the section header string table which gives the name of the section. */
	public final int name_ndx; // Elf32_Word or Elf64_Word - 4 bytes in both. 
	/** Section content and semantics. */
	public final int type; // Elf32_Word or Elf64_Word - 4 bytes in both.
	/** Flags. */
	public final long flags; // Elf32_Word
	/**
	 * If the section will be in the memory image of a process this will be the address at which the first byte of
	 * section will be loaded. Otherwise, this value is 0.
	 */
	public final long address; // Elf32_Addr
	/** Offset from beginning of file to first byte of the section. */
	public final long section_offset; // Elf32_Off
	/** Size in bytes of the section. TYPE_NOBITS is a special case. */
	public final long size; // Elf32_Word
	/** Section header table index link. */
	public final int link; // Elf32_Word or Elf64_Word - 4 bytes in both.
	/** Extra information determined by the section type. */
	public final int info; // Elf32_Word or Elf64_Word - 4 bytes in both.
	/** Address alignment constraints for the section. */
	public final long address_alignment; // Elf32_Word
	/** Size of a fixed-size entry, 0 if none. */
	public final long entry_size; // Elf32_Word

	/** Memoized symbol table. */
	private MemoizedObject[] symbols;
	/** Memoized string table. */
	private MemoizedObject stringTable;
	/** Memoized hash table. */
	private MemoizedObject hashTable;

	private final ElfFile elfHeader;

	/**
	 * Reads the section header information located at offset.
	 */
	ElfSectionHeader(ElfParser parser, long offset) throws ElfException, IOException {
		this.elfHeader = parser.elfFile;
		parser.fsFile.seek(offset);

		name_ndx = parser.readInt();
		type = parser.readInt();
		flags = parser.readIntOrLong();
		address = parser.readIntOrLong();
		section_offset = parser.readIntOrLong();
		size = parser.readIntOrLong();
		link = parser.readInt();
		info = parser.readInt();
		address_alignment = parser.readIntOrLong();
		entry_size = parser.readIntOrLong();

		switch (type) {
		case ElfSectionHeader.TYPE_NULL:
			break;
		case ElfSectionHeader.TYPE_PROGBITS:
			break;
		case ElfSectionHeader.TYPE_SYMTBL:
		case ElfSectionHeader.TYPE_DYNSYM:
			// Setup the symbol table.
			int num_entries = (int) (size / entry_size);
			symbols = new MemoizedObject[num_entries];
			for (int i = 0; i < num_entries; i++) {
				final long symbolOffset = section_offset + (i * entry_size);
				symbols[i] = new MemoizedObject() {
					@Override
					public Object computeValue() throws IOException {
						return new ElfSymbol(parser, symbolOffset, (int) type);
					}
				};
			}
			break;
		case ElfSectionHeader.TYPE_STRTBL:
			stringTable = new MemoizedObject() {
				@Override
				public Object computeValue() throws IOException {
					return new ElfStringTable(parser, section_offset, (int) size);
				}
			};
			break;
		case ElfSectionHeader.TYPE_RELO_EXPLICIT:
			break;
		case ElfSectionHeader.TYPE_HASH:
			hashTable = new MemoizedObject() {
				@Override
				public Object computeValue() throws IOException {
					return new ElfHashTable(parser, section_offset, (int) size);
				}
			};
			break;
		case ElfSectionHeader.TYPE_DYNAMIC:
			// TMP
			break;
		case ElfSectionHeader.TYPE_NOTE:
			break;
		case ElfSectionHeader.TYPE_NOBITS:
			break;
		case ElfSectionHeader.TYPE_RELO:
			break;
		case ElfSectionHeader.TYPE_SHLIB:
			break;
		default:
			break;
		}
	}

	/** Returns the number of symbols in this section or 0 if none. */
	public int getNumberOfSymbols() {
		return (symbols != null) ? symbols.length : 0;
	}

	/** Returns the symbol at the specified index. The ELF symbol at index 0 is the undefined symbol. */
	public ElfSymbol getELFSymbol(int index) throws IOException {
		return (ElfSymbol) symbols[index].getValue();
	}

	/** Returns the string table for this section or null if one does not exist. */
	public ElfStringTable getStringTable() throws IOException {
		return (stringTable != null) ? (ElfStringTable) stringTable.getValue() : null;
	}

	/**
	 * Returns the hash table for this section or null if one does not exist. NOTE: currently the ELFHashTable does not
	 * work and this method will always return null.
	 */
	public ElfHashTable getHashTable() throws IOException {
		return (hashTable != null) ? (ElfHashTable) hashTable.getValue() : null;
	}

	/** Returns the name of the section or null if the section has no name. */
	public String getName() throws ElfException, IOException {
		if (name_ndx == 0) return null;
		ElfStringTable tbl = elfHeader.getSectionHeaderStringTable();
		return tbl.get(name_ndx);
	}

	@Override
	public String toString() {
		try {
			return "ElfSectionHeader[name=" + getName() + ", type=0x" + Long.toHexString(type) + "]";
		} catch (ElfException | IOException e) {
			throw new RuntimeException(e);
		}
	}

}
