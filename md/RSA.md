# RSA 算法

`叶宇航 921127970158`

## 简介

RSA（Rivest-Shamir-Adleman）是一种非对称加密算法，用于加密和解密数字信息。它是第一个既能用于加密又能用于数字签名的算法，并且目前被广泛应用于安全通信领域。RSA加密过程如下：

1. **密钥生成**：选择两个大素数 $p$ 和 $q$，计算它们的乘积 $n$，然后选择与 $\phi(n)=(p - 1)(q - 1)$ 互质的整数 $e$ （常取 $2^{16}=65,535$）作为公钥指数，再找到与 $e$ 乘积模 $(p-1)(q-1)$ 等于 $1$ 的整数 $d$ 作为私钥指数，即 $ed\equiv1 \mod \phi(n)$。
2. **加密**：将明文转换为数字，使用公钥进行加密。加密过程是将明文 m 的 e 次方与 n 取模，得到密文 c，即 $c\equiv m^e \mod n$。
3. **解密**：使用私钥进行解密。解密过程是将密文 c 的 d 次方与 n 取模，得到原始信息 m，即 $m\equiv c^d \mod n$。

RSA加密的特点包括：

1. **安全性高**：RSA 算法的安全性基于大整数的质因数分解难题，即将一个大整数分解成其质因数的乘积是一项极其困难的数学问题。
2. **非对称性**：RSA 采用公钥加密，私钥解密的方式，公钥可以公开，而私钥必须保密，这种非对称性保证了通信安全。
3. **数字签名**：除了加密外，RSA 还可用于数字签名。发送方可以使用自己的私钥对信息进行签名，接收方可以使用发送方的公钥验证签名的真实性。
4. **速度较慢**：相对于对称加密算法，RSA 的加解密速度较慢，尤其是对大数据的处理速度较慢。
5. **密钥管理复杂**：RSA 算法的密钥管理比较复杂，特别是对密钥的生成、存储、分发和更新需要严格的管理。

尽管 RSA 算法具有较高的安全性，但由于其运算速度较慢和密钥管理复杂等缺点，实际应用中常常结合对称加密算法，如 AES，以达到更高的安全性和更高的效率。

## 流程图

<img src="https://media.geeksforgeeks.org/wp-content/uploads/20200518124317/RSA3.png" alt="Difference between RSA algorithm and DSA - GeeksforGeeks" style="zoom:80%;" />

## 编程实现

采用 Java 语言实现，类实现 `crypto.RSA`

### 目录结构

```txt
├─src
│  └─crypto
│      │  RSA.java
│      └─util
│              Bytes.java
│              Number.java
└─test
    └─crypto
        │  RSATest.java
        └─util
                BytesTest.java
                NumberTest.java
```

`src/crypto` 目录下为源代码，`src/crypto/util` 目录存放工具函数，`test/crypto` 目录下存放各类的测试用例

### 大整数素性的判定算法

**米勒-拉宾素性检验**（英语：Miller–Rabin primality test）是一种素数判定法则質數判定法則)，利用随机化算法随机化算法)判断一个数是合数还是*可能是*素数。

#### 伪代码

```pseudocode
Input #1: n > 2, an odd integer to be tested for primality
Input #2: k, the number of rounds of testing to perform
Output: “composite” if n is found to be composite, “probably prime” otherwise

let s > 0 and d odd > 0 such that n − 1 = 2sd  # by factoring out powers of 2 from n − 1
repeat k times:
    a ← random(2, n − 2)  # n is always a probable prime to base 1 and n − 1
    x ← ad mod n
    repeat s times:
        y ← x2 mod n
        if y = 1 and x ≠ 1 and x ≠ n − 1 then # nontrivial square root of 1 modulo n
            return “composite”
        x ← y
    if y ≠ 1 then
        return “composite”
return “probably prime”
```

#### java 实现

```java
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
```

`isPrime` 函数用于检测一个大整数是否为素数，如果传入的整数小于等于 2 或者是偶数，直接检查并返回结果。接着根据给定的误报概率 （默认为 `1e-6`）计算所需的 Miller-Rabin 测试轮数。最后调用 Miller-Rabin 测试方法进行素性检测并返回结果。

`MillerRabinTest` 方法实现了 Miller-Rabin 素性测试。如果经过所有轮数的迭代后都没有发现 $n$ 是合数，那么 $n$ 可能是一个素数。

### 扩展 Euclid 算法求乘法逆元

用于计算两个整数的最大公约数以及一对整数系数 x 和 y ，使得这两个整数满足贝祖等式：$ax + by = gcd(a, b)$。

