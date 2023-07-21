package net.fornwall.jelf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
            TestHelper.assertSectionNames(file, null, ".interp", ".dynsym", ".dynstr", ".hash", ".rel.dyn", ".rel.plt",
                    ".plt", ".text");
            Assertions.assertEquals("/system/bin/linker", file.getInterpreter());

            ElfDynamicSection dynamic = file.getDynamicSection();
            Assertions.assertNotNull(dynamic);
            Assertions.assertEquals(".dynamic", dynamic.header.getName());
            Assertions.assertEquals(8, dynamic.header.sh_entsize);
            Assertions.assertEquals(248, dynamic.header.sh_size);
            Assertions.assertEquals(ElfDynamicSection.DF_BIND_NOW, dynamic.getFlags());
            Assertions.assertEquals(ElfDynamicSection.DF_1_NOW, dynamic.getFlags1());

            Assertions.assertEquals(Arrays.asList("libncursesw.so.6", "libc.so", "libdl.so"),
                    dynamic.getNeededLibraries());
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
            Assertions.assertEquals(".note.gnu.gold-version", noteSections.get(0).header.getName());
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

            ElfSymbol symbol = dynsym.symbols[0];
            Assertions.assertNull(symbol.getName());
            Assertions.assertEquals(ElfSymbol.STT_NOTYPE, symbol.getType());
            Assertions.assertEquals(0, symbol.st_size);
            Assertions.assertEquals(ElfSymbol.BINDING_LOCAL, symbol.getBinding());
            Assertions.assertEquals(ElfSymbol.Visibility.STV_DEFAULT, symbol.getVisibility());
            symbol = dynsym.symbols[1];
            Assertions.assertEquals("__cxa_finalize", symbol.getName());
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
            TestHelper.assertSectionNames(file, null, ".interp", ".note.ABI-tag", ".note.gnu.build-id", ".gnu.hash",
                    ".dynsym");

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
            Assertions.assertEquals(ElfNoteSection.GnuAbiDescriptor.ELF_NOTE_OS_LINUX,
                    note1.descriptorAsGnuAbi().operatingSystem);
            Assertions.assertEquals(2, note1.descriptorAsGnuAbi().majorVersion);
            Assertions.assertEquals(6, note1.descriptorAsGnuAbi().minorVersion);
            Assertions.assertEquals(24, note1.descriptorAsGnuAbi().subminorVersion);
            Assertions.assertEquals(".note.gnu.build-id", note2.header.getName());
            Assertions.assertEquals("GNU", note2.getName());
            Assertions.assertEquals(ElfNoteSection.NT_GNU_BUILD_ID, note2.n_type);
            Assertions.assertEquals(0x14, note2.descriptorBytes().length);
            Assertions.assertEquals(0x0f, note2.descriptorBytes()[0]);
            Assertions.assertArrayEquals(new byte[]{0x0f, 0x7f, (byte) 0xf2, (byte) 0x87, (byte) 0xcf, 0x26,
                    (byte) 0xeb, (byte) 0xa9, (byte) 0xa6, 0x64, 0x3b, 0x12, 0x26, 0x08, (byte) 0x9e, (byte) 0xea, 0x57,
                    (byte) 0xcb, 0x7e, 0x44}, note2.descriptorBytes());

            TestHelper.validateHashTable(file);
        });
    }

    @Test
    public void testObjectFile() throws Exception {
        TestHelper.parseFile("objectFile.o", file -> {
            Assertions.assertEquals(ElfFile.CLASS_32, file.ei_class);
            Assertions.assertEquals(ElfFile.DATA_LSB, file.ei_data);
            Assertions.assertEquals(ElfFile.ET_REL, file.e_type);
            TestHelper.assertSectionNames(file, null, ".text", ".rel.text", ".data", ".bss", ".comment",
                    ".ARM.attributes", ".symtab", ".strtab", ".shstrtab");

            List<ElfSection> sections = file.sectionsOfType(ElfSectionHeader.SHT_REL);
            Assertions.assertEquals(1, sections.size());
            ElfRelocationSection relocations = (ElfRelocationSection) sections.get(0);
            Assertions.assertEquals(1, relocations.relocations.length);

            // "Relocation section '.rel.text' at offset 0x14c contains 1 entry:
            // Offset     Info    Type            Sym.Value  Sym. Name
            //00000006  0000080a R_ARM_THM_CALL    00000001   callee"
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
            TestHelper.assertSectionNames(file, null, ".text", ".rela.text", ".data", ".bss", ".comment",
                    ".note.GNU-stack", ".note.gnu.property", ".eh_frame", ".rela.eh_frame", ".symtab", ".strtab", ".shstrtab");

            List<ElfSection> sections = file.sectionsOfType(ElfSectionHeader.SHT_RELA);
            Assertions.assertEquals(2, sections.size());
            Assertions.assertEquals(".rela.text", sections.get(0).header.getName());
            Assertions.assertEquals(".rela.eh_frame", sections.get(1).header.getName());

            ElfRelocationAddendSection relocations = (ElfRelocationAddendSection) sections.get(0);
            // readelf -a:
            // "Relocation section '.rela.text' at offset 0x1a0 contains 1 entry:
            //  Offset          Info           Type           Sym. Value    Sym. Name + Addend
            //00000000000d  000400000002 R_X86_64_PC32     0000000000000000 value_to_add - 4"
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
            Assertions.assertArrayEquals(new byte[]{
                    0x2f, 0x6c, 0x69, 0x62, 0x36, 0x34, 0x2f, 0x6c, 0x64, 0x2d, 0x6c, 0x69, 0x6e, 0x75,
                    0x78, 0x2d, 0x78, 0x38, 0x36, 0x2d, 0x36, 0x34, 0x2e, 0x73, 0x6f, 0x2e, 0x32, 0x00
            }, interpSection.getData());
        });
    }
}
