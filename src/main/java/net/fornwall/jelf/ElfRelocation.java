package net.fornwall.jelf;

/**
 * A relocation connects a symbolic reference with its actual definition.
 * Relocatable files must have information that describes how to modify their
 * section contents, thus allowing executable and shared object files to hold
 * the right information for a process's program image. Relocation entries are
 * these data.
 * <p>
 * These elf relocation entries can be obtained from {@link ElfRelocationSection}:s.
 * <p>
 * Corresponds to the below C structs:
 * <code><pre>
 * typedef struct {
 *   Elf32_Addr r_offset;
 *   Elf32_Word r_info;
 * } Elf32_Rel;
 *
 * typedef struct {
 *   Elf64_Addr	r_offset;
 *   Elf64_Xword r_info;
 * } Elf64_Rel;
 * </pre></code>
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
	private final ElfFile elfFile;

	ElfRelocation(ElfParser parser, long offset) {
		parser.seek(offset);

		r_offset = parser.readIntOrLong();
		r_info = parser.readIntOrLong();
		elfFile = parser.elfFile;
	}

	/**
	 * Corresponds to the ELF32_R_TYPE / ELF64_R_TYPE macros.
	 *
	 * @see ElfRelocationTypes
	 */
	public long getType() {
		return elfFile.is32Bits() ? (r_info & 0xFF) : ((int) r_info);
	}

	/**
	 * The symbol table index, with respect to which the relocation must be made.
	 * Use {@link #getSymbol()} to get the resolved {@link ElfSymbol} from this index.
	 * <p>
	 * Corresponds to the ELF32_R_SYM / ELF64_R_SYM macros.
	 */
	public int getSymbolIndex() {
		return (int) (r_info >> (elfFile.is32Bits() ? 8 : 32));
	}

	/**
	 * The symbol with respect to which the relocation must be made.
	 * Use {@link #getSymbolIndex()}} to get the resolved {@link ElfSymbol} from this index.
	 */
	public ElfSymbol getSymbol() {
		return elfFile.getSymbolTableSection().symbols[getSymbolIndex()];
	}
}
