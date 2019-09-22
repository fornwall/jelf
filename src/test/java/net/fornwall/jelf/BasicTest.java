package net.fornwall.jelf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
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

public class BasicTest {

	private static interface TestMethod {
		public void test(ElfFile file) throws Exception;
	}

	private void parseFile(String fileName, TestMethod consumer) throws Exception {
		ElfFile fromStream = ElfFile.from(BasicTest.class.getResourceAsStream('/' + fileName));
		consumer.test(fromStream);

		Path path = Paths.get(BasicTest.class.getResource('/' + fileName).getPath());
		try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(path, EnumSet.of(StandardOpenOption.READ))) {
			MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
			ElfFile fromMappedBuffer = ElfFile.from(mappedByteBuffer);
			consumer.test(fromStream);
		}
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
	void testAndroidArmBinTset() throws Exception {
		parseFile("android_arm_tset", file -> {
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
			Assertions.assertEquals("/system/bin/linker", file.getInterpreter());

			ElfSection dynamic = file.getDynamicLinkSection();
			Assertions.assertNotNull(dynamic);
			Assertions.assertEquals(8, dynamic.entry_size);
			Assertions.assertEquals(248, dynamic.size);

			ElfDynamicStructure dynamicStructure = dynamic.getDynamicSection();
			Assertions.assertEquals(Arrays.asList("libncursesw.so.6", "libc.so", "libdl.so"), dynamicStructure.getNeededLibraries());
			Assertions.assertEquals("/data/data/com.termux/files/usr/lib", dynamicStructure.getRunPath());

			Assertions.assertEquals(26, dynamicStructure.entries.size());
			Assertions.assertEquals(new ElfDynamicStructure.ElfDynamicSectionEntry(3, 0xbf44), dynamicStructure.entries.get(0));
			Assertions.assertEquals(new ElfDynamicStructure.ElfDynamicSectionEntry(2, 352), dynamicStructure.entries.get(1));
			Assertions.assertEquals(new ElfDynamicStructure.ElfDynamicSectionEntry(0x17, 0x8868), dynamicStructure.entries.get(2));
			Assertions.assertEquals(new ElfDynamicStructure.ElfDynamicSectionEntry(0x6ffffffb, 1), dynamicStructure.entries.get(24));
			Assertions.assertEquals(new ElfDynamicStructure.ElfDynamicSectionEntry(0, 0), dynamicStructure.entries.get(25));
		});
	}

	@Test
	void testAndroidArmLibNcurses() throws Exception {
		parseFile("android_arm_libncurses", file -> {
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
		});
	}

	@Test
	void testLinxAmd64BinDash() throws Exception {
		parseFile("linux_amd64_bindash", file -> {
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
		});
	}

}
