package io.github.jsssk.bf;

import com.google.common.hash.Hashing;

public enum BloomFilterHashStrategies implements BloomFilterHashStrategy {

    /**
     * <a href="http://www.eecs.harvard.edu/~michaelm/postscripts/rsa2008.pdf">Less Hashing, Same Performance: Building a Better Bloom Filter</a>
     * <br>
     * "A standard technique from the hashing literature is to use two hash functions h1(x) and h2(x) to
     * simulate additional hash functions of the form gi(x) = h1(x) + ih2(x).
     * We demonstrate that this technique can be usefully applied to Bloom filters and related data structures. Specifically,
     * only two hash functions are necessary to effectively implement a Bloom filter without any loss in the asymptotic false positive probability.
     * This leads to less computation and potentially less need for randomness in practice."
     */
    MURMUR128_MITZ_64() {
        @Override
        public long[] indexes(byte[] raw, BloomFilterParam param) {
            long bitSize = param.getBitSize();
            @SuppressWarnings("UnstableApiUsage")
            byte[] bytes = Hashing.murmur3_128().hashBytes(raw).asBytes();
            long hash1 = (bytes[0] & 0xFFL) << 56
                | (bytes[1] & 0xFFL) << 48
                | (bytes[2] & 0xFFL) << 40
                | (bytes[3] & 0xFFL) << 32
                | (bytes[4] & 0xFFL) << 24
                | (bytes[5] & 0xFFL) << 16
                | (bytes[6] & 0xFFL) << 8
                | (bytes[7] & 0xFFL);
            long hash2 = (bytes[8] & 0xFFL) << 56
                | (bytes[9]  & 0xFFL) << 48
                | (bytes[10] & 0xFFL) << 40
                | (bytes[11] & 0xFFL) << 32
                | (bytes[12] & 0xFFL) << 24
                | (bytes[13] & 0xFFL) << 16
                | (bytes[14] & 0xFFL) << 8
                | (bytes[15] & 0xFFL);
            long[] indexes = new long[param.getNumOfHashFunctions()];
            long combinedHash = hash1;
            for (int i = 0; i < param.getNumOfHashFunctions(); i++) {
                // Make the combined hash positive and indexable
                indexes[i] = (combinedHash & Long.MAX_VALUE) % bitSize;
                combinedHash += hash2;
            }
            return indexes;
        }
    },

    DEFAULT() {
        @Override
        public long[] indexes(byte[] raw, BloomFilterParam param) {
            return MURMUR128_MITZ_64.indexes(raw, param);
        }
    }



}
