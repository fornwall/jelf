package net.fornwall.jelf;

import static net.fornwall.jelf.ElfNoteTypes.ELF_NOTE_GNU;
import static net.fornwall.jelf.ElfNoteTypes.ELF_NOTE_GO;
import static net.fornwall.jelf.ElfNoteTypes.ELF_NOTE_NETBSD;
import static net.fornwall.jelf.ElfNoteTypes.Gnu.NT_GNU_ABI_TAG;
import static net.fornwall.jelf.ElfNoteTypes.Gnu.NT_GNU_BUILD_ID;
import static net.fornwall.jelf.ElfNoteTypes.Gnu.NT_GNU_PROPERTY_TYPE_0;
import static net.fornwall.jelf.ElfNoteTypes.Go.NT_GO_BUILD_ID;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class BasicTest {

    @Test
    void testAndroidArmBinTset() throws Exception {
        TestHelper.parseFile("android_arm_tset", file -> {
            Assertions.assertEquals(ElfFile.CLASS_32, file.ei_class);
            Assertions.assertEquals(ElfFile.DATA_LSB, file.ei_data);
            Assertions.assertEquals(ElfFile.ET_EXEC, file.e_type);
            Assertions.assertEquals(ElfFile.ARCH_ARM, file.e_machine);
            Assertions.assertEquals(32, file.e_phentsize);
            Assertions.assertEquals(7, file.e_phnum);
            Assertions.assertEquals(52, file.e_phoff);
            Assertions.assertEquals(40, file.e_shentsize);
            Assertions.assertEquals(25, file.e_shnum);
            Assertions.assertEquals(15856, file.e_shoff);
            TestHelper.assertSectionNames(
                    file, null, ".interp", ".dynsym", ".dynstr", ".hash", ".rel.dyn", ".rel.plt", ".plt", ".text");
            Assertions.assertEquals("/system/bin/linker", file.getInterpreter());

            ElfDynamicSection dynamic = file.getDynamicSection();
            Assertions.assertNotNull(dynamic);
            Assertions.assertEquals(".dynamic", dynamic.header.getName());
            Assertions.assertEquals(8, dynamic.header.sh_entsize);
            Assertions.assertEquals(248, dynamic.header.sh_size);
            Assertions.assertEquals(ElfDynamicSection.DF_BIND_NOW, dynamic.getFlags());
            Assertions.assertEquals(ElfDynamicSection.DF_1_NOW, dynamic.getFlags1());

            Assertions.assertEquals(
                    Arrays.asList("libncursesw.so.6", "libc.so", "libdl.so"), dynamic.getNeededLibraries());
            Assertions.assertEquals("/data/data/com.termux/files/usr/lib", dynamic.getRunPath());

            Assertions.assertEquals(26, dynamic.entries.size());
            Assertions.assertEquals(new ElfDynamicSection.ElfDynamicStructure(3, 0xbf44), dynamic.entries.get(0));
            Assertions.assertEquals(new ElfDynamicSection.ElfDynamicStructure(2, 352), dynamic.entries.get(1));
            Assertions.assertEquals(new ElfDynamicSection.ElfDynamicStructure(0x17, 0x8868), dynamic.entries.get(2));
            Assertions.assertEquals(new ElfDynamicSection.ElfDynamicStructure(0x6ffffffb, 1), dynamic.entries.get(24));
            Assertions.assertEquals(new ElfDynamicSection.ElfDynamicStructure(0, 0), dynamic.entries.get(25));

            TestHelper.validateHashTable(file);
        });
    }

    @Test
    void testAndroidArmLibNcurses() throws Exception {
        TestHelper.parseFile("android_arm_libncurses", file -> {
            Assertions.assertEquals(ElfFile.CLASS_32, file.ei_class);
            Assertions.assertEquals(ElfFile.DATA_LSB, file.ei_data);
            Assertions.assertEquals(ElfFile.ET_DYN, file.e_type);
            Assertions.assertEquals(ElfFile.ARCH_ARM, file.e_machine);
            Assertions.assertEquals("/system/bin/linker", file.getInterpreter());

            List<ElfSection> noteSections = file.sectionsOfType(ElfSectionHeader.SHT_NOTE);
            Assertions.assertEquals(1, noteSections.size());
            Assertions.assertEquals(
                    ".note.gnu.gold-version", noteSections.get(0).header.getName());
            Assertions.assertEquals("GNU", ((ElfNoteSection) noteSections.get(0)).getName());
            Assertions.assertEquals(ElfNoteSection.NT_GNU_GOLD_VERSION, ((ElfNoteSection) noteSections.get(0)).n_type);
            Assertions.assertEquals("gold 1.11", ((ElfNoteSection) noteSections.get(0)).descriptorAsString());

            ElfNoteSection noteSection = file.firstSectionByType(ElfNoteSection.class);
            Assertions.assertNotNull(noteSection);
            Assertions.assertEquals(".note.gnu.gold-version", noteSection.header.getName());
            Assertions.assertSame(noteSection, noteSections.get(0));

            ElfSymbolTableSection dynsym = (ElfSymbolTableSection) file.firstSectionByType(ElfSectionHeader.SHT_DYNSYM);
            Assertions.assertNotNull(dynsym);
            Assertions.assertEquals(".dynsym", dynsym.header.getName());
            Assertions.assertEquals(768, dynsym.symbols.length);
            Assertions.assertSame(file.getDynamicStringTable(), dynsym.getStringTableSection());

            ElfSymbol symbol = dynsym.symbols[0];
            Assertions.assertNull(symbol.getName());
            Assertions.assertEquals(ElfSymbol.STT_NOTYPE, symbol.getType());
            Assertions.assertEquals(0, symbol.st_size);
            Assertions.assertEquals(ElfSymbol.BINDING_LOCAL, symbol.getBinding());
            Assertions.assertEquals(ElfSymbol.Visibility.STV_DEFAULT, symbol.getVisibility());
            symbol = dynsym.symbols[1];
            Assertions.assertEquals("__cxa_finalize", symbol.getName());
            Assertions.assertSame(dynsym.getStringTableSection(), symbol.getStringTableSection());
            Assertions.assertEquals(ElfSymbol.STT_FUNC, symbol.getType());
            Assertions.assertEquals(0, symbol.st_size);
            Assertions.assertEquals(ElfSymbol.BINDING_GLOBAL, symbol.getBinding());
            Assertions.assertEquals(ElfSymbol.Visibility.STV_DEFAULT, symbol.getVisibility());
            symbol = dynsym.symbols[767];
            Assertions.assertEquals("_Unwind_GetTextRelBase", symbol.getName());
            Assertions.assertEquals(ElfSymbol.STT_FUNC, symbol.getType());
            Assertions.assertEquals(8, symbol.st_size);
            Assertions.assertEquals(ElfSymbol.BINDING_GLOBAL, symbol.getBinding());
            Assertions.assertEquals(ElfSymbol.Visibility.STV_DEFAULT, symbol.getVisibility());

            ElfSymbolTableSection symtab = (ElfSymbolTableSection) file.firstSectionByType(ElfSectionHeader.SHT_SYMTAB);
            Assertions.assertNotNull(symtab);
            Assertions.assertEquals(".symtab", symtab.header.getName());
            Assertions.assertEquals(2149, symtab.symbols.length);
            Assertions.assertSame(file.getStringTable(), symtab.getStringTableSection());
            symbol = symtab.symbols[0];
            Assertions.assertNull(symbol.getName());
            Assertions.assertEquals(ElfSymbol.STT_NOTYPE, symbol.getType());
            Assertions.assertEquals(ElfSymbol.BINDING_LOCAL, symbol.getBinding());
            Assertions.assertEquals(ElfSymbol.Visibility.STV_DEFAULT, symbol.getVisibility());
            symbol = symtab.symbols[1];
            Assertions.assertEquals("crtbegin_so.c", symbol.getName());
            Assertions.assertEquals(ElfSymbol.STT_FILE, symbol.getType());
            Assertions.assertEquals(ElfSymbol.BINDING_LOCAL, symbol.getBinding());
            Assertions.assertEquals(ElfSymbol.Visibility.STV_DEFAULT, symbol.getVisibility());
            symbol = symtab.symbols[2148];
            Assertions.assertEquals("_Unwind_GetTextRelBase", symbol.getName());
            Assertions.assertEquals(ElfSymbol.STT_FUNC, symbol.getType());
            Assertions.assertEquals(ElfSymbol.BINDING_GLOBAL, symbol.getBinding());
            Assertions.assertEquals(ElfSymbol.Visibility.STV_DEFAULT, symbol.getVisibility());

            // Relocation entries index into the symbol table linked to by the sh_link field of the
            // containing relocation section - here .dynsym, and not the .symtab section of the file.
            List<ElfRelocationSection> relSections = file.sectionsOfType(ElfRelocationSection.class);
            Assertions.assertEquals(2, relSections.size());
            Assertions.assertEquals(".rel.dyn", relSections.get(0).header.getName());
            ElfRelocationSection relPlt = relSections.get(1);
            Assertions.assertEquals(".rel.plt", relPlt.header.getName());
            Assertions.assertSame(dynsym, relPlt.getSymbolTableSection());
            Assertions.assertEquals(83, relPlt.relocations.length);

            // readelf -r:
            // "Relocation section '.rel.plt' at offset 0x9934 contains 83 entries:
            //  Offset     Info    Type            Sym.Value  Sym. Name
            // 0003ceb4  00000216 R_ARM_JUMP_SLOT   00000000   __cxa_atexit"
            ElfRelocation relocation = relPlt.relocations[0];
            Assertions.assertEquals(0x0003_ceb4, relocation.r_offset);
            Assertions.assertEquals(0x0000_0216, relocation.r_info);
            Assertions.assertEquals(2, relocation.getSymbolIndex());
            Assertions.assertSame(dynsym, relocation.getSymbolTableSection());
            Assertions.assertEquals("__cxa_atexit", relocation.getSymbol().getName());
            // The symbol at the same index in .symtab, which should not be used here:
            Assertions.assertEquals("$a", symtab.symbols[2].getName());

            TestHelper.validateHashTable(file);

            ElfDynamicSection dynamic = file.firstSectionByType(ElfDynamicSection.class);
            Assertions.assertNotNull(dynamic);
            Assertions.assertEquals(ElfDynamicSection.DF_SYMBOLIC | ElfDynamicSection.DF_BIND_NOW, dynamic.getFlags());
            Assertions.assertEquals(ElfDynamicSection.DF_1_NOW, dynamic.getFlags1());

            Assertions.assertTrue(file.getProgramHeader(0).isReadable());
            Assertions.assertFalse(file.getProgramHeader(0).isWriteable());
            Assertions.assertFalse(file.getProgramHeader(0).isExecutable());

            Assertions.assertTrue(file.getProgramHeader(2).isReadable());
            Assertions.assertFalse(file.getProgramHeader(2).isWriteable());
            Assertions.assertTrue(file.getProgramHeader(2).isExecutable());
        });
    }

    @Test
    void testLinuxAmd64BinDash() throws Exception {
        TestHelper.parseFile("linux_amd64_bindash", file -> {
            Assertions.assertEquals(ElfFile.CLASS_64, file.ei_class);
            Assertions.assertEquals(ElfFile.DATA_LSB, file.ei_data);
            Assertions.assertEquals(ElfFile.ET_DYN, file.e_type);
            Assertions.assertEquals(ElfFile.ARCH_X86_64, file.e_machine);
            Assertions.assertEquals(56, file.e_phentsize);
            Assertions.assertEquals(9, file.e_phnum);
            Assertions.assertEquals(64, file.e_shentsize);
            Assertions.assertEquals(64, file.e_phoff);
            Assertions.assertEquals(27, file.e_shnum);
            Assertions.assertEquals(119544, file.e_shoff);
            TestHelper.assertSectionNames(
                    file, null, ".interp", ".note.ABI-tag", ".note.gnu.build-id", ".gnu.hash", ".dynsym");

            ElfDynamicSection ds = file.getDynamicSection();
            Assertions.assertEquals(Collections.singletonList("libc.so.6"), ds.getNeededLibraries());

            Assertions.assertEquals("/lib64/ld-linux-x86-64.so.2", file.getInterpreter());

            ElfSection rodata = file.firstSectionByName(ElfSectionHeader.NAME_RODATA);
            Assertions.assertNotNull(rodata);
            Assertions.assertEquals(ElfSectionHeader.SHT_PROGBITS, rodata.header.sh_type);

            List<ElfSection> noteSections = file.sectionsOfType(ElfSectionHeader.SHT_NOTE);
            Assertions.assertEquals(2, noteSections.size());
            ElfNoteSection note1 = (ElfNoteSection) noteSections.get(0);
            ElfNoteSection note2 = (ElfNoteSection) noteSections.get(1);
            Assertions.assertEquals(".note.ABI-tag", note1.header.getName());
            Assertions.assertEquals("GNU", note1.getName());
            Assertions.assertEquals(ElfNoteSection.NT_GNU_ABI_TAG, note1.n_type);
            Assertions.assertEquals(
                    ElfNoteSection.GnuAbiDescriptor.ELF_NOTE_OS_LINUX, note1.descriptorAsGnuAbi().operatingSystem);
            Assertions.assertEquals(2, note1.descriptorAsGnuAbi().majorVersion);
            Assertions.assertEquals(6, note1.descriptorAsGnuAbi().minorVersion);
            Assertions.assertEquals(24, note1.descriptorAsGnuAbi().subminorVersion);
            // readelf -x .note.ABI-tag, the 16 descriptor bytes following the "GNU\0" note name:
            Assertions.assertArrayEquals(
                    new byte[] {0, 0, 0, 0, 2, 0, 0, 0, 6, 0, 0, 0, 24, 0, 0, 0}, note1.descriptorBytes());
            Assertions.assertEquals(".note.gnu.build-id", note2.header.getName());
            Assertions.assertEquals("GNU", note2.getName());
            Assertions.assertEquals(ElfNoteSection.NT_GNU_BUILD_ID, note2.n_type);
            Assertions.assertEquals(0x14, note2.descriptorBytes().length);
            Assertions.assertEquals(0x0f, note2.descriptorBytes()[0]);
            Assertions.assertArrayEquals(
                    new byte[] {
                        0x0f,
                        0x7f,
                        (byte) 0xf2,
                        (byte) 0x87,
                        (byte) 0xcf,
                        0x26,
                        (byte) 0xeb,
                        (byte) 0xa9,
                        (byte) 0xa6,
                        0x64,
                        0x3b,
                        0x12,
                        0x26,
                        0x08,
                        (byte) 0x9e,
                        (byte) 0xea,
                        0x57,
                        (byte) 0xcb,
                        0x7e,
                        0x44
                    },
                    note2.descriptorBytes());

            // This file is stripped and has no .symtab section, so relocations can only be resolved
            // through the .dynsym section linked to by the sh_link field of the relocation sections.
            Assertions.assertNull(file.getSymbolTableSection());
            ElfSymbolTableSection dynsym = file.getDynamicSymbolTableSection();
            Assertions.assertEquals(".dynsym", dynsym.header.getName());

            List<ElfRelocationAddendSection> relaSections = file.sectionsOfType(ElfRelocationAddendSection.class);
            Assertions.assertEquals(2, relaSections.size());
            Assertions.assertEquals(".rela.dyn", relaSections.get(0).header.getName());
            ElfRelocationAddendSection relaPlt = relaSections.get(1);
            Assertions.assertEquals(".rela.plt", relaPlt.header.getName());
            Assertions.assertSame(dynsym, relaPlt.getSymbolTableSection());
            Assertions.assertEquals(97, relaPlt.relocations.length);

            // readelf -r:
            // "Relocation section '.rela.plt' at offset 0x3cd0 contains 97 entries:
            //   Offset          Info           Type           Sym. Value    Sym. Name + Addend
            // 00000021cc90  000200000007 R_X86_64_JUMP_SLO 0000000000000000 sigprocmask@GLIBC_2.2.5 + 0"
            ElfRelocationAddend relocation = relaPlt.relocations[0];
            Assertions.assertEquals(0x0021_cc90, relocation.r_offset);
            Assertions.assertEquals(0x0002_0000_0007L, relocation.r_info);
            Assertions.assertEquals(0, relocation.r_addend);
            Assertions.assertEquals(ElfRelocationTypes.R_X86_64_JUMP_SLOT, relocation.getType());
            Assertions.assertEquals(2, relocation.getSymbolIndex());
            Assertions.assertSame(dynsym, relocation.getSymbolTableSection());
            Assertions.assertEquals("sigprocmask", relocation.getSymbol().getName());

            TestHelper.validateHashTable(file);
        });
    }

    @Test
    public void testObjectFile() throws Exception {
        TestHelper.parseFile("objectFile.o", file -> {
            Assertions.assertEquals(ElfFile.CLASS_32, file.ei_class);
            Assertions.assertEquals(ElfFile.DATA_LSB, file.ei_data);
            Assertions.assertEquals(ElfFile.ET_REL, file.e_type);
            TestHelper.assertSectionNames(
                    file,
                    null,
                    ".text",
                    ".rel.text",
                    ".data",
                    ".bss",
                    ".comment",
                    ".ARM.attributes",
                    ".symtab",
                    ".strtab",
                    ".shstrtab");

            List<ElfSection> sections = file.sectionsOfType(ElfSectionHeader.SHT_REL);
            Assertions.assertEquals(1, sections.size());
            ElfRelocationSection relocations = (ElfRelocationSection) sections.get(0);
            Assertions.assertEquals(1, relocations.relocations.length);

            // "Relocation section '.rel.text' at offset 0x14c contains 1 entry:
            // Offset     Info    Type            Sym.Value  Sym. Name
            // 00000006  0000080a R_ARM_THM_CALL    00000001   callee"
            ElfRelocation rel = relocations.relocations[0];
            Assertions.assertEquals(0x0000_0006, rel.r_offset);
            Assertions.assertEquals(0x0000_080A, rel.r_info);
            Assertions.assertEquals(ElfRelocationTypes.R_ARM_THM_CALL, rel.getType());
            Assertions.assertEquals("callee", rel.getSymbol().getName());
        });
    }

    @Test
    public void testObjectFile64() throws Exception {
        TestHelper.parseFile("objectFile-64.o", file -> {
            Assertions.assertEquals(ElfFile.CLASS_64, file.ei_class);
            Assertions.assertEquals(ElfFile.DATA_LSB, file.ei_data);
            Assertions.assertEquals(ElfFile.ET_REL, file.e_type);
            TestHelper.assertSectionNames(
                    file,
                    null,
                    ".text",
                    ".rela.text",
                    ".data",
                    ".bss",
                    ".comment",
                    ".note.GNU-stack",
                    ".note.gnu.property",
                    ".eh_frame",
                    ".rela.eh_frame",
                    ".symtab",
                    ".strtab",
                    ".shstrtab");

            List<ElfSection> sections = file.sectionsOfType(ElfSectionHeader.SHT_RELA);
            Assertions.assertEquals(2, sections.size());
            Assertions.assertEquals(".rela.text", sections.get(0).header.getName());
            Assertions.assertEquals(".rela.eh_frame", sections.get(1).header.getName());

            ElfRelocationAddendSection relocations = (ElfRelocationAddendSection) sections.get(0);
            // readelf -a:
            // "Relocation section '.rela.text' at offset 0x1a0 contains 1 entry:
            //  Offset          Info           Type           Sym. Value    Sym. Name + Addend
            // 00000000000d  000400000002 R_X86_64_PC32     0000000000000000 value_to_add - 4"
            Assertions.assertEquals(1, relocations.relocations.length);
            ElfRelocationAddend rel = relocations.relocations[0];
            Assertions.assertEquals(0x0000_000d, rel.r_offset);
            Assertions.assertEquals(0x0004_0000_0002L, rel.r_info);
            Assertions.assertEquals(-4, rel.r_addend);
            Assertions.assertEquals(ElfRelocationTypes.R_X86_64_PC32, rel.getType());
            Assertions.assertEquals("value_to_add", rel.getSymbol().getName());

            relocations = (ElfRelocationAddendSection) sections.get(1);
            // readelf -a:
            // "Relocation section '.rela.eh_frame' at offset 0x1b8 contains 1 entry:
            // Offset          Info           Type           Sym. Value    Sym. Name + Addend
            // 000000000020  000200000002 R_X86_64_PC32     0000000000000000 .text + 0
            // No processor specific unwind information to decode"
            Assertions.assertEquals(1, relocations.relocations.length);
            rel = relocations.relocations[0];
            Assertions.assertEquals(0x0000_0020, rel.r_offset);
            Assertions.assertEquals(0x0002_0000_0002L, rel.r_info);
            Assertions.assertEquals(0, rel.r_addend);
            Assertions.assertEquals(ElfRelocationTypes.R_X86_64_PC32, rel.getType());
        });
    }

    @Test
    void testLinuxUsrBinYes() throws Exception {
        TestHelper.parseFile("usr-bin-yes", file -> {
            ElfSymbol s = file.getELFSymbol("fputc_unlocked");
            Assertions.assertNotNull(s);
            Assertions.assertEquals("fputc_unlocked", s.getName());

            ElfSection interpSection = file.getSection(1);
            // objcopy --dump-section .interp=OUT src/test/resources/usr-bin-yes:
            Assertions.assertArrayEquals(
                    new byte[] {
                        0x2f, 0x6c, 0x69, 0x62, 0x36, 0x34, 0x2f, 0x6c, 0x64, 0x2d, 0x6c, 0x69, 0x6e, 0x75,
                        0x78, 0x2d, 0x78, 0x38, 0x36, 0x2d, 0x36, 0x34, 0x2e, 0x73, 0x6f, 0x2e, 0x32, 0x00
                    },
                    interpSection.getData());
        });
    }

    @Test
    void testSegmentGetData() throws Exception {
        TestHelper.parseFile("linux_amd64_bindash", file -> {
            ElfSegment interp = file.firstSegmentByType(ElfSegment.PT_INTERP);
            Assertions.assertNotNull(interp);
            byte[] interpData = interp.getData();
            String interpString = new String(interpData, 0, interpData.length - 1);
            Assertions.assertEquals("/lib64/ld-linux-x86-64.so.2", interpString);
            List<ElfSegment> noteSegments = file.segmentsOfType(ElfSegment.PT_NOTE);
            Assertions.assertFalse(noteSegments.isEmpty());
            ElfSegment note = noteSegments.get(0);
            byte[] noteData = note.getData();
            Assertions.assertTrue(noteData.length > 0);
        });
    }

    @Test
    void testAlign4Notes() throws Exception {
        TestHelper.parseFile("linux_amd64_bindash", file -> {
            List<ElfNoteSection> noteSections = file.sectionsOfType(ElfNoteSection.class);
            Assertions.assertEquals(2, noteSections.size());
            ElfNoteSection abiTag = noteSections.get(0);
            Assertions.assertEquals(4, abiTag.header.sh_addralign);
            List<ElfNoteSection.ElfNote> abiNotes = abiTag.notes();
            Assertions.assertEquals(1, abiNotes.size());
            ElfNoteSection.ElfNote abiNote = abiNotes.get(0);
            Assertions.assertEquals(ELF_NOTE_GNU, abiNote.name);
            Assertions.assertEquals(NT_GNU_ABI_TAG, abiNote.type);
            Assertions.assertEquals(16, abiNote.descriptorBytes().length);
            ElfNoteSection buildId = noteSections.get(1);
            Assertions.assertEquals(4, buildId.header.sh_addralign);
            List<ElfNoteSection.ElfNote> buildIdNotes = buildId.notes();
            Assertions.assertEquals(1, buildIdNotes.size());
            ElfNoteSection.ElfNote buildIdNote = buildIdNotes.get(0);
            Assertions.assertEquals(ELF_NOTE_GNU, buildIdNote.name);
            Assertions.assertEquals(NT_GNU_BUILD_ID, buildIdNote.type);
            Assertions.assertEquals(20, buildIdNote.descriptorBytes().length);
        });
    }

    @Test
    void testAlign8GnuPropertyNotes() throws Exception {
        TestHelper.parseFile("usr-bin-yes", file -> {
            for (ElfNoteSection noteSection : file.sectionsOfType(ElfNoteSection.class)) {
                if (noteSection.header.sh_addralign == Long.BYTES) {
                    List<ElfNoteSection.ElfNote> notes = noteSection.notes();
                    Assertions.assertEquals(1, notes.size());
                    ElfNoteSection.ElfNote note = notes.get(0);
                    Assertions.assertEquals(ELF_NOTE_GNU, note.name);
                    Assertions.assertEquals(NT_GNU_PROPERTY_TYPE_0, note.type);
                    Assertions.assertEquals(32, note.descriptorBytes().length);
                    return;
                }
            }

            Assertions.fail();
        });
    }

    @Test
    void testUnalignedNoteName() throws Exception {
        TestHelper.parseFile("netbsd_amd64_yes", file -> {
            List<ElfNoteSection> noteSections = file.sectionsOfType(ElfNoteSection.class);
            Assertions.assertEquals(2, noteSections.size());
            ElfNoteSection note = noteSections.get(0);
            int length = ELF_NOTE_NETBSD.length() + 1;
            Assertions.assertEquals(ELF_NOTE_NETBSD, note.getName());
            Assertions.assertEquals(length, note.n_namesz);
            Assertions.assertNotEquals(0, length % 4);
        });
    }

    private record NotePatch(int offset, int value) {}

    private static void assertParsingFailsContainingMessage(String message, NotePatch... patches) throws Exception {
        try (InputStream in = BasicTest.class.getResourceAsStream("/linux_amd64_bindash")) {
            Assertions.assertNotNull(in);
            byte[] data = in.readAllBytes();
            ElfFile originalFile = ElfFile.from(data);
            List<ElfNoteSection> elfNoteSections = originalFile.sectionsOfType(ElfNoteSection.class);
            ElfNoteSection noteSection = elfNoteSections.get(0);
            int noteOffset = (int) noteSection.header.sh_offset;
            ByteOrder order = originalFile.ei_data == ElfFile.DATA_LSB ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;

            for (NotePatch patch : patches) {
                ByteBuffer.wrap(data, noteOffset + patch.offset, Integer.BYTES)
                        .order(order)
                        .putInt(patch.value);
            }

            ElfFile patchedFile = ElfFile.from(data);
            ElfException e = Assertions.assertThrows(
                    ElfException.class, () -> patchedFile.sectionsOfType(ElfSectionHeader.SHT_NOTE));
            Assertions.assertTrue(e.getMessage().contains(message), e.getMessage());
        }
    }

    private static final int NAMESZ_OFFSET = 0;

    private static final int DESCSZ_OFFSET = Integer.BYTES;

    @Test
    void testNegativeNamesz() throws Exception {
        assertParsingFailsContainingMessage("namesz", new NotePatch(NAMESZ_OFFSET, -1));
    }

    @Test
    void testNegativeDescsz() throws Exception {
        assertParsingFailsContainingMessage("descsz", new NotePatch(DESCSZ_OFFSET, Integer.MIN_VALUE));
    }

    @Test
    void testNameszExceedsShSize() throws Exception {
        assertParsingFailsContainingMessage("namesz", new NotePatch(NAMESZ_OFFSET, Integer.MAX_VALUE));
    }

    @Test
    void testDescszExceedsShSize() throws Exception {
        assertParsingFailsContainingMessage("descsz", new NotePatch(DESCSZ_OFFSET, Integer.MAX_VALUE));
    }

    @Test
    void testCombinedNameszDescszExceedsShSize() throws Exception {
        assertParsingFailsContainingMessage(
                "descsz",
                new NotePatch(NAMESZ_OFFSET, ElfNoteSection.NHDR_SIZE),
                new NotePatch(DESCSZ_OFFSET, ElfNoteSection.NHDR_SIZE));
    }

    @Test
    void testAlign8GnuPropertyNote() throws Exception {
        TestHelper.parseFile("usr-bin-yes", file -> {
            for (ElfNoteSection noteSection : file.sectionsOfType(ElfNoteSection.class)) {
                if (noteSection.header.sh_addralign == Long.BYTES) {
                    Assertions.assertEquals("GNU", noteSection.getName());
                    Assertions.assertEquals(5, noteSection.n_type); // NT_GNU_PROPERTY_TYPE_0
                    Assertions.assertTrue(noteSection.n_descsz > 0);
                    return;
                }
            }

            Assertions.fail();
        });
    }

    @Test
    void testNoteNamesz() throws Exception {
        TestHelper.parseFile("go_amd64_notes", file -> {
            List<ElfNoteSection> noteSections = file.sectionsOfType(ElfNoteSection.class);
            Assertions.assertEquals(2, noteSections.size());
            ElfNoteSection gnuNoteSection = noteSections.get(0);
            List<ElfNoteSection.ElfNote> gnuNotes = gnuNoteSection.notes();
            Assertions.assertEquals(1, gnuNotes.size());
            ElfNoteSection.ElfNote gnuNote = gnuNotes.get(0);
            Assertions.assertEquals(ELF_NOTE_GNU, gnuNote.name);
            Assertions.assertEquals(3, gnuNote.name.length());
            Assertions.assertEquals(4, gnuNote.namesz);
            Assertions.assertEquals(NT_GNU_BUILD_ID, gnuNote.type);
            ElfNoteSection goNoteSection = noteSections.get(1);
            List<ElfNoteSection.ElfNote> goNotes = goNoteSection.notes();
            Assertions.assertEquals(1, goNotes.size());
            ElfNoteSection.ElfNote goNote = goNotes.get(0);
            Assertions.assertEquals(ELF_NOTE_GO, goNote.name);
            Assertions.assertEquals(2, goNote.name.length());
            Assertions.assertEquals(4, goNote.namesz);
            Assertions.assertEquals(NT_GO_BUILD_ID, goNote.type);
        });
    }

    @Test
    void testSegmentNotes() throws Exception {
        TestHelper.parseFile("linux_amd64_bindash", file -> {
            List<ElfSegment> noteSegments = file.segmentsOfType(ElfSegment.PT_NOTE);

            for (ElfSegment seg : noteSegments) {
                List<ElfNoteSection.ElfNote> notes = seg.notes();

                for (ElfNoteSection.ElfNote note : notes) {
                    if (ELF_NOTE_GNU.equals(note.name)) {
                        return;
                    }
                }
            }

            Assertions.fail();
        });
    }

    @Test
    void testSectionAndSegmentNotesSame() throws Exception {
        TestHelper.parseFile("linux_amd64_bindash", file -> {
            List<ElfNoteSection> noteSections = file.sectionsOfType(ElfNoteSection.class);
            int numNoteSections = noteSections.size();
            List<ElfNoteSection.ElfNote> sectionNotes = new ArrayList<>(numNoteSections);

            for (ElfNoteSection noteSection : noteSections) {
                List<ElfNoteSection.ElfNote> noteSectionNotes = noteSection.notes();
                sectionNotes.addAll(noteSectionNotes);
            }

            List<ElfSegment> noteSegments = file.segmentsOfType(ElfSegment.PT_NOTE);
            int numNoteSegments = noteSegments.size();
            List<ElfNoteSection.ElfNote> segmentNotes = new ArrayList<>(numNoteSegments);

            for (ElfSegment noteSegment : noteSegments) {
                List<ElfNoteSection.ElfNote> noteSegmentNotes = noteSegment.notes();
                segmentNotes.addAll(noteSegmentNotes);
            }

            int numSectionNotes = sectionNotes.size();
            int numSegmentNotes = segmentNotes.size();
            Assertions.assertEquals(numSectionNotes, numSegmentNotes);

            for (int i = 0; i < numSectionNotes; i++) {
                ElfNoteSection.ElfNote sectionNote = sectionNotes.get(i);
                ElfNoteSection.ElfNote segmentNote = segmentNotes.get(i);
                Assertions.assertEquals(sectionNote, segmentNote);
            }
        });
    }

    /**
     * The descriptor of a note section should be the same one as of the first note in the section - which
     * used to not be the case for {@link ElfNoteSection#NT_GNU_ABI_TAG} notes, where reading the descriptor
     * as a {@link ElfNoteSection.GnuAbiDescriptor} advanced past the descriptor before it was read.
     */
    @Test
    void testNoteSectionDescriptorIsThatOfFirstNote() throws Exception {
        List<String> fileNames = Arrays.asList(
                "android_arm_libncurses",
                "android_arm_tset",
                "go_amd64_notes",
                "linux_amd64_bindash",
                "little-endian-test",
                // Has a .note.gnu.property section with two notes in it:
                "objectFile-64.o",
                "netbsd_amd64_yes",
                "usr-bin-yes");

        for (String fileName : fileNames) {
            TestHelper.parseFile(fileName, file -> {
                List<ElfNoteSection> noteSections = file.sectionsOfType(ElfNoteSection.class);
                Assertions.assertFalse(noteSections.isEmpty());

                for (ElfNoteSection noteSection : noteSections) {
                    ElfNoteSection.ElfNote firstNote = noteSection.notes().get(0);
                    Assertions.assertEquals(noteSection.getName(), firstNote.name);
                    Assertions.assertEquals(noteSection.n_namesz, firstNote.namesz);
                    Assertions.assertEquals(noteSection.n_type, firstNote.type);
                    Assertions.assertEquals(noteSection.n_descsz, firstNote.descriptorBytes().length);
                    Assertions.assertArrayEquals(firstNote.descriptorBytes(), noteSection.descriptorBytes());
                    Assertions.assertEquals(firstNote.descriptorAsString(), noteSection.descriptorAsString());
                    Assertions.assertEquals(firstNote.descriptorAsGnuAbi(), noteSection.descriptorAsGnuAbi());
                }
            });
        }
    }

    @Test
    void testGnuAbiNoteWithTooSmallDescriptor() throws Exception {
        // A NT_GNU_ABI_TAG note descriptor needs four words - a smaller one cannot be interpreted as one.
        // Note that the notes() of the patched section cannot be read, as the descriptor size no longer
        // matches the section size, so only the note section itself is validated here:
        for (int descsz : new int[] {ElfNoteSection.GNU_ABI_DESCRIPTOR_SIZE - Integer.BYTES, 0}) {
            ElfNoteSection patchedSection = patchedAbiTagNoteSection(NOTE_DESCSZ_OFFSET, intBytes(descsz));
            Assertions.assertEquals(descsz, patchedSection.n_descsz);
            Assertions.assertEquals(descsz, patchedSection.descriptorBytes().length);
            Assertions.assertNull(patchedSection.descriptorAsGnuAbi());
        }
    }

    @Test
    void testGnuAbiDescriptorRequiresGnuAbiTagNote() throws Exception {
        // A note of another type than NT_GNU_ABI_TAG, here NT_GNU_BUILD_ID:
        assertNoGnuAbiDescriptor(NOTE_TYPE_OFFSET, intBytes(NT_GNU_BUILD_ID));
        // A note with another owner than "GNU", even when using the NT_GNU_ABI_TAG note type, which is
        // only defined for GNU notes - it is for example NT_PRSTATUS in a "CORE" note of a core dump:
        assertNoGnuAbiDescriptor(NOTE_NAME_OFFSET, new byte[] {'C', 'O', 'R', 'E'});
    }

    private static void assertNoGnuAbiDescriptor(int offsetInNote, byte[] patchedBytes) throws Exception {
        ElfNoteSection patchedSection = patchedAbiTagNoteSection(offsetInNote, patchedBytes);
        Assertions.assertNull(patchedSection.descriptorAsGnuAbi());
        Assertions.assertNull(patchedSection.notes().get(0).descriptorAsGnuAbi());
    }

    /** The offset of the n_descsz field inside a note, which follows the n_namesz field. */
    private static final int NOTE_DESCSZ_OFFSET = Integer.BYTES;
    /** The offset of the n_type field inside a note, which follows the n_namesz and n_descsz fields. */
    private static final int NOTE_TYPE_OFFSET = 2 * Integer.BYTES;
    /** The offset of the note name inside a note, which follows the note header. */
    private static final int NOTE_NAME_OFFSET = ElfNoteSection.NHDR_SIZE;

    private static byte[] intBytes(int value) {
        // All of the test files, including the linux_amd64_bindash one patched below, are little-endian:
        return ByteBuffer.allocate(Integer.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(value)
                .array();
    }

    /**
     * Patch the specified bytes into the NT_GNU_ABI_TAG note of the linux_amd64_bindash file, which has a
     * valid GNU ABI tag descriptor before being patched, and return the note section of the patched file.
     */
    private static ElfNoteSection patchedAbiTagNoteSection(int offsetInNote, byte[] patchedBytes) throws Exception {
        try (InputStream in = BasicTest.class.getResourceAsStream("/linux_amd64_bindash")) {
            Assertions.assertNotNull(in);
            byte[] data = in.readAllBytes();

            ElfFile originalFile = ElfFile.from(data);
            Assertions.assertEquals(ElfFile.DATA_LSB, originalFile.ei_data);
            int abiTagSectionIndex = -1;
            for (int i = 1; i < originalFile.e_shnum; i++) {
                if (originalFile.getSection(i) instanceof ElfNoteSection noteSection
                        && noteSection.n_type == NT_GNU_ABI_TAG) {
                    abiTagSectionIndex = i;
                }
            }
            Assertions.assertNotEquals(-1, abiTagSectionIndex);

            ElfNoteSection abiTagSection = (ElfNoteSection) originalFile.getSection(abiTagSectionIndex);
            Assertions.assertEquals(ELF_NOTE_GNU, abiTagSection.getName());
            Assertions.assertNotNull(abiTagSection.descriptorAsGnuAbi());
            Assertions.assertNotNull(abiTagSection.notes().get(0).descriptorAsGnuAbi());

            int patchOffset = (int) abiTagSection.header.sh_offset + offsetInNote;
            System.arraycopy(patchedBytes, 0, data, patchOffset, patchedBytes.length);
            return (ElfNoteSection) ElfFile.from(data).getSection(abiTagSectionIndex);
        }
    }

    @Test
    void testRelocationWithInvalidSymbolTableLink() throws Exception {
        // The linux_amd64_bindash file has 27 sections, and its .rela.plt section is of type SHT_RELA (0x4).
        String noSymbolTable = "No symbol table linked from the section of type 0x4 in a file with 27 sections";

        // No linked section:
        assertRelocationSymbolLookupFails(0, false, noSymbolTable + " (sh_link=0)");
        // No linked section, in a file which also has no section name string table:
        assertRelocationSymbolLookupFails(0, true, noSymbolTable + " (sh_link=0)");
        // Section indexes past the end of the section header table, the last one having the highest bit set:
        assertRelocationSymbolLookupFails(Short.MAX_VALUE, false, noSymbolTable + " (sh_link=32767)");
        assertRelocationSymbolLookupFails(Integer.MIN_VALUE, false, noSymbolTable + " (sh_link=2147483648)");
        // The .interp section, which is of type SHT_PROGBITS (0x1) and not a symbol table:
        assertRelocationSymbolLookupFails(
                1,
                false,
                "The section linked from the section of type 0x4 is not a symbol table,"
                        + " but of type 0x1 (sh_link=1)");
    }

    @Test
    void testRelocationWithInvalidSymbolIndex() throws Exception {
        assertRelocationSymbolIndexFails("android_arm_libncurses", ".rel.plt", 768L << 8, 768);
        assertRelocationSymbolIndexFails("linux_amd64_bindash", ".rela.plt", 0x8000_0000_0000_0007L, 0x8000_0000L);
    }

    @Test
    void testUnsignedElf32Fields() throws Exception {
        try (InputStream in = BasicTest.class.getResourceAsStream("/android_arm_tset")) {
            Assertions.assertNotNull(in);
            byte[] data = in.readAllBytes();
            ElfFile originalFile = ElfFile.from(data);
            ByteOrder order = originalFile.ei_data == ElfFile.DATA_LSB ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
            ByteBuffer buffer = ByteBuffer.wrap(data).order(order);

            buffer.putShort(18, (short) 0x8000);
            buffer.putInt(36, 0x8000_0000);

            int segmentIndex = originalFile.e_phnum - 1;
            int segmentOffset = (int) originalFile.e_phoff + segmentIndex * originalFile.e_phentsize;
            buffer.putInt(segmentOffset, 0x8000_0000);
            buffer.putInt(segmentOffset + 4, 0x8000_0000);
            buffer.putInt(segmentOffset + 8, 0x8000_0000);

            int sectionIndex = -1;
            int symbolTableIndex = -1;
            for (int i = 1; i < originalFile.e_shnum; i++) {
                ElfSection section = originalFile.getSection(i);
                if (".comment".equals(section.header.getName())) sectionIndex = i;
                if (section.header.sh_type == ElfSectionHeader.SHT_SYMTAB) symbolTableIndex = i;
            }
            Assertions.assertNotEquals(-1, sectionIndex);
            Assertions.assertNotEquals(-1, symbolTableIndex);

            int sectionHeaderOffset = (int) originalFile.e_shoff + sectionIndex * originalFile.e_shentsize;
            buffer.putInt(sectionHeaderOffset + 4, 0x8000_0000);
            buffer.putInt(sectionHeaderOffset + 12, 0x8000_0000);
            buffer.putInt(sectionHeaderOffset + 16, 0x8000_0000);

            ElfSymbolTableSection originalSymbolTable =
                    (ElfSymbolTableSection) originalFile.getSection(symbolTableIndex);
            int symbolOffset = (int) originalSymbolTable.header.sh_offset;
            buffer.putInt(symbolOffset, 0x8000_0000);
            buffer.putInt(symbolOffset + 4, 0x8000_0000);
            buffer.putInt(symbolOffset + 8, 0x8000_0000);
            buffer.putShort(symbolOffset + 14, (short) 0x8000);

            ElfFile file = ElfFile.from(data);
            Assertions.assertEquals(0x8000, file.e_machine);
            Assertions.assertEquals(0x8000_0000L, file.e_flags);

            ElfSegment segment = file.getProgramHeader(segmentIndex);
            Assertions.assertEquals(0x8000_0000L, segment.p_type);
            Assertions.assertEquals(0x8000_0000L, segment.p_offset);
            Assertions.assertEquals(0x8000_0000L, segment.p_vaddr);

            ElfSectionHeader sectionHeader = file.getSection(sectionIndex).header;
            Assertions.assertEquals(0x8000_0000L, sectionHeader.sh_type);
            Assertions.assertEquals(0x8000_0000L, sectionHeader.sh_addr);
            Assertions.assertEquals(0x8000_0000L, sectionHeader.sh_offset);

            ElfSymbol symbol = ((ElfSymbolTableSection) file.getSection(symbolTableIndex)).symbols[0];
            Assertions.assertEquals(0x8000_0000L, symbol.st_name);
            Assertions.assertEquals(0x8000_0000L, symbol.st_value);
            Assertions.assertEquals(0x8000_0000L, symbol.st_size);
            Assertions.assertEquals(0x8000, symbol.st_shndx);
        }
    }

    @Test
    void testExtendedElfHeaderCounts() throws Exception {
        try (InputStream in = BasicTest.class.getResourceAsStream("/android_arm_tset")) {
            Assertions.assertNotNull(in);
            byte[] data = in.readAllBytes();
            ElfFile originalFile = ElfFile.from(data);
            ByteOrder order = originalFile.ei_data == ElfFile.DATA_LSB ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
            ByteBuffer buffer = ByteBuffer.wrap(data).order(order);

            buffer.putShort(44, (short) 0xffff);
            buffer.putShort(48, (short) 0);
            buffer.putShort(50, (short) 0xffff);
            int sectionHeaderOffset = (int) originalFile.e_shoff;
            buffer.putInt(sectionHeaderOffset + 20, originalFile.e_shnum);
            buffer.putInt(sectionHeaderOffset + 24, originalFile.e_shstrndx);
            buffer.putInt(sectionHeaderOffset + 28, originalFile.e_phnum);

            ElfFile file = ElfFile.from(data);
            Assertions.assertEquals(originalFile.e_phnum, file.e_phnum);
            Assertions.assertEquals(originalFile.e_shnum, file.e_shnum);
            Assertions.assertEquals(originalFile.e_shstrndx, file.e_shstrndx);
            Assertions.assertEquals("/system/bin/linker", file.getInterpreter());
            TestHelper.assertSectionNames(file, null, ".interp", ".dynsym", ".dynstr", ".hash");
        }
    }

    @Test
    void testMissingSectionNameStringTable() throws Exception {
        byte[] data = bindashData();
        ElfFile originalFile = ElfFile.from(data);
        Assertions.assertNotEquals(0, originalFile.e_shstrndx);
        ByteOrder order = originalFile.ei_data == ElfFile.DATA_LSB ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;

        // Clear e_shstrndx to SHN_UNDEF, which section header strippers do, so that section 0 - which is of
        // the SHT_NULL type and not a string table - is pointed out as the section name string table:
        ByteBuffer.wrap(data).order(order).putShort(62, (short) 0);
        ElfFile patchedFile = ElfFile.from(data);
        Assertions.assertEquals(0, patchedFile.e_shstrndx);

        String expectedMessage =
                "The section name string table section at index 0 is not a string table, but of type 0x0";
        for (Executable sectionNameLookup : List.<Executable>of(
                patchedFile::getSectionNameStringTable,
                () -> patchedFile.firstSectionByName(ElfSectionHeader.NAME_RODATA),
                () -> patchedFile.getSection(1).header.getName(),
                () -> patchedFile.getSection(1).header.toString())) {
            ElfException e = Assertions.assertThrows(ElfException.class, sectionNameLookup);
            Assertions.assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    void testExtendedElfHeaderCountsWithoutSectionHeaderTable() throws Exception {
        try (InputStream in = BasicTest.class.getResourceAsStream("/android_arm_tset")) {
            Assertions.assertNotNull(in);
            byte[] data = in.readAllBytes();
            ElfFile originalFile = ElfFile.from(data);
            Assertions.assertTrue(originalFile.is32Bits());
            ByteOrder order = originalFile.ei_data == ElfFile.DATA_LSB ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;

            // The extended program header count (e_phnum) and section name string table index (e_shstrndx)
            // are both read from the initial section header, so neither can be used without one:
            for (int extendedFieldOffset : new int[] {44, 50}) {
                byte[] patchedData = data.clone();
                ByteBuffer buffer = ByteBuffer.wrap(patchedData).order(order);
                // Strip the section header table, the way section header strippers and packers do:
                buffer.putInt(32, 0); // e_shoff
                buffer.putShort(48, (short) 0); // e_shnum
                buffer.putShort(50, (short) 0); // e_shstrndx
                buffer.putShort(extendedFieldOffset, (short) 0xffff);

                ElfException e = Assertions.assertThrows(ElfException.class, () -> ElfFile.from(patchedData));
                Assertions.assertEquals(
                        "Extended header counts used in a file without a section header table", e.getMessage());
            }
        }
    }

    /** The offset of the sh_size field inside a 64-bit section header. */
    private static final int SH_SIZE_OFFSET = 32;

    /** The offset of the sh_entsize field inside a 64-bit section header. */
    private static final int SH_ENTSIZE_OFFSET = 56;

    /** The table sections of the linux_amd64_bindash file, with the entry kind named in their error messages. */
    private static final List<String[]> TABLE_SECTIONS =
            List.of(new String[] {".dynsym", "symbol table"}, new String[] {".rela.plt", "relocation"}, new String[] {
                ".dynamic", "dynamic"
            });

    @Test
    void testTrailingPartialTableEntryIsIgnored() throws Exception {
        // Growing a table section by half an entry - as a truncated or padded table is - leaves all of the
        // whole entries readable, so the section should still be parsed and expose them.
        for (String[] tableSection : TABLE_SECTIONS) {
            String sectionName = tableSection[0];
            ElfSection original = tableSectionOf(ElfFile.from(bindashData()), sectionName);
            long partialEntrySize = original.header.sh_size + original.header.sh_entsize / 2;
            ElfSection patched = patchedTableSection(sectionName, SH_SIZE_OFFSET, partialEntrySize);

            switch (sectionName) {
                case ".dynsym" ->
                    Assertions.assertEquals(
                            ((ElfSymbolTableSection) original).symbols.length,
                            ((ElfSymbolTableSection) patched).symbols.length);
                case ".rela.plt" ->
                    Assertions.assertEquals(
                            ((ElfRelocationAddendSection) original).relocations.length,
                            ((ElfRelocationAddendSection) patched).relocations.length);
                default ->
                    Assertions.assertEquals(List.of("libc.so.6"), ((ElfDynamicSection) patched).getNeededLibraries());
            }
        }
    }

    @Test
    void testZeroTableEntrySize() throws Exception {
        for (String[] tableSection : TABLE_SECTIONS) {
            String sectionName = tableSection[0];
            ElfException e = Assertions.assertThrows(
                    ElfException.class, () -> patchedTableSection(sectionName, SH_ENTSIZE_OFFSET, 0));
            Assertions.assertEquals("Invalid " + tableSection[1] + " entry size: 0", e.getMessage());
        }
    }

    private static byte[] bindashData() throws Exception {
        try (InputStream in = BasicTest.class.getResourceAsStream("/linux_amd64_bindash")) {
            Assertions.assertNotNull(in);
            return in.readAllBytes();
        }
    }

    private static ElfSection tableSectionOf(ElfFile file, String sectionName) {
        Assertions.assertFalse(file.is32Bits());
        for (int i = 1; i < file.e_shnum; i++) {
            ElfSection section = file.getSection(i);
            if (sectionName.equals(section.header.getName())) return section;
        }
        return Assertions.fail("No " + sectionName + " section");
    }

    /** The specified section of the linux_amd64_bindash file, with the specified section header field patched. */
    private static ElfSection patchedTableSection(String sectionName, int fieldOffset, long fieldValue)
            throws Exception {
        byte[] data = bindashData();
        ElfFile originalFile = ElfFile.from(data);
        int sectionIndex = -1;
        for (int i = 1; i < originalFile.e_shnum; i++) {
            if (sectionName.equals(originalFile.getSection(i).header.getName())) sectionIndex = i;
        }
        Assertions.assertNotEquals(-1, sectionIndex, sectionName);

        ByteOrder order = originalFile.ei_data == ElfFile.DATA_LSB ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        int fieldFileOffset = (int) originalFile.e_shoff + sectionIndex * originalFile.e_shentsize + fieldOffset;
        ByteBuffer.wrap(data).order(order).putLong(fieldFileOffset, fieldValue);
        return ElfFile.from(data).getSection(sectionIndex);
    }

    @Test
    void testHeaderTablesMustFitFile() throws Exception {
        try (InputStream in = BasicTest.class.getResourceAsStream("/android_arm_tset")) {
            Assertions.assertNotNull(in);
            byte[] data = in.readAllBytes();
            ElfFile originalFile = ElfFile.from(data);
            ByteOrder order = originalFile.ei_data == ElfFile.DATA_LSB ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
            int sectionHeaderOffset = (int) originalFile.e_shoff;

            byte[] sectionCountData = data.clone();
            ByteBuffer sectionCountBuffer = ByteBuffer.wrap(sectionCountData).order(order);
            int excessiveSectionCount = (int) ((data.length - originalFile.e_shoff) / originalFile.e_shentsize) + 1;
            sectionCountBuffer.putShort(48, (short) 0);
            sectionCountBuffer.putInt(sectionHeaderOffset + 20, excessiveSectionCount);
            ElfException sectionCountException =
                    Assertions.assertThrows(ElfException.class, () -> ElfFile.from(sectionCountData));
            Assertions.assertEquals(
                    "section header table exceeds file size (offset=" + originalFile.e_shoff + ", entrySize="
                            + originalFile.e_shentsize + ", entryCount=" + excessiveSectionCount + ", fileSize="
                            + data.length + ")",
                    sectionCountException.getMessage());

            byte[] programCountData = data.clone();
            ByteBuffer programCountBuffer = ByteBuffer.wrap(programCountData).order(order);
            int excessiveProgramCount = (int) ((data.length - originalFile.e_phoff) / originalFile.e_phentsize) + 1;
            programCountBuffer.putShort(44, (short) 0xffff);
            programCountBuffer.putInt(sectionHeaderOffset + 28, excessiveProgramCount);
            ElfException programCountException =
                    Assertions.assertThrows(ElfException.class, () -> ElfFile.from(programCountData));
            Assertions.assertEquals(
                    "program header table exceeds file size (offset=" + originalFile.e_phoff + ", entrySize="
                            + originalFile.e_phentsize + ", entryCount=" + excessiveProgramCount + ", fileSize="
                            + data.length + ")",
                    programCountException.getMessage());

            byte[] entrySizeData = data.clone();
            ByteBuffer.wrap(entrySizeData).order(order).putShort(46, (short) 0);
            ElfException entrySizeException =
                    Assertions.assertThrows(ElfException.class, () -> ElfFile.from(entrySizeData));
            Assertions.assertEquals("Invalid section header entry size: 0", entrySizeException.getMessage());

            byte[] unknownSizeData = data.clone();
            ByteBuffer unknownSizeBuffer = ByteBuffer.wrap(unknownSizeData).order(order);
            unknownSizeBuffer.putShort(48, (short) 0);
            unknownSizeBuffer.putInt(sectionHeaderOffset + 20, 0x1_0000);
            BackingFile delegate = new ByteArrayAsFile(unknownSizeData);
            BackingFile unknownSizeBackingFile = new BackingFile() {
                @Override
                public void seek(long offset) {
                    delegate.seek(offset);
                }

                @Override
                public void skip(int bytesToSkip) {
                    delegate.skip(bytesToSkip);
                }

                @Override
                public short readUnsignedByte() {
                    return delegate.readUnsignedByte();
                }

                @Override
                public int read(byte[] target) {
                    return delegate.read(target);
                }
            };
            ElfException unknownSizeException =
                    Assertions.assertThrows(ElfException.class, () -> ElfFile.from(unknownSizeBackingFile));
            Assertions.assertEquals(
                    "Cannot validate section header table with 65536 entries because the backing file size is unknown",
                    unknownSizeException.getMessage());
        }
    }

    @Test
    void testUnsignedElf64AddressLookup() throws Exception {
        try (InputStream in = BasicTest.class.getResourceAsStream("/linux_amd64_bindash")) {
            Assertions.assertNotNull(in);
            byte[] data = in.readAllBytes();
            ElfFile originalFile = ElfFile.from(data);
            ElfSymbolTableSection symbolTable = originalFile.getDynamicSymbolTableSection();
            int symbolIndex = -1;
            for (int i = 0; i < symbolTable.symbols.length; i++) {
                if (symbolTable.symbols[i].st_size > 0) {
                    symbolIndex = i;
                    break;
                }
            }
            Assertions.assertNotEquals(-1, symbolIndex);

            ByteOrder order = originalFile.ei_data == ElfFile.DATA_LSB ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
            int symbolOffset = (int) symbolTable.header.sh_offset + symbolIndex * (int) symbolTable.header.sh_entsize;
            ByteBuffer.wrap(data).order(order).putLong(symbolOffset + 8, Long.MIN_VALUE);

            ElfSymbol symbol = ElfFile.from(data).getELFSymbol(Long.MIN_VALUE);
            Assertions.assertNotNull(symbol);
            Assertions.assertEquals(Long.MIN_VALUE, symbol.st_value);
        }
    }

    @Test
    void testSymbolWithInvalidStringTableLink() throws Exception {
        try (InputStream in = BasicTest.class.getResourceAsStream("/linux_amd64_bindash")) {
            Assertions.assertNotNull(in);
            byte[] data = in.readAllBytes();
            ElfFile originalFile = ElfFile.from(data);
            int symbolTableIndex = -1;
            for (int i = 1; i < originalFile.e_shnum; i++) {
                if (originalFile.getSection(i).header.sh_type == ElfSectionHeader.SHT_DYNSYM) {
                    symbolTableIndex = i;
                    break;
                }
            }
            Assertions.assertNotEquals(-1, symbolTableIndex);

            ByteOrder order = originalFile.ei_data == ElfFile.DATA_LSB ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
            int shLinkOffset = (int) originalFile.e_shoff + symbolTableIndex * originalFile.e_shentsize + 40;
            ByteBuffer.wrap(data).order(order).putInt(shLinkOffset, 0x8000_0000);

            ElfSymbolTableSection symbolTable =
                    (ElfSymbolTableSection) ElfFile.from(data).getSection(symbolTableIndex);
            String expected = "No string table linked from the section of type 0xb in a file with 27 sections"
                    + " (sh_link=2147483648)";
            for (Executable lookup : List.<Executable>of(
                    symbolTable::getStringTableSection,
                    symbolTable.symbols[2]::getStringTableSection,
                    symbolTable.symbols[2]::getName)) {
                ElfException exception = Assertions.assertThrows(ElfException.class, lookup);
                Assertions.assertEquals(expected, exception.getMessage());
            }
        }
    }

    private static void assertRelocationSymbolIndexFails(
            String resourceName, String sectionName, long rInfo, long expectedSymbolIndex) throws Exception {
        try (InputStream in = BasicTest.class.getResourceAsStream("/" + resourceName)) {
            Assertions.assertNotNull(in);
            byte[] data = in.readAllBytes();
            ElfFile originalFile = ElfFile.from(data);
            ElfSection originalSection = originalFile.firstSectionByName(sectionName);
            Assertions.assertNotNull(originalSection);

            int rInfoOffset = (int) originalSection.header.sh_offset + (originalFile.is32Bits() ? 4 : 8);
            ByteOrder order = originalFile.ei_data == ElfFile.DATA_LSB ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
            ByteBuffer buffer = ByteBuffer.wrap(data).order(order);
            if (originalFile.is32Bits()) {
                buffer.putInt(rInfoOffset, (int) rInfo);
            } else {
                buffer.putLong(rInfoOffset, rInfo);
            }

            ElfSection patchedSection = ElfFile.from(data).firstSectionByName(sectionName);
            ElfRelocation relocation =
                    patchedSection instanceof ElfRelocationSection section ? section.relocations[0] : null;
            ElfRelocationAddend relocationAddend =
                    patchedSection instanceof ElfRelocationAddendSection section ? section.relocations[0] : null;
            Assertions.assertTrue(relocation != null || relocationAddend != null);
            long symbolIndex = relocation != null ? relocation.getSymbolIndex() : relocationAddend.getSymbolIndex();
            Assertions.assertEquals(expectedSymbolIndex, symbolIndex);
            Executable lookup = relocation != null ? relocation::getSymbol : relocationAddend::getSymbol;
            ElfException exception = Assertions.assertThrows(ElfException.class, lookup);
            Assertions.assertEquals("Relocation symbol index out of range: " + symbolIndex, exception.getMessage());
        }
    }

    private static void assertRelocationSymbolLookupFails(
            int shLink, boolean clearSectionNameStringTable, String expectedMessage) throws Exception {
        try (InputStream in = BasicTest.class.getResourceAsStream("/linux_amd64_bindash")) {
            Assertions.assertNotNull(in);
            byte[] data = in.readAllBytes();

            ElfFile originalFile = ElfFile.from(data);
            Assertions.assertFalse(originalFile.is32Bits());
            int sectionIndex = -1;
            for (int i = 1; i < originalFile.e_shnum; i++) {
                if (".rela.plt".equals(originalFile.getSection(i).header.getName())) {
                    sectionIndex = i;
                }
            }
            Assertions.assertNotEquals(-1, sectionIndex);
            ByteOrder order = originalFile.ei_data == ElfFile.DATA_LSB ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;

            int shLinkOffset = (int) originalFile.e_shoff + sectionIndex * originalFile.e_shentsize + 40;
            ByteBuffer.wrap(data, shLinkOffset, Integer.BYTES).order(order).putInt(shLink);
            if (clearSectionNameStringTable) {
                ByteBuffer.wrap(data, 62, Short.BYTES).order(order).putShort((short) 0);
            }

            ElfFile patchedFile = ElfFile.from(data);
            ElfRelocationAddendSection relaPlt = (ElfRelocationAddendSection) patchedFile.getSection(sectionIndex);
            Assertions.assertEquals(Integer.toUnsignedLong(shLink), relaPlt.header.sh_link);
            ElfRelocationAddend relocation = relaPlt.relocations[0];

            for (Executable symbolLookup : List.<Executable>of(
                    relaPlt::getSymbolTableSection, relocation::getSymbolTableSection, relocation::getSymbol)) {
                ElfException e = Assertions.assertThrows(ElfException.class, symbolLookup);
                Assertions.assertEquals(expectedMessage, e.getMessage());
            }
        }
    }
}
