package io.github.jsssk.bf.sync.mem;

import io.github.jsssk.bf.sync.BitMap;
import io.github.jsssk.bf.util.BitUtils;

import java.io.Serializable;
import java.util.Iterator;

public class InMemoryBitMap implements BitMap, Serializable {

    final byte[] bits;
    long size;
    int byteSize;

    public InMemoryBitMap(long size) {
        if (size > ((1L << 31)  -1)) {
            throw new RuntimeException("too long");
        }
        this.size = size;
        byteSize = (int) (size >> 3);
        bits = new byte[byteSize];
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public void delete() {
        throw new RuntimeException("not supported.");
    }

    @Override
    public void clear() {
        for (int i = 0; i < byteSize; i ++) {
            bits[i] = 0;
        }
    }

    @Override
    public void set(long index) {
        int byteIndex = (int) (index >> 3);
        int remainder = (int) (index & 7);
        bits[byteIndex] = BitUtils.setBit(bits[byteIndex], remainder);
    }

    @Override
    public boolean get(long index) {
        int byteIndex = (int) (index >> 3);
        int remainder = (int) (index & 7);
        return BitUtils.getBit(bits[byteIndex], remainder) == 1;
    }

    @Override
    public void write(long i, byte[] bytes) {
        if (i + bytes.length > bits.length) {
            throw new IllegalArgumentException("out of bound");
        }
        System.arraycopy(bytes, 0 , bits, (int) i, bytes.length);
    }
    @Override
    public void merge(long i, byte[] bytes) {
        if (i + bytes.length > bits.length) {
            throw new IllegalArgumentException("out of bound");
        }
        for (int j = 0; j < bytes.length; j ++) {
            bits[(int)i + j] = (byte) (bits[(int)i + j] | bytes[j]);
        }
    }

    @Override
    public void read(long i, byte[] bytes) {
        if (i + bytes.length > bits.length) {
            throw new IllegalArgumentException("out of bound");
        }
        System.arraycopy(bits, (int) i, bytes, 0, bytes.length);
    }


    @Override
    public Iterator<byte[]> iterator() {
        return new Itr();
    }


    class Itr implements Iterator<byte[]> {
        int cursor = 0;
        int batch = 1 << 20;
        int byteSize;
        private Itr() {
            this.byteSize = (int) (size >> 3);
        }

        @Override
        public boolean hasNext() {
            return cursor < byteSize;
        }

        @Override
        public byte[] next() {
            byte[] res = new byte[cursor + batch > byteSize ? byteSize - cursor : batch];
            System.arraycopy(bits, cursor, res, 0, res.length);
            cursor += res.length;
            return res;
        }
    }
}
