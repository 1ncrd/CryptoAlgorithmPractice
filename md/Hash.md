

# Hash 算法

`叶宇航 921127970158`

本报告包含了 MD5 和 SHA-512 的程序实现。

#### 目录结构

```
├─src
│  └─crypto
│      │  MD5.java
|      |  SHA512.java
│      └─util
│              Bytes.java
│              Number.java
└─test
    └─crypto
        │  MD5Test.java
        │  SHA512Test.java
        └─util
                BytesTest.java
                NumberTest.java
```

`src/crypto` 目录下为源代码，`src/crypto/util` 目录存放工具函数，`test/crypto` 目录下存放各类的测试用例

## MD5

**MD5消息摘要算法**（英语：MD5 Message-Digest Algorithm），一种被广泛使用的密码散列函数，可以产生出一个128位（16个字符(BYTES)）的散列值（hash value），用于确保信息传输完整一致。

### 流程图

<img src="D:\Operator\Study\密码学实训\Hash.assets\md5-no-2fix-02-scaled.webp" alt="md5-no-2fix-02-scaled" height=700 />

### 编程实现

采用 java 语言实现，类实现 `crypto.MD5`

#### 明文填充 Padding

##### Step 1 附加填充位

填充消息使其长度（以位为单位）对 512 取模等于 448 。也就是说，扩展消息以使其长度正好比 n * 512 位少 64 位。 始终执行填充操作，即使消息的长度已经对 512 取模等于 448。

填充的操作如下：将单个“1”比特追加到消息末尾，然后追加“0”比特，使填充后的消息的位长度对 512 取模等于 448。总共至少追加一个比特，最多追加 512 比特。

##### Step 2 附加长度信息

将 64 位的 $length$（在添加填充位之前的消息长度）追加到上一步的结果中。如果 $length$ 大于 $2^{64}$，则仅使用 $length$ 的低 64 位。（这些位按照先前的约定作为两个32位字追加，先追加低序字。）

此时，结果消息（在添加位填充和 $length$ 后）的长度是 512 位的整数倍。同样，该消息的长度是 16 个 32 位字的整数倍。设 M[0 ... N-1] 表示结果消息的字，其中 N 是 16 的倍数。

![File:Hash padding.png - Wikipedia](data:image/png;base64,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)

in java

```java
public static byte[] pad(byte[] message) {
    int numBlocks = (message.length + 8) / 64 + 1;
    int paddingLength = numBlocks * 64;
    byte[] paddingMessage = new byte[paddingLength];
    System.arraycopy(message, 0, paddingMessage, 0, message.length);
    paddingMessage[message.length] = (byte) 0x80;
    long messageBitLen = (long) message.length * 8;
    for (int i = 0; i < 8; i++) {
        paddingMessage[paddingMessage.length - 8 + i] = (byte) messageBitLen;
        messageBitLen >>>= 8;
    }

    return paddingMessage;
}
```

#### 初始化消息摘要缓冲区

> 3.3 Step 3. Initialize MD Buffer
>
> A four-word buffer (A,B,C,D) is used to compute the message digest. Here each of A, B, C, D is a 32-bit register. These registers are initialized to the following values in hexadecimal, ***low-order bytes first***):

```txt
word A: 01 23 45 67
word B: 89 ab cd ef
word C: fe dc ba 98
word D: 76 54 32 10
```

以 word A 为例，由于标准给出的字节是按照低字节到高字节的顺序，故在 Java `int`（默认大端序）中，其顺序为

```txt
int A: 67 45 23 01
       ^        ^
       high     low
```

in java:

```java
private static final int INIT_A = 0x67452301;
private static final int INIT_B = (int) 0xEFCDAB89L;
private static final int INIT_C = (int) 0x98BADCFEL;
private static final int INIT_D = 0x10325476;
private static final int[] SHIFT = { 7, 12, 17, 22, 5, 9, 14, 20, 4, 11, 16, 23, 6, 10, 15, 21 };
private static final int[] T = new int[64];
```

