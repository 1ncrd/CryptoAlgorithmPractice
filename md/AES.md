# AES 算法

`叶宇航 921127970158`

## 简介

AES 加密过程是在一个 4×4 的字节矩阵上运作，这个矩阵又称为“体（state）”，其初值就是一个明文区块（矩阵中一个元素大小就是明文区块中的一个 Byte）。加密时，各轮 AES 加密循环（除最后一轮外）均包含 4 个步骤：

- **AddRoundKey**：矩阵中的每一个字节都与该次回合密钥（round key）做 XOR 运算；每个子密钥由密钥生成方案产生。

- **SubBytes**：通过一个非线性的替换函数，用查表的方式把每个字节替换成对应的字节。

- **ShiftRows**：将矩阵中的每个横列进行循环移位。

- **MixColumns**：为了充分混合矩阵中各个直行的操作。这个步骤使用线性转换来混合每内联的四个字节。最后一个加密循环中省略 **MixColumns** 步骤，而以另一个 **AddRoundKey** 取代。

## 流程图

<img src="D:\Operator\Study\密码学实训\AES.assets\image-20231004182410783.png" alt="image-20231004182410783" height=500px />

## 编程实现

采用 Java 语言实现，类实现 `crypto.AES`

### 目录结构

```
├─src
│  └─crypto
│      │  AES.java
│      └─util
│              Bytes.java
│              Number.java
└─test
    └─crypto
        │  AESTest.java
        └─util
                BytesTest.java
                NumberTest.java
```

`src/crypto` 目录下为源代码，`src/crypto/util` 目录存放工具函数，`test/crypto` 目录下存放各类的测试用例

### 加解密类和工具类

主要功能实现于 `AES` 类中

#### 构造函数

若未提供密钥则使用 `genKey` 函数生成随机密钥

```java
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
```



#### 随机密钥生成函数

使用 `SecureRandom.getInstanceStrong().nextBytes` 生成十六字节的强随机数

```java
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
```

#### 密钥扩展

接受一个字节数组 `key` 作为输入，用于生成加密和解密的轮密钥。

1. 前 4 个字节（32位）的 `key` 被装载到 `encryptKey` 数组中，其中 `Bytes.load4ByteToInt` 用于将 4 个字节转换为一个整数。这些整数构成初始的加密密钥。
2. 接下来，通过循环迭代 10 次，从轮密钥中生成更多的密钥。这是AES中的子密钥生成步骤。在每一轮中，将之前生成的密钥与一种称为 `gFunc` 的函数进行 XOR 运算，并将结果存储在 `encryptKey` 数组中的相应位置。每轮生成 4 个 32 位的子密钥。
3. 最后，将生成的轮密钥复制到 `decryptKey` 数组中，以便稍后用于解密过程。这是 AES 的加密和解密之间共享相同的密钥扩展过程。

```java
private void keyExpansion(byte[] key) {
    for (int i = 0; i < 4; i++) {
        encryptKey[i] = Bytes.load4ByteToInt(key[i * 4], key[i * 4 + 1], key[i * 4 + 2], key[i * 4 + 3]);
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
```

##### g 函数

密钥拓展中的 *g function*

```java
private int gFunc(int w, int i) {
    return load4ByteToInt(S[byteAt(w, 2) & 0xff], S[byteAt(w, 1) & 0xff],
            S[byteAt(w, 0) & 0xff], S[byteAt(w, 3) & 0xff]) ^ rcon[i];
}
```

#### 加解密函数

AES加密过程。接受明文（`plaintext`）作为输入并返回加密后的密文。

1. `dataToState` 函数将明文转换为 AES 状态矩阵（`state`）。
2. `addRoundKey` 函数将初始轮密钥添加到状态矩阵中。
3. 接下来，通过进行 9 轮循环迭代，执行以下操作：
    - `subBytes`：对状态矩阵中的字节执行字节替代。
    - `shiftRows`：执行状态矩阵的行位移操作。
    - `mixColumns`：执行状态矩阵的列混淆操作。
    - `addRoundKey`：添加轮密钥到状态矩阵中，其中轮密钥从`encryptKey`数组中取出。
4. 最后，执行最后一轮操作，包括 `subBytes`，`shiftRows`，和最后一次的 `addRoundKey`。然后，将状态矩阵转换回字节数组（密文）并返回。

AES解密过程。它接受密文（`cipher`）作为输入并返回解密后的明文。

1. `dataToState` 函数将密文转换为AES状态矩阵（`state`）。
2. `addRoundKey `函数将初始轮密钥添加到状态矩阵中。
3. 接下来，通过进行 9 轮循环迭代，执行以下操作：
    - `invShiftRows`：执行状态矩阵的逆行位移操作。
    - `invSubBytes`：对状态矩阵中的字节执行逆字节替代。
    - `addRoundKey`：添加轮密钥到状态矩阵中，其中轮密钥从`decryptKey`数组中取出。
    - `invMixColumns`：执行状态矩阵的逆列混淆操作。
