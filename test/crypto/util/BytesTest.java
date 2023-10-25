package crypto.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class BytesTest {
    @Test
    void removePrefixZerosTest() {
        byte[] a = new byte[] {0, 0, 0, 1, 2, 3};
        byte[] b = new byte[] {1, 2, 3};
        assertArrayEquals(b, Bytes.removePrefixZeros(a));
        assertArrayEquals(b, Bytes.removePrefixZeros(b));
    }
}