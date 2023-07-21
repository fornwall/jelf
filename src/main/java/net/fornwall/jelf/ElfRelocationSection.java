package net.fornwall.jelf;

public final class ElfRelocationSection extends ElfSection {
	public final ElfRelocation[] relocations;

	public ElfRelocationSection(ElfParser parser, ElfSectionHeader header) {
		super(header);

		int num_entries = (int) (header.sh_size / header.sh_entsize);
		relocations = new ElfRelocation[num_entries];
		for (int i = 0; i < num_entries; i++) {
			final long relOffset = header.sh_offset + (i * header.sh_entsize);
			relocations[i] = new ElfRelocation(parser, relOffset);
		}
	}

}
