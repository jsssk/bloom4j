package io.github.jsssk.bf;

import io.github.jsssk.bf.slot.DefaultSlotStrategy;
import io.github.jsssk.bf.slot.SlotStrategy;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class SlotsStrategyTest {

    @Test
    public void genSlotsStrategy() {
        SlotStrategy slotStrategy = DefaultSlotStrategy.of(1_000_000_000L);
        BloomFilterParam param = slotStrategy.param();
        System.out.println("bit size: " + param.getBitSize() + "");
        System.out.println("num of items: " + param.getNumOfItems() + "");
        System.out.println("num of hash: " + param.getNumOfHashFunctions() + "");
        System.out.println("false positive probablity: " + param.getFalsePositiveProbability() + "");
        System.out.println();
        System.out.println("slot number: " + slotStrategy.slotNum() + "");
        System.out.println("slot size: " + slotStrategy.slotSize() + "");
        System.out.println("slot of \"1\": " + slotStrategy.slot("1".getBytes(StandardCharsets.UTF_8)) + "");
    }


}
