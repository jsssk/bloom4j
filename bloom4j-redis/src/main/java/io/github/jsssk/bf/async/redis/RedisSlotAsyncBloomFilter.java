package io.github.jsssk.bf.async.redis;

import io.github.jsssk.bf.BloomFilterHashStrategy;
import io.github.jsssk.bf.BloomFilterParam;
import io.github.jsssk.bf.async.DefaultSlotAsyncBloomFilter;
import io.github.jsssk.bf.slot.DefaultSlotStrategy;
import io.github.jsssk.bf.slot.SlotStrategy;
import io.github.jsssk.bf.slot.Slots;
import io.lettuce.core.api.async.RedisKeyAsyncCommands;
import io.lettuce.core.api.async.RedisStringAsyncCommands;

import java.util.ArrayList;

public class RedisSlotAsyncBloomFilter extends DefaultSlotAsyncBloomFilter {
    public static
    <C extends RedisKeyAsyncCommands<String, ?> & RedisStringAsyncCommands<String, ?>>
    RedisSlotAsyncBloomFilter create(C commands, String identifierPrefix,
                                        long n, double fpp, BloomFilterHashStrategy strategy) {

        return new RedisSlotAsyncBloomFilter(commands, identifierPrefix, n, fpp, strategy);
    }
    public static
    <C extends RedisKeyAsyncCommands<String, ?> & RedisStringAsyncCommands<String, ?>>
    RedisSlotAsyncBloomFilter create(C commands,
                                        String identifierPrefix,
                                        SlotStrategy slotStrategy,
                                        BloomFilterHashStrategy strategy) {
        return new RedisSlotAsyncBloomFilter(commands, identifierPrefix, slotStrategy, strategy);
    }


    <C extends RedisKeyAsyncCommands<String, ?> & RedisStringAsyncCommands<String, ?>>
    RedisSlotAsyncBloomFilter(C commands,
                              String identifierPrefix, long n, double fpp,
                              BloomFilterHashStrategy strategy) {
        this.strategy = strategy;

        this.slotStrategy = DefaultSlotStrategy.of(n, fpp);

        this.slots = new ArrayList<>(slotStrategy.slotNum());
        for (int i = 0; i < slotStrategy.slotNum(); i++) {
            BloomFilterParam slotParam =
                BloomFilterParam.of(n / slotStrategy.slotNum(), fpp);
            this.slots.add(RedisAsyncBloomFilter.create(commands, Slots.identifier(identifierPrefix, i), slotParam, strategy));
        }
    }

    <C extends RedisKeyAsyncCommands<String, ?> & RedisStringAsyncCommands<String, ?>>
    RedisSlotAsyncBloomFilter(C commands,
                              String identifierPrefix,
                              SlotStrategy slotStrategy,
                              BloomFilterHashStrategy strategy) {
        this.strategy = strategy;

        this.slotStrategy = slotStrategy;

        this.slots = new ArrayList<>(slotStrategy.slotNum());
        for (int i = 0; i < slotStrategy.slotNum(); i++) {
            BloomFilterParam slotParam =
                BloomFilterParam.of(slotStrategy.param().getNumOfItems() / slotStrategy.slotNum(),
                    slotStrategy.param().getFalsePositiveProbability());
            this.slots.add(RedisAsyncBloomFilter.create(commands, Slots.identifier(identifierPrefix, i), slotParam, strategy));
        }
    }

}
