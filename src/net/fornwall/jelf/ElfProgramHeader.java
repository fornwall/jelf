package net.fornwall.jelf;

import java.io.IOException;

/**
 * Class corresponding to the Elf32_Phdr/Elf64_Phdr struct.
 * 
 * An executable or shared object file's program header table is an array of structures, each describing a segment or
 * other information the system needs to prepare the program for execution. An object file segment contains one or more
 * sections. Program headers are meaningful only for executable and shared object files. A file specifies its own
 * program header size with the ELF header's {@link ElfFile#ph_entry_size e_phentsize} and {@link ElfFile#num_ph
 * e_phnum} members.
 * 
 * http://www.sco.com/developers/gabi/latest/ch5.pheader.html#p_type
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

	/** Elf{32,64}_Phdr#p_type. Kind of segment this element describes. */
	public final int type; // Elf32_Word/Elf64_Word - 4 bytes in both.
	/** Elf{32,64}_Phdr#p_offset. File offset at which the first byte of the segment resides. */
	public final long offset; // Elf32_Off/Elf64_Off - 4 or 8 bytes.
	/** Elf{32,64}_Phdr#p_vaddr. Virtual address at which the first byte of the segment resides in memory. */
	public final long virtual_address; // Elf32_Addr/Elf64_Addr - 4 or 8 bytes.
	/** Reserved for the physical address of the segment on systems where physical addressing is relevant. */
	public final long physical_address; // Elf32_addr/Elf64_Addr - 4 or 8 bytes.

	/** Elf{32,64}_Phdr#p_filesz. File image size of segment in bytes, may be 0. */
	public final long file_size; // Elf32_Word/Elf64_Xword -
	/** Elf{32,64}_Phdr#p_memsz. Memory image size of segment in bytes, may be 0. */
	public final long mem_size; // Elf32_Word
	/**
	 * Flags relevant to this segment. Values for flags are defined in ELFSectionHeader.
	 */
	public final int flags; // Elf32_Word
	public final long alignment; // Elf32_Word

	ElfProgramHeader(ElfParser parser, long offset) throws IOException {
		parser.fsFile.seek(offset);
		if (parser.elfFile.objectSize == ElfFile.CLASS_32) {
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
		switch (type) {
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

		String pFlagsString = "";
		if ((flags & /* PF_R= */4) != 0) pFlagsString += (pFlagsString.isEmpty() ? "" : "|") + "read";
		if ((flags & /* PF_W= */2) != 0) pFlagsString += (pFlagsString.isEmpty() ? "" : "|") + "write";
		if ((flags & /* PF_X= */1) != 0) pFlagsString += (pFlagsString.isEmpty() ? "" : "|") + "execute";

		if (pFlagsString.isEmpty()) pFlagsString = "0x" + Long.toHexString(flags);

		return "ElfProgramHeader[p_type=" + typeString + ", p_filesz=" + file_size + ", p_memsz=" + mem_size + ", p_flags=" + pFlagsString + ", p_align="
				+ alignment + ", range=[0x" + Long.toHexString(virtual_address) + "-0x" + Long.toHexString(virtual_address + mem_size) + "]]";
	}

}
