package net.fornwall.jelf;

/**
 * A relocation connects a symbolic reference with its actual definition.
 * Relocatable files must have information that describes how to modify their
 * section contents, thus allowing executable and shared object files to hold
 * the right information for a process's program image. Relocation entries are
 * these data.
 * <p>
 * In the elf.h header file the struct definitions are:
 *
 * <pre>
 *
 * typedef struct
 * {
 *   Elf32_Addr r_offset;
 *   Elf32_Word r_info;
 * } Elf32_Rel;
 *
 * typedef struct
 * {
 *   Elf64_Addr	r_offset;
 *   Elf64_Xword	r_info;
 * } Elf64_Rel;
 * </pre>
 */

public final class ElfRelocation {
	/**
	 * The location at which to apply the relocation. For a relocatable file,
	 * this is the byte offset from the beginning of the section. For an
	 * executable or shared object, the value is the virtual address.
	 */
	public final long r_offset; // Elf32_Addr or Elf64_Addr

	/**
	 * This member gives both the symbol table index to which the relocation
	 * must be made, and the type of relocation to apply. Relocation types are
	 * processor specific.
	 */
	public final long r_info; // Elf32_Word or Elf64_Xword

	ElfRelocation(ElfParser parser, long offset) {
		parser.seek(offset);

		r_offset = parser.readIntOrLong();
		r_info = parser.readIntOrLong();
	}

	public long getType() {
		return r_info & 0xFF;
	}

	public int getSymbol() {
		return (int) r_info >> 8;
	}

}
