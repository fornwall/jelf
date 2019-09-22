package net.fornwall.jelf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BasicTest {

	private ElfFile parseFile(String name) throws ElfException, FileNotFoundException, IOException {
		return ElfFile.fromStream(BasicTest.class.getResourceAsStream("/" + name));
	}

	private static void assertSectionNames(ElfFile file, String... expectedSectionNames) throws IOException {
		for (int i = 0; i < expectedSectionNames.length; i++) {
			String expected = expectedSectionNames[i];
			String actual = file.getSection(i).getName();
			if (expected == null) {
				Assertions.assertNull(actual);
			} else {
				Assertions.assertEquals(expected, actual);
			}
		}
	}

	@Test
	public void testAndroidArmBinTset() throws ElfException, FileNotFoundException, IOException {
		ElfFile file = parseFile("android_arm_tset");
		Assertions.assertEquals(ElfFile.CLASS_32, file.objectSize);
		Assertions.assertEquals(ElfFile.DATA_LSB, file.encoding);
		Assertions.assertEquals(ElfFile.FT_EXEC, file.file_type);
		Assertions.assertEquals(ElfFile.ARCH_ARM, file.arch);
		Assertions.assertEquals(32, file.ph_entry_size);
		Assertions.assertEquals(7, file.num_ph);
		Assertions.assertEquals(52, file.ph_offset);
		Assertions.assertEquals(40, file.sh_entry_size);
		Assertions.assertEquals(25, file.num_sh);
		Assertions.assertEquals(15856, file.sh_offset);
		assertSectionNames(file, null, ".interp", ".dynsym", ".dynstr", ".hash", ".rel.dyn", ".rel.plt", ".plt", ".text");

		ElfSection dynamic = file.getDynamicLinkSection();
		Assertions.assertNotNull(dynamic);
		// typedef struct {
		// Elf32_Sword d_tag;
		// union {
		// Elf32_Word d_val;
		// Elf32_Addr d_ptr;
		// } d_un;
		// } Elf32_Dyn;
		Assertions.assertEquals(8, dynamic.entry_size);
		Assertions.assertEquals(248, dynamic.size);

		ElfDynamicStructure ds = dynamic.getDynamicSection();
		Assertions.assertEquals(Arrays.asList("libncursesw.so.6", "libc.so", "libdl.so"), ds.getNeededLibraries());

		Assertions.assertEquals("/system/bin/linker", file.getInterpreter());

		// Dynamic section at offset 0x2e14 contains 26 entries:
		// Tag Type Name/Value
		// 0x00000003 (PLTGOT) 0xbf44
		// 0x00000002 (PLTRELSZ) 352 (bytes)
		// 0x00000017 (JMPREL) 0x8868
		// 0x00000014 (PLTREL) REL
		// 0x00000011 (REL) 0x8828
		// 0x00000012 (RELSZ) 64 (bytes)
		// 0x00000013 (RELENT) 8 (bytes)
		// 0x00000015 (DEBUG) 0x0
		// 0x00000006 (SYMTAB) 0x8128
		// 0x0000000b (SYMENT) 16 (bytes)
		// 0x00000005 (STRTAB) 0x84a8
		// 0x0000000a (STRSZ) 513 (bytes)
		// 0x00000004 (HASH) 0x86ac
		// 0x00000001 (NEEDED) Shared library: [libncursesw.so.6]
		// 0x00000001 (NEEDED) Shared library: [libc.so]
		// 0x00000001 (NEEDED) Shared library: [libdl.so]
		// 0x0000001a (FINI_ARRAY) 0xbdf4
		// 0x0000001c (FINI_ARRAYSZ) 8 (bytes)
		// 0x00000019 (INIT_ARRAY) 0xbdfc
		// 0x0000001b (INIT_ARRAYSZ) 16 (bytes)
		// 0x00000020 (PREINIT_ARRAY) 0xbe0c
		// 0x00000021 (PREINIT_ARRAYSZ) 0x8
		// 0x0000001d (RUNPATH) Library runpath: [/data/data/com.termux/files/usr/lib]
		// 0x0000001e (FLAGS) BIND_NOW
		// 0x6ffffffb (FLAGS_1) Flags: NOW
		// 0x00000000 (NULL) 0x0
		ElfDynamicStructure dynamicStructure = file.getDynamicLinkSection().getDynamicSection();
		Assertions.assertEquals(26, dynamicStructure.entries.size());
		Assertions.assertEquals(new ElfDynamicStructure.ElfDynamicSectionEntry(3, 0xbf44), dynamicStructure.entries.get(0));
		Assertions.assertEquals(new ElfDynamicStructure.ElfDynamicSectionEntry(2, 352), dynamicStructure.entries.get(1));
		Assertions.assertEquals(new ElfDynamicStructure.ElfDynamicSectionEntry(0x17, 0x8868), dynamicStructure.entries.get(2));
		Assertions.assertEquals(new ElfDynamicStructure.ElfDynamicSectionEntry(0x6ffffffb, 1), dynamicStructure.entries.get(24));
		Assertions.assertEquals(new ElfDynamicStructure.ElfDynamicSectionEntry(0, 0), dynamicStructure.entries.get(25));
	}

	@Test
	public void testAndroidArmLibNcurses() throws ElfException, FileNotFoundException, IOException {
		ElfFile file = parseFile("android_arm_libncurses");
		Assertions.assertEquals(ElfFile.CLASS_32, file.objectSize);
		Assertions.assertEquals(ElfFile.DATA_LSB, file.encoding);
		Assertions.assertEquals(ElfFile.FT_DYN, file.file_type);
		Assertions.assertEquals(ElfFile.ARCH_ARM, file.arch);
		Assertions.assertEquals("/system/bin/linker", file.getInterpreter());

		List<ElfSection> noteSections = file.sectionsWithType(ElfSection.SHT_NOTE);
		Assertions.assertEquals(1, noteSections.size());
		Assertions.assertEquals(".note.gnu.gold-version", noteSections.get(0).getName());
		Assertions.assertEquals("GNU", noteSections.get(0).getNote().getName());
		Assertions.assertEquals(ElfNote.NT_GNU_GOLD_VERSION, noteSections.get(0).getNote().type);
		Assertions.assertEquals("gold 1.11", noteSections.get(0).getNote().descriptorAsBytes());
	}

	@Test
	public void testLinxAmd64BinDash() throws ElfException, FileNotFoundException, IOException {
		ElfFile file = parseFile("linux_amd64_bindash");
		Assertions.assertEquals(ElfFile.CLASS_64, file.objectSize);
		Assertions.assertEquals(ElfFile.DATA_LSB, file.encoding);
		Assertions.assertEquals(ElfFile.FT_DYN, file.file_type);
		Assertions.assertEquals(ElfFile.ARCH_X86_64, file.arch);
		Assertions.assertEquals(56, file.ph_entry_size);
		Assertions.assertEquals(9, file.num_ph);
		Assertions.assertEquals(64, file.sh_entry_size);
		Assertions.assertEquals(64, file.ph_offset);
		Assertions.assertEquals(27, file.num_sh);
		Assertions.assertEquals(119544, file.sh_offset);
		assertSectionNames(file, null, ".interp", ".note.ABI-tag", ".note.gnu.build-id", ".gnu.hash", ".dynsym");

		ElfDynamicStructure ds = file.getDynamicLinkSection().getDynamicSection();
		Assertions.assertEquals(Collections.singletonList("libc.so.6"), ds.getNeededLibraries());

		Assertions.assertEquals("/lib64/ld-linux-x86-64.so.2", file.getInterpreter());

		ElfSection rodata = file.firstSectionByName(ElfSection.NAME_RODATA);
		Assertions.assertNotNull(rodata);
		Assertions.assertEquals(ElfSection.SHT_PROGBITS, rodata.type);

		List<ElfSection> noteSections = file.sectionsWithType(ElfSection.SHT_NOTE);
		Assertions.assertEquals(2, noteSections.size());
		Assertions.assertEquals(".note.ABI-tag", noteSections.get(0).getName());
		Assertions.assertEquals("GNU", noteSections.get(0).getNote().getName());
		Assertions.assertEquals(ElfNote.NT_GNU_ABI_TAG, noteSections.get(0).getNote().type);
		Assertions.assertEquals(ElfNote.GnuAbiDescriptor.ELF_NOTE_OS_LINUX, noteSections.get(0).getNote().descriptorAsGnuAbi().operatingSystem);
		Assertions.assertEquals(2, noteSections.get(0).getNote().descriptorAsGnuAbi().majorVersion);
		Assertions.assertEquals(6, noteSections.get(0).getNote().descriptorAsGnuAbi().minorVersion);
		Assertions.assertEquals(24, noteSections.get(0).getNote().descriptorAsGnuAbi().subminorVersion);
		Assertions.assertEquals(".note.gnu.build-id", noteSections.get(1).getName());
		Assertions.assertEquals("GNU", noteSections.get(1).getNote().getName());
		Assertions.assertEquals(ElfNote.NT_GNU_BUILD_ID, noteSections.get(1).getNote().type);
		Assertions.assertEquals(0x14, noteSections.get(1).getNote().descriptorBytes().length);
		Assertions.assertEquals(0x0f, noteSections.get(1).getNote().descriptorBytes()[0]);
		Assertions.assertArrayEquals(new byte[]{0x0f, 0x7f, (byte) 0xf2, (byte) 0x87, (byte) 0xcf, 0x26, (byte) 0xeb, (byte) 0xa9, (byte) 0xa6, 0x64, 0x3b, 0x12, 0x26, 0x08, (byte) 0x9e, (byte) 0xea, 0x57, (byte) 0xcb, 0x7e, 0x44},
				noteSections.get(1).getNote().descriptorBytes());
	}

}
