package crypto;

import static crypto.util.Bytes.ASCIIStringToByteArray;
import static crypto.util.Bytes.byteArrayToHexString;

/**
 * @see <a href="https://www.ietf.org/rfc/rfc1321.txt">rfc1321</a>
 */
public class MD5 {
    private static final int INIT_A = 0x67452301;
    private static final int INIT_B = (int) 0xEFCDAB89L;
    private static final int INIT_C = (int) 0x98BADCFEL;
    private static final int INIT_D = 0x10325476;
    private static final int[] SHIFT = { 7, 12, 17, 22, 5, 9, 14, 20, 4, 11, 16, 23, 6, 10, 15, 21 };
    private static final int[] T = new int[64];

    static {
        for (int i = 0; i < 64; i++) {
            T[i] = (int) (long) ((1L << 32) * Math.abs(Math.sin(i + 1)));
        }
    }

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

    public static String hash(String message) {
        return byteArrayToHexString(hash(ASCIIStringToByteArray(message)));
    }

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
}
