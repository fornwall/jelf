package net.fornwall.jelf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ElfGnuHashTableTest {

    @Test
    void gnuHash() {
        Assertions.assertEquals(0xfde460be, ElfGnuHashTable.gnuHash("foobar"));
        Assertions.assertEquals(0x90f1e4b0, ElfGnuHashTable.gnuHash("strsigna"));
    }

}
