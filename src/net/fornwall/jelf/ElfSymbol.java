package net.fornwall.jelf;

import java.io.IOException;

public class ElfSymbol {

	/** Binding specifying that local symbols are not visible outside the object file that contains its definition. */
	public static final int BINDING_LOCAL = 0;
	/** Binding specifying that global symbols are visible to all object files being combined. */
	public static final int BINDING_GLOBAL = 1;
	/** Binding specifying that the symbol resembles a global symbol, but has a lower precedence. */
	public static final int BINDING_WEAK = 2;
	/** Lower bound binding values reserved for processor specific semantics. */
	public static final int BINDING_LOPROC = 13;
	/** Upper bound binding values reserved for processor specific semantics. */
	public static final int BINDING_HIPROC = 15;

	/** Type specifying that the symbol is unspecified. */
	public static final byte TYPE_NOOBJECT = 0;
	/** Type specifying that the symbol is associated with an object. */
	public static final byte TYPE_OBJECT = 1;
	/** Type specifying that the symbol is associated with a function. */
	public static final byte TYPE_FUNCTION = 2;
	/**
	 * Type specifying that the symbol is associated with a section. Symbol table entries of this type exist for
	 * relocation and normally have the binding BINDING_LOCAL.
	 */
	public static final byte TYPE_SECTION = 3;
	/** Type defining that the symbol is associated with a file. */
	public static final byte TYPE_FILE = 4;
	/** Lower bound type reserved for processor specific semantics. */
	public static final byte TYPE_LOPROC = 13;
	/** Upper bound type reserved for processor specific semantics. */
	public static final byte TYPE_HIPROC = 15;

	/**
	 * Index into the symbol string table that holds the character representation of the symbols. 0 means the symbol has
	 * no character name.
	 */
	private final int name_ndx; // Elf32_Word
	/** Value of the associated symbol. This may be a relativa address for .so or absolute address for other ELFs. */
	public final int value; // Elf32_Addr
	/** Size of the symbol. 0 if the symbol has no size or the size is unknown. */
	public final int size; // Elf32_Word
	/** Specifies the symbol type and binding attributes. */
	private final byte info; // unsigned char
	/** Currently holds the value of 0 and has no meaning. */
	public final byte other; // unsigned char
	/**
	 * Index to the associated section header. This value will need to be read as an unsigned short if we compare it to
	 * ELFSectionHeader.NDX_LORESERVE and ELFSectionHeader.NDX_HIRESERVE.
	 */
	public final short section_header_ndx; // Elf32_Half

	private final int section_type;

	/** Offset from the beginning of the file to this symbol. */
	public final long offset;

	private final ElfFile elfHeader;

	ElfSymbol(ElfParser parser, long offset, int section_type) throws ElfException, IOException {
		this.elfHeader = parser.elfFile;
		parser.fsFile.seek(offset);
		this.offset = offset;
		name_ndx = parser.readInt();
		value = parser.readInt();
		size = parser.readInt();
		info = parser.readByte();
		other = parser.readByte();
		section_header_ndx = parser.readShort();

		this.section_type = section_type;

		switch (getType()) {
		case TYPE_NOOBJECT:
			break;
		case TYPE_OBJECT:
			break;
		case TYPE_FUNCTION:
			break;
		case TYPE_SECTION:
			break;
		case TYPE_FILE:
			break;
		case TYPE_LOPROC:
			break;
		case TYPE_HIPROC:
			break;
		default:
			break;
		}
	}

	/** Returns the binding for this symbol. */
	public int getBinding() {
		return info >> 4;
	}

	/** Returns the symbol type. */
	public int getType() {
		return info & 0x0F;
	}

	/** Returns the name of the symbol or null if the symbol has no name. */
	public String getName() throws ElfException, IOException {
		// Check to make sure this symbol has a name.
		if (name_ndx == 0) return null;

		// Retrieve the name of the symbol from the correct string table.
		String symbol_name = null;
		if (section_type == ElfSectionHeader.TYPE_SYMTBL) {
			symbol_name = elfHeader.getStringTable().get(name_ndx);
		} else if (section_type == ElfSectionHeader.TYPE_DYNSYM) {
			symbol_name = elfHeader.getDynamicStringTable().get(name_ndx);
		}
		return symbol_name;
	}

	@Override
	public String toString() {
		String typeString;
		switch (getType()) {
		case TYPE_NOOBJECT:
			typeString = "object";
			break;
		case TYPE_OBJECT:
			typeString = "object";
			break;
		case TYPE_FUNCTION:
			typeString = "function";
			break;
		case TYPE_SECTION:
			typeString = "section";
			break;
		case TYPE_FILE:
			typeString = "file";
			break;
		case TYPE_LOPROC:
			typeString = "loproc";
			break;
		case TYPE_HIPROC:
			typeString = "hiproc";
			break;
		default:
			typeString = "???";
			break;
		}

		try {
			return "ElfSymbol[name=" + getName() + ", type=" + typeString + "]";
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
