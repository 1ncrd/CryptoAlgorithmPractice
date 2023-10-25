package crypto.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Bytes {
    public static void printByteArrayInHex(byte[] x) {
        String hex = byteArrayToHexString(x);
        for (int i = 0, counter = 0; i < hex.length(); i += 8, counter++) {
            if (counter != 0 && counter % 8 == 0) {
                System.out.println();
            }
            if (i + 8 <= hex.length()) {
                System.out.printf("%8s ", hex.substring(i, i + 8));
            } else {
                System.out.printf("%-8s ", hex.substring(i));
            }
        }
        System.out.println();
    }
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

        // Convert each byte to hexadecimal and append it to the StringBuilder
        for (byte b : byteArray) {
            // Convert the byte to an integer, and mask with 0xFF to ensure it's treated as unsigned
            int intValue = b & 0xFF;

            // Convert the integer to a hexadecimal string and append it
            String hex = Integer.toHexString(intValue);

            // Pad with a leading zero if necessary to ensure two characters
            if (hex.length() == 1) {
                hexString.append("0");
            }

            hexString.append(hex);
        }

        return hexString.toString();
    }

    public static byte @NotNull [] ASCIIStringToByteArray(@NotNull String ascii) {
        int len = ascii.length();
        byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            data[i] = (byte) ascii.charAt(i);
        }
        return data;
    }

    @Contract(pure = true)
    public static int load4ByteToInt(byte @NotNull [] x) {
        if (x.length != 4) {
            throw new IllegalArgumentException("Byte length must be 4");
        }

        return ((x[0] & 0xff) << 24) | ((x[1] & 0xff) << 16) | ((x[2] & 0xff) << 8) | ((x[3] & 0xff));
    }

    @Contract(pure = true)
    public static long load8ByteToLong(byte @NotNull [] x) {
        if (x.length != 8) {
            throw new IllegalArgumentException("Byte length must be 8");
        }

        return ((long) (x[0] & 0xff) << 56) |
                ((long) (x[1] & 0xff) << 48) |
                ((long) (x[2] & 0xff) << 40) |
                ((long) (x[3] & 0xff) << 32) |
                ((long) (x[4] & 0xff) << 24) |
                ((x[5] & 0xff) << 16) |
                ((x[6] & 0xff) << 8) |
                (x[7] & 0xff);
    }

    public static byte[] storeLongTo8Byte(long value) {
        byte[] result = new byte[8];

        result[0] = (byte) ((value >> 56) & 0xff);
        result[1] = (byte) ((value >> 48) & 0xff);
        result[2] = (byte) ((value >> 40) & 0xff);
        result[3] = (byte) ((value >> 32) & 0xff);
        result[4] = (byte) ((value >> 24) & 0xff);
        result[5] = (byte) ((value >> 16) & 0xff);
        result[6] = (byte) ((value >> 8) & 0xff);
        result[7] = (byte) (value & 0xff);

        return result;
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

    public static byte byteAt(int x, int n) {
        return (byte) ((x >> (n * 8)) & 0xff);
    }

    /**
     * Return a new array that don't contain prefix zero
     *
     * @return a new array that don't contain prefix zero
     */
    public static byte[] removePrefixZeros(byte @NotNull [] a) {
        int lastPrefixZeroIndex = -1;
        for (int i = 0; i < a.length; i++) {
            if (a[i] == 0) {
                lastPrefixZeroIndex = i;
            }
        }
        return Arrays.copyOfRange(a, lastPrefixZeroIndex + 1, a.length);
    }

    public static void main(String[] args) {
        byte[] a = new byte[]{
                0, 1, 34, 4, 59
        };
        System.out.printf((byteArrayToHexString(a)));
    }
}
