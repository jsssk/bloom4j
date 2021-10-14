package io.github.jsssk.bf.async.redis;

import io.github.jsssk.bf.BloomFilterHashStrategy;
import io.github.jsssk.bf.BloomFilterParam;
import io.github.jsssk.bf.async.DefaultAsyncBloomFilter;
import io.lettuce.core.api.async.RedisKeyAsyncCommands;
import io.lettuce.core.api.async.RedisStringAsyncCommands;

public class RedisAsyncBloomFilter extends DefaultAsyncBloomFilter {

    /**
     * create an instance of lettuce redis bloom filter.
     * @param commands redis commands instance.
     * @param identifier Key of bitmap.
     * @param param param for bloom filter.
     * @param strategy strategies of generating the k * log(M) bits required for an element
     *                 to be mapped to a BloomFilter of M bits and k hash functions.
     * @return Bloom Filter instance implemented by redis bitmap.
     */
    public static
    <C extends RedisKeyAsyncCommands<String, ?> & RedisStringAsyncCommands<String, ?>>
    RedisAsyncBloomFilter create(C commands,
                                    String identifier,
                                    BloomFilterParam param,
                                    BloomFilterHashStrategy strategy) {

        return new RedisAsyncBloomFilter(commands, identifier, param, strategy);
    }
    private
    <C extends RedisKeyAsyncCommands<String, ?> & RedisStringAsyncCommands<String, ?>>
    RedisAsyncBloomFilter(C commands,
                          String bitmapIdentifier,
                          BloomFilterParam param,
                          BloomFilterHashStrategy strategy) {
        this.bits = new RedisAsyncBitMap<>(bitmapIdentifier, commands, param.getBitSize());
        this.param = param;
        this.strategy = strategy;
    }

}
