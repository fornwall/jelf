package net.fornwall.jelf;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link ElfSection} with information necessary for dynamic linking.
 * <p>
 * Given an {@link ElfFile}, use {@link ElfFile#getDynamicSection()} to obtain the dynamic section for it if one exists,
 * which it only does if the ELF file is an object file participating in dynamic linking.
 * <p>
 * This dynamic linking section contains a list of {@link ElfDynamicStructure}:s.
 * <pre>
 * Name                     Value  d_un         Executable   Shared Object
 * ----------------------------------------------------------------------
 * DT_NULL                      0  ignored      mandatory    mandatory
 * DT_NEEDED                    1  d_val        optional     optional
 * DT_PLTRELSZ                  2  d_val        optional     optional
 * DT_PLTGOT                    3  d_ptr        optional     optional
 * DT_HASH                      4  d_ptr        mandatory    mandatory
 * DT_STRTAB                    5  d_ptr        mandatory    mandatory
 * DT_SYMTAB                    6  d_ptr        mandatory    mandatory
 * DT_RELA                      7  d_ptr        mandatory    optional
 * DT_RELASZ                    8  d_val        mandatory    optional
 * DT_RELAENT                   9  d_val        mandatory    optional
 * DT_STRSZ                    10  d_val        mandatory    mandatory
 * DT_SYMENT                   11  d_val        mandatory    mandatory
 * DT_INIT                     12  d_ptr        optional     optional
 * DT_FINI                     13  d_ptr        optional     optional
 * DT_SONAME                   14  d_val        ignored      optional
 * DT_RPATH*                   15  d_val        optional     ignored
 * DT_SYMBOLIC*                16  ignored      ignored      optional
 * DT_REL                      17  d_ptr        mandatory    optional
 * DT_RELSZ                    18  d_val        mandatory    optional
 * DT_RELENT                   19  d_val        mandatory    optional
 * DT_PLTREL                   20  d_val        optional     optional
 * DT_DEBUG                    21  d_ptr        optional     ignored
 * DT_TEXTREL*                 22  ignored      optional     optional
 * DT_JMPREL                   23  d_ptr        optional     optional
 * DT_BIND_NOW*                24  ignored      optional     optional
 * DT_INIT_ARRAY               25  d_ptr        optional     optional
 * DT_FINI_ARRAY               26  d_ptr        optional     optional
 * DT_INIT_ARRAYSZ             27  d_val        optional     optional
 * DT_FINI_ARRAYSZ             28  d_val        optional     optional
 * DT_RUNPATH                  29  d_val        optional     optional
 * DT_FLAGS                    30  d_val        optional     optional
 * DT_ENCODING                 32  unspecified  unspecified  unspecified
 * DT_PREINIT_ARRAY            32  d_ptr        optional     ignored
 * DT_PREINIT_ARRAYSZ          33  d_val        optional     ignored
 * DT_LOOS             0x6000000D  unspecified  unspecified  unspecified
 * DT_HIOS             0x6ffff000  unspecified  unspecified  unspecified
 * DT_LOPROC           0x70000000  unspecified  unspecified  unspecified
 * DT_HIPROC           0x7fffffff  unspecified  unspecified  unspecified
 * "*" Signifies an entry that is at level 2.
 * </pre>
 * <p>
 * Read more about dynamic sections at <a href="https://refspecs.linuxbase.org/elf/gabi4+/ch5.dynamic.html#dynamic_section">Dynamic Section</a>.
 */
public class ElfDynamicSection extends ElfSection {

    /**
     * An entry with a DT_NULL tag marks the end of the _DYNAMIC array.
     */
    public static final int DT_NULL = 0;
    /**
     * This element holds the string table offset of a null-terminated string, giving the
     * name of a needed library. The offset is an index into the table recorded in the
     * {@link #DT_STRTAB} code.
     * <p>
     * See <a href="https://refspecs.linuxbase.org/elf/gabi4+/ch5.dynamic.html#shobj_dependencies">Shared Object Dependencies</a> for more information about these names.
     * <p>
     * The dynamic array may contain multiple entries with this type.
     * <p>
     * These entries' relative order is significant, though their relation to entries of other types is not.
     */
    public static final int DT_NEEDED = 1;
    public static final int DT_PLTRELSZ = 2;
    public static final int DT_PLTGOT = 3;
    public static final int DT_HASH = 4;
    /**
     * DT_STRTAB entry holds the address, not offset, of the dynamic string table.
     */
    public static final int DT_STRTAB = 5;
    public static final int DT_SYMTAB = 6;
    public static final int DT_RELA = 7;
    public static final int DT_RELASZ = 8;
    public static final int DT_RELAENT = 9;
    /**
     * The size in bytes of the {@link #DT_STRTAB} string table.
     */
    public static final int DT_STRSZ = 10;
    public static final int DT_SYMENT = 11;
    public static final int DT_INIT = 12;
    public static final int DT_FINI = 13;
    public static final int DT_SONAME = 14;
    public static final int DT_RPATH = 15;
    public static final int DT_SYMBOLIC = 16;
    public static final int DT_REL = 17;
    public static final int DT_RELSZ = 18;
    public static final int DT_RELENT = 19;
    public static final int DT_PLTREL = 20;
    public static final int DT_DEBUG = 21;
    public static final int DT_TEXTREL = 22;
    public static final int DT_JMPREL = 23;
    public static final int DT_BIND_NOW = 24;
    public static final int DT_INIT_ARRAY = 25;
    public static final int DT_FINI_ARRAY = 26;
    public static final int DT_INIT_ARRAYSZ = 27;
    public static final int DT_FINI_ARRAYSZ = 28;
    public static final int DT_RUNPATH = 29;
    public static final int DT_FLAGS = 30;
    public static final int DT_PREINIT_ARRAY = 32;
    public static final int DT_GNU_HASH = 0x6ffffef5;
    public static final int DT_FLAGS_1 = 0x6ffffffb;
    public static final int DT_VERDEF = 0x6ffffffc; /* Address of version definition */
    public static final int DT_VERDEFNUM = 0x6ffffffd; /* Number of version definitions */
    public static final int DT_VERNEEDED = 0x6ffffffe;
    public static final int DT_VERNEEDNUM = 0x6fffffff;

    public static final int DF_ORIGIN = 0x1;
    public static final int DF_SYMBOLIC = 0x2;
    public static final int DF_TEXTREL = 0x4;
    public static final int DF_BIND_NOW = 0x8;

    /**
     * Set RTLD_NOW for this object.
     */
    public static final int DF_1_NOW = 0x00000001;
    /**
     * Set RTLD_GLOBAL for this object.
     */
    public static final int DF_1_GLOBAL = 0x00000002;
    /**
     * Set RTLD_GROUP for this object.
     */
    public static final int DF_1_GROUP = 0x00000004;
    /**
     * Set RTLD_NODELETE for this object.
     */
    public static final int DF_1_NODELETE = 0x00000008;
    public static final int DF_1_LOADFLTR = 0x00000010;
    public static final int DF_1_INITFIRST = 0x00000020;
    /**
     * Object can not be used with dlopen(3)
     */
    public static final int DF_1_NOOPEN = 0x00000040;
    public static final int DF_1_ORIGIN = 0x00000080;
    public static final int DF_1_DIRECT = 0x00000100;
    public static final int DF_1_TRANS = 0x00000200;
    public static final int DF_1_INTERPOSE = 0x00000400;
    public static final int DF_1_NODEFLIB = 0x00000800;
    /**
     * Object cannot be dumped with dldump(3)
     */
    public static final int DF_1_NODUMP = 0x00001000;
    public static final int DF_1_CONFALT = 0x00002000;
    public static final int DF_1_ENDFILTEE = 0x00004000;
    public static final int DF_1_DISPRELDNE = 0x00008000;
    public static final int DF_1_DISPRELPND = 0x00010000;
    public static final int DF_1_NODIRECT = 0x00020000;
    public static final int DF_1_IGNMULDEF = 0x00040000;
    public static final int DF_1_NOKSYMS = 0x00080000;
    public static final int DF_1_NOHDR = 0x00100000;
    public static final int DF_1_EDITED = 0x00200000;
    public static final int DF_1_NORELOC = 0x00400000;
    public static final int DF_1_SYMINTPOSE = 0x00800000;
    public static final int DF_1_GLOBAUDIT = 0x01000000;
    public static final int DF_1_SINGLETON = 0x02000000;
    public static final int DF_1_STUB = 0x04000000;
    public static final int DF_1_PIE = 0x08000000;

    /**
     * For the {@link #DT_STRTAB}. Mandatory.
     */
    public long dt_strtab_offset;

    /**
     * For the {@link #DT_STRSZ}. Mandatory.
     */
    public int dt_strtab_size;

    private MemoizedObject<ElfStringTable> dtStringTable;
    public final List<ElfDynamicStructure> entries = new ArrayList<>();

    /**
     * An entry in the {@link #entries} of a {@link ElfDynamicSection}.
     * <p>
     * In the elf.h header file this represents either of the following structures:
     *
     * <pre>
     * typedef struct {
     *     Elf32_Sword d_tag;
     *     union {
     *         Elf32_Word      d_val;
     *         Elf32_Addr      d_ptr;
     *         Elf32_Off       d_off;
     *     } d_un;
     * } Elf32_Dyn;
     *
     * typedef struct {
     *     Elf64_Xword d_tag;
     *     union {
     *         Elf64_Xword d_val;
     *         Elf64_Addr d_ptr;
     *     } d_un;
     * } Elf64_Dyn;
     * </pre>
     */
    public static class ElfDynamicStructure {
        public ElfDynamicStructure(long d_tag, long d_val_or_ptr) {
            this.d_tag = d_tag;
            this.d_val_or_ptr = d_val_or_ptr;
        }

        /**
         * A tag value whose value defines how to interpret {@link #d_val_or_ptr}.
         * <p>
         * One of the DT_* constants in {@link ElfDynamicSection}.
         */
        public final long d_tag;
        /**
         * A field whose value is to be interpreted as specified by the {@link #d_tag}.
         */
        public final long d_val_or_ptr;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (d_tag ^ (d_tag >>> 32));
            result = prime * result + (int) (d_val_or_ptr ^ (d_val_or_ptr >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            ElfDynamicStructure other = (ElfDynamicStructure) obj;
            if (d_tag != other.d_tag) return false;
            return d_val_or_ptr == other.d_val_or_ptr;
        }

        @Override
        public String toString() {
            return "ElfDynamicSectionEntry{tag=" + d_tag + ", d_val_or_ptr=" + d_val_or_ptr + "}";
        }
    }

    public ElfDynamicSection(final ElfParser parser, ElfSectionHeader header) {
        super(header);

        parser.seek(header.sh_offset);
        int numEntries = (int) (header.sh_size / 8);

        // Except for the DT_NULL element at the end of the array, and the relative order of DT_NEEDED elements, entries
        // may appear in any order. So important to use lazy evaluation to only evaluating e.g. DT_STRTAB after the
        // necessary DT_STRSZ is read.
        loop:
        for (int i = 0; i < numEntries; i++) {
            long d_tag = parser.readIntOrLong();
            final long d_val_or_ptr = parser.readIntOrLong();
            entries.add(new ElfDynamicStructure(d_tag, d_val_or_ptr));
            switch ((int) d_tag) {
                case DT_NULL:
                    // A DT_NULL element ends the array (may be following DT_NULL values, but no need to look at them).
                    break loop;
                case DT_STRTAB: {
                    dtStringTable = new MemoizedObject<ElfStringTable>() {
                        @Override
                        protected ElfStringTable computeValue() throws ElfException {
                            long fileOffsetForStringTable = parser.virtualMemoryAddrToFileOffset(d_val_or_ptr);
                            return new ElfStringTable(parser, fileOffsetForStringTable, dt_strtab_size, null); // FIXME: null header
                        }
                    };
                    dt_strtab_offset = d_val_or_ptr;
                }
                break;
                case DT_STRSZ:
                    if (d_val_or_ptr > Integer.MAX_VALUE) throw new ElfException("Too large DT_STRSZ: " + d_val_or_ptr);
                    dt_strtab_size = (int) d_val_or_ptr;
                    break;
            }
        }

    }

    private ElfDynamicStructure firstEntryWithTag(long desiredTag) {
        for (ElfDynamicStructure entry : this.entries) {
            if (entry.d_tag == desiredTag) return entry;
        }
        return null;
    }

    public List<String> getNeededLibraries() throws ElfException {
        ElfStringTable stringTable = dtStringTable.getValue();
        List<String> result = new ArrayList<>();
        for (ElfDynamicStructure entry : this.entries) {
            if (entry.d_tag == DT_NEEDED) result.add(stringTable.get((int) entry.d_val_or_ptr));
        }
        return result;
    }

    public String getRunPath() {
        ElfDynamicStructure runPathEntry = firstEntryWithTag(DT_RUNPATH);
        return runPathEntry == null ? null : dtStringTable.getValue().get((int) runPathEntry.d_val_or_ptr);
    }

    public long getFlags() {
        ElfDynamicStructure flagsEntry = firstEntryWithTag(DT_FLAGS);
        return flagsEntry == null ? 0 : flagsEntry.d_val_or_ptr;
    }

    public long getFlags1() {
        ElfDynamicStructure flagsEntry = firstEntryWithTag(DT_FLAGS_1);
        return flagsEntry == null ? 0 : flagsEntry.d_val_or_ptr;
    }

    @Override
    public String toString() {
        return "ElfDynamicStructure{entries=" + this.entries + "}";
    }
}
