package crypto;

import crypto.util.Bytes;

import java.math.BigInteger;

import static crypto.util.Number.*;
import static java.math.BigInteger.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class RSA {
    public RSAKey key;
    private MessageDigest hashFunc;
    public static class RSAKey {
        BigInteger p, q, n, e, d;
        public RSAKey(int nBit) {
            p = getPrime(nBit / 2);
            do {
                q = getPrime(nBit / 2);
            } while (p.equals(q));
            n = p.multiply(q);
            BigInteger phi_n = (p.subtract(ONE)).multiply(q.subtract(ONE));
            do {
                e = randomBigInteger(TWO, phi_n.subtract(ONE));
            } while (!e.gcd(phi_n).equals(ONE));
            d = inverse(e, phi_n);
        }

        @Override
        public String toString() {
            return "RSAKey{" +
                    "p=" + p +
                    ", q=" + q +
                    ", n=" + n +
                    ", e=" + e +
                    ", d=" + d +
                    '}';
        }
    }
    public static RSAKey genKey(int nBit) {
        return new RSAKey(nBit);
    }

    public RSA(int nBit) {
        key = genKey(nBit);
        try {
            hashFunc = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The function encrypts a BigInteger using the public key's exponent and modulus.
     * 
     * @param plain The plain parameter is a BigInteger representing the plaintext message that needs
     * to be encrypted.
     * @return the result of the plain number raised to the power of key.e, modulo key.n.
     */
    public BigInteger encrypt(BigInteger plain) {
        return plain.modPow(key.e, key.n);
    }

    /**
     * The function encrypts a byte array by converting it to a BigInteger, encrypting it, and then
     * converting it back to a byte array.
     * 
     * @param plain The parameter "plain" is a byte array that represents the plaintext data that needs
     * to be encrypted.
     * @return The method is returning a byte array.
     */
    public byte[] encrypt(byte[] plain) {
        return bigIntToBytes(encrypt(bytesToBigInt(plain)));
    }

    /**
     * The function decrypts a BigInteger cipher using the private key.
     * 
     * @param cipher The cipher is a BigInteger representing the encrypted message.
     * @return The method is returning the result of the cipher raised to the power of key.d, modulo
     * key.n.
     */
    public BigInteger decrypt(BigInteger cipher) {
        return cipher.modPow(key.d, key.n);
    }

   /**
    * The function decrypts a byte array by converting it to a BigInteger, performing decryption, and
    * then converting the result back to a byte array.
    * 
    * @param cipher a byte array that represents the encrypted data that needs to be decrypted.
    * @return The method is returning a byte array.
    */
    public byte[] decrypt(byte[] cipher) {
        return bigIntToBytes(decrypt(bytesToBigInt(cipher)));
    }

    /**
     * The function takes a string message as input, converts it to bytes, and returns the hash value
     * as a byte array.
     * 
     * @param message The parameter "message" is a string that represents the message that needs to be
     * hashed.
     * @return The method is returning a byte array.
     */
    private byte[] hash(String message) {
        return hash(message.getBytes());
    }

    /**
     * The function takes a byte array as input and returns the hash value of the input using a
     * specified hash function.
     * 
     * @param message a byte array that represents the input message that
     * needs to be hashed.
     * @return The method is returning a byte array.
     */
    private byte[] hash(byte[] message) {
        return hashFunc.digest(message);
    }

    /**
     * The sign function takes a message as input, converts it to bytes, and returns the signed
     * message.
     * 
     * @param message a string that represents the message that needs to be
     * signed.
     * @return signed message String.
     */
    public String sign(String message) {
        return sign(message.getBytes());
    }

    public String sign(byte[] message) {
        return Bytes.byteArrayToHexString(encrypt(hash(message)));
    }

    /**
     * The function verifies if a given message and its signature match by decrypting the signature and
     * comparing it with the hash of the message.
     * 
     * @param message a string that represents the message that needs to be
     * verified. It could be any text or data that was signed using a cryptographic algorithm.
     * @param signature a string representing the digital signature of message.
     * @return a Boolean value indicating whether the verification was successful.
     */
    public boolean verify(String message, String signature) {
        byte[] decryptSign = decrypt(Bytes.hexStringToByteArray(signature));
        byte[] messageHash = hash(message);
        return (Arrays.equals(messageHash, decryptSign));
    }
}
