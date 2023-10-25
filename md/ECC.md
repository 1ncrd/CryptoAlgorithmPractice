# ECC 算法

`叶宇航 921127970158`

## 简介

椭圆曲线密码学（英语：Elliptic Curve Cryptography，缩写：ECC）是一种基于椭圆曲线数学的公开密钥加密算法。

ECC的主要优势是它相比RSA加密算法使用较小的密钥长度并提供相当等级的安全性。ECC的另一个优势是可以定义群之间的双线性映射，基于 Weil 对或是 Tate 对；双线性映射已经在密码学中发现了大量的应用，例如基于身份的加密。

本实验主要实现了

## 编程实现

采用 Java 语言实现，类实现 `crypto.ECC`

### 目录结构

```txt
├─src
│  └─crypto
│      │  ECC.java
│      └─util
│              Bytes.java
│              Number.java
└─test
    └─crypto
        │  ECCTest.java
        └─util
                BytesTest.java
                NumberTest.java
```

`src/crypto` 目录下为源代码，`src/crypto/util` 目录存放工具函数，`test/crypto` 目录下存放各类的测试用例

### 椭圆曲线上的点和点对

椭圆曲线计算基于曲线上的点，而加密结果中用到了点对，故编写了以下实现

```java
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
```

`Point` 公共静态类，用于表示椭圆曲线上的点：

1. `Point`类的静态常量`O`代表了一个特殊的点，即无穷远点，该点不具有特定的坐标值，其 `x` 和 `y` 均为 `null`。
2. 该类具有四个构造函数的重载：
    - `Point()`：初始化一个具有默认坐标 `(0, 0)` 的点。
    - `Point(BigInteger x, BigInteger y)`：用给定的 BigInteger 类型的 x 和 y 坐标值初始化一个点。
    - `Point(int x, int y)`：用给定的整型x和y坐标值初始化一个点，它会将整型转换为BigInteger。
    - `Point(Point p)`：使用给定点`p`的 x 和 y 坐标值初始化一个新的点。
3. `negate`方法用于返回当前点在椭圆曲线上的负值。
4. `toString`方法被覆盖以返回一个描述当前点x和y坐标的字符串。

```java
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
```

`PointPair` 类包含两个 `Point` 类型的公共实例变量：`first` 和 `second`，分别表示点对的第一个和第二个点。

### 点加与点乘运算

加法运算定义如下

令 $P=(x_1, y_1),Q=(x_2,y_2)$ 是 $E$ 上的点

- 若 $x_1=x_2$ 且 $y_1=-y_2$，则 $P+Q=O$；

