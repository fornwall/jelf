package net.fornwall.jelf;


public class ElfHashTable {

	ElfHashTable(ElfParser parser, long offset, int length) {
		parser.seek(offset);

		int num_buckets = parser.readInt();
		int num_chains = parser.readInt();

		// These could probably be memoized.
		int[] buckets = new int[num_buckets];
		int[] chains = new int[num_chains];
		// Read the bucket data.
		for (int i = 0; i < num_buckets; i++) {
			buckets[i] = parser.readInt();
		}

		// Read the chain data.
		for (int i = 0; i < num_chains; i++) {
			chains[i] = parser.readInt();
		}

		// Make sure that the amount of bytes we were supposed to read
		// was what we actually read.
		int actual = num_buckets * 4 + num_chains * 4 + 8;
		if (length != actual) {
			throw new ElfException("Error reading string table (read " + actual + "bytes, expected to " + "read " + length + "bytes).");
		}
	}

	/**
	 * This method doesn't work every time and is unreliable. Use ELFSection.getELFSymbol(String) to retrieve symbols by
	 * name. NOTE: since this method is currently broken it will always return null.
	 */
	// public ElfSymbol getSymbol(String symbolName) {
	// if (symbolName == null) {
	// return null;
	// }
	//
	// long hash = 0;
	// long g = 0;
	//
	// for (int i = 0; i < symbolName.length(); i++) {
	// hash = (hash << 4) + symbolName.charAt(i);
	// if ((g = hash & 0xf0000000) != 0) {
	// hash ^= g >>> 24;
	// }
	// hash &= ~g;
	// }
	//
	// ELFSymbol symbol = null;
	// ELFSectionHeader dyn_sh =
	// getHeader().getDynamicSymbolTableSection();
	// int index = (int)hash % num_buckets;
	// while(index != 0) {
	// symbol = dyn_sh.getELFSymbol(index);
	// if (symbolName.equals(symbol.getName())) {
	// break;
	// }
	// symbol = null;
	// index = chains[index];
	// }
	// return symbol;
	// return null;
	// }

}
