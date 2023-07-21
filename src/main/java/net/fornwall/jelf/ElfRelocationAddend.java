package net.fornwall.jelf;

/**
 * Relocation is the process of connecting symbolic references with symbolic definitions.
 * For example, when a program calls a function, the associated call instruction must transfer
 * control to the proper destination address at execution. Relocatable files must have
 * information that describes how to modify their section contents. This information allows
 * executable and shared object files to hold the right information for a process's program
 * image. Relocation entries are these data.
 * <p>
 * These elf relocation entries can be obtained from {@link ElfRelocationAddend}:s.
 * <p>
 * Corresponds to the below C structs:
 * <pre><code>
 * typedef struct {
 *     Elf32_Addr r_offset;
 *     uint32_t   r_info;
 *     int32_t    r_addend;
 * } Elf32_Rela;
 *
 * typedef struct {
 *     Elf64_Addr r_offset;
 *     uint64_t   r_info;
 *     int64_t    r_addend;
 * } Elf64_Rela;
 * </code></pre>
 */
public final class ElfRelocationAddend {
    /**
     * This member gives the location at which to apply the
     * relocation action. For a relocatable file, the value is
     * the byte offset from the beginning of the section to the
     * storage unit affected by the relocation. For an
     * executable file or shared object, the value is the virtual
     * address of the storage unit affected by the relocation.
     */
    public final long r_offset;

    /**
     * This member gives both the symbol table index with respect
     * to which the relocation must be made and the type of
     * relocation to apply. Relocation types are processor-
     * specific. When the text refers to a relocation entry's
     * relocation type or symbol table index, it means the result
     * of applying ELF[32|64]_R_TYPE or ELF[32|64]_R_SYM,
     * respectively, to the entry's r_info member.
     */
    public final long r_info;

    /**
     * This member specifies a constant addend used to compute
     * the value to be stored into the relocatable field.
     */
    public final long r_addend; // int32_t or int64_t
    private final ElfFile elfFile;

    ElfRelocationAddend(ElfParser parser, long offset) {
        parser.seek(offset);

        r_offset = parser.readIntOrLong();
        r_info = parser.readIntOrLong();
        r_addend = parser.readIntOrLong();
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
     * The symbol table index, with respect to which the relocation must be made.
     * Use {@link #getSymbolIndex()}} to get the resolved {@link ElfSymbol} from this index.
     */
    public ElfSymbol getSymbol() {
        return elfFile.getSymbolTableSection().symbols[getSymbolIndex()];
    }
}
