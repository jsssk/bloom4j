package io.github.jsssk.bf.slot;

import com.google.common.hash.Hashing;
import io.github.jsssk.bf.BloomFilterParam;

import java.io.Serializable;

public class DefaultSlotStrategy implements SlotStrategy, Serializable {

    BloomFilterParam param;

    final int slotNum;

    final long slotSize = 1L << 31;

    public static DefaultSlotStrategy of(long n) {
        return of(n, 1e-4, 1L << 31);
    }

    public static DefaultSlotStrategy of(long n, double fpp) {
        return new DefaultSlotStrategy(n, fpp);
    }

    public static DefaultSlotStrategy of(long n, double fpp, long slotSize) {
        return new DefaultSlotStrategy(n, fpp, slotSize);
    }

    private DefaultSlotStrategy(long n, double fpp, long slotSize) {
        this.param = BloomFilterParam.of(n, fpp);
        this.slotNum = ((int)(param.getBitSize() / slotSize) + (param.getBitSize() % slotSize > 0 ? 1 : 0));
    }

    private DefaultSlotStrategy(long n, double fpp) {
        this.param = BloomFilterParam.of(n, fpp);
        this.slotNum = ((int) (param.getBitSize() >> 31))
            + ((0x7fffffff & param.getBitSize()) > 0 ? 1 : 0);
    }

    @Override
    public long slotSize() {
        return slotSize;
    }

    @Override
    public int slotNum() {
        return slotNum;
    }

    @Override
    public int slot(byte[] raw) {
        @SuppressWarnings("UnstableApiUsage")
        byte[] bytes = Hashing.murmur3_128().hashBytes(raw).asBytes();
        long hash =
              (bytes[5] & 0x7FL)  << 56
            | (bytes[6] & 0xFFL)  << 48
            | (bytes[7] & 0xFFL)  << 40
            | (bytes[8] & 0xFFL)  << 32
            | (bytes[9] & 0xFFL)  << 24
            | (bytes[10] & 0xFFL) << 16
            | (bytes[11] & 0xFFL) << 8
            | (bytes[12] & 0xFFL);
        return Math.toIntExact(hash % slotNum);
    }

    @Override
    public BloomFilterParam param() {
        return param;
    }
}
