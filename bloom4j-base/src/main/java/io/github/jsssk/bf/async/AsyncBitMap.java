package io.github.jsssk.bf.async;

import java.util.function.BiConsumer;

public interface AsyncBitMap {

    /**
     * return size of this BitMap in bits.
     */
    long size();

    /**
     * clear this BitMap.
     */
    void clear(BiConsumer<Void, Throwable> callback);

    /**
     * delete this BitMap.
     */
    void delete(BiConsumer<Void, Throwable> callback);


    /**
     * set the bit to 1 at certain index.
     *
     * @param index index
     */
    void set(long index, BiConsumer<Void, Throwable> callback);

    /**
     * get the bit at certain index.
     *
     * @param index index
     */
    void get(long index, BiConsumer<Boolean, Throwable> callback);

    /**
     * set the bit to 1 at certain indexes.
     * @param indexes indexes
     * @param callback callback consumer
     */
    void set(long[] indexes, BiConsumer<Void, Throwable> callback);

    /**
     * determine if all bits at indexes are 1.
     * @param indexes indexes
     * @param callback callback consumer
     */
    void allMatch(long[] indexes, BiConsumer<Boolean, Throwable> callback);

}
