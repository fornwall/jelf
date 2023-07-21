package net.fornwall.jelf;

/**
 * @see ElfRelocation#getType()
 * @see ElfRelocationAddend#getType()
 */
public final class ElfRelocationTypes {
    /**
     * AMD x86-64: No reloc
     */
    public static final int R_X86_64_NONE = 0;
    /**
     * AMD x86-64: Direct 64 bit.
     */
    public static final int R_X86_64_64 = 1;
    /**
     * AMD x86-64: PC relative 32 bit signed.
     */
    public static final int R_X86_64_PC32 = 2;
    /**
     * AMD x86-64: 32 bit GOT entry.
     */
    public static final int R_X86_64_GOT32 = 3;
    /**
     * AMD x86-64: 32 bit PLT address.
     */
    public static final int R_X86_64_PLT32 = 4;
    /**
     * AMD x86-64: Copy symbol at runtime.
     */
    public static final int R_X86_64_COPY = 5;
    /**
     * AMD x86-64: Create GOT entry.
     */
    public static final int R_X86_64_GLOB_DAT = 6;
    /**
     * AMD x86-64: Create PLT entry.
     */
    public static final int R_X86_64_JUMP_SLOT = 7;
    /**
     * AMD x86-64: Adjust by program base.
     */
    public static final int R_X86_64_RELATIVE = 8;
    /**
     * AMD x86-64: 32 bit signed PC relative offset to GOT.
     */
    public static final int R_X86_64_GOTPCREL = 9;
    /**
     * AMD x86-64: Direct 32 bit zero extended.
     */
    public static final int R_X86_64_32 = 10;
    /**
     * AMD x86-64: Direct 32 bit sign extended.
     */
    public static final int R_X86_64_32S = 11;
    /**
     * AMD x86-64: Direct 16 bit zero extended.
     */
    public static final int R_X86_64_16 = 12;
    /**
     * AMD x86-64: 16 bit sign extended pc relative.
     */
    public static final int R_X86_64_PC16 = 13;
    /**
     * AMD x86-64: Direct 8 bit sign extended.
     */
    public static final int R_X86_64_8 = 14;
    /**
     * AMD x86-64: 8 bit sign extended pc relative.
     */
    public static final int R_X86_64_PC8 = 15;
    /**
     * AMD x86-64: ID of module containing symbol.
     */
    public static final int R_X86_64_DTPMOD64 = 16;
    /**
     * AMD x86-64: Offset in module's TLS block.
     */
    public static final int R_X86_64_DTPOFF64 = 17;
    /**
     * AMD x86-64: Offset in initial TLS block
     */
    public static final int R_X86_64_TPOFF64 = 18;
    /**
     * AMD x86-64: 32 bit signed PC relative offset to two GOT entries for GD symbol.
     */
    public static final int R_X86_64_TLSGD = 19;
    /**
     * AMD x86-64: 32 bit signed PC relative offset to two GOT entries for LD symbol.
     */
    public static final int R_X86_64_TLSLD = 20;
    /**
     * AMD x86-64: Offset in TLS block.
     */
    public static final int R_X86_64_DTPOFF32 = 21;
    /**
     * AMD x86-64: 32 bit signed PC relative offset to GOT entry for IE symbol.
     */
    public static final int R_X86_64_GOTTPOFF = 22;
    /**
     * AMD x86-64: Offset in initial TLS block.
     */
    public static final int R_X86_64_TPOFF32 = 23;
    /**
     * AMD x86-64: PC relative 64 bit.
     */
    public static final int R_X86_64_PC64 = 24;
    /**
     * AMD x86-64: 64 bit offset to GOT.
     */
    public static final int R_X86_64_GOTOFF64 = 25;
    /**
     * AMD x86-64: 32 bit signed pc relative offset to GOT.
     */
    public static final int R_X86_64_GOTPC32 = 26;
    /**
     * AMD x86-64: 64-bit GOT entry offset.
     */
    public static final int R_X86_64_GOT64 = 27;
    /**
     * AMD x86-64:64-bit PC relative offset to GOT entry.
     */
    public static final int R_X86_64_GOTPCREL64 = 28;
    /**
     * AMD x86-64:64-bit PC relative offset to GOT.
     */
    public static final int R_X86_64_GOTPC64 = 29;
    /**
     * AMD x86-64:like GOT64, says PLT entry needed.
     */
    public static final int R_X86_64_GOTPLT64 = 30;
    /**
     * AMD x86-64:64-bit GOT relative offset to PLT entry.
     */
    public static final int R_X86_64_PLTOFF64 = 31;
    /**
     * AMD x86-64:Size of symbol plus 32-bit addend.
     */
    public static final int R_X86_64_SIZE32 = 32;
    /**
     * AMD x86-64:Size of symbol plus 64-bit addend.
     */
    public static final int R_X86_64_SIZE64 = 33;
    /**
     * AMD x86-64:GOT offset for TLS descriptor.
     */
    public static final int R_X86_64_GOTPC32_TLSDESC = 34;
    /**
     * AMD x86-64:Marker for call through TLS descriptor.
     */
    public static final int R_X86_64_TLSDESC_CALL = 35;
    /**
     * AMD x86-64:TLS descriptor.
     */
    public static final int R_X86_64_TLSDESC = 36;
    /**
     * AMD x86-64:Adjust indirectly by program base
     */
    public static final int R_X86_64_IRELATIVE = 37;
    /**
     * AMD x86-64:64-bit adjust by program base.
     */
    public static final int R_X86_64_RELATIVE64 = 38;
    /**
     * AMD x86-64:Load from 32 bit signed pc relative offset to GOT entry without REX prefix, relaxable.
     */
    public static final int R_X86_64_GOTPCRELX = 41;
    /**
     * AMD x86-64:Load from 32 bit signed pc relative offset to GOT entry with REX prefix, relaxable.
     */
    public static final int R_X86_64_REX_GOTPCRELX = 42;
    public static final int R_X86_64_NUM = 43;

    public static final int R_ARM_NONE = 0;
    public static final int R_ARM_PC24 = 1;
    public static final int R_ARM_ABS32 = 2;
    public static final int R_ARM_REL32 = 3;
    public static final int R_ARM_THM_CALL = 10;
    public static final int R_ARM_CALL = 28;
    public static final int R_ARM_JUMP24 = 29;
    public static final int R_ARM_THM_JUMP24 = 30;
    public static final int R_ARM_TARGET1 = 38;
    public static final int R_ARM_V4BX = 40;
    public static final int R_ARM_PREL31 = 42;
    public static final int R_ARM_MOVW_ABS_NC = 43;
    public static final int R_ARM_MOVT_ABS = 44;
    public static final int R_ARM_MOVW_PREL_NC = 45;
    public static final int R_ARM_MOVT_PREL = 46;
    public static final int R_ARM_THM_MOVW_ABS_NC = 47;
    public static final int R_ARM_THM_MOVT_ABS = 48;
    public static final int R_ARM_THM_MOVW_PREL_NC = 49;
    public static final int R_ARM_THM_MOVT_PREL = 50;

}