#### 定义四个辅助函数

```pseudocode
F(X,Y,Z) = XY v not(X) Z
G(X,Y,Z) = XZ v Y not(Z)
H(X,Y,Z) = X xor Y xor Z
I(X,Y,Z) = Y xor (X v not(Z))
```

in java:

```java
switch (div16) {
    case 0:
        f = (b & c) | (~b & d);
        break;
    case 1:
        f = (b & d) | (c & ~d);
        bufferIndex = (bufferIndex * 5 + 1) & 0x0F;
        break;
    case 2:
        f = b ^ c ^ d;
        bufferIndex = (bufferIndex * 3 + 5) & 0x0F;
        break;
    case 3:
        f = c ^ (b | ~d);
        bufferIndex = (bufferIndex * 7) & 0x0F;
        break;
}
```

#### 构造表格 T

根据 $sin$ 函数构造一个长度为 64 的表格，其中
$$
t[i]=4294967296 * abs(sin(i))
$$

$$
T[i] = Integer\ part\ of\ t[i]
$$



$T[i]$ 表示表中第 i 个元素，且 i 为弧度制。

in java:

```java
private static final int[] T = new int[64];

static {
    for (int i = 0; i < 64; i++) {
        T[i] = (int) (long) ((1L << 32) * Math.abs(Math.sin(i + 1)));
    }
}
```

#### 主要处理逻辑

```pseudocode
For i = 0 to N/16-1 do
  /* Copy block i into X. */
  For j = 0 to 15 do
    Set X[j] to M[i*16+j].
  end /* of loop on j */
  /* Save A as AA, B as BB, C as CC, and D as DD. */
  AA = A
  BB = B
  CC = C
  DD = D
  /* Round 1. */
  /* Let [abcd k s i] denote the operation
       a = b + ((a + F(b,c,d) + X[k] + T[i]) <<< s). */
  /* Do the following 16 operations. */
  [ABCD  0  7  1]  [DABC  1 12  2]  [CDAB  2 17  3]  [BCDA  3 22  4]
  [ABCD  4  7  5]  [DABC  5 12  6]  [CDAB  6 17  7]  [BCDA  7 22  8]
  [ABCD  8  7  9]  [DABC  9 12 10]  [CDAB 10 17 11]  [BCDA 11 22 12]
  [ABCD 12  7 13]  [DABC 13 12 14]  [CDAB 14 17 15]  [BCDA 15 22 16]
  /* Round 2. */
  /* Let [abcd k s i] denote the operation
       a = b + ((a + G(b,c,d) + X[k] + T[i]) <<< s). */
  /* Do the following 16 operations. */
  [ABCD  1  5 17]  [DABC  6  9 18]  [CDAB 11 14 19]  [BCDA  0 20 20]
  [ABCD  5  5 21]  [DABC 10  9 22]  [CDAB 15 14 23]  [BCDA  4 20 24]
  [ABCD  9  5 25]  [DABC 14  9 26]  [CDAB  3 14 27]  [BCDA  8 20 28]
  [ABCD 13  5 29]  [DABC  2  9 30]  [CDAB  7 14 31]  [BCDA 12 20 32]
  /* Round 3. */
  /* Let [abcd k s t] denote the operation
       a = b + ((a + H(b,c,d) + X[k] + T[i]) <<< s). */
  /* Do the following 16 operations. */
  [ABCD  5  4 33]  [DABC  8 11 34]  [CDAB 11 16 35]  [BCDA 14 23 36]
  [ABCD  1  4 37]  [DABC  4 11 38]  [CDAB  7 16 39]  [BCDA 10 23 40]
  [ABCD 13  4 41]  [DABC  0 11 42]  [CDAB  3 16 43]  [BCDA  6 23 44]
  [ABCD  9  4 45]  [DABC 12 11 46]  [CDAB 15 16 47]  [BCDA  2 23 48]
  /* Round 4. */
  /* Let [abcd k s t] denote the operation
       a = b + ((a + I(b,c,d) + X[k] + T[i]) <<< s). */
  /* Do the following 16 operations. */
  [ABCD  0  6 49]  [DABC  7 10 50]  [CDAB 14 15 51]  [BCDA  5 21 52]
  [ABCD 12  6 53]  [DABC  3 10 54]  [CDAB 10 15 55]  [BCDA  1 21 56]
  [ABCD  8  6 57]  [DABC 15 10 58]  [CDAB  6 15 59]  [BCDA 13 21 60]
  [ABCD  4  6 61]  [DABC 11 10 62]  [CDAB  2 15 63]  [BCDA  9 21 64]
  /* Then perform the following additions. (That is increment each
     of the four registers by the value it had before this block
     was started.) */
  A = A + AA
  B = B + BB
  C = C + CC
  D = D + DD
end /* of loop on i */
```

