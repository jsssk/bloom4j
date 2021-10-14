package io.github.jsssk.bf;


import java.io.Serializable;

/**
 * <a href="https://en.wikipedia.org/wiki/Bloom_filter">Bloom Filter</a>
 * <br><br>
 * n Number of items in the filter <br>
 * p Probability of false positives, fraction between 0 and 1 or a number indicating 1-in-p <br>
 * m Number of bits in the filter <br>
 * k Number of hash functions <br>
 * <br>
 * n = ceil(m / (-k / log(1 - exp(log(p) / k))))<br>
 * p = pow(1 - exp(-k / (m / n)), k)<br>
 * m = ceil((n * log(p)) / log(1 / pow(2, log(2))))<br>
 * k = round((m / n) * log(2)) <br>
 * <br><br>
 */
public class BloomFilterParam implements Serializable {

    /**
     * false positive probability
     */
    final double falsePositiveProbability;
    /**
     * num of bits
     */
    final long bitSize;
    /**
     * num of item
     */
    final long numOfItems;
    /**
     * num of Hash Functions
     */
    final int numOfHashFunctions;

    private BloomFilterParam(long numOfItems, double falsePositiveProbability) {
        this.falsePositiveProbability = falsePositiveProbability;
        this.numOfItems = numOfItems;
        this.bitSize = BloomFilterCalculator.roundBitSize(numOfItems, falsePositiveProbability);
        this.numOfHashFunctions = BloomFilterCalculator.numOfHashFunctions(this.bitSize, this.numOfItems);
    }

    private BloomFilterParam(long numOfItems, double falsePositiveProbability, long maxBitSize) {
        this.falsePositiveProbability = falsePositiveProbability;
        this.numOfItems = numOfItems;
        this.bitSize = Math.min(BloomFilterCalculator.roundBitSize(numOfItems, falsePositiveProbability), maxBitSize);
        this.numOfHashFunctions = BloomFilterCalculator.numOfHashFunctions(this.bitSize, this.numOfItems);
    }

    public static BloomFilterParam of(long numOfItems, double falsePositiveProbability) {
        return new BloomFilterParam(numOfItems, falsePositiveProbability);
    }

    public static BloomFilterParam of(long numOfItems, double falsePositiveProbability, long maxBitSize) {
        return new BloomFilterParam(numOfItems, falsePositiveProbability, maxBitSize);
    }

    public double getFalsePositiveProbability() {
        return falsePositiveProbability;
    }

    public long getBitSize() {
        return bitSize;
    }

    public long getNumOfItems() {
        return numOfItems;
    }

    public int getNumOfHashFunctions() {
        return numOfHashFunctions;
    }
}

