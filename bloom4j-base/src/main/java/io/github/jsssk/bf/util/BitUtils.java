package io.github.jsssk.bf.util;

public class BitUtils {

    public static int getBit(byte b, int i) {
        if (i < 0 || i > 7) {
            throw new IllegalArgumentException("range of i is 0 to 7.");
        }
        i = 7 - i;
        return (b >>> i) & 0x1;
    }

    public static byte setBit(byte b, int i) {
        if (i < 0 || i > 7) {
            throw new IllegalArgumentException("range of i is 0 to 7.");
        }
        i = 7 - i;
        return (byte) (b | (0x1 << i));
    }

}
