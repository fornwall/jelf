package net.fornwall.jelf;

import java.io.IOException;

/**
 * http://www.sco.com/developers/gabi/latest/ch5.pheader.html#p_type
 * 
 * http://stackoverflow.com/questions/22612735/how-can-i-find-the-dynamic-libraries-required-by-an-elf-binary-in-c
 */
public class ElfProgramHeader {

	/** PT_NULL. Type defining that the array element is unused. Other member values are undefined. */
	public static final int TYPE_NULL = 0;
	/** PT_LOAD. Type defining that the array element specifies a loadable segment. */
	public static final int TYPE_LOAD = 1;
	/** PT_DYNAMIC. The array element specifies dynamic linking information. */
	public static final int TYPE_DYNAMIC = 2;
	/**
	 * PT_INTERP. The array element specifies the location and size of a null-terminated path name to invoke as an
	 * interpreter.
	 */
	public static final int TYPE_INTERP = 3;
	/** PT_NOTE. The array element specifies the location and size of auxiliary information. */
	public static final int TYPE_NOTE = 4;
	/** PT_SHLIB. This segment type is reserved but has unspecified semantics. */
	public static final int TYPE_SHLIB = 5;
	/**
	 * PT_PHDR. The array element, if present, specifies the location and size of the program header table itself, both
	 * in the file and in the memory image of the program. This segment type may not occur more than once in a file.
	 */
	public static final int TYPE_PHDR = 6;
	public static final int TYPE_LOPROC = 0x70000000;
	public static final int TYPE_HIPROC = 0x7fffffff;

	/** Defines the kind of segment this element describes. */
	public final int type; // Elf32_Word or Elf64_Word - 4 bytes in both.

	/** p_offset. Offset from the beginning of the file at which the first byte of the segment resides. */
	public final long offset; // Elf32_Off
	/** p_vaddr. This member gives the virtual address at which the first byte of the segment resides in memory. */
	public final long virtual_address; // Elf32_Addr
	/**
	 * Reserved for the physical address of the segment on systems where physical addressing is relevant.
	 */
	public final long physical_address; // Elf32_addr

	/** File image size of segment in bytes, may be 0. */
	public final long file_size; // Elf32_Word
	/** Memory image size of segment in bytes, may be 0. */
	public final long mem_size; // Elf32_Word
	/**
	 * Flags relevant to this segment. Values for flags are defined in ELFSectionHeader.
	 */
	public final int flags; // Elf32_Word
	public final long alignment; // Elf32_Word

	private MemoizedObject[] symbols;

	ElfProgramHeader(ElfParser parser, long offset) throws IOException {
		parser.fsFile.seek(offset);
		if (parser.elfFile.objectSize == 32) {
			// typedef struct {
			// Elf32_Word p_type;
			// Elf32_Off p_offset;
			// Elf32_Addr p_vaddr;
			// Elf32_Addr p_paddr;
			// Elf32_Word p_filesz;
			// Elf32_Word p_memsz;
			// Elf32_Word p_flags;
			// Elf32_Word p_align;
			// } Elf32_Phdr;
			type = parser.readInt();
			this.offset = parser.readInt();
			virtual_address = parser.readInt();
			physical_address = parser.readInt();
			file_size = parser.readInt();
			mem_size = parser.readInt();
			flags = parser.readInt();
			alignment = parser.readInt();
		} else {
			// typedef struct {
			// Elf64_Word p_type;
			// Elf64_Word p_flags;
			// Elf64_Off p_offset;
			// Elf64_Addr p_vaddr;
			// Elf64_Addr p_paddr;
			// Elf64_Xword p_filesz;
			// Elf64_Xword p_memsz;
			// Elf64_Xword p_align;
			// } Elf64_Phdr;
			type = parser.readInt();
			flags = parser.readInt();
			this.offset = parser.readLong();
			virtual_address = parser.readLong();
			physical_address = parser.readLong();
			file_size = parser.readLong();
			mem_size = parser.readLong();
			alignment = parser.readLong();
		}
		//
		// switch (type) {
		// case ELFSectionHeader.TYPE_NULL:
		// break;
		// case ELFSectionHeader.TYPE_PROGBITS:
		// break;
		// case ELFSectionHeader.TYPE_SYMTBL:
		// case ELFSectionHeader.TYPE_DYNSYM:
		// break;
		// case ELFSectionHeader.TYPE_STRTBL:
		// // Setup the string table.
		// final int strTableOffset = section_offset;
		// final int strTableSize = size;
		// stringTable = new MemoizedObject() {
		// public Object computeValue() {
		// return new ELFStringTableImpl(strTableOffset,
		// strTableSize);
		// }
		// };
		// new ELFStringTableImpl(offset, file_size);
		// break;
		// case ELFSectionHeader.TYPE_RELO_EXPLICIT:
		// break;
		// case ELFSectionHeader.TYPE_HASH:
		// break;
		// case ELFSectionHeader.TYPE_DYNAMIC:
		// break;
		// case ELFSectionHeader.TYPE_NOTE:
		// break;
		// case ELFSectionHeader.TYPE_NOBITS:
		// break;
		// case ELFSectionHeader.TYPE_RELO:
		// break;
		// case ELFSectionHeader.TYPE_SHLIB:
		// break;
		// default:
		// break;
		// }
		// }
		//
	}

	@Override
	public String toString() {
		String typeString;
		switch ((int) type) {
		case TYPE_NULL:
			typeString = "PT_NULL";
			break;
		case TYPE_LOAD:
			typeString = "PT_LOAD";
			break;
		case TYPE_DYNAMIC:
			typeString = "PT_DYNAMIC";
			break;
		case TYPE_INTERP:
			typeString = "PT_INTERP";
			break;
		case TYPE_NOTE:
			typeString = "PT_NOTE";
			break;
		case TYPE_SHLIB:
			typeString = "PT_SHLIB";
			break;
		case TYPE_PHDR:
			typeString = "PT_PHDR";
			break;
		default:
			typeString = "0x" + Long.toHexString(type);
			break;
		}

		return "ElfProgramHeader[type=" + typeString + "]";
	}

}