in java:

```java
public static byte[] hash(byte[] message) {
    byte[] paddingMessage = pad(message);
    int numBlocks = (paddingMessage.length) / 64;
    int a = INIT_A;
    int b = INIT_B;
    int c = INIT_C;
    int d = INIT_D;
    int[] buffer = new int[16];
    for (int i = 0; i < numBlocks; i++) {
        int index = i << 6;
        for (int j = 0; j < 64; j++, index++) {
            buffer[j >>> 2] = ((int) paddingMessage[index] << 24) | (buffer[j >>> 2] >>> 8);
        }
        int originalA = a;
        int originalB = b;
        int originalC = c;
        int originalD = d;
        for (int j = 0; j < 64; j++) {
            int div16 = j >>> 4;
            int f = 0;
            int bufferIndex = j;
            switch (div16) {
                case 0:
                    f = (b & c) | (~b & d);
                    break;
                case 1:
                    f = (b & d) | (c & ~d);
                    bufferIndex = (bufferIndex * 5 + 1) & 0x0F;
                    break;
                case 2:
                    f = b ^ c ^ d;
                    bufferIndex = (bufferIndex * 3 + 5) & 0x0F;
                    break;
                case 3:
                    f = c ^ (b | ~d);
                    bufferIndex = (bufferIndex * 7) & 0x0F;
                    break;
            }
            int temp = b + Integer.rotateLeft(a + f + buffer[bufferIndex] + T[j], SHIFT[(div16 << 2) | (j & 3)]);
            a = d;
            d = c;
            c = b;
            b = temp;
        }
        a += originalA;
        b += originalB;
        c += originalC;
        d += originalD;
    }
    byte[] md5 = new byte[16];
    int count = 0;
    for (int i = 0; i < 4; i++) {
        int n = (i == 0) ? a : ((i == 1) ? b : ((i == 2) ? c : d));
        for (int j = 0; j < 4; j++) {
            md5[count++] = (byte) n;
            n >>>= 8;
        }
    }
    return md5;
}
```

### 测试

根据 rfc1321 给出的测试用例

```txt
MD5 ("") = d41d8cd98f00b204e9800998ecf8427e
MD5 ("a") = 0cc175b9c0f1b6a831c399e269772661
MD5 ("abc") = 900150983cd24fb0d6963f7d28e17f72
MD5 ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789") =
d174ab98d277d9f5a5611c2c9f419d9f
```

JUnit 测试

```java
package crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MD5Test {
    @Test
    void hashTest() {
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", MD5.hash(""));
        assertEquals("0cc175b9c0f1b6a831c399e269772661", MD5.hash("a"));
        assertEquals("900150983cd24fb0d6963f7d28e17f72", MD5.hash("abc"));
        assertEquals("d174ab98d277d9f5a5611c2c9f419d9f", MD5.hash("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"));
    }
}
```

测试通过截图

