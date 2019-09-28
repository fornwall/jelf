package net.fornwall.jelf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ElfHashTableTest {

    @Test
    void elfHash() {
        Assertions.assertEquals(0x0bc334fc, ElfHashTable.elfHash("freelocal"));
        Assertions.assertEquals(0x06d65882, ElfHashTable.elfHash("foobar"));
        Assertions.assertEquals(0x007b7cb3, ElfHashTable.elfHash("tputs"));
    }

}
