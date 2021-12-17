package net.fornwall.jelf;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

import org.junit.jupiter.api.Assertions;

public class TestHelper {
	public interface TestMethod {
		void test(ElfFile file) throws Exception;
	}

	public static void parseFile(String fileName, TestMethod consumer) throws Exception {
		ElfFile fromStream = ElfFile.from(BasicTest.class.getResourceAsStream('/' + fileName));
		consumer.test(fromStream);

		Path path = Paths.get(BasicTest.class.getResource('/' + fileName).toURI());
		try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(path, EnumSet.of(StandardOpenOption.READ))) {
			MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
			ElfFile fromMappedBuffer = ElfFile.from(mappedByteBuffer);
			consumer.test(fromMappedBuffer);
		}
	}

	public static void assertSectionNames(ElfFile file, String... expectedSectionNames) {
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

	public static void validateHashTable(ElfFile file) {
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
				if (i++ < gnuHashTable.symoffset)
					continue;
				Assertions.assertSame(s, gnuHashTable.lookupSymbol(s.getName(), dynsym));
			}
			Assertions.assertNull(gnuHashTable.lookupSymbol("non_existing", dynsym));
		}

	}

}
