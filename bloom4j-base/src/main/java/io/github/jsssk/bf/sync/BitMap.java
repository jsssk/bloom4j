package io.github.jsssk.bf.sync;


public interface BitMap extends Iterable<byte[]> {
    /**
     * return size of this BitMap in bits.
     */
    long size();

    /**
     * delete this BitMap.
     */
    void delete();

    /**
     * clear this BitMap.
     */
    void clear();

    /**
     * set the bit to 1 at certain index.
     *
     * @param index index
     */
    void set(long index);

    /**
     * get the bit at certain index.
     *
     * @param index index
     * @return true if the bit is 1, or false if 0.
     */
    boolean get(long index);

    /**
     * write the certain bytes with data.
     *
     * @param i     first index of byte
     * @param bytes data
     */
    void write(long i, byte[] bytes);
    /**
     * merge the certain bytes with data in start with certain index.
     *
     * @param i     first index of byte
     * @param bytes data
     */
    void merge(long i, byte[] bytes);

    /**
     * @param i     first index of byte
     * @param bytes data
     */
    void read(long i, byte[] bytes);

    // default empty
    default void set(long[] indexes) {
        for (long index : indexes) {
            set(index);
        }
    }

    default boolean allMatch(long[] indexes) {
        for (long index : indexes) {
            if (! get(index)) {
                return false;
            }
        }
        return true;
    }
}