4. 最后，执行最后一轮操作，包括 `invShiftRows`，`invSubBytes`，和最后一次的 `addRoundKey`。然后，将状态矩阵转换回字节数组（明文）并返回。

```java
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

public static byte[] encrypt(byte[] plain, byte[] key) {
    AES aes = new AES(key);
    return aes.encrypt(plain);
}

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

public static byte[] decrypt(byte[] cipher, byte[] key) {
    AES aes = new AES(key);
    return aes.decrypt(cipher);
}
```

#### 数据与State相互转换

 字节数据与 AES 加密的操作单位 State 之间的相互转换

```java
private byte[][] dataToState(byte[] in) {
    byte[][] state = new byte[4][4];
    for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
            state[j][i] = in[i * 4 + j];
        }
    }
    return state;
}

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
```

#### 轮密钥加

在该操作中，每个字节都与对应位置的密钥字节进行异或运算。具体来说，代码中的双重循环遍历了 4x4 的状态矩阵，并对每个元素执行了异或操作。

```java
private void addRoundKey(byte[][] state, int[] key) {
    for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
            state[i][j] ^= Bytes.byteAt(key[j], 3 - i);
        }
    }
}
```

#### （逆）字节替代

在字节替代中，每个字节都被 S 盒中对应的值替换。而在逆字节替代中，每个字节都被逆 S 盒中对应的值替换。这两个操作通过使用预先计算好的 *S 盒* 和 *逆 S 盒* 来完成。代码中的双重循环遍历了 4x4 的状态矩阵，并将每个字节替换为相应的 S盒 或 逆S盒 中的值。

```java
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
            state[i][j] = (byte) inv_S[state[i][j] & 0xff];
        }
    }
}
```

#### （逆）行移位

每一行都被循环左移不同的次数。而在逆行移位中，每一行都被循环右移不同的次数。代码中的循环遍历了4个字节的数组，通过使用位移操作来实现行的移位。

```java
private void shiftRows(byte[][] state) {
    for (int i = 0; i < 4; i++) {
        int block = Bytes.load4ByteToInt(state[i]);
        block = Integer.rotateLeft(block, 8 * i);
        state[i] = Bytes.storeIntTo4Byte(block);
    }
}

private void invShiftRows(byte[][] state) {
    for (int i = 0; i < 4; i++) {
        int block = Bytes.load4ByteToInt(state[i]);
        block = Integer.rotateRight(block, 8 * i);
        state[i] = Bytes.storeIntTo4Byte(block);
    }
}
```

#### （逆）列混淆

这部分代码实现了AES中的列混淆操作和其逆操作。在列混淆中，每一列都与特定矩阵进行乘法运算。而在逆列混淆中，每一列也与特定矩阵进行乘法运算。这两个操作通过使用预先定义的矩阵来完成。代码中创建了一个临时的 4x4 字节矩阵用于存储中间结果，并通过调用`Number.GMatrixMul` 方法来实现矩阵乘法。

```java
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
```

#### Bytes Util

`Bytes` 工具类，提供了一系列用于处理字节数组的静态方法。

`Bytes` 工具类包含以下主要功能：

1. `hexStringToByteArray`：将十六进制字符串转换为字节数组。
2. `ASCIIStringToByteArray`：将 ASCII 字符串转换为字节数组。
3. `load4ByteToInt`：将 4 个字节的字节数组或整数加载为一个整数。
4. `storeIntTo4Byte`：将整数存储为 4 个字节的字节数组。
5. `byteAt`：获取 `int` 中指定位置的字节。
6. `byteArrayToHexString`：将字节数组转换为十六进制字符串。
7. `removePrefixZeros`：创建一个新的字节数组，不包含前导零字节。

```java
package crypto.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Bytes {
    public static byte @NotNull [] hexStringToByteArray(@NotNull String hex) {...}

    public static byte @NotNull [] ASCIIStringToByteArray(@NotNull String ascii) {...}

    @Contract(pure = true)
    public static int load4ByteToInt(byte @NotNull [] x) {...}

    @Contract(pure = true)
    public static int load4ByteToInt(byte x0, byte x1, byte x2, byte x3) {...}

    @Contract(pure = true)
    public static int load4ByteToInt(int x0, int x1, int x2, int x3) {...}

    @Contract(pure = true)
    public static byte @NotNull [] storeIntTo4Byte(int x) {...}

    public static byte byteAt(int x, int n) {...}

    public static String byteArrayToHexString(byte[] byteArray) {...}

    public static byte[] removePrefixZeros(byte @NotNull [] a) {...}

    public static void main(String[] args) {...}
}

```

以下是具体实现：

16 进制字符串与字节数组相互转换

