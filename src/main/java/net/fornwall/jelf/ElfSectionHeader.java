package net.fornwall.jelf;

import java.io.IOException;

/**
 * Class corresponding to the Elf32_Shdr/Elf64_Shdr struct.
 *
 * <p>
 * An object file's section header table lets one locate all the file's sections. The section header table is an array
 * of Elf32_Shdr or Elf64_Shdr structures. A section header table index is a subscript into this array. The ELF header's
 * {@link ElfFile#sh_offset e_shoff member} gives the byte offset from the beginning of the file to the section header
 * table with each section header entry being {@link ElfFile#sh_entry_size e_shentsize} bytes big.
 *
 * <p>
 * {@link ElfFile#num_sh e_shnum} normally tells how many entries the section header table contains, but if the number
 * of sections is greater than or equal to SHN_LORESERVE (0xff00), e_shnum has the value SHN_UNDEF (0) and the actual
 * number of section header table entries is contained in the sh_size field of the section header at index 0 (otherwise,
 * the sh_size member of the initial entry contains 0).
 *
 * <p>
 * Some section header table indexes are reserved in contexts where index size is restricted, for example, the st_shndx
 * member of a symbol table entry and the e_shnum and e_shstrndx members of the ELF header. In such contexts, the
 * reserved values do not represent actual sections in the object file. Also in such contexts, an escape value indicates
 * that the actual section index is to be found elsewhere, in a larger field.
 *
 */
public class ElfSectionHeader {

	/**
	 * Marks the section header as inactive; it does not have an associated section. Other members of the section header
	 * have undefined values.
	 */
	public static final int SHT_NULL = 0;
	/** Section holds information defined by the program. */
	public static final int SHT_PROGBITS = 1;
	/**
	 * The {@link #type} value for a section containing complete symbol table information necessary for link editing.
	 *
	 * See {@link ElfSymbolTableSection}, which is the class representing sections of this type, for more information.
	 */
	public static final int SHT_SYMTAB = 2;
	/** Section holds string table information. */
	public static final int SHT_STRTAB = 3;
	/** Section holds relocation entries with explicit addends. */
	public static final int SHT_RELA = 4;
	/** Section holds symbol hash table. */
	public static final int SHT_HASH = 5;
	/**
	 * Section holds information for dynamic linking. Only one per ELF file. The dynsym is allocable, and contains the
	 * symbols needed to support runtime operation.
	 */
	public static final int SHT_DYNAMIC = 6;
	/** Section holds information that marks the file. */
	public static final int SHT_NOTE = 7;
	/** Section occupies no space but resembles TYPE_PROGBITS. */
	public static final int SHT_NOBITS = 8;
	/** Section holds relocation entries without explicit addends. */
	public static final int SHT_REL = 9;
	/** Section is reserved but has unspecified semantics. */
	public static final int SHT_SHLIB = 10;
	/**
	 * The {@link #type} value for a section containing a minimal set of symbols needed for dynamic linking at runtime.
	 *
	 * See {@link ElfSymbolTableSection}, which is the class representing sections of this type, for more information.
	 */
	public static final int SHT_DYNSYM = 11;
	public static final int SHT_INIT_ARRAY = 14;
	public static final int SHT_FINI_ARRAY = 15;
	public static final int SHT_PREINIT_ARRAY = 16;
	public static final int SHT_GROUP = 17;
	public static final int SHT_SYMTAB_SHNDX = 18;

	public static final int SHT_GNU_verdef = 0x6ffffffd;
	public static final int SHT_GNU_verneed = 0x6ffffffe;
	public static final int SHT_GNU_versym = 0x6fffffff;

	/** Lower bound of the range of indexes reserved for operating system-specific semantics. */
	public static final int SHT_LOOS = 0x60000000;
	/** Upper bound of the range of indexes reserved for operating system-specific semantics. */
	public static final int SHT_HIOS = 0x6fffffff;
	/** Lower bound of the range of indexes reserved for processor-specific semantics. */
	public static final int SHT_LOPROC = 0x70000000;
	/** Upper bound of the range of indexes reserved for processor-specific semantics. */
	public static final int SHT_HIPROC = 0x7fffffff;
	/** Lower bound of the range of indexes reserved for application programs. */
	public static final int SHT_LOUSER = 0x80000000;
	/** Upper bound of the range of indexes reserved for application programs. */
	public static final int SHT_HIUSER = 0xffffffff;

	/** Flag informing that this section contains data that should be writable during process execution. */
	public static final int FLAG_WRITE = 0x1;
	/** Flag informing that section occupies memory during process execution. */
	public static final int FLAG_ALLOC = 0x2;
	/** Flag informing that section contains executable machine instructions. */
	public static final int FLAG_EXEC_INSTR = 0x4;
	/** Flag informing that all the bits in the mask are reserved for processor specific semantics. */
	public static final int FLAG_MASK = 0xf0000000;

	/**
	 * Name for the section containing the string table.
	 * <p>
	 * This section contains a string table which contains names for symbol structures
	 * by being indexed by the {@link ElfSymbol#st_name} field.
	 */
	public static final String NAME_STRTAB = ".strtab";
	/**
	 * Name for the section containing the dynamic string table.
	 */
	public static final String NAME_DYNSTR = ".dynstr";
	/**
	 * Name for the section containing read-only initialized data.
	 */
	public static final String NAME_RODATA = ".rodata";

	/** Index into the section header string table which gives the name of the section. */
	public final int name_ndx; // Elf32_Word or Elf64_Word - 4 bytes in both.
	/** Section content and semantics. */
	public final int type; // Elf32_Word or Elf64_Word - 4 bytes in both.
	/** Flags. */
	public final long flags; // Elf32_Word or Elf64_Xword.
	/**
	 * sh_addr. If the section will be in the memory image of a process this will be the address at which the first byte
	 * of section will be loaded. Otherwise, this value is 0.
	 */
	public final long address; // Elf32_Addr
	/** Offset from beginning of file to first byte of the section. */
	public final long section_offset; // Elf32_Off
	/** Size in bytes of the section. TYPE_NOBITS is a special case. */
	public final /* uint32_t */ long size;
	/** Section header table index link. */
	public final /* uint32_t */ int link;
	/** Extra information determined by the section type. */
	public final /* uint32_t */ int info;
	/** Address alignment constraints for the section. */
	public final /* uint32_t */ long address_alignment;
	/** Size of a fixed-size entry, 0 if none. */
	public final long entry_size; // Elf32_Word

	private final ElfFile elfHeader;

	/** Reads the section header information located at offset. */
	ElfSectionHeader(final ElfParser parser, long offset) {
		this.elfHeader = parser.elfFile;
		parser.seek(offset);

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
	}

	/** Returns the name of the section or null if the section has no name. */
	public String getName() {
		if (name_ndx == 0) return null;
		ElfStringTable tbl = elfHeader.getSectionNameStringTable();
		return tbl.get(name_ndx);
	}

	@Override
	public String toString() {
		return "ElfSectionHeader[name=" + getName() + ", type=0x" + Long.toHexString(type) + "]";
	}

}
