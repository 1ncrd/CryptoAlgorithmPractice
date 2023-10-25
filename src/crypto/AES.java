package crypto;

import crypto.util.Number;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import static crypto.util.Bytes.*;

/**
 * only support ECB mode and 128-bit AES
 */
public class AES {
    // Constants for AES
    private static final int[] RCON = {
            0x01000000, 0x02000000, 0x04000000, 0x08000000, 0x10000000,
            0x20000000, 0x40000000, 0x80000000, 0x1B000000, 0x36000000};
    // AES S-Box
    private static final int[] S = {
            0x63, 0x7C, 0x77, 0x7B, 0xF2, 0x6B, 0x6F, 0xC5, 0x30, 0x01, 0x67, 0x2B, 0xFE, 0xD7, 0xAB, 0x76,
            0xCA, 0x82, 0xC9, 0x7D, 0xFA, 0x59, 0x47, 0xF0, 0xAD, 0xD4, 0xA2, 0xAF, 0x9C, 0xA4, 0x72, 0xC0,
            0xB7, 0xFD, 0x93, 0x26, 0x36, 0x3F, 0xF7, 0xCC, 0x34, 0xA5, 0xE5, 0xF1, 0x71, 0xD8, 0x31, 0x15,
            0x04, 0xC7, 0x23, 0xC3, 0x18, 0x96, 0x05, 0x9A, 0x07, 0x12, 0x80, 0xE2, 0xEB, 0x27, 0xB2, 0x75,
            0x09, 0x83, 0x2C, 0x1A, 0x1B, 0x6E, 0x5A, 0xA0, 0x52, 0x3B, 0xD6, 0xB3, 0x29, 0xE3, 0x2F, 0x84,
            0x53, 0xD1, 0x00, 0xED, 0x20, 0xFC, 0xB1, 0x5B, 0x6A, 0xCB, 0xBE, 0x39, 0x4A, 0x4C, 0x58, 0xCF,
            0xD0, 0xEF, 0xAA, 0xFB, 0x43, 0x4D, 0x33, 0x85, 0x45, 0xF9, 0x02, 0x7F, 0x50, 0x3C, 0x9F, 0xA8,
            0x51, 0xA3, 0x40, 0x8F, 0x92, 0x9D, 0x38, 0xF5, 0xBC, 0xB6, 0xDA, 0x21, 0x10, 0xFF, 0xF3, 0xD2,
            0xCD, 0x0C, 0x13, 0xEC, 0x5F, 0x97, 0x44, 0x17, 0xC4, 0xA7, 0x7E, 0x3D, 0x64, 0x5D, 0x19, 0x73,
            0x60, 0x81, 0x4F, 0xDC, 0x22, 0x2A, 0x90, 0x88, 0x46, 0xEE, 0xB8, 0x14, 0xDE, 0x5E, 0x0B, 0xDB,
            0xE0, 0x32, 0x3A, 0x0A, 0x49, 0x06, 0x24, 0x5C, 0xC2, 0xD3, 0xAC, 0x62, 0x91, 0x95, 0xE4, 0x79,
            0xE7, 0xC8, 0x37, 0x6D, 0x8D, 0xD5, 0x4E, 0xA9, 0x6C, 0x56, 0xF4, 0xEA, 0x65, 0x7A, 0xAE, 0x08,
            0xBA, 0x78, 0x25, 0x2E, 0x1C, 0xA6, 0xB4, 0xC6, 0xE8, 0xDD, 0x74, 0x1F, 0x4B, 0xBD, 0x8B, 0x8A,
            0x70, 0x3E, 0xB5, 0x66, 0x48, 0x03, 0xF6, 0x0E, 0x61, 0x35, 0x57, 0xB9, 0x86, 0xC1, 0x1D, 0x9E,
            0xE1, 0xF8, 0x98, 0x11, 0x69, 0xD9, 0x8E, 0x94, 0x9B, 0x1E, 0x87, 0xE9, 0xCE, 0x55, 0x28, 0xDF,
            0x8C, 0xA1, 0x89, 0x0D, 0xBF, 0xE6, 0x42, 0x68, 0x41, 0x99, 0x2D, 0x0F, 0xB0, 0x54, 0xBB, 0x16
    };
    // AES Inverse S-Box
    private static final int[] INV_S = {
            0x52, 0x09, 0x6A, 0xD5, 0x30, 0x36, 0xA5, 0x38, 0xBF, 0x40, 0xA3, 0x9E, 0x81, 0xF3, 0xD7, 0xFB,
            0x7C, 0xE3, 0x39, 0x82, 0x9B, 0x2F, 0xFF, 0x87, 0x34, 0x8E, 0x43, 0x44, 0xC4, 0xDE, 0xE9, 0xCB,
            0x54, 0x7B, 0x94, 0x32, 0xA6, 0xC2, 0x23, 0x3D, 0xEE, 0x4C, 0x95, 0x0B, 0x42, 0xFA, 0xC3, 0x4E,
            0x08, 0x2E, 0xA1, 0x66, 0x28, 0xD9, 0x24, 0xB2, 0x76, 0x5B, 0xA2, 0x49, 0x6D, 0x8B, 0xD1, 0x25,
            0x72, 0xF8, 0xF6, 0x64, 0x86, 0x68, 0x98, 0x16, 0xD4, 0xA4, 0x5C, 0xCC, 0x5D, 0x65, 0xB6, 0x92,
            0x6C, 0x70, 0x48, 0x50, 0xFD, 0xED, 0xB9, 0xDA, 0x5E, 0x15, 0x46, 0x57, 0xA7, 0x8D, 0x9D, 0x84,
            0x90, 0xD8, 0xAB, 0x00, 0x8C, 0xBC, 0xD3, 0x0A, 0xF7, 0xE4, 0x58, 0x05, 0xB8, 0xB3, 0x45, 0x06,
            0xD0, 0x2C, 0x1E, 0x8F, 0xCA, 0x3F, 0x0F, 0x02, 0xC1, 0xAF, 0xBD, 0x03, 0x01, 0x13, 0x8A, 0x6B,
            0x3A, 0x91, 0x11, 0x41, 0x4F, 0x67, 0xDC, 0xEA, 0x97, 0xF2, 0xCF, 0xCE, 0xF0, 0xB4, 0xE6, 0x73,
            0x96, 0xAC, 0x74, 0x22, 0xE7, 0xAD, 0x35, 0x85, 0xE2, 0xF9, 0x37, 0xE8, 0x1C, 0x75, 0xDF, 0x6E,
            0x47, 0xF1, 0x1A, 0x71, 0x1D, 0x29, 0xC5, 0x89, 0x6F, 0xB7, 0x62, 0x0E, 0xAA, 0x18, 0xBE, 0x1B,
            0xFC, 0x56, 0x3E, 0x4B, 0xC6, 0xD2, 0x79, 0x20, 0x9A, 0xDB, 0xC0, 0xFE, 0x78, 0xCD, 0x5A, 0xF4,
            0x1F, 0xDD, 0xA8, 0x33, 0x88, 0x07, 0xC7, 0x31, 0xB1, 0x12, 0x10, 0x59, 0x27, 0x80, 0xEC, 0x5F,
            0x60, 0x51, 0x7F, 0xA9, 0x19, 0xB5, 0x4A, 0x0D, 0x2D, 0xE5, 0x7A, 0x9F, 0x93, 0xC9, 0x9C, 0xEF,
            0xA0, 0xE0, 0x3B, 0x4D, 0xAE, 0x2A, 0xF5, 0xB0, 0xC8, 0xEB, 0xBB, 0x3C, 0x83, 0x53, 0x99, 0x61,
            0x17, 0x2B, 0x04, 0x7E, 0xBA, 0x77, 0xD6, 0x26, 0xE1, 0x69, 0x14, 0x63, 0x55, 0x21, 0x0C, 0x7D
    };
    // Default file to encrypt
    public static String fileToEncrypt = "plain.txt";
    private byte[] key;
    private int[] encryptKey = new int[44];
    private int[] decryptKey = new int[44];

