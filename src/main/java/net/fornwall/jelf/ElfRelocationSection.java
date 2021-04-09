package net.fornwall.jelf;

public class ElfRelocationSection extends ElfSection {
	public final ElfRelocation[] relocations;

	public ElfRelocationSection(ElfParser parser, ElfSectionHeader header) {
		super(header);

		int num_entries = (int) (header.size / header.entry_size);
		relocations = new ElfRelocation[num_entries];
		for (int i = 0; i < num_entries; i++) {
			final long relOffset = header.section_offset + (i * header.entry_size);
			relocations[i] = new ElfRelocation(parser, relOffset);
		}
	}

}
