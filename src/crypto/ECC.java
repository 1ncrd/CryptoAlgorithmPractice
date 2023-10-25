package crypto;

import crypto.util.Number;

import java.math.BigInteger;
import java.util.Objects;

import static crypto.util.Number.*;
import static java.math.BigInteger.*;

public class ECC {
    public static class Point {
        static final Point O = new Point(null, null);
        public BigInteger x, y;
        public Point() {
            this(0, 0);
        }

        public Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }

        public Point(int x, int y) {
            this(valueOf(x), valueOf(y));
        }

        public Point(Point p) {
            this(p.x, p.y);
        }

        /**
         * negate a point on elliptic curve
         * @return a new Point that equals -this on elliptic curve;
         */
        public Point negate() {
            return new Point(this.x, this.y.negate());
        }

        @Override
        public String toString() {
            return "Point{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return Objects.equals(x, point.x) && Objects.equals(y, point.y);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    public static class PointPair {
        public Point first;
        public Point second;
        public PointPair(Point first, Point second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public String toString() {
            return "PointPair{" +
                    "first=" + first +
                    ", second=" + second +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PointPair pointPair = (PointPair) o;
            return Objects.equals(first, pointPair.first) && Objects.equals(second, pointPair.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }

    private BigInteger a, b, p;

    public ECC(BigInteger a, BigInteger b, BigInteger p) {
        this.a = a;
        this.b = b;
        this.p = p;
    }

    public ECC(int a, int b, int p) {
        this(valueOf(a), valueOf(b), valueOf(p));
    }
    public ECC(int a, int b, BigInteger p) {
        this(valueOf(a), valueOf(b), p);
    }

    public BigInteger calculateRightValue(BigInteger x) {
        return x.pow(3).add(a.multiply(x)).add(b);
    }

    public Point getPoint() {
        BigInteger x;
        BigInteger y;
        do {
            x = randomBigInteger(p);
            BigInteger rValue = calculateRightValue(x).mod(p);
            y = sqrtPrimeField(rValue, p);
        } while (y == null);
        return new Point(x, y);
    }

    public Point add(Point p1, Point p2) {
        /* if p1 = -p2 */
        if (Objects.equals(p1.x, p2.x) && Objects.equals(p1.y, p1.x.negate())) {
            return Point.O;
        }

        /* P + O = P */
        if (p1.equals(Point.O)) {
            return p2;
        } else if (p2.equals(Point.O)) {
            return p1;
        }

        BigInteger s;
        if (!p1.equals(p2)) {
            s = p2.y.subtract(p1.y).multiply(inverse(p2.x.subtract(p1.x), p)).mod(p);
        } else {
            s = (p1.x.pow(2).multiply(valueOf(3)).add(a)).multiply(inverse(p1.y.multiply(TWO), p)).mod(p);
        }

        Point R = new Point();
        R.x = (s.pow(2).subtract(p1.x).subtract(p2.x)).mod(p);
        R.y = (s.multiply(p1.x.subtract(R.x)).subtract(p1.y)).mod(p);
        return R;
    }

    public Point multiply(Point p1, BigInteger m) {
        Point r = Point.O;
        Point t = p1;
        if (m.signum() == -1) {
            r = r.negate();
            m = m.negate();
        }
        while (m.signum() == 1) {
            if ((m.and(ONE)).equals(ONE)) {
               r = add(r, t);
            }
            t = add(t, t);
            m = m.shiftRight(1);
        }
        return r;
    }

    public Point multiply(Point p1, int m) {
        return multiply(p1, valueOf(m));
    }

    public Point encode(BigInteger message) {
        if (message.compareTo(p.divide(valueOf(1000)).subtract(ONE)) >= 0) {
            throw new RuntimeException("Can not encode message because m is greater than p / 1000 - 1");
        }
        BigInteger m = message;
        BigInteger x = null;
        BigInteger y = null;
        for (int i = 0; i < 1000; i++) {
            x = m.multiply(valueOf(1000)).add(valueOf(i));
            BigInteger rValue = calculateRightValue(x).mod(p);
            y = sqrtPrimeField(rValue, p);
            if (y != null) {
                break;
            }
        }
        return new Point(x, y);
    }

    public Point encode(int message) {
        return encode(valueOf(message));
    }

    public BigInteger decode(Point encodedMessage) {
        BigInteger x = encodedMessage.x;
        x = x.divide(valueOf(1000));
        return x;
    }

    public PointPair encrypt(Point messagePoint, Point G, BigInteger privateKey) {
        BigInteger randomNumber = randomBigInteger(p);
        Point publicKey = multiply(G, privateKey);
        return new PointPair(multiply(G, randomNumber), add(messagePoint, multiply(publicKey, randomNumber)));
    }

    public PointPair encrypt(Point messagePoint, Point G, int privateKey) {
        return encrypt(messagePoint, G, valueOf(privateKey));
    }

    public PointPair encrypt(Point messagePoint, Point G, Point publicKey) {
        BigInteger randomNumber = Number.randomBigInteger(p);
        return new PointPair(multiply(G, randomNumber), add(messagePoint, multiply(publicKey, randomNumber)));
    }

    public PointPair encrypt(Point messagePoint, Point G, Point publicKey, int randomNumber) {
        return new PointPair(multiply(G, randomNumber), add(messagePoint, multiply(publicKey, randomNumber)));
    }

    public Point decrypt(PointPair cipherPointPair, BigInteger privateKey) {
        return add(cipherPointPair.second, multiply(cipherPointPair.first, privateKey).negate());
    }

    public Point decrypt(PointPair cipherPointPair, int privateKey) {
        return decrypt(cipherPointPair, valueOf(privateKey));
    }
}
