package crypto;
import crypto.util.Bytes;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class AESTest {
    @Test
    public void encryptTest() {
        byte[] plain = Bytes.ASCIIStringToByteArray("AESisabestcipher");
        byte[] cipher = Bytes.hexStringToByteArray("d5b09264080180d5c56f26701294d20d");
        byte[] key = Bytes.ASCIIStringToByteArray(("8765432187654321"));
        AES aes = new AES(key);
        assertArrayEquals(cipher, aes.encrypt(plain));
    }
    @Test
    public void decryptTest() {
        byte[] plain = Bytes.ASCIIStringToByteArray("AESisabestcipher");
        byte[] cipher = Bytes.hexStringToByteArray("d5b09264080180d5c56f26701294d20d");
        byte[] key = Bytes.ASCIIStringToByteArray(("8765432187654321"));
        AES aes = new AES(key);
        assertArrayEquals(plain, aes.decrypt(cipher));
    }
    public static String fileToEncrypt = "plain.txt";
    @Test
    public void readFileTest() {
        Path path = Paths.get(fileToEncrypt);
        byte[] plain;
        try {
            plain = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] key = Bytes.ASCIIStringToByteArray(("abcdefgh12345678"));
        byte[] cipher = AES.encrypt(plain, key);
        System.out.println("File content:");
        Bytes.printByteArrayInHex(plain);
        System.out.println("Encrypt result:");
        Bytes.printByteArrayInHex(cipher);
        assertArrayEquals(plain, AES.decrypt(cipher, key));
    }
}