```java
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
```

`exgcd` 方法用于计算两个大整数 $a$ 和 $b$ 的最大公约数。通过调用 `_exgcd`  方法实现了扩展欧几里德算法的递归计算。这个算法能够返回两个整数的最大公约数，同时也计算出了满足贝祖等式的整数系数 $x$ 和 $y$。

`inverse` 方法用于计算给定整数 $a$ 在模 $b$下的乘法逆元。该方法首先调用 `exgcd` 方法来计算 $a$ 和 $b$ 的最大公约数，并检查是否为互质数。然后使用计算得到的 $x$ 值进行模运算，以获得 $a$ 在模 $b$ 下的乘法逆元。

另外，代码中还包含了针对整数类型的重载方法，用于处理 `int` 类型的输入。

### 公钥、私钥的生成算法及编程实现

首先实现随机大质数生成算法

```java
public static BigInteger getPrime(int nBit) {
    BigInteger x;
    do {
        x = randomBigInteger(nBit).or(ONE);
    } while (x.compareTo(TWO) <= 0 || !isPrime(x));
    return x;
}
```

方法生成一个拥有指定比特长度（nBit）的素数。

1. 首先，声明一个 `BigInteger` 变量 `x`，用于存储生成的候选素数。
2. 进入一个循环，不断生成随机的 `BigInteger`，其位长度为 nBit。使用 `or(ONE)` 确保生成的数是奇数。
3. 在循环中，检查生成的候选素数 x 是否小于等于 2 或者不是素数，使用上文所述的 `isPrime` 函数进行素性判断。如果 x 满足这两个条件之一，就继续生成新的候选素数。
4. 一旦生成了满足条件的素数 x，循环结束，然后返回这个素数。

### 密钥类 `RSAKey` 实现

```java
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
}
```

这段代码用于生成 RSA 密钥。在代码中，定义了一个名为`RSAKey` 的公共静态类，包含了大整数 `p`、`q`、`n`、`e` 和 `d`，分别表示RSA算法中的两个素数、模数、公钥和私钥。

在构造函数 `RSAKey` 中，首先通过 `getPrime` 函数生成了两个长度为 `nBit / 2` 的大素数 `p` 和 `q`。然后利用这两个素数计算得到了模数 `n`，并计算了欧拉函数值 `phi_n`。

接着，代码在循环中使用 `randomBigInteger` 函数随机生成一个大整数 `e`，并检查 `e` 与 `phi_n` 的最大公约数是否为1，确保 `e` 与 `phi_n` 互素。最后，通过调用 `inverse` 函数计算了私钥 `d`，使得 `d` 是 `e` 关于模 `phi_n` 的乘法逆元。

### 编码算法

将字节数据转换为大整数的方法，根据二进制信息将其编码为大整数。

下表说明了此过程。

| string | abc                        |
| ------ | -------------------------- |
| byte   | 01100001 01100010 01100011 |
| number | 6382179                    |

```java
public static BigInteger bytesToBigInt(byte[] data) {
    return new BigInteger(data);
}

private static int[] stripLeadingZeroBytes(byte[] a, int off, int len) {
    int indexBound = off + len;
    int keep;

    // Find first nonzero byte
    for (keep = off; keep < indexBound && a[keep] == 0; keep++)
        ;

    // Allocate new array and copy relevant part of input array
    int intLength = ((indexBound - keep) + 3) >>> 2;
    int[] result = new int[intLength];
    int b = indexBound - 1;
    for (int i = intLength-1; i >= 0; i--) {
        result[i] = a[b--] & 0xff;
        int bytesRemaining = b - keep + 1;
        int bytesToTransfer = Math.min(3, bytesRemaining);
        for (int j=8; j <= (bytesToTransfer << 3); j += 8)
            result[i] |= ((a[b--] & 0xff) << j);
    }
    return result;
}
```

###  加、解密算法

RSA 加解密的核心为模幂运算，即
$$
c&\equiv& m^e &\mod n \\
m&\equiv& c^d &\mod n
$$

因此加解密算法只需要实现 `modPow` 运算即可

```java
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
```

### 数字签名

S: Signature, H: Hash Function
$$
S=H(x)^d\mod n
$$
验证签名
$$
H(x) = S^e \mod n
$$
 若上式成立，则签名验证通过，否则不通过。

```java
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
```

## 测试

JUnit 测试程序

```java
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
```

测试通过

![image-20231021190343879](D:\Operator\Study\密码学实训\RSA.assets\image-20231021190343879.png)
