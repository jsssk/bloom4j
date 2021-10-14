package io.github.jsssk.bf.sync.redis;

import io.github.jsssk.bf.BloomFilterHashStrategy;
import io.github.jsssk.bf.BloomFilterParam;
import io.github.jsssk.bf.sync.DefaultBloomFilter;
import io.lettuce.core.api.sync.RedisKeyCommands;
import io.lettuce.core.api.sync.RedisStringCommands;


public class RedisBloomFilter extends DefaultBloomFilter {


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
    <C extends RedisKeyCommands<String, ?> & RedisStringCommands<String, ?>>
    RedisBloomFilter create(C commands,
                             String identifier,
                             BloomFilterParam param,
                             BloomFilterHashStrategy strategy) {

        return new RedisBloomFilter(commands, identifier, param, strategy);
    }


    private
    <C extends RedisKeyCommands<String, ?> & RedisStringCommands<String, ?>>
    RedisBloomFilter(C commands,
                     String bitmapIdentifier,
                     BloomFilterParam param,
                     BloomFilterHashStrategy strategy) {
        this.bits = new RedisBitMap<>(bitmapIdentifier, commands, param.getBitSize());
        this.param = param;
        this.strategy = strategy;
    }

}