    /**
     * Initializes a new AES object with a randomly generated key.
     */
    public AES() {
        this(genKey());
    }

    /**
     * Initializes a new AES object with a specified key.
     *
     * @param key The key as a byte array to initialize the AES object with.
     */
    public AES(byte[] key) {
        setKey(key);
        keyExpansion(key);
    }

    /**
     * Generates a random 128-bit key for AES encryption.
     *
     * @return A randomly generated key as a byte array.
     */
    public static byte @NotNull [] genKey() {
        byte[] key = new byte[16];
        try {
            SecureRandom.getInstanceStrong().nextBytes(key);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return key;
    }

    /**
     * Main method for testing AES encryption.
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: AES {-e(encrypt) -d(decrypt)} {message} {key}");
            System.out.println("Example: AES -e 12345678901234561234567890123456 12345678901234561234567890123456");
            System.out.println("Encrypted result\n5a4d39a0 a8fcf4f6 9ba284f0 89386e6a");
            return;
        }

        String mode = args[0];
        String messageHex = args[1];
        String keyHex = args[2];

        byte[] message = hexStringToByteArray(messageHex);
        byte[] key = hexStringToByteArray(keyHex);

        AES aes = new AES(key);

        if (mode.equals("-e")) {
            byte[] cipher = aes.encrypt(message);
            System.out.println("Encrypted result: ");
            printByteArrayInHex(cipher);
        } else if (mode.equals("-d")) {
            byte[] decrypted = aes.decrypt(message);
            System.out.println("Decrypted result: ");
            printByteArrayInHex(decrypted);
        } else {
            System.out.println("Invalid mode. Use -e for encryption or -d for decryption.");
        }
    }

    /**
     * Gets the current AES encryption key.
     *
     * @return The AES encryption key as a byte array.
     */
    public byte[] getKey() {
        return key;
    }

    /**
     * Sets the AES encryption key.
     *
     * @param key The key as a byte array.
     */
    public void setKey(byte @NotNull [] key) {
        if (key.length != 16) {
            throw new IllegalArgumentException("Key length should be 16");
        }
        this.key = Arrays.copyOf(key, key.length);
    }

    /**
     * Encrypts a given plaintext using AES encryption.
     *
     * @param plaintext The plaintext to encrypt.
     * @return The ciphertext as a byte array.
     */
    public byte[] encrypt(byte[] plaintext) {
        byte[][] state = dataToState(plaintext);
        addRoundKey(state, Arrays.copyOfRange(encryptKey, 0, 4));

        for (int round = 1; round <= 9; round++) {
            subBytes(state);
            shiftRows(state);
            mixColumns(state);
            addRoundKey(state, Arrays.copyOfRange(encryptKey, round * 4, round * 4 + 4));
        }

        subBytes(state);
        shiftRows(state);
        addRoundKey(state, Arrays.copyOfRange(encryptKey, 40, 44));
        return stateToData(state);
    }

    /**
     * Encrypts the given plaintext using AES encryption with the specified encryption key.
     *
     * @param plain The plaintext to be encrypted, provided as a byte array.
     * @param key   The encryption key used for the AES encryption, provided as a byte array.
     * @return The ciphertext as a byte array after AES encryption.
     */
    public static byte[] encrypt(byte[] plain, byte[] key) {
        AES aes = new AES(key);
        return aes.encrypt(plain);
    }

    /**
     * Decrypts a given ciphertext using AES decryption.
     *
     * @param cipher The ciphertext to decrypt.
     * @return The plaintext as a byte array.
     */
    public byte[] decrypt(byte[] cipher) {
        byte[][] state = dataToState(cipher);
        addRoundKey(state, Arrays.copyOfRange(decryptKey, 0, 4));

        for (int round = 1; round <= 9; round++) {
            invShiftRows(state);
            invSubBytes(state);
            addRoundKey(state, Arrays.copyOfRange(decryptKey, round * 4, round * 4 + 4));
            invMixColumns(state);
        }
        invShiftRows(state);
        invSubBytes(state);
        addRoundKey(state, Arrays.copyOfRange(decryptKey, 40, 44));
        return stateToData(state);
    }

    /**
     * Decrypts the given ciphertext using AES decryption with the specified decryption key.
     *
     * @param cipher The ciphertext to be decrypted, provided as a byte array.
     * @param key    The decryption key used for AES decryption, provided as a byte array.
     * @return The plaintext as a byte array after AES decryption.
     */
    public static byte[] decrypt(byte[] cipher, byte[] key) {
        AES aes = new AES(key);
        return aes.decrypt(cipher);
    }

    /**
     * Print the state matrix to the console for debugging purposes.
     *
     * @param state The state matrix to be printed, provided as a two-dimensional byte array.
     */
    private void printState(byte[][] state) {
        System.out.println(Arrays.deepToString(state));
    }

    /**
     * Convert a one-dimensional byte array into a 4x4 state matrix for AES encryption/decryption.
     *
     * @param in The input one-dimensional byte array to be transformed into a state matrix.
     * @return A 4x4 state matrix containing the data from the input byte array.
     */
    private byte[][] dataToState(byte[] in) {
        byte[][] state = new byte[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[j][i] = in[i * 4 + j];
            }
        }
        return state;
    }

