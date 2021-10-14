package io.github.jsssk.bf.sync;

public interface TransferableBloomFilter extends BloomFilter, Iterable<byte[]>{

    default void transferFrom(TransferableBloomFilter src) {
        transferFrom(src, false);
    }

    default void transferFrom(TransferableBloomFilter src, boolean cover) {
        long i = 0;
        if (cover) {
            for (byte[] b : src) {
                putBytes(i, b);
                i += b.length;
            }
        } else {
            for (byte[] b : src) {
                mergeBytes(i, b);
                i += b.length;
            }
        }
    }

    /**
     * @param i first index of byte of bitmap
     */
    void putBytes(long i, byte[] b);

    /**
     * @param i first index of byte of bitmap
     */
    void mergeBytes(long i, byte[] b);

}