![image-20231021145415028](D:\Operator\Study\密码学实训\Hash.assets\image-20231021145415028.png)

> 参考 [rfc1321](https://www.ietf.org/rfc/rfc1321.txt)

## SHA-512

### 流程图

![Elementary SHA-512 operation (single round). | Download Scientific Diagram](https://www.researchgate.net/publication/283184632/figure/fig3/AS:360493903564802@1462959852788/Elementary-SHA-512-operation-single-round.png)

### 编程实现

#### 初始哈希值

8 个 64 位的 words，取自前八个素数的平方根的小数部分。

```java
public static final long[] IV = new long[] {
    0x6A09E667F3BCC908L,
    0xBB67AE8584CAA73BL,
    0x3C6EF372FE94F82BL,
    0xA54FF53A5F1D36F1L,
    0x510E527FADE682D1L,
    0x9B05688C2B3E6C1FL,
    0x1F83D9ABFB41BD6BL,
    0x5BE0CD19137E2179L
};
```



#### 明文填充 Padding

将单个“1”比特追加到消息末尾，然后追加“0”比特，使填充后的消息的位长度对 1024 取模等于 896。然后填充 128 位的消息长度。

in java:

```java
public static byte[] pad(byte[] message) {
    int paddedLength = message.length + 17;
    if (paddedLength % 128 != 0) {
        paddedLength += 128 - paddedLength % 128;
    }
    byte[] paddedMessage = new byte[paddedLength];
    System.arraycopy(message, 0, paddedMessage, 0, message.length);
    paddedMessage[message.length] = (byte) 0x80;

    byte[] lenInBytes = BigInteger.valueOf(message.length * 8L).toByteArray();
    System.arraycopy(lenInBytes, 0, paddedMessage, paddedLength - lenInBytes.length, lenInBytes.length);

    return paddedMessage;
}
```

#### 定义逻辑函数

$$
\begin{align} \label{eq}
Ch(x, y, z ) ~=~& (x \and y) \oplus (x \and z) \\
Maj (x, y, z ) ~=~& (x \and y ) \oplus (x \and z ) \oplus (y \and z) \\
\Sigma_0(x) ~=~& S^{28} (x) \oplus S^{34} (x) \oplus S^{39} (x) \\
\Sigma_1(x) ~=~& S^{14} (x) \oplus S^{18} (x) \oplus S^{41} (x) \\
\sigma_0(x) ~=~& S^{1} (x) \oplus S^{8} (x) \oplus R^{7} (x) \\
\sigma_1(x) ~=~& S^{19} (x) \oplus S^{61} (x) \oplus R^{6} (x)
\end{align}
$$

in java:

```java
public static long Ch(long x, long y, long z) {
    return (x & y) ^ (~x & z);
}

public static long Maj(long x, long y, long z) {
    return (x & y) ^ (x & z) ^ (y & z);
}

public static long SIGMA0(long x) {
    return rotateRight(x, 28) ^ rotateRight(x, 34) ^ rotateRight(x, 39);
}

public static long SIGMA1(long x) {
    return rotateRight(x, 14) ^ rotateRight(x, 18) ^ rotateRight(x, 41);
}

public static long sigma0(long x) {
    return rotateRight(x, 1) ^ rotateRight(x, 8) ^ (x >>> 7);
}

public static long sigma1(long x) {
    return rotateRight(x, 19) ^ rotateRight(x, 61) ^ (x >>> 6);
}
```

#### 计算拓展消息块

以 1024 位划分消息为 $M^{(1)}, M^{(2)}, ...,M^{(N)}$，每个消息分组又被分为 16 组，每组 64 bits，记为 $M^{(i)}_0, M^{(i)}_1,...,M^{(i)}_{15}$，转换过程均使用大端序。

然后根据以下规则计算拓展消息块。

$W_j = M^{(i)}_j$ for $j=0,1,...,15$

For $j=16$ to $79$

$\{$

​	$W_j\leftarrow\sigma_1(W_{j-2})+W_{j-7}+\sigma_0(W_{j-15})+W_{j-16}$

$\}$

in java，通过 `toWords` 函数实现此功能。

```java
public static long[] toWords(byte[] block) {
    if (block.length != 128) {
        throw new IllegalArgumentException("Block length must be 128");
    }
    long[] words = new long[80];
    for (int i = 0; i < 16; i++) {
        words[i] = Bytes.load8ByteToLong(Arrays.copyOfRange(block, i * 8, i * 8 + 8));
    }

    for (int i = 16; i < 80; i++) {
        words[i] = sigma1(words[i - 2]) + words[i - 7] + sigma0(words[i - 15]) + words[i - 16];
    }
    return words;
}
```

#### 主要哈希计算过程

in java

```java
public static byte[] hash(byte[] message) {
    byte[] paddedMessage = pad(message);
    long[] buffer = Arrays.copyOf(IV, IV.length);
    for (int blockNumber = 0; blockNumber < paddedMessage.length / 128; blockNumber++) {
        long[] words = toWords(Arrays.copyOfRange(paddedMessage, blockNumber * 128, blockNumber * 128 + 128));
        long a = buffer[0];
        long b = buffer[1];
        long c = buffer[2];
        long d = buffer[3];
        long e = buffer[4];
        long f = buffer[5];
        long g = buffer[6];
        long h = buffer[7];
        for (int j = 0; j < 80; j++) {
            long t1 = h + SIGMA1(e) + Ch(e, f, g) + K[j] + words[j];
            long t2 = SIGMA0(a) + Maj(a, b, c);
            h = g;
            g = f;
            f = e;
            e = d + t1;
            d = c;
            c = b;
            b = a;
            a = t1 + t2;
        }
        buffer[0] = a + buffer[0];
        buffer[1] = b + buffer[1];
        buffer[2] = c + buffer[2];
        buffer[3] = d + buffer[3];
        buffer[4] = e + buffer[4];
        buffer[5] = f + buffer[5];
        buffer[6] = g + buffer[6];
        buffer[7] = h + buffer[7];
    }

    byte[] hashResult = new byte[64];
    for (int i = 0; i < 8; i++) {
        System.arraycopy(Bytes.storeLongTo8Byte(buffer[i]), 0, hashResult, i * 8, 8);
    }
    return hashResult;
}
```

首先，对输入的消息进行填充，然后，使用初始向量（IV）初始化缓冲区。

接下来，按照每个 128 Byte = 1024 bit 的消息块进行迭代处理。将每个消息块拆分成一个长字数组，并使用一系列变量来保存中间结果。这些变量包括a，b，c，d，e，f，g和h，代表了算法中的中间状态。

每个消息块应用 80 轮的操作。在每一轮中，我们根据消息块中的字、常量和当前状态变量计算出临时变量 t1 和 t2，并使用这些临时变量来更新状态变量a到h。

在处理完所有的消息块后，我们将最终的状态变量转换为字节数组，以得到最终的哈希结果。这个字节数组将作为函数的返回值。

### 测试

根据 [Descriptions of SHA-256, SHA-384, and SHA-512](https://eips.ethereum.org/assets/eip-2680/sha256-384-512.pdf) 中给出的测试用例

````txt
Hash of "abc" = 
ddaf35a193617aba cc417349ae204131 12e6fa4e89a97ea2 0a9eeee64b55d39a
2192992a274fc1a8 36ba3c23a3feebbd 454d4423643ce80e 2a9ac94fa54ca49f
````

JUnit 测试：

```java
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
```

测试通过

![image-20231021185842333](D:\Operator\Study\密码学实训\Hash.assets\image-20231021185842333.png)

> 参考 [Descriptions of SHA-256, SHA-384, and SHA-512](https://eips.ethereum.org/assets/eip-2680/sha256-384-512.pdf)