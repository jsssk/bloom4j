package io.github.jsssk.bf;


import static java.lang.Math.*;
import static java.lang.Math.log;

public class BloomFilterCalculator {

    private BloomFilterCalculator() {}

    public static double falsePositiveProbability(long m, long n, int k) {
        return pow(1 - exp(- k / ((double) m / n)), k);
    }

    public static long bitSize(long n, double fpp) {
        return (long) ceil((n * log(fpp)) / log(1 / pow(2, log(2))));
    }
    public static long roundBitSize(long n, double fpp) {
        long bitSize = bitSize(n, fpp);
        if ((bitSize & 0x7) > 0) {
            bitSize = (bitSize & 0xffff_fff8) + 8;
        }
        return bitSize;
    }

    public static int numOfHashFunctions(long m, long n) {
        return (int) round(((double)m / n) * log(2));
    }

    public static long numOfItems(long m, double fpp, int k) {
        return (long) ceil(m / (-k / log(1 - exp(log(fpp) / k))));
    }

}