    /**
     * Convert a 4x4 state matrix, typically from AES encryption/decryption, into a one-dimensional byte array.
     *
     * @param state The 4x4 state matrix to be transformed into a one-dimensional byte array.
     * @return A one-dimensional byte array containing the data from the state matrix.
     */
    @Contract(pure = true)
    private byte @NotNull [] stateToData(byte[][] state) {
        byte[] out = new byte[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                out[i * 4 + j] = state[j][i];
            }
        }
        return out;
    }

    private void subBytes(byte[][] state) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] = (byte) S[state[i][j] & 0xff];
            }
        }
    }

    private void invSubBytes(byte[][] state) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] = (byte) INV_S[state[i][j] & 0xff];
            }
        }
    }

    private void shiftRows(byte[][] state) {
        for (int i = 0; i < 4; i++) {
            int block = load4ByteToInt(state[i]);
            block = Integer.rotateLeft(block, 8 * i);
            state[i] = storeIntTo4Byte(block);
        }
    }

    private void invShiftRows(byte[][] state) {
        for (int i = 0; i < 4; i++) {
            int block = load4ByteToInt(state[i]);
            block = Integer.rotateRight(block, 8 * i);
            state[i] = storeIntTo4Byte(block);
        }
    }

    private void mixColumns(byte[][] state) {
        byte[][] tmp = new byte[4][4];
        byte[][] M = {
                {0x02, 0x03, 0x01, 0x01},
                {0x01, 0x02, 0x03, 0x01},
                {0x01, 0x01, 0x02, 0x03},
                {0x03, 0x01, 0x01, 0x02}
        };

        /* copy state[4][4] to tmp[4][4] */
        for (int i = 0; i < 4; i++) {
            System.arraycopy(state[i], 0, tmp[i], 0, 4);
        }

        Number.GMatrixMul(M, tmp, state);
    }

    private void invMixColumns(byte[][] state) {
        byte[][] tmp = new byte[4][4];
        byte[][] M = {
                {0x0E, 0x0B, 0x0D, 0x09},
                {0x09, 0x0E, 0x0B, 0x0D},
                {0x0D, 0x09, 0x0E, 0x0B},
                {0x0B, 0x0D, 0x09, 0x0E}
        };

        /* copy state[4][4] to tmp[4][4] */
        for (int i = 0; i < 4; i++) {
            System.arraycopy(state[i], 0, tmp[i], 0, 4);
        }

        Number.GMatrixMul(M, tmp, state);
    }

    private int gFunc(int w, int i) {
        return load4ByteToInt(S[byteAt(w, 2) & 0xff], S[byteAt(w, 1) & 0xff],
                S[byteAt(w, 0) & 0xff], S[byteAt(w, 3) & 0xff]) ^ RCON[i];
    }

    private void keyExpansion(byte[] key) {
        for (int i = 0; i < 4; i++) {
            encryptKey[i] = load4ByteToInt(key[i * 4], key[i * 4 + 1], key[i * 4 + 2], key[i * 4 + 3]);
        }

        for (int i = 0; i < 10; i++) {
            encryptKey[i * 4 + 4] = encryptKey[i * 4] ^ gFunc(encryptKey[i * 4 + 3], i);
            encryptKey[i * 4 + 5] = encryptKey[i * 4 + 1] ^ encryptKey[i * 4 + 4];
            encryptKey[i * 4 + 6] = encryptKey[i * 4 + 2] ^ encryptKey[i * 4 + 5];
            encryptKey[i * 4 + 7] = encryptKey[i * 4 + 3] ^ encryptKey[i * 4 + 6];
        }

        for (int i = 0; i < 11; i++) {
            System.arraycopy(encryptKey, (10 - i) * 4, decryptKey, i * 4, 4);
        }
    }

    private void addRoundKey(byte[][] state, int[] key) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] ^= byteAt(key[j], 3 - i);
            }
        }
    }

}