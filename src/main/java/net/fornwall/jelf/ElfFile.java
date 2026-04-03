package net.fornwall.jelf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An ELF (Executable and Linkable Format) file that can be a relocatable, executable, shared or core file.
 * <p>
 * Use one of the following methods to parse input to get an instance of this class:
 * <ul>
 *     <li>{@link #from(File)}</li>
 *     <li>{@link #from(byte[])}</li>
 *     <li>{@link #from(InputStream)}</li>
 *     <li>{@link #from(MappedByteBuffer)}</li>
 *     <li>{@link #from(SeekableByteChannel)}</li>
 *     <li>{@link #from(Path)}</li>
 * </ul>
 * <p>
 * Resources about ELF files:
 * <ul>
 *  <li><a href="https://man7.org/linux/man-pages/man5/elf.5.html">elf(5) — Linux manual page</a></li>
 *  <li><a href="https://en.wikipedia.org/wiki/Executable_and_Linkable_Format">Wikipedia - Executable and Linkable Format</a></li>
 *  <li><a href="https://downloads.openwatcom.org/ftp/devel/docs/elf-64-gen.pdf">ELF-64 Object File Format</a></li>
 * </ul>
 */
public final class ElfFile {

    /**
     * Relocatable file type. A possible value of {@link #e_type}.
     */
    public static final int ET_REL = 1;
    /**
     * Executable file type. A possible value of {@link #e_type}.
     */
    public static final int ET_EXEC = 2;
    /**
     * Shared object file type. A possible value of {@link #e_type}.
     */
    public static final int ET_DYN = 3;
    /**
     * Core file file type. A possible value of {@link #e_type}.
     */
    public static final int ET_CORE = 4;

    /**
     * 32-bit objects. A possible value of {@link #ei_class}.
     */
    public static final byte CLASS_32 = 1;
    /**
     * 64-bit objects. A possible value of {@link #ei_class}.
     */
    public static final byte CLASS_64 = 2;

    /**
     * System V application binary interface. A possible value of {@link #ei_osabi}.
     */
    public static final byte ABI_SYSTEMV = 0x00;
    /**
     * HP-UX application binary interface. A possible value of {@link #ei_osabi}.
     */
    public static final byte ABI_HPUX = 0x01;
    /**
     * NetBSD application binary interface. A possible value of {@link #ei_osabi}.
     */
    public static final byte ABI_NETBSD = 0x02;
    /**
     * Linux application binary interface. A possible value of {@link #ei_osabi}.
     */
    public static final byte ABI_LINUX = 0x03;
    /**
     * GNU Hurd application binary interface. A possible value of {@link #ei_osabi}.
     */
    public static final byte ABI_GNUHERD = 0x04;
    /**
     * Solaris application binary interface. A possible value of {@link #ei_osabi}.
     */
    public static final byte ABI_SOLARIS = 0x06;
    /**
     * AIX application binary interface. A possible value of {@link #ei_osabi}.
     */
    public static final byte ABI_AIX = 0x07;
    /**
     * IRIX application binary interface. A possible value of {@link #ei_osabi}.
     */
    public static final byte ABI_IRIX = 0x08;
    /**
     * FreeBSD application binary interface. A possible value of {@link #ei_osabi}.
     */
    public static final byte ABI_FREEBSD = 0x09;
    /**
     * Tru64 application binary interface. A possible value of {@link #ei_osabi}.
     */
    public static final byte ABI_TRU64 = 0x0A;
    /**
     * Novell Modesto application binary interface. A possible value of {@link #ei_osabi}.
     */
    public static final byte ABI_MODESTO = 0x0B;
    /**
     * OpenBSD application binary interface. A possible value of {@link #ei_osabi}.
     */
    public static final byte ABI_OPENBSD = 0x0C;
    /**
     * OpenVMS application binary interface. A possible value of {@link #ei_osabi}.
     */
    public static final byte ABI_OPENVMS = 0x0D;
    /**
     * NonStop Kernel application binary interface. A possible value of {@link #ei_osabi}.
     */
    public static final byte ABI_NONSTOP = 0x0E;
    /**
     * AROS application binary interface. A possible value of {@link #ei_osabi}.
     */
    public static final byte ABI_AROS = 0x0F;
    /**
     * Fenix OS application binary interface. A possible value of {@link #ei_osabi}.
     */
    public static final byte ABI_FENIX = 0x10;
    /**
     * CloudABI application binary interface. A possible value of {@link #ei_osabi}.
     */
    public static final byte ABI_CLOUD = 0x11;
    /**
     * Stratus Technologies OpenVOS application binary interface. A possible value of {@link #ei_osabi}.
     */
    public static final byte ABI_OPENVOS = 0x12;

    /**
     * LSB data encoding. A possible value of {@link #ei_data}.
     */
    public static final byte DATA_LSB = 1;
    /**
     * MSB data encoding. A possible value of {@link #ei_data}.
     */
    public static final byte DATA_MSB = 2;

    /**
     * No architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_NONE = 0;
    /**
     * AT&amp;T architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_ATT = 1;
    /**
     * AT&amp;T architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_M32 = ARCH_ATT;
    /**
     * SPARC architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_SPARC = 2;
    /**
     * Intel 386 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_i386 = 3;
    /**
     * Intel 386 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_386 = ARCH_i386;
    /**
     * Motorola 68000 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_68k = 4;
    /**
     * Motorola 68000 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_68K = ARCH_68k;
    /**
     * Motorola 88000 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_88k = 5;
    /**
     * Motorola 8800 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_88K = ARCH_88k;
    /**
     * Intel MCU architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_IAMCU = 6;
    /**
     * Intel 860 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_i860 = 7;
    /**
     * Intel 860 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_860 = ARCH_i860;
    /**
     * MIPS R3000 big-endian architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_MIPS = 8;
    /**
     * IBM System/370 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_S370 = 9;
    /**
     * MIPS R3000 little-endian architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_MIPS_RS3_LE = 10;
    /**
     * HPPA architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_PARISC = 15;
    /**
     * Fujitsu VPP500 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_VPP500 = 17;
    /**
     * Sun's "v8plus" architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_SPARC32PLUS = 18;
    /**
     * Intel 80960 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_960 = 19;
    /**
     * PowerPC architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_PPC = 20;
    /**
     * PowerPC 64-bit architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_PPC64 = 21;
    /**
     * IBM S390 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_S390 = 22;
    /**
     * IBM SPU/SPC architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_SPU = 23;
    /**
     * NEC V800 series architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_V800 = 36;
    /**
     * Fujitsu FR20 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_FR20 = 37;
    /**
     * TRW RH-32 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_RH32 = 38;
    /**
     * Motorola RCE architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_RCE = 39;
    /**
     * ARM architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_ARM = 40;
    /**
     * Digital Alpha architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_FAKE_ALPHA = 41;
    /**
     * Hitachi SH architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_SH = 42;
    /**
     * SPARC v9 64-bit architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_SPARCV9 = 43;
    /**
     * Siemens Tricore architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_TRICORE = 44;
    /**
     * Argonaut RISC Core architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_ARC = 45;
    /**
     * Hitachi H8/300 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_H8_300 = 46;
    /**
     * Hitachi H8/300H architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_H8_300H = 47;
    /**
     * Hitachi H8S architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_H8S = 48;
    /**
     * Hitachi H8/500 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_H8_500 = 49;
    /**
     * Intel Merced architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_IA_64 = 50;
    /**
     * Stanford MIPS-X architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_MIPS_X = 51;
    /**
     * Motorola Coldfire architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_COLDFIRE = 52;
    /**
     * Motorola M68HC12 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_68HC12 = 53;
    /**
     * Fujitsu MMA Multimedia Accelerator architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_MMA = 54;
    /**
     * Siemens PCP architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_PCP = 55;
    /**
     * Sony nCPU embedded RISC architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_NCPU = 56;
    /**
     * Denso NDR1 microprocessor architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_NDR1 = 57;
    /**
     * Motorola Start*Core processor architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_STARCORE = 58;
    /**
     * Toyota ME16 processor architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_ME16 = 59;
    /**
     * STMicroelectronic ST100 processor architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_ST100 = 60;
    /**
     * Advanced Logic Corp. Tinyj emb.fam architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_TINYJ = 61;
    /**
     * AMD x86-64 architecture architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_X86_64 = 62;
    /**
     * Sony DSP Processor architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_PDSP = 63;
    /**
     * Digital PDP-10 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_PDP10 = 64;
    /**
     * Digital PDP-11 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_PDP11 = 65;
    /**
     * Siemens FX66 microcontroller architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_FX66 = 66;
    /**
     * STMicroelectronics ST9+ 8/16 mc architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_ST9PLUS = 67;
    /**
     * STmicroelectronics ST7 8 bit mc architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_ST7 = 68;
    /**
     * Motorola MC68HC16 microcontroller architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_68HC16 = 69;
    /**
     * Motorola MC68HC11 microcontroller architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_68HC11 = 70;
    /**
     * Motorola MC68HC08 microcontroller architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_68HC08 = 71;
    /**
     * Motorola MC68HC05 microcontroller architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_68HC05 = 72;
    /**
     * Silicon Graphics SVx architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_SVX = 73;
    /**
     * STMicroelectronics ST19 8 bit mc architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_ST19 = 74;
    /**
     * Digital VAX architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_VAX = 75;
    /**
     * Axis Communications 32-bit emb.proc architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_CRIS = 76;
    /**
     * Infineon Technologies 32-bit emb.proc architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_JAVELIN = 77;
    /**
     * Element 14 64-bit DSP Processor architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_FIREPATH = 78;
    /**
     * LSI Logic 16-bit DSP Processor architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_ZSP = 79;
    /**
     * Donald Knuth's educational 64-bit proc architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_MMIX = 80;
    /**
     * Harvard University machine-independent object files architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_HUANY = 81;
    /**
     * SiTera Prism architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_PRISM = 82;
    /**
     * Atmel AVR 8-bit microcontroller architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_AVR = 83;
    /**
     * Fujitsu FR30 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_FR30 = 84;
    /**
     * Mitsubishi D10V architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_D10V = 85;
    /**
     * Mitsubishi D30V architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_D30V = 86;
    /**
     * NEC v850 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_V850 = 87;
    /**
     * Mitsubishi M32R architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_M32R = 88;
    /**
     * Matsushita MN10300 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_MN10300 = 89;
    /**
     * Matsushita MN10200 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_MN10200 = 90;
    /**
     * picoJava architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_PJ = 91;
    /**
     * OpenRISC 32-bit embedded processor architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_OPENRISC = 92;
    /**
     * ARC International ARCompact architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_ARC_COMPACT = 93;
    /**
     * Tensilica Xtensa Architecture architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_XTENSA = 94;
    /**
     * Alphamosaic VideoCore architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_VIDEOCORE = 95;
    /**
     * Thompson Multimedia General Purpose Proc architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_TMM_GPP = 96;
    /**
     * National Semi. 32000 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_NS32K = 97;
    /**
     * Tenor Network TPC architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_TPC = 98;
    /**
     * Trebia SNP 1000 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_SNP1K = 99;
    /**
     * STMicroelectronics ST200 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_ST200 = 100;
    /**
     * Ubicom IP2xxx architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_IP2K = 101;
    /**
     * MAX processor architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_MAX = 102;
    /**
     * National Semi. CompactRISC architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_CR = 103;
    /**
     * Fujitsu F2MC16 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_F2MC16 = 104;
    /**
     * Texas Instruments msp430 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_MSP430 = 105;
    /**
     * Analog Devices Blackfin DSP architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_BLACKFIN = 106;
    /**
     * Seiko Epson S1C33 family architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_SE_C33 = 107;
    /**
     * Sharp embedded microprocessor architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_SEP = 108;
    /**
     * Arca RISC architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_ARCA = 109;
    /**
     * PKU-Unity &amp; MPRC Peking Uni. mc series architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_UNICORE = 110;
    /**
     * eXcess configurable cpu architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_EXCESS = 111;
    /**
     * Icera Semi. Deep Execution Processor architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_DXP = 112;
    /**
     * Altera Nios II architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_ALTERA_NIOS2 = 113;
    /**
     * National Semi. CompactRISC CRX architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_CRX = 114;
    /**
     * Motorola XGATE architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_XGATE = 115;
    /**
     * Infineon C16x/XC16x architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_C166 = 116;
    /**
     * Renesas M16C architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_M16C = 117;
    /**
     * Microchip Technology dsPIC30F architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_DSPIC30F = 118;
    /**
     * Freescale Communication Engine RISC architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_CE = 119;
    /**
     * Renesas M32C architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_M32C = 120;
    /**
     * Altium TSK3000 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_TSK3000 = 131;
    /**
     * Freescale RS08 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_RS08 = 132;
    /**
     * Analog Devices SHARC family architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_SHARC = 133;
    /**
     * Cyan Technology eCOG2 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_ECOG2 = 134;
    /**
     * Sunplus S+core7 RISC architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_SCORE7 = 135;
    /**
     * New Japan Radio (NJR) 24-bit DSP architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_DSP24 = 136;
    /**
     * Broadcom VideoCore III architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_VIDEOCORE3 = 137;
    /**
     * RISC for Lattice FPGA architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_LATTICEMICO32 = 138;
    /**
     * Seiko Epson C17 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_SE_C17 = 139;
    /**
     * Texas Instruments TMS320C6000 DSP architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_TI_C6000 = 140;
    /**
     * Texas Instruments TMS320C2000 DSP architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_TI_C2000 = 141;
    /**
     * Texas Instruments TMS320C55x DSP architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_TI_C5500 = 142;
    /**
     * Texas Instruments App. Specific RISC architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_TI_ARP32 = 143;
    /**
     * Texas Instruments Prog. Realtime Unit architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_TI_PRU = 144;
    /**
     * STMicroelectronics 64bit VLIW DSP architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_MMDSP_PLUS = 160;
    /**
     * Cypress M8C architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_CYPRESS_M8C = 161;
    /**
     * Renesas R32C architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_R32C = 162;
    /**
     * NXP Semi. TriMedia architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_TRIMEDIA = 163;
    /**
     * QUALCOMM DSP6 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_QDSP6 = 164;
    /**
     * Intel 8051 and variants architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_8051 = 165;
    /**
     * STMicroelectronics STxP7x architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_STXP7X = 166;
    /**
     * Andes Tech. compact code emb. RISC architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_NDS32 = 167;
    /**
     * Cyan Technology eCOG1X architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_ECOG1X = 168;
    /**
     * Dallas Semi. MAXQ30 mc architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_MAXQ30 = 169;
    /**
     * New Japan Radio (NJR) 16-bit DSP architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_XIMO16 = 170;
    /**
     * M2000 Reconfigurable RISC architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_MANIK = 171;
    /**
     * Cray NV2 vector architecture architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_CRAYNV2 = 172;
    /**
     * Renesas RX architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_RX = 173;
    /**
     * Imagination Tech. META architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_METAG = 174;
    /**
     * MCST Elbrus architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_MCST_ELBRUS = 175;
    /**
     * Cyan Technology eCOG16 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_ECOG16 = 176;
    /**
     * National Semi. CompactRISC CR16 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_CR16 = 177;
    /**
     * Freescale Extended Time Processing Unit architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_ETPU = 178;
    /**
     * Infineon Tech. SLE9X architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_SLE9X = 179;
    /**
     * Intel L10M architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_L10M = 180;
    /**
     * Intel K10M architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_K10M = 181;
    /**
     * ARM AARCH64 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_AARCH64 = 183;
    /**
     * Amtel 32-bit microprocessor architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_AVR32 = 185;
    /**
     * STMicroelectronics STM8 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_STM8 = 186;
    /**
     * Tilera TILE64 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_TILE64 = 187;
    /**
     * Tilera TILEPro architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_TILEPRO = 188;
    /**
     * Xilinx MicroBlaze architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_MICROBLAZE = 189;
    /**
     * NVIDIA CUDA architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_CUDA = 190;
    /**
     * Tilera TILE-Gx architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_TILEGX = 191;
    /**
     * CloudShield architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_CLOUDSHIELD = 192;
    /**
     * KIPO-KAIST Core-A 1st gen. architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_COREA_1ST = 193;
    /**
     * KIPO-KAIST Core-A 2nd gen. architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_COREA_2ND = 194;
    /**
     * Synopsys ARCv2 ISA.  architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_ARCV2 = 195;
    /**
     * Open8 RISC architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_OPEN8 = 196;
    /**
     * Renesas RL78 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_RL78 = 197;
    /**
     * Broadcom VideoCore V architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_VIDEOCORE5 = 198;
    /**
     * Renesas 78KOR architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_78KOR = 199;
    /**
     * Freescale 56800EX DSC architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_56800EX = 200;
    /**
     * Beyond BA1 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_BA1 = 201;
    /**
     * Beyond BA2 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_BA2 = 202;
    /**
     * XMOS xCORE architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_XCORE = 203;
    /**
     * Microchip 8-bit PIC(r) architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_MCHP_PIC = 204;
    /**
     * Intel Graphics Technology architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_INTELGT = 205;
    /**
     * KM211 KM32 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_KM32 = 210;
    /**
     * KM211 KMX32 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_KMX32 = 211;
    /**
     * KM211 KMX16 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_EMX16 = 212;
    /**
     * KM211 KMX8 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_EMX8 = 213;
    /**
     * KM211 KVARC architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_KVARC = 214;
    /**
     * Paneve CDP architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_CDP = 215;
    /**
     * Cognitive Smart Memory Processor architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_COGE = 216;
    /**
     * Bluechip CoolEngine architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_COOL = 217;
    /**
     * Nanoradio Optimized RISC architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_NORC = 218;
    /**
     * CSR Kalimba architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_CSR_KALIMBA = 219;
    /**
     * Zilog Z80 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_Z80 = 220;
    /**
     * Controls and Data Services VISIUMcore architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_VISIUM = 221;
    /**
     * FTDI Chip FT32 architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_FT32 = 222;
    /**
     * Moxie processor architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_MOXIE = 223;
    /**
     * AMD GPU architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_AMDGPU = 224;
    /**
     * RISC-V architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_RISCV = 243;
    /**
     * Linux BPF -- in-kernel virtual machine architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_BPF = 247;
    /**
     * C-SKY architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_CSKY = 252;     /*  */
    /**
     * LoongArch architecture type. A possible value of {@link #e_machine}.
     */
    public static final int ARCH_LOONGARCH = 258;

    /**
     * Byte identifying the size of objects, either {@link #CLASS_32} or {@link #CLASS_64}.
     */
    public final byte ei_class;

    /**
     * Returns a byte identifying the data encoding of the processor specific data. This byte will be either
     * DATA_INVALID, DATA_LSB or DATA_MSB.
     */
    public final byte ei_data;

    /**
     * Set to 1 for the original and current (as of writing) version of ELF.
     */
    public final byte ei_version;

    /**
     * Identifies the target operating system ABI.
     */
    public final byte ei_osabi;

    /**
     * Further specifies the ABI version. Its interpretation depends on the target ABI.
     */
    public final byte es_abiversion;

    /**
     * Identifies the object file type. One of the ET_* constants in the class.
     */
    public final short e_type; // Elf32_Half

    /**
     * The required architecture. One of the ARCH_* constants in the class.
     */
    public final short e_machine; // Elf32_Half
    /**
     * Version
     */
    public final int e_version; // Elf32_Word
    /**
     * Virtual address to which the system first transfers control. If there is no entry point for the file the value is
     * 0.
     */
    public final long e_entry; // Elf32_Addr
    /**
     * e_phoff. Program header table offset in bytes. If there is no program header table the value is 0.
     */
    public final long e_phoff; // Elf32_Off
    /**
     * e_shoff. Section header table offset in bytes. If there is no section header table the value is 0.
     */
    public final long e_shoff; // Elf32_Off
    /**
     * e_flags. Processor specific flags.
     */
    public final int e_flags; // Elf32_Word
    /**
     * e_ehsize. ELF header size in bytes.
     */
    public final short e_ehsize; // Elf32_Half
    /**
     * e_phentsize. Size of one entry in the file's program header table in bytes. All entries are the same size.
     */
    public final short e_phentsize; // Elf32_Half
    /**
     * e_phnum. Number of {@link ElfSegment} entries in the program header table, 0 if no entries.
     */
    public short e_phnum; // Elf32_Half
    /**
     * e_shentsize. Section header entry size in bytes - all entries are the same size.
     */
    public final short e_shentsize; // Elf32_Half
    /**
     * e_shnum. Number of entries in the section header table, 0 if no entries.
     */
    public short e_shnum; // Elf32_Half

    /**
     * Elf{32,64}_Ehdr#e_shstrndx. Index into the section header table associated with the section name string table.
     * SH_UNDEF if there is no section name string table.
     */
    public short e_shstrndx; // Elf32_Half

    /**
     * MemoizedObject array of section headers associated with this ELF file.
     */
    private final MemoizedObject<ElfSection>[] sections;
    /**
     * MemoizedObject array of program headers associated with this ELF file.
     */
    private final MemoizedObject<ElfSegment>[] programHeaders;

    /**
     * Used to cache symbol table lookup.
     */
    private ElfSymbolTableSection symbolTableSection;
    /**
     * Used to cache dynamic symbol table lookup.
     */
    private ElfSymbolTableSection dynamicSymbolTableSection;

    private ElfDynamicSection dynamicSection;

    public boolean is32Bits() {
        return ei_class == CLASS_32;
    }

    /**
     * Returns the section header at the specified index. The section header at index 0 is defined as being an undefined
     * section.
     *
     * @param index the index of the ELF section to fetch
     * @return the ELF section at the specified index
     */
    public ElfSection getSection(int index) throws ElfException {
        return sections[index].getValue();
    }

    public List<ElfSection> sectionsOfType(int sectionType) throws ElfException {
        if (e_shnum < 2) return Collections.emptyList();
        List<ElfSection> result = new ArrayList<>();
        for (int i = 1; i < e_shnum; i++) {
            ElfSection section = getSection(i);
            if (section.header.sh_type == sectionType) {
                result.add(section);
            }
        }
        return result;
    }


    /**
     * Returns the section header string table associated with this ELF file.
     *
     * @return the section header string table for this file
     */
    public ElfStringTable getSectionNameStringTable() throws ElfException {
        return (ElfStringTable) getSection(e_shstrndx);
    }

    /**
     * Returns the string table associated with this ELF file.
     *
     * @return the string table for this file
     */
    public ElfStringTable getStringTable() throws ElfException {
        return findStringTableWithName(ElfSectionHeader.NAME_STRTAB);
    }

    /**
     * Returns the dynamic symbol table associated with this ELF file, or null if one does not exist.
     *
     * @return the dynamic symbol table for this file, if any
     */
    public ElfStringTable getDynamicStringTable() throws ElfException {
        return findStringTableWithName(ElfSectionHeader.NAME_DYNSTR);
    }

    private ElfStringTable findStringTableWithName(String tableName) throws ElfException {
        // Loop through the section header and look for a section
        // header with the name "tableName". We can ignore entry 0
        // since it is defined as being undefined.
        return (ElfStringTable) firstSectionByName(tableName);
    }

    /**
     * The {@link ElfSectionHeader#SHT_SYMTAB} section (of which there may be only one), if any.
     *
     * @return the symbol table section for this file, if any
     */
    public ElfSymbolTableSection getSymbolTableSection() throws ElfException {
        return (symbolTableSection != null) ? symbolTableSection : (symbolTableSection = (ElfSymbolTableSection) firstSectionByType(ElfSectionHeader.SHT_SYMTAB));
    }

    /**
     * The {@link ElfSectionHeader#SHT_DYNSYM} section (of which there may be only one), if any.
     *
     * @return the dynamic symbol table section for this file, if any
     */
    public ElfSymbolTableSection getDynamicSymbolTableSection() throws ElfException {
        return (dynamicSymbolTableSection != null) ? dynamicSymbolTableSection : (dynamicSymbolTableSection = (ElfSymbolTableSection) firstSectionByType(ElfSectionHeader.SHT_DYNSYM));
    }

    /**
     * The {@link ElfSectionHeader#SHT_DYNAMIC} section (of which there may be only one). Named ".dynamic".
     *
     * @return the dynamic section for this file, if any
     */
    public ElfDynamicSection getDynamicSection() {
        return (dynamicSection != null) ? dynamicSection : (dynamicSection = (ElfDynamicSection) firstSectionByType(ElfSectionHeader.SHT_DYNAMIC));
    }

    public ElfSection firstSectionByType(int type) throws ElfException {
        for (int i = 1; i < e_shnum; i++) {
            ElfSection sh = getSection(i);
            if (sh.header.sh_type == type) return sh;
        }
        return null;
    }

    public <T extends ElfSection> T firstSectionByType(Class<T> type) throws ElfException {
        for (int i = 1; i < e_shnum; i++) {
            ElfSection sh = getSection(i);
            if (type.isInstance(sh)) return type.cast(sh);
        }
        return null;
    }

    public ElfSection firstSectionByName(String sectionName) throws ElfException {
        for (int i = 1; i < e_shnum; i++) {
            ElfSection sh = getSection(i);
            if (sectionName.equals(sh.header.getName())) return sh;
        }
        return null;
    }

    /**
     * Returns the elf symbol with the specified name or null if one is not found.
     *
     * @param symbolName the name of the symbol to fetch
     * @return information about the specified symbol
     */
    public ElfSymbol getELFSymbol(String symbolName) throws ElfException {
        if (symbolName == null) return null;

        // Check dynamic symbol table for symbol name.
        ElfSymbolTableSection sh = getDynamicSymbolTableSection();
        if (sh != null) {
            final int numSymbols = sh.symbols.length;
            for (int i = 0; i < numSymbols; i++) {
                ElfSymbol symbol = sh.symbols[i];
                if (symbolName.equals(symbol.getName())) {
                    return symbol;
                }
            }
        }

        // Check symbol table for symbol name.
        sh = getSymbolTableSection();
        if (sh != null) {
            final int numSymbols = sh.symbols.length;
            for (int i = 0; i < numSymbols; i++) {
                ElfSymbol symbol = sh.symbols[i];
                if (symbolName.equals(symbol.getName())) {
                    return symbol;
                }
            }
        }
        return null;
    }

    /**
     * Returns the elf symbol with the specified address or null if one is not found. 'address' is relative to base of
     * shared object for .so's.
     *
     * @param address the address of the symbol to fetch
     * @return the symbol at the specified address, if any
     */
    public ElfSymbol getELFSymbol(long address) throws ElfException {
        // Check dynamic symbol table for address.
        ElfSymbol symbol;
        long value;

        ElfSymbolTableSection sh = getDynamicSymbolTableSection();
        if (sh != null) {
            final int numSymbols = sh.symbols.length;
            for (int i = 0; i < numSymbols; i++) {
                symbol = sh.symbols[i];
                value = symbol.st_value;
                if (address >= value && address < value + symbol.st_size) return symbol;
            }
        }

        // Check symbol table for symbol name.
        sh = getSymbolTableSection();
        if (sh != null) {
            final int numSymbols = sh.symbols.length;
            for (int i = 0; i < numSymbols; i++) {
                symbol = sh.symbols[i];
                value = symbol.st_value;
                if (address >= value && address < value + symbol.st_size) return symbol;
            }
        }
        return null;
    }

    public ElfSegment getProgramHeader(int index) {
        return programHeaders[index].getValue();
    }

    public ElfSegment firstSegmentByType(int type) {
        for (int i = 0; i < e_phnum; i++) {
            ElfSegment seg = getProgramHeader(i);
            if (seg.p_type == type) {
                return seg;
            }
        }
        return null;
    }

    public List<ElfSegment> segmentsOfType(int type) {
        if (e_phnum == 0) {
            return Collections.emptyList();
        }
        List<ElfSegment> result = new ArrayList<>();
        for (int i = 0; i < e_phnum; i++) {
            ElfSegment seg = getProgramHeader(i);
            if (seg.p_type == type) {
                result.add(seg);
            }
        }
        return result;
    }

    public static ElfFile from(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int totalRead = 0;
        byte[] buffer = new byte[8096];
        boolean firstRead = true;
        while (true) {
            int readNow = in.read(buffer, totalRead, buffer.length - totalRead);
            if (readNow == -1) {
                return from(baos.toByteArray());
            } else {
                if (firstRead) {
                    // Abort early.
                    if (readNow < 4) {
                        throw new ElfException("Bad first read");
                    } else {
                        if (!(0x7f == buffer[0] && 'E' == buffer[1] && 'L' == buffer[2] && 'F' == buffer[3]))
                            throw new ElfException("Bad magic number for file");
                    }
                    firstRead = false;
                }
                baos.write(buffer, 0, readNow);
            }
        }
    }

    public static ElfFile from(File file) throws ElfException, IOException {
        byte[] buffer = new byte[(int) file.length()];
        try (FileInputStream in = new FileInputStream(file)) {
            int totalRead = 0;
            while (totalRead < buffer.length) {
                int readNow = in.read(buffer, totalRead, buffer.length - totalRead);
                if (readNow == -1) {
                    throw new ElfException("Premature end of file");
                } else {
                    totalRead += readNow;
                }
            }
        }
        return from(buffer);
    }

    public static ElfFile from(Path path) throws ElfException, IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            return from(buffer);
        }
    }

    public static ElfFile from(byte[] buffer) throws ElfException {
        return new ElfFile(new ByteArrayAsFile(buffer));
    }

    public static ElfFile from(MappedByteBuffer mappedByteBuffer) throws ElfException {
        return new ElfFile(new MappedFile(mappedByteBuffer));
    }

    public static ElfFile from(SeekableByteChannel channel) throws ElfException, IOException {
        if (channel instanceof FileChannel fc) {
            return new ElfFile(new MappedFile(fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size())));
        }

        long size = channel.size();

        if (size > Integer.MAX_VALUE) {
            throw new ElfException("Channel too large: " + size);
        }

        ByteBuffer buf = ByteBuffer.allocate((int) size);
        channel.position(0);

        while (buf.hasRemaining()) {
            if (channel.read(buf) == -1) {
                break;
            }
        }

        return new ElfFile(new ByteArrayAsFile(buf.array()));
    }

    public static ElfFile from(BackingFile backingFile) throws ElfException {
        return new ElfFile(backingFile);
    }

    ElfFile(BackingFile backingFile) throws ElfException {
        final ElfParser parser = new ElfParser(this, backingFile);

        byte[] ident = new byte[16];
        int bytesRead = parser.read(ident);
        if (bytesRead != ident.length)
            throw new ElfException("Error reading elf header (read " + bytesRead + "bytes - expected to read " + ident.length + "bytes)");

        if (!(0x7f == ident[0] && 'E' == ident[1] && 'L' == ident[2] && 'F' == ident[3]))
            throw new ElfException("Bad magic number for file");

        ei_class = ident[4];
        if (!(ei_class == CLASS_32 || ei_class == CLASS_64))
            throw new ElfException("Invalid object size class: " + ei_class);
        ei_data = ident[5];
        if (!(ei_data == DATA_LSB || ei_data == DATA_MSB)) throw new ElfException("Invalid encoding: " + ei_data);
        ei_version = ident[6];
        if (ei_version != 1) throw new ElfException("Invalid elf version: " + ei_version);
        ei_osabi = ident[7]; // EI_OSABI, target operating system ABI
        es_abiversion = ident[8]; // EI_ABIVERSION, ABI version. Linux kernel (after at least 2.6) has no definition of it.
        // ident[9-15] // EI_PAD, currently unused.

        e_type = parser.readShort();
        e_machine = parser.readShort();
        e_version = parser.readInt();
        e_entry = parser.readIntOrLong();
        e_phoff = parser.readIntOrLong();
        e_shoff = parser.readIntOrLong();
        e_flags = parser.readInt();
        e_ehsize = parser.readShort();
        e_phentsize = parser.readShort();
        e_phnum = parser.readShort();
        e_shentsize = parser.readShort();
        e_shnum = parser.readShort();
        e_shstrndx = parser.readShort();


        if (e_shnum == 0 && e_shstrndx == 0xffff) {
            ElfSectionHeader elfSectionHeader = new ElfSectionHeader(parser, e_shoff);
            e_shnum = (short) elfSectionHeader.sh_size;
            e_shstrndx = (short) elfSectionHeader.sh_link;
            e_phnum = (short) elfSectionHeader.sh_info;
        }

        sections = MemoizedObject.uncheckedArray(e_shnum);
        for (int i = 0; i < e_shnum; i++) {
            final long sectionHeaderOffset = e_shoff + (i * e_shentsize);
            sections[i] = new MemoizedObject<ElfSection>() {
                @Override
                public ElfSection computeValue() throws ElfException {
                    ElfSectionHeader elfSectionHeader = new ElfSectionHeader(parser, sectionHeaderOffset);
                    switch (elfSectionHeader.sh_type) {
                        case ElfSectionHeader.SHT_DYNAMIC:
                            return new ElfDynamicSection(parser, elfSectionHeader);
                        case ElfSectionHeader.SHT_SYMTAB:
                        case ElfSectionHeader.SHT_DYNSYM:
                            return new ElfSymbolTableSection(parser, elfSectionHeader);
                        case ElfSectionHeader.SHT_STRTAB:
                            return new ElfStringTable(parser, elfSectionHeader.sh_offset, (int) elfSectionHeader.sh_size, elfSectionHeader);
                        case ElfSectionHeader.SHT_HASH:
                            return new ElfHashTable(parser, elfSectionHeader);
                        case ElfSectionHeader.SHT_NOTE:
                            return new ElfNoteSection(parser, elfSectionHeader);
                        case ElfSectionHeader.SHT_RELA:
                            return new ElfRelocationAddendSection(parser, elfSectionHeader);
                        case ElfSectionHeader.SHT_REL:
                            return new ElfRelocationSection(parser, elfSectionHeader);
                        case ElfSectionHeader.SHT_GNU_HASH:
                            return new ElfGnuHashTable(parser, elfSectionHeader);
                        default:
                            return new ElfSection(parser, elfSectionHeader);
                    }
                }
            };
        }

        programHeaders = MemoizedObject.uncheckedArray(e_phnum);
        for (int i = 0; i < e_phnum; i++) {
            final long programHeaderOffset = e_phoff + (i * e_phentsize);
            programHeaders[i] = new MemoizedObject<ElfSegment>() {
                @Override
                public ElfSegment computeValue() {
                    return new ElfSegment(parser, programHeaderOffset);
                }
            };
        }
    }

    /**
     * The interpreter specified by the {@link ElfSegment#PT_INTERP} program header, if any.
     *
     * @return the interpreter for this file, if any
     */
    public String getInterpreter() {
        for (MemoizedObject<ElfSegment> programHeader : programHeaders) {
            ElfSegment ph = programHeader.getValue();
            if (ph.p_type == ElfSegment.PT_INTERP) return ph.getIntepreter();
        }
        return null;
    }

}
