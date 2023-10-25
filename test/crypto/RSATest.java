package crypto;

import crypto.util.Bytes;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class RSATest {

    @Test
    void encryptTest1() {
        for (int i = 0; i < 100; i++) {
            BigInteger p = BigInteger.valueOf(3);
            RSA rsa = new RSA(512);
            BigInteger c = rsa.encrypt(p);
            BigInteger _p = rsa.decrypt(c);

            assertEquals(p, _p);
        }
    }

    @Test
    void encryptTest2() {
        byte[] plain = Bytes.ASCIIStringToByteArray("Hello, Hello, Hello, Hello, Hello, Hello");
        RSA rsa = new RSA(4096);
        byte[] cipher = rsa.encrypt(plain);
        byte[] _plain = rsa.decrypt(cipher);

        assertArrayEquals(plain, _plain);
    }

    @Test
    void sign() {
        RSA rsa = new RSA(1024);
        String msg = "msg";
        String sign = rsa.sign(msg);

        assertTrue(rsa.verify(msg, sign));
    }
}