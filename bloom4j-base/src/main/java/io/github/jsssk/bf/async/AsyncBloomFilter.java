package io.github.jsssk.bf.async;

import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface AsyncBloomFilter {


    /**
     * clear this BloomFilter.
     * all bits would be set to 0.
     */
    void clear(BiConsumer<Void, Throwable> callback);

    /**
     * delete this BloomFilter.
     */
    void delete(BiConsumer<Void, Throwable> callback);

    /**
     * return actual false positive probability of this BloomFilter.
     */
    double fpp();

    /**
     * add a element
     */
    void put(byte[] raw, BiConsumer<Void, Throwable> callback);

    /**
     * determine if this filter might contain the certain bytes.
     */
    void contains(byte[] raw, BiConsumer<Boolean, Throwable> callback);

}
