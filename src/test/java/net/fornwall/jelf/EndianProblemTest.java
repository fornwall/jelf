package net.fornwall.jelf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EndianProblemTest {
	@Test
	public void testObjectFile() throws Exception {
		TestHelper.parseFile("little-endian-test", file -> {
			Assertions.assertEquals(ElfFile.CLASS_64, file.objectSize);
			Assertions.assertEquals(ElfFile.DATA_LSB, file.encoding);
			Assertions.assertEquals(0x8000_0040L, file.entry_point);
		});
	}
}
