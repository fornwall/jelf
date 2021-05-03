package net.fornwall.jelf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

class BasicTest {

	private interface TestMethod {
		void test(ElfFile file) throws Exception;
	}

	private void parseFile(String fileName, TestMethod consumer) throws Exception {
		ElfFile fromStream = ElfFile.from(BasicTest.class.getResourceAsStream('/' + fileName));
		consumer.test(fromStream);

		Path path = Paths.get(BasicTest.class.getResource('/' + fileName).getPath());
		try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(path, EnumSet.of(StandardOpenOption.READ))) {
			MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
			ElfFile fromMappedBuffer = ElfFile.from(mappedByteBuffer);
			consumer.test(fromMappedBuffer);
		}
	}

	private static void assertSectionNames(ElfFile file, String... expectedSectionNames) {
		for (int i = 0; i < expectedSectionNames.length; i++) {
			String expected = expectedSectionNames[i];
			String actual = file.getSection(i).header.getName();
			if (expected == null) {
				Assertions.assertNull(actual);
			} else {
				Assertions.assertEquals(expected, actual);
			}
		}
	}

	private void validateHashTable(ElfFile file) {
		ElfSymbolTableSection dynsym = (ElfSymbolTableSection) file.firstSectionByType(ElfSectionHeader.SHT_DYNSYM);

		ElfHashTable hashTable = file.firstSectionByType(ElfHashTable.class);
		if (hashTable != null) {
			for (ElfSymbol s : dynsym.symbols) {
				if (s.getName() != null) {
					Assertions.assertSame(s, hashTable.lookupSymbol(s.getName(), dynsym));
				}
			}
			Assertions.assertNull(hashTable.lookupSymbol("non_existing", dynsym));
		}

		ElfGnuHashTable gnuHashTable = file.firstSectionByType(ElfGnuHashTable.class);
		if (gnuHashTable != null) {
		    int i = 0;
			for (ElfSymbol s : dynsym.symbols) {
				if (i++ < gnuHashTable.symbolOffset) continue;
				Assertions.assertSame(s, gnuHashTable.lookupSymbol(s.getName(), dynsym));
			}
			Assertions.assertNull(gnuHashTable.lookupSymbol("non_existing", dynsym));
		}

	}

	@Test
	void testAndroidArmBinTset() throws Exception {
		parseFile("android_arm_tset", file -> {
			Assertions.assertEquals(ElfFile.CLASS_32, file.objectSize);
			Assertions.assertEquals(ElfFile.DATA_LSB, file.encoding);
			Assertions.assertEquals(ElfFile.ET_EXEC, file.e_type);
			Assertions.assertEquals(ElfFile.ARCH_ARM, file.arch);
			Assertions.assertEquals(32, file.ph_entry_size);
			Assertions.assertEquals(7, file.num_ph);
			Assertions.assertEquals(52, file.ph_offset);
			Assertions.assertEquals(40, file.sh_entry_size);
			Assertions.assertEquals(25, file.num_sh);
			Assertions.assertEquals(15856, file.sh_offset);
			assertSectionNames(file, null, ".interp", ".dynsym", ".dynstr", ".hash", ".rel.dyn", ".rel.plt", ".plt", ".text");
			Assertions.assertEquals("/system/bin/linker", file.getInterpreter());

			ElfDynamicSection dynamic = file.getDynamicSection();
			Assertions.assertNotNull(dynamic);
			Assertions.assertEquals(".dynamic", dynamic.header.getName());
			Assertions.assertEquals(8, dynamic.header.entry_size);
			Assertions.assertEquals(248, dynamic.header.size);
			Assertions.assertEquals(ElfDynamicSection.DF_BIND_NOW, dynamic.getFlags());
			Assertions.assertEquals(ElfDynamicSection.DF_1_NOW, dynamic.getFlags1());

			Assertions.assertEquals(Arrays.asList("libncursesw.so.6", "libc.so", "libdl.so"), dynamic.getNeededLibraries());
			Assertions.assertEquals("/data/data/com.termux/files/usr/lib", dynamic.getRunPath());

			Assertions.assertEquals(26, dynamic.entries.size());
			Assertions.assertEquals(new ElfDynamicSection.ElfDynamicStructure(3, 0xbf44), dynamic.entries.get(0));
			Assertions.assertEquals(new ElfDynamicSection.ElfDynamicStructure(2, 352), dynamic.entries.get(1));
			Assertions.assertEquals(new ElfDynamicSection.ElfDynamicStructure(0x17, 0x8868), dynamic.entries.get(2));
			Assertions.assertEquals(new ElfDynamicSection.ElfDynamicStructure(0x6ffffffb, 1), dynamic.entries.get(24));
			Assertions.assertEquals(new ElfDynamicSection.ElfDynamicStructure(0, 0), dynamic.entries.get(25));

			validateHashTable(file);
		});
	}

	@Test
	void testAndroidArmLibNcurses() throws Exception {
		parseFile("android_arm_libncurses", file -> {
			Assertions.assertEquals(ElfFile.CLASS_32, file.objectSize);
			Assertions.assertEquals(ElfFile.DATA_LSB, file.encoding);
			Assertions.assertEquals(ElfFile.ET_DYN, file.e_type);
			Assertions.assertEquals(ElfFile.ARCH_ARM, file.arch);
			Assertions.assertEquals("/system/bin/linker", file.getInterpreter());

			List<ElfSection> noteSections = file.sectionsOfType(ElfSectionHeader.SHT_NOTE);
			Assertions.assertEquals(1, noteSections.size());
			Assertions.assertEquals(".note.gnu.gold-version", noteSections.get(0).header.getName());
			Assertions.assertEquals("GNU", ((ElfNoteSection) noteSections.get(0)).getName());
			Assertions.assertEquals(ElfNoteSection.NT_GNU_GOLD_VERSION, ((ElfNoteSection) noteSections.get(0)).type);
			Assertions.assertEquals("gold 1.11", ((ElfNoteSection) noteSections.get(0)).descriptorAsString());

			ElfNoteSection noteSection = file.firstSectionByType(ElfNoteSection.class);
			Assertions.assertEquals(".note.gnu.gold-version", noteSection.header.getName());
			Assertions.assertSame(noteSection, noteSections.get(0));

			ElfSymbolTableSection dynsym = (ElfSymbolTableSection) file.firstSectionByType(ElfSectionHeader.SHT_DYNSYM);
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

			validateHashTable(file);

			ElfDynamicSection dynamic = file.firstSectionByType(ElfDynamicSection.class);
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
	void testLinxAmd64BinDash() throws Exception {
		parseFile("linux_amd64_bindash", file -> {
			Assertions.assertEquals(ElfFile.CLASS_64, file.objectSize);
			Assertions.assertEquals(ElfFile.DATA_LSB, file.encoding);
			Assertions.assertEquals(ElfFile.ET_DYN, file.e_type);
			Assertions.assertEquals(ElfFile.ARCH_X86_64, file.arch);
			Assertions.assertEquals(56, file.ph_entry_size);
			Assertions.assertEquals(9, file.num_ph);
			Assertions.assertEquals(64, file.sh_entry_size);
			Assertions.assertEquals(64, file.ph_offset);
			Assertions.assertEquals(27, file.num_sh);
			Assertions.assertEquals(119544, file.sh_offset);
			assertSectionNames(file, null, ".interp", ".note.ABI-tag", ".note.gnu.build-id", ".gnu.hash", ".dynsym");

			ElfDynamicSection ds = file.getDynamicSection();
			Assertions.assertEquals(Collections.singletonList("libc.so.6"), ds.getNeededLibraries());

			Assertions.assertEquals("/lib64/ld-linux-x86-64.so.2", file.getInterpreter());

			ElfSection rodata = file.firstSectionByName(ElfSectionHeader.NAME_RODATA);
			Assertions.assertNotNull(rodata);
			Assertions.assertEquals(ElfSectionHeader.SHT_PROGBITS, rodata.header.type);

			List<ElfSection> noteSections = file.sectionsOfType(ElfSectionHeader.SHT_NOTE);
			Assertions.assertEquals(2, noteSections.size());
			ElfNoteSection note1 = (ElfNoteSection) noteSections.get(0);
			ElfNoteSection note2 = (ElfNoteSection) noteSections.get(1);
			Assertions.assertEquals(".note.ABI-tag", note1.header.getName());
			Assertions.assertEquals("GNU", note1.getName());
			Assertions.assertEquals(ElfNoteSection.NT_GNU_ABI_TAG, note1.type);
			Assertions.assertEquals(ElfNoteSection.GnuAbiDescriptor.ELF_NOTE_OS_LINUX, note1.descriptorAsGnuAbi().operatingSystem);
			Assertions.assertEquals(2, note1.descriptorAsGnuAbi().majorVersion);
			Assertions.assertEquals(6, note1.descriptorAsGnuAbi().minorVersion);
			Assertions.assertEquals(24, note1.descriptorAsGnuAbi().subminorVersion);
			Assertions.assertEquals(".note.gnu.build-id", note2.header.getName());
			Assertions.assertEquals("GNU", note2.getName());
			Assertions.assertEquals(ElfNoteSection.NT_GNU_BUILD_ID, note2.type);
			Assertions.assertEquals(0x14, note2.descriptorBytes().length);
			Assertions.assertEquals(0x0f, note2.descriptorBytes()[0]);
			Assertions.assertArrayEquals(new byte[]{0x0f, 0x7f, (byte) 0xf2, (byte) 0x87, (byte) 0xcf, 0x26, (byte) 0xeb, (byte) 0xa9, (byte) 0xa6, 0x64, 0x3b, 0x12, 0x26, 0x08, (byte) 0x9e, (byte) 0xea, 0x57, (byte) 0xcb, 0x7e, 0x44},
					note2.descriptorBytes());

			validateHashTable(file);
		});
	}

	@Test
	public void testObjectFile() throws Exception {
		parseFile("objectFile.o", file -> {
			Assertions.assertEquals(ElfFile.CLASS_32, file.objectSize);
			Assertions.assertEquals(ElfFile.DATA_LSB, file.encoding);
			Assertions.assertEquals(ElfFile.ET_REL, file.e_type);
			assertSectionNames(file, null, ".text", ".rel.text", ".data", ".bss",
					".comment", ".ARM.attributes", ".symtab", ".strtab", ".shstrtab");

			List<ElfSection> sections = file.sectionsOfType(ElfSectionHeader.SHT_REL);
			Assertions.assertEquals(1, sections.size());
			ElfRelocationSection relocations = (ElfRelocationSection) sections.get(0);
			Assertions.assertEquals(1, relocations.relocations.length);

			ElfRelocation rel = relocations.relocations[0];
			Assertions.assertEquals(0x0000_0006, rel.r_offset);
			Assertions.assertEquals(0x0000_080A, rel.r_info);
		});
	}

}