```java
public static byte @NotNull [] hexStringToByteArray(@NotNull String hex) {
    if (hex.length() % 2 == 1) {
        hex = "0" + hex;
    }
    int len = hex.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
        data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                              + Character.digit(hex.charAt(i + 1), 16));
    }
    return data;
}

public static @NotNull String byteArrayToHexString(byte[] byteArray) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : byteArray) {
        int intValue = b & 0xFF;
        String hex = Integer.toHexString(intValue);
        if (hex.length() == 1) {
            hexString.append("0");
        }
        hexString.append(hex);
    }
    return hexString.toString();
}
```

Ascii字符串转换为字节数组

```java
public static byte @NotNull [] ASCIIStringToByteArray(@NotNull String ascii) {
    int len = ascii.length();
    byte[] data = new byte[len];
    for (int i = 0; i < len; i++) {
        data[i] = (byte) ascii.charAt(i);
    }
    return data;
}
```

4 个 `byte` 与 `int` 之间的转换

```java
@Contract(pure = true)
public static int load4ByteToInt(byte @NotNull [] x) {
    if (x.length != 4) {
        throw new IllegalArgumentException("Byte length must be 4");
    }

    return ((x[0] & 0xff) << 24) | ((x[1] & 0xff) << 16) | ((x[2] & 0xff) << 8) | ((x[3] & 0xff));
}

@Contract(pure = true)
public static int load4ByteToInt(byte x0, byte x1, byte x2, byte x3) {
    return ((x0 & 0xff) << 24) | ((x1 & 0xff) << 16) | ((x2 & 0xff) << 8) | ((x3 & 0xff));
}

@Contract(pure = true)
public static int load4ByteToInt(int x0, int x1, int x2, int x3) {
    return ((x0 & 0xff) << 24) | ((x1 & 0xff) << 16) | ((x2 & 0xff) << 8) | ((x3 & 0xff));
}

@Contract(pure = true)
public static byte @NotNull [] storeIntTo4Byte(int x) {
    byte[] result = new byte[4];
    result[0] = (byte) ((x >> 24) & 0xff);
    result[1] = (byte) ((x >> 16) & 0xff);
    result[2] = (byte) ((x >> 8) & 0xff);
    result[3] = (byte) (x & 0xff);
    return result;
}
```

计算 `int` 中第 n 个字节

```java
public static byte byteAt(int x, int n) {
    return (byte) ((x >> (n * 8)) & 0xff);
}
```

#### Number Util

`Number` 工具类，提供数论相关算法

```java
public class Number {
    /* see https://abcdxyzk.github.io/blog/2018/04/16/isal-erase-3/ */
    public static byte GMul(byte u, byte v) {...}

    /** in place */
    public static void GMatrixMul(byte[][] a, byte[][] b, byte[][] r) {...}
```

伽罗华域（Galois Fields）乘法。

伽罗华域上的多项式乘法，其结果需要 mod P(x)。

```java
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
```

伽罗华域（Galois Fields）矩阵乘法 

```java
public static void GMatrixMul(byte[][] a, byte[][] b, byte[][] r) {
    for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
            r[i][j] = (byte) (GMul(a[i][0], b[0][j]) ^ GMul(a[i][1], b[1][j]) ^ GMul(a[i][2], b[2][j]) ^ GMul(a[i][3], b[3][j]));
        }
    }
}
```



## 测试

> 建立一个明文文本文件 plain.txt，从中读取16个字节作为AES加密的一个分组
>
> 设置AES加密算法的密钥
>
> 编程实现对一个分组的AES加密，获得相应的密文

### 文件读取加密

plain.txt 文件内容

```txt
AESisabestcipher
```

```java
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
}
```

```txt
File content:
41455369 73616265 73746369 70686572 
Encrypt result:
67c4e32a 7c7cdc0d ec89552f 80a828cc
```

在这段代码中执行了以下操作：

1. 使用 `Files.readAllBytes(path)` 从指定文件（"plain.txt"）中读取明文数据，将文件内容读取为字节数组，并存储在 `bytes` 变量中。
2. 然后，设置一个用于加密的密钥，这个密钥是由 ASCII 字符串转换而来，通过 `ASCIIStringToByteArray` 方法转换为字节数组，并将其存储在 `key` 变量中。
3. 接着，创建一个 AES 加密类的实例 `AES aes`，同时将设置好的密钥 `key` 传递给该实例，以便在加密中使用。
4. 最后，通过调用 `aes.encrypt(bytes)` 方法，使用 AES 加密算法对明文数据 `bytes` 进行加密，生成了密文数据，并将其存储在 `cipher` 变量中。

整个过程完成后，在控制台以十六进制输出了明文数据的字节信息和密文数据的字节数组，以可视化结果。

### Junit 测试程序

> JUnit 是一个用于 Java 编程语言的开源测试框架，用于编写和运行单元测试。它是软件开发中的一种自动化测试工具，

```java
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
```

测试通过

![image-20231011225438621](D:\Operator\Study\密码学实训\AES.assets\image-20231011225438621.png)

 