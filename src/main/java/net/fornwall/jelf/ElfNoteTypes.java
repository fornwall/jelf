package net.fornwall.jelf;

public final class ElfNoteTypes {
    private ElfNoteTypes() {}

    public static final String GNU = "GNU";

    public static final String SOLARIS = "SUNW Solaris";

    public static final String FDO = "FDO";

    public static final String NETBSD = "NetBSD";

    public static final String FREEBSD = "FreeBSD";

    public static final String OPENBSD = "OpenBSD";

    public static final String DRAGONFLY = "DragonFly";

    public static final String GO = "Go";

    public static final String ANDROID = "Android";

    public static final String CORE = "CORE";

    public static final String NETBSD_CORE = "NetBSD-CORE";

    public static final class Gnu {
        private Gnu() {}

        public static final int ABI_TAG = 1;

        public static final int HWCAP = 2;

        public static final int BUILD_ID = 3;

        public static final int GOLD_VERSION = 4;

        public static final int PROPERTY_TYPE_0 = 5;

        public static final class Property {
            private Property() {}

            public static final int STACK_SIZE = 1;

            public static final int NO_COPY_ON_PROTECTED = 2;

            public static final int UINT32_AND_LO = 0xb0000000;

            public static final int UINT32_AND_HI = 0xb0007fff;

            public static final int UINT32_OR_LO = 0xb0008000;

            public static final int UINT32_OR_HI = 0xb000ffff;

            public static final int NEEDED = UINT32_OR_LO;

            public static final int NEEDED_INDIRECT_EXTERN_ACCESS = 1 << 0;

            public static final int LOPROC = 0xc0000000;

            public static final int HIPROC = 0xdfffffff;

            public static final int LOUSER = 0xe0000000;

            public static final int HIUSER = 0xffffffff;

            public static final int AARCH64_FEATURE_1_AND = 0xc0000000;

            public static final int AARCH64_FEATURE_1_BTI = 1 << 0;

            public static final int AARCH64_FEATURE_1_PAC = 1 << 1;

            public static final int AARCH64_FEATURE_1_GCS = 1 << 2;

            public static final int X86_ISA_1_USED = 0xc0010002;

            public static final int X86_ISA_1_NEEDED = 0xc0008002;

            public static final int X86_FEATURE_1_AND = 0xc0000002;

            public static final int X86_ISA_1_BASELINE = 1 << 0;

            public static final int X86_ISA_1_V2 = 1 << 1;

            public static final int X86_ISA_1_V3 = 1 << 2;

            public static final int X86_ISA_1_V4 = 1 << 3;

            public static final int X86_FEATURE_1_IBT = 1 << 0;

            public static final int X86_FEATURE_1_SHSTK = 1 << 1;
        }
    }

    public static final class Solaris {
        private Solaris() {}

        public static final int PAGESIZE_HINT = 1;
    }

    public static final class Fdo {
        private Fdo() {}

        public static final int PACKAGING_METADATA = 0xcafe1a7e;

        public static final int DLOPEN_METADATA = 0x407c0c0a;
    }

    public static final class NetBsd {
        private NetBsd() {}

        public static final int VERSION = 1;

        public static final int EMULATION = 2;

        public static final int PAX = 3;

        public static final int PAX_MPROTECT = 0x01;

        public static final int PAX_NOMPROTECT = 0x02;

        public static final int PAX_GUARD = 0x04;

        public static final int PAX_NOGUARD = 0x08;

        public static final int PAX_ASLR = 0x10;

        public static final int PAX_NOASLR = 0x20;

        public static final int MARCH = 5;

        public static final int CMODEL = 6;
    }

    public static final class FreeBsd {
        private FreeBsd() {}

        public static final int VERSION = 1;

        public static final int PROCSTAT_AUXV = 16;
    }

    public static final class OpenBsd {
        private OpenBsd() {}

        public static final int VERSION = 1;
    }

    public static final class DragonFly {
        private DragonFly() {}

        public static final int VERSION = 1;
    }

    public static final class Go {
        private Go() {}

        public static final int BUILD_ID = 4;
    }

    public static final class Android {
        private Android() {}

        public static final int VERSION = 1;

        public static final int KUSER = 3;

        public static final int MEMTAG = 4;

        public static final int MEMTAG_LEVEL_NONE = 0;

        public static final int MEMTAG_LEVEL_ASYNC = 1;

        public static final int MEMTAG_LEVEL_SYNC = 2;

        public static final int MEMTAG_LEVEL_MASK = 3;

        public static final int MEMTAG_HEAP = 4;

        public static final int MEMTAG_STACK = 8;
    }

    public static final class Core {
        private Core() {}

        public static final int PRSTATUS = 1;

        public static final int PRFPREG = 2;

        public static final int PRPSINFO = 3;

        public static final int PRXREG = 4;

        public static final int TASKSTRUCT = 4;

        public static final int PLATFORM = 5;

        public static final int AUXV = 6;
    }

    public static final class NetBsdCore {
        private NetBsdCore() {}

        public static final int PROCINFO = 1;

        public static final int AUXV = 2;
    }
}
