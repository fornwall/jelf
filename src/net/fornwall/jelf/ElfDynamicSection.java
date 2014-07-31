package net.fornwall.jelf;

/**
 * http://www.sco.com/developers/gabi/latest/ch5.dynamic.html#dynamic_section
 * 
 * "If an object file participates in dynamic linking, its program header table will have an element of type PT_DYNAMIC.
 * This ``segment'' contains the .dynamic section. A special symbol, _DYNAMIC, labels the section, which contains an
 * array of the following structures."
 */
public class ElfDynamicSection {

	public ElfDynamicSection(ElfParser parser, long offset) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "ElfDynamicSection[]";
	}
}
