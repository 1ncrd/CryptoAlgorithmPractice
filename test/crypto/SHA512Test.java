package crypto;

import crypto.util.Bytes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SHA512Test {
    @Test
    void hashTest() {
        String message = "abc";
        byte[] result = SHA512.hash(message.getBytes());
        assertArrayEquals(
                Bytes.hexStringToByteArray("ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a2192992a274fc1a836ba3c23a3feebbd454d4423643ce80e2a9ac94fa54ca49f"),
                result
        );
        Bytes.printByteArrayInHex(result);
    }
}