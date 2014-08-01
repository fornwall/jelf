package net.fornwall.jelf;

import java.io.RandomAccessFile;

public class Main {

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.out.println("Usage: java ELFFileParser <elf file>");
			System.exit(0);
		}

		// Parse the file.
		ElfFile elfFile = new ElfFile(new RandomAccessFile(args[0], "r"));
		System.out.println("ELF File: " + args[0]);

		System.out.println("ELF object size: " + ((elfFile.objectSize == 0) ? "Invalid Object Size" : (elfFile.objectSize == 1) ? "32-bit" : "64-bit"));
		System.out.println("ELF data encoding: " + ((elfFile.encoding == 0) ? "Invalid Data Encoding" : (elfFile.encoding == 1) ? "LSB" : "MSB"));

		System.out.println("--> Start: reading " + elfFile.num_sh + " section headers.");
		for (int i = 0; i < elfFile.num_sh; i++) {
			ElfSectionHeader sh = elfFile.getSectionHeader(i);
			int numSymbols = sh.getNumberOfSymbols();
			System.out.println("----> Start: Section (" + i + "): " + sh + ", numSymbols=" + numSymbols);

			for (int j = 0; j < numSymbols; j++) {
				ElfSymbol sym = sh.getELFSymbol(j);
				System.out.println("   " + sym);
			}
			;
			if (sh.type == ElfSectionHeader.SHT_STRTAB) {
				System.out.println("------> Start: reading string table.");
				// ElfStringTable st = sh.getStringTable();
				System.out.println("<------ End: reading string table.");
			} else if (sh.type == ElfSectionHeader.SHT_HASH) {
				System.out.println("------> Start: reading hash table.");
				sh.getHashTable();
				System.out.println("<------ End: reading hash table.");
			}
			System.out.println("<---- End: Section (" + i + ")");
		}
		System.out.println("<-- End: reading " + elfFile.num_sh + " section headers.");

		System.out.println("--> Start: reading " + elfFile.num_ph + " program headers.");
		for (int i = 0; i < elfFile.num_ph; i++) {
			ElfProgramHeader ph = elfFile.getProgramHeader(i);
			System.out.println("   " + ph);
			if (ph.type == ElfProgramHeader.PT_INTERP) {
				System.out.println("   INTERPRETER: " + ph.getIntepreter());
			}
		}
		System.out.println("<-- End: reading " + elfFile.num_ph + " program headers.");

	}
}
