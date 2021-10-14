package io.github.jsssk.bf.sync.redis;

import io.github.jsssk.bf.BloomFilterHashStrategy;
import io.github.jsssk.bf.BloomFilterParam;
import io.github.jsssk.bf.slot.DefaultSlotStrategy;
import io.github.jsssk.bf.slot.SlotStrategy;
import io.github.jsssk.bf.sync.DefaultSlotBloomFilter;
import io.lettuce.core.api.sync.RedisKeyCommands;
import io.lettuce.core.api.sync.RedisStringCommands;

import java.util.ArrayList;

public class RedisSlotBloomFilter extends DefaultSlotBloomFilter {

    public static <C extends RedisKeyCommands<String, ?> & RedisStringCommands<String, ?>>
    RedisSlotBloomFilter create(C commands, String prefix, long n, double fpp, BloomFilterHashStrategy strategy) {
        return new RedisSlotBloomFilter(commands, prefix, n, fpp, strategy);
    }

    public static <C extends RedisKeyCommands<String, ?> & RedisStringCommands<String, ?>>
    RedisSlotBloomFilter create(C commands, String prefix, SlotStrategy slotStrategy, BloomFilterHashStrategy strategy) {
        return new RedisSlotBloomFilter(commands, prefix, slotStrategy,strategy);
    }

    <C extends RedisKeyCommands<String, ?> & RedisStringCommands<String, ?>>
    RedisSlotBloomFilter(C commands, String prefix, long n, double fpp, BloomFilterHashStrategy strategy) {
        this.strategy = strategy;

        this.slotStrategy = DefaultSlotStrategy.of(n, fpp);

        this.slots = new ArrayList<>(slotStrategy.slotNum());
        for (int i = 0; i < slotStrategy.slotNum(); i++) {
            BloomFilterParam slotParam =
                BloomFilterParam.of(n / slotStrategy.slotNum(), fpp);
            this.slots.add(RedisBloomFilter.create(
                commands, prefix + "-" + i, slotParam, strategy));
        }
    }

    <C extends RedisKeyCommands<String, ?> & RedisStringCommands<String, ?>>
    RedisSlotBloomFilter(C commands, String prefix, SlotStrategy slotStrategy, BloomFilterHashStrategy strategy) {
        this.strategy = strategy;

        this.slotStrategy = slotStrategy;

        this.slots = new ArrayList<>(slotStrategy.slotNum());

        for (int i = 0; i < slotStrategy.slotNum(); i++) {
            BloomFilterParam slotParam = BloomFilterParam.of(
                slotStrategy.param().getNumOfItems() / slotStrategy.slotNum(),
                slotStrategy.param().getFalsePositiveProbability());
            this.slots.add(RedisBloomFilter.create(
                commands, prefix + "-" + i, slotParam, strategy));
        }
    }

}
