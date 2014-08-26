package net.fornwall.jelf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BasicTest {

	private File testDir;

	@Before
	public void setUp() throws Exception {
		testDir = new File(BasicTest.class.getResource(".").getFile());
	}

	private ElfFile parseFile(String name) throws ElfException, FileNotFoundException, IOException {
		return ElfFile.fromFile(new File(testDir, name));
	}

	private static void assertSectionNames(ElfFile file, String... expectedSectionNames) throws IOException {
		for (int i = 0; i < expectedSectionNames.length; i++) {
			String expected = expectedSectionNames[i];
			String actual = file.getSectionHeader(i).getName();
			if (expected == null) {
				Assert.assertNull(actual);
			} else {
				Assert.assertEquals(expected, actual);
			}
		}
	}

	@Test
	public void testAndroidArmBinTset() throws ElfException, FileNotFoundException, IOException {
		ElfFile file = parseFile("android_arm_tset");
		Assert.assertEquals(ElfFile.CLASS_32, file.objectSize);
		Assert.assertEquals(ElfFile.DATA_LSB, file.encoding);
		Assert.assertEquals(ElfFile.FT_EXEC, file.file_type);
		Assert.assertEquals(ElfFile.ARCH_ARM, file.arch);
		Assert.assertEquals(32, file.ph_entry_size);
		Assert.assertEquals(7, file.num_ph);
		Assert.assertEquals(52, file.ph_offset);
		Assert.assertEquals(40, file.sh_entry_size);
		Assert.assertEquals(25, file.num_sh);
		Assert.assertEquals(15856, file.sh_offset);
		assertSectionNames(file, null, ".interp", ".dynsym", ".dynstr", ".hash", ".rel.dyn", ".rel.plt", ".plt", ".text");

		ElfSectionHeader dynamic = file.getDynamicLinkSection();
		Assert.assertNotNull(dynamic);
		// typedef struct {
		// Elf32_Sword d_tag;
		// union {
		// Elf32_Word d_val;
		// Elf32_Addr d_ptr;
		// } d_un;
		// } Elf32_Dyn;
		Assert.assertEquals(8, dynamic.entry_size);
		Assert.assertEquals(248, dynamic.size);

		ElfDynamicStructure ds = dynamic.getDynamicSection();
		Assert.assertEquals(Arrays.asList("libncursesw.so.6", "libc.so", "libdl.so"), ds.getNeededLibraries());

		Assert.assertEquals("/system/bin/linker", file.getInterpreter());
	}

	@Test
	public void testAndroidArmLibNcurses() throws ElfException, FileNotFoundException, IOException {
		ElfFile file = parseFile("android_arm_libncurses");
		Assert.assertEquals(ElfFile.CLASS_32, file.objectSize);
		Assert.assertEquals(ElfFile.DATA_LSB, file.encoding);
		Assert.assertEquals(ElfFile.FT_DYN, file.file_type);
		Assert.assertEquals(ElfFile.ARCH_ARM, file.arch);
		Assert.assertEquals("/system/bin/linker", file.getInterpreter());
	}

	@Test
	public void testLinxAmd64BinDash() throws ElfException, FileNotFoundException, IOException {
		ElfFile file = parseFile("linux_amd64_bindash");
		Assert.assertEquals(ElfFile.CLASS_64, file.objectSize);
		Assert.assertEquals(ElfFile.DATA_LSB, file.encoding);
		Assert.assertEquals(ElfFile.FT_DYN, file.file_type);
		Assert.assertEquals(ElfFile.ARCH_X86_64, file.arch);
		Assert.assertEquals(56, file.ph_entry_size);
		Assert.assertEquals(9, file.num_ph);
		Assert.assertEquals(64, file.sh_entry_size);
		Assert.assertEquals(64, file.ph_offset);
		Assert.assertEquals(27, file.num_sh);
		Assert.assertEquals(119544, file.sh_offset);
		assertSectionNames(file, null, ".interp", ".note.ABI-tag", ".note.gnu.build-id", ".gnu.hash", ".dynsym");

		ElfDynamicStructure ds = file.getDynamicLinkSection().getDynamicSection();
		Assert.assertEquals(Arrays.asList("libc.so.6"), ds.getNeededLibraries());

		Assert.assertEquals("/lib64/ld-linux-x86-64.so.2", file.getInterpreter());
	}

}
