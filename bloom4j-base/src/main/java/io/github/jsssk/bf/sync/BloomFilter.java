package io.github.jsssk.bf.sync;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;

/**
 * BloomFilter
 *
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
 *
 * @author Jasper Xu
 */
public interface BloomFilter extends Predicate<byte[]>, Serializable {

    /**
     * clear this BloomFilter.
     * all bits would be set to 0.
     */
    void clear();

    /**
     * delete this BloomFilter.
     */
    void delete();

    /**
     * return actual false positive probability of this BloomFilter.
     */
    double fpp();

    /**
     * add a element
     */
    void put(byte[] raw);

    /**
     * determine this filter contains the certain bytes.
     */
    boolean contains(byte[] raw);


    @Override
    default boolean test(byte[] raw) { return contains(raw); }
}
