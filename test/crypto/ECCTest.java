package crypto;

import crypto.util.Number;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static java.math.BigInteger.TWO;
import static java.math.BigInteger.valueOf;
import static org.junit.jupiter.api.Assertions.*;

class ECCTest {

    @Test
    void plusTest1() {
        ECC ecc = new ECC(1, 1, 23);
        ECC.Point P = new ECC.Point(3, 10);
        ECC.Point Q = new ECC.Point(9, 7);
        ECC.Point plusResult = new ECC.Point(17, 20);

        assertEquals(plusResult, ecc.add(P, Q));
    }
    @Test
    void plusTest2() {
        BigInteger q = Number.getPrime(30);
        ECC ecc = new ECC(1, 3, q);
        ECC.Point G = ecc.getPoint();
        BigInteger d = Number.randomBigInteger(q);
        ECC.Point pk = ecc.multiply(G, d);
        ECC.Point msg = ecc.encode(2174);
        ECC.PointPair cipher = ecc.encrypt(msg, G, pk);
        ECC.Point dec = ecc.decrypt(cipher, d);

        assertEquals(msg, dec);
    }
    @Test
    void multiplyTest1() {
        ECC ecc = new ECC(1, 1, 23);
        ECC.Point P = new ECC.Point(3, 10);
        ECC.Point mulResult = new ECC.Point(7, 12);

        assertEquals(mulResult, ecc.multiply(P, 2));
    }
    @Test
    void multiplyTest2() {
        ECC ecc = new ECC(1, 1, 23);
        ECC.Point P = new ECC.Point(3, 10);
        ECC.Point mulResult = ecc.add(new ECC.Point(7, 12), P);

        assertEquals(mulResult, ecc.multiply(P, 3));
    }

    @Test
    void encryptTest() {
        ECC ecc = new ECC(-1, 188, 751);
        ECC.Point G = new ECC.Point(0, 376);
        ECC.Point publicKey = new ECC.Point(201, 5);
        ECC.Point messagePoint = new ECC.Point(562, 201);
        int r = 386;
        ECC.PointPair expect = new ECC.PointPair(new ECC.Point(676, 558), new ECC.Point(385,328));
        ECC.PointPair cipher = ecc.encrypt(messagePoint, G, publicKey, r);

        assertEquals(expect, cipher);
    }
    @Test
    void encryptTest2() {
        BigInteger p = Number.getPrime(30);
        ECC ecc = new ECC(1, 3, p);
        ECC.Point G = ecc.getPoint();
        BigInteger d = Number.randomBigInteger(p);
        ECC.Point pk = ecc.multiply(G, d);
        ECC.Point msg = ecc.encode(2174);
        ECC.PointPair cipher = ecc.encrypt(msg, G, pk);
        ECC.Point dec = ecc.decrypt(cipher, d);
        assertEquals(valueOf(2174), ecc.decode(dec));
        assertEquals(msg, dec);
    }
    @Test
    void decryptTest() {
        ECC ecc = new ECC(-1, 188, 751);
        ECC.Point messagePoint = new ECC.Point(562, 201);
        ECC.Point G = new ECC.Point(0, 376);
        int privateKey = 33;
        ECC.PointPair cipher = ecc.encrypt(messagePoint, G, privateKey);
        ECC.Point messageDecrypted = ecc.decrypt(cipher, privateKey);

        assertEquals(messagePoint, messageDecrypted);
    }
    @Test
    void getPointTest() {
        BigInteger p = Number.getPrime(20);
        ECC ecc = new ECC(1, 3, p);
        ECC.Point G = ecc.getPoint();

        assertEquals(G.y.modPow(TWO, p), ecc.calculateRightValue(G.x).mod(p));
    }
}