- 否则 $P+Q=(x_3,y_3)$，其中

    - $$
        \left\{ \begin{aligned} x_3 &=\lambda^2-x_1-x_2 \\ y_3&=\lambda(x_1-x_3)-y_1 \end{aligned} \right.
        $$

    - $$
        \lambda=\left \{ \begin{aligned} & (y_2-y_1)(x_2-x_1)^{-1} &P \neq Q \\ & (3x_1^2+a)(2y_1)^{-1} &P = Q\end{aligned} \right.
        $$

- 对于所有的 $P\in E$，$P+O=O+P=P$。

- 对于 $P$ 的标量乘法定义为：$nP=\sum\limits_{i=1}^{n}P$。

点加实现

```java
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
```

点乘实现

```java
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
```

应用了类似快速幂的方法缩减算法复杂度为 $O(log(n))$

如 $155$ 的二进制形式为 $10011011_2$。即 $155=2^7+2^4+2^3+2^1+2^0$ 所以计算 $155P$ 时
$$
155P=(2^7+2^4+2^3+2^1+2^0)P=(1+2(1+2^2(1+2(1+2^3))))P=P+2(P+2^2(P+2(P+2^3P)))
$$
同理，计算 $155P$ 只需要计算 $2P=Q_1$, $2Q_1=Q_2$, $2Q_2=Q_3$ 记 $Q_3=2^3P$, next $P+Q_3=Q_4$, $2Q_4=Q_5$, $P+Q_5=Q_6$, $2Q_6=Q_7$, $2Q_7=Q_8$, $P+Q_8=Q_9$, $2Q_9=Q_{10}$, $P+Q_{10}=Q_{11}=155P$。总计只需 $10<154$ 次操作。

### 编码与解码

编解码方案：

假设非负消息数字 $m < p/1000 - 1$ ，编码时尝试 $x=1000m,1000m+1,...,1000m+999$，直到获得一个 $x$ 使得 $x^3+ax+b$ 在模 $p$ 有限域中存在平方根，从而计算 $y=\sqrt{x^3+ax+b}$，得到编码后的消息点 $(x,y)$。

```java
public Point encode(BigInteger message) {x
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
```

解码 $m=x/1000$

```java
public BigInteger decode(Point encodedMessage) {
    BigInteger x = encodedMessage.x;
    x = x.divide(valueOf(1000));
    return x;
}
```

> [Encoding a message as points on elliptic curve - Cryptography Stack Exchange](https://crypto.stackexchange.com/questions/103132/encoding-a-message-as-points-on-elliptic-curve)

### 加密与解密

选取生成元 $G$，曲线 $E$ 作为公开参数，选取整数 $d_A$ 作为私钥，产生公钥 $P_A=d_AG$。

加密：
$$
C_m=\{rG, P_m+rP_A\}
$$
解密：
$$
P_m+rP_A-d_A(rG)=P_m+r(d_AG)-d_A(rG)=P_m
$$

```java
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
```

编写了四个重载的 `encrypt`方法，用于对给定的消息点进行加密操作，返回值均是密文点对：

1. 第一个`encrypt`方法先生成一个随机数 `randomNumber`，然后利用基准点`G`和私钥 `privateKey` 计算出公钥 `publicKey`。接着，生成一个加密后的 `PointPair` 对象。
2. 第二个 `encrypt` 方法简化了对于整型私钥的调用，将整型私钥转换为`BigInteger` 类型，然后调用第一个 `encrypt` 方法。
3. 第三个 `encrypt` 方法接受一个消息点 `messagePoint`、一个基准点 `G` 和一个公钥 `publicKey`，生成一个随机数`randomNumber`，然后根据给定的参数生成加密后的 `PointPair` 对象。
4. 第四个 `encrypt` 方法简化了对于整型随机数的调用，接受一个消息点`messagePoint`、一个基准点`G`、一个公钥`publicKey`和一个整型随机数 `randomNumber`，然后生成加密后的 `PointPair` 对象。

```java
public Point decrypt(PointPair cipherPointPair, BigInteger privateKey) {
    return add(cipherPointPair.second, multiply(cipherPointPair.first, privateKey).negate());
}

public Point decrypt(PointPair cipherPointPair, int privateKey) {
    return decrypt(cipherPointPair, valueOf(privateKey));
}
```

两个重载的 `decrypt` 方法，用于解密密文点对，返回解密后的消息点。

### 随机取点方法 `getPoint`

随机取 $x$，使用 `sqrtPrimeField` 方法判断 $x^3+ax+b$ 是否存在对应的平方根即 $y$。

```java
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
```

### 有限域开方算法 `sqrtPrimeField`

```java
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
```

参考 [Tonelli–Shanks algorithm](https://en.wikipedia.org/wiki/Tonelli–Shanks_algorithm) 实现，对于 $n <p$，方法返回 $r$ 其中 $r^2=n\mod p$，若不存在平方根，则返回 `null`。

## 示例

> 假如另一用户 A 发送明文消息“2174”并加密传输给用户 B，用户 B 接收消息后要能解密为明文。试用 ECC 密码体制实现此功能。

```java
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
```

- 首先初始化一个 `ECC` 对象 `new ECC(1, 3, p);`，`p` 为 30 bit 长的质数，作为有限域的模数，`G` 随机取椭圆曲线上的一点，`d` 取范围 $(0, p)$ 随机整数作为私钥，计算 $dG$ 得到公钥。
- 随后调用 `ecc.encode(2174)` 编码 $2174$ 为椭圆曲线上的点。
- 执行加密解密后，调用 `assertEquals(valueOf(2174), ecc.decode(dec))` 验证编码消息在经过加密解密后，并解码后是否还原为 `2174`。

<img src="D:\Operator\Study\密码学实训\ECC.assets\image-20231020172437068.png" alt="image-20231020172437068" height=250 />
