package crypto.util;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static java.math.BigInteger.*;

public class Number {
    public static final String BAD_RANGE = "bound must be greater than origin";
    /* only use for method exgcd */
    private static BigInteger x = BigInteger.valueOf(0);
    private static BigInteger y = BigInteger.valueOf(0);

    private static int intX = 0;
    private static int intY = 0;

    /* see https://abcdxyzk.github.io/blog/2018/04/16/isal-erase-3/ */
    public static byte GMul(byte u, byte v) {
        byte p = 0;
        for (int i = 0; i < 8; i++) {
            if ((u & 0x01) != 0) {
                p ^= v;
            }

            int flag = (v & 0x80);
            v <<= 1;
            if (flag != 0) {
                v ^= 0x1B; /* x^8 + x^4 + x^3 + x + 1  1 0001 1011 */
            }

            u >>= 1;
        }

        return p;
    }

    /**
     * in place
     */
    public static void GMatrixMul(byte[][] a, byte[][] b, byte[][] r) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                r[i][j] = (byte) (GMul(a[i][0], b[0][j]) ^ GMul(a[i][1], b[1][j]) ^ GMul(a[i][2], b[2][j]) ^ GMul(a[i][3], b[3][j]));
            }
        }
    }

    public static void checkRange(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException(BAD_RANGE);
        }
    }

    public static void checkRange(@NotNull BigInteger bound) {
        if (bound.compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException(BAD_RANGE);
        }
    }

    public static void checkRange(BigInteger origin, BigInteger bound) {
        if (origin.compareTo(bound) > 0) {
            throw new IllegalArgumentException(BAD_RANGE);
        }
    }

    /**
     * Generate a random BigInteger from 2**(nBit-1) to 2**nBit-1
     *
     * @param nBit
     * @return a random BigInteger from 2**(nBit-1) to 2**nBit-1
     */
    public static @NotNull BigInteger randomBigInteger(int nBit) {
        checkRange(nBit);
        BigInteger randomNumber = (new BigInteger(nBit - 1, new Random())).or(ONE.shiftLeft(nBit - 1));

        return randomNumber;
    }

    /**
     * bound included
     */
    @NotNull
    public static BigInteger randomBigInteger(BigInteger bound) {
        checkRange(bound);
        BigInteger randomNumber;
        do {
            randomNumber = new BigInteger(bound.bitLength(), new Random());
        } while (randomNumber.compareTo(bound) > 0);

        return randomNumber;
    }

    /**
     * both included
     */
    @NotNull
    public static BigInteger randomBigInteger(BigInteger origin, BigInteger bound) {
        checkRange(origin, bound);
        return origin.add(randomBigInteger(bound.subtract(origin)));
    }

    /**
     * Use Miller-rabin
     */
    public static boolean isPrime(BigInteger n) {
        return isPrime(n, 1e-6);
    }

    public static boolean isPrime(@NotNull BigInteger n, double false_positive_prob) {
        if (n.compareTo(TWO) <= 0 || n.and(ONE).equals(ZERO)) {
            return n.equals(TWO);
        }
        int rounds = (int) Math.ceil(-Math.log(false_positive_prob) / Math.log(4));

        return MillerRabinTest(n, rounds);
    }

    /**
     * @param n an odd integer to be tested for primality
     * @param k the number of rounds of testing to perform
     * @return false if n is found to be composite, true otherwise
     * @see <a href="https://en.wikipedia.org/wiki/Miller%E2%80%93Rabin_primality_test">wiki</a>
     */
    public static boolean MillerRabinTest(@NotNull BigInteger n, int k) {
        /* factoring out powers of 2 from n − 1 */
        /* 2^s * d = n - 1 */
        BigInteger d = n.subtract(ONE).divide(TWO);
        BigInteger s = ONE;
        while (d.and(ONE).equals(ZERO)) {
            s = s.add(ONE);
            d = d.shiftRight(1);
        }
        for (int i = 0; i < k; i++) {
            BigInteger a = randomBigInteger(TWO, n);
            BigInteger x = a.modPow(d, n);
            BigInteger y = x.modPow(TWO, n);
            for (int j = 0; j < s.intValue(); j++) {
                y = x.modPow(TWO, n);
                if (y.equals(ONE) && !x.equals(ONE) && !x.equals(n.subtract(ONE))) {
                    return false;
                }
                x = y;
            }
            if (!y.equals(ONE)) {
                return false;
            }
        }
        return true;
    }
    @NotNull
    public static BigInteger exgcd(BigInteger a, BigInteger b) {
        if (b.compareTo(ZERO) < 0) {
            a = a.negate();
            b = b.negate();
        }
        return  _exgcd(a, b);
    }
    @NotNull
    private static BigInteger _exgcd(BigInteger a, BigInteger b) {
        if (b.equals(ZERO)) {
            x = ONE;
            y = ZERO;
            return a;
        }
        BigInteger ans = _exgcd(b, a.mod(b));
        BigInteger t = x;
        x = y;
        y = t.subtract(a.divide(b).multiply(y));
        return ans;
    }

    /**
     * Returns an BigInteger whose value is (a^(-1) mod b).
     *
     * @return a^(-1) mod b
     */
    @NotNull
    public static BigInteger inverse(BigInteger a, BigInteger b) {
        if (b.signum() != 1) {
            throw new IllegalArgumentException("b must be positive");
        }
        BigInteger gcd = exgcd(a, b);
        if (!gcd.equals(ONE)) {
            throw new IllegalArgumentException("a, b must be coprime (gcd(a, b) = 1)");
        }

        return x.mod(b);
    }
    public static int exgcd(int a, int b) {
        if (b < 0) {
            a = -a;
            b = -b;
        }
        return  _exgcd(a, b);
    }
    private static int _exgcd(int a, int b) {
        if (b == 0) {
            intX = 1;
            intY = 0;
            return a;
        }
        int ans = _exgcd(b, Math.floorMod(a, b));
        int t = intX;
        intX = intY;
        intY = t - (a / b * intY);
        return ans;
    }

    /**
     * Returns an int whose value is (a^(-1) mod b).
     *
     * @return a^(-1) mod b
     */
    public static int inverse(int a, int b) {
        if (b <= 0) {
            throw new IllegalArgumentException("b must be positive");
        }
        int gcd = exgcd(a, b);
        if (gcd != 1) {
            throw new IllegalArgumentException("a, b must be coprime (gcd(a, b) = 1)");
        }

        return Math.floorMod(intX, b);
    }

    /**
     * @param nBit the bit length of the prime return
     * @return a n-bits prime
     */
    public static BigInteger getPrime(int nBit) {
        BigInteger x;
        do {
            x = randomBigInteger(nBit).or(ONE);
        } while (x.compareTo(TWO) <= 0 || !isPrime(x));

        return x;
    }

    public static BigInteger bytesToBigInt(byte[] data) {
        return new BigInteger(data);
    }

    public static byte[] bigIntToBytes(BigInteger data) {
        return data.toByteArray();
    }

    public static int randomInt(int bound) {
        return ThreadLocalRandom.current().nextInt(0, bound);
    }

    public static Boolean isResidue(BigInteger a, BigInteger p) {
        a = a.mod(p);
        BigInteger exp = p.subtract(BigInteger.ONE).divide(BigInteger.valueOf(2));
        return a.modPow(exp, p).compareTo(BigInteger.ONE) == 0;
    }
    public static BigInteger sqrtPrimeField(BigInteger n, BigInteger p) {
        BigInteger s = ZERO;
        BigInteger q = p.subtract(ONE);
        n = n.mod(p);
        while (!q.testBit(0)) {
            q = q.shiftRight(1);
            s = s.add(ONE);
        }

        if (s.compareTo(ONE) == 0) {
            BigInteger exp = p.add(ONE).divide(valueOf(4));
            BigInteger r = n.modPow(exp, p);
            if (r.pow(2).mod(p).compareTo(n) == 0) return r;
            else return null;
        }

        BigInteger z = ONE;
        do {
            z = z.add(ONE);
        } while (isResidue(z, p));

        // c = z^q mod p
        BigInteger c = z.modPow(q, p);
        // r = n^( (q+1)/2 ) mod p
        BigInteger expR = q.add(ONE).divide(valueOf(2));
        BigInteger r = n.modPow(expR, p);
        // t = n^q mod p
        BigInteger t = n.modPow(q, p);
        // m = s
        BigInteger m = s;

        while (t.compareTo(ONE) != 0) {
            BigInteger tt = t;
            BigInteger i = ZERO;
            while (tt.compareTo(ONE) != 0) {
                tt = tt.multiply(tt).mod(p);
                i = i.add(ONE);
                if (i.compareTo(m) == 0) return null;
            }
            // b = c^( 2^(M-i-1) ) mod p
            BigInteger exp2 = m.subtract(i).subtract(ONE);
            BigInteger expB = valueOf(2).modPow(exp2, p.subtract(ONE));
            BigInteger b = c.modPow(expB, p);
            // bb = b^2 mod p
            BigInteger bb = b.pow(2).mod(p);
            // r = r*b mod p
            r = r.multiply(b).mod(p);
            // t = t*bb mod p
            t = t.multiply(bb).mod(p);
            c = bb;
            m = i;
        }
        if (r.pow(2).mod(p).compareTo(n) == 0) return r;
        return null;
    }
}
