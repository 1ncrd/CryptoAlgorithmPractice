package crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MD5Test {
    @Test
    void hashTest() {
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", MD5.hash(""));
        assertEquals("0cc175b9c0f1b6a831c399e269772661", MD5.hash("a"));
        assertEquals("900150983cd24fb0d6963f7d28e17f72", MD5.hash("abc"));
        assertEquals("d174ab98d277d9f5a5611c2c9f419d9f", MD5.hash("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"));
    }
}