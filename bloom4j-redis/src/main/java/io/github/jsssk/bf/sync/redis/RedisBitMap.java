package io.github.jsssk.bf.sync.redis;

import io.github.jsssk.bf.sync.BitMap;
import io.lettuce.core.BitFieldArgs;
import io.lettuce.core.api.sync.RedisKeyCommands;
import io.lettuce.core.api.sync.RedisStringCommands;

import java.util.Iterator;
import java.util.List;

public class RedisBitMap
    <C extends RedisKeyCommands<String, ?> & RedisStringCommands<String, ?>>
    implements BitMap {

    /**
     * redis key
     */
    final String identifier;

    /**
     * redis commands
     */
    final C commands;

    /**
     * pre-defined size in bits
     */
    final long size;

    /**
     * 512 Kb
     */
    int batch = 2 << 19;


    RedisBitMap(String identifier, C commands, long size) {
        this.identifier = identifier;
        this.commands = commands;
        this.size = size;
    }

    RedisBitMap(String identifier, C commands, long size, int batch) {
        this.identifier = identifier;
        this.commands = commands;
        this.size = size;
        this.batch = batch;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public void delete() {
        commands.del(identifier);
    }

    @Override
    public void clear() {
        delete();
    }

    @Override
    public void set(long index) {
        commands.setbit(identifier, index, 1);
    }

    @Override
    public boolean get(long index) {
        return commands.getbit(identifier, index) > 0;
    }

    public void setByte(long i, byte b) {
        long index = i << 3;
        for (int j = 0; j < 8; j++) {
            commands.setbit(identifier, index + j, ((b >>> (7 - j)) & 0x1));
        }
    }

    public byte getByte(long i) {
        long index = i << 3;
        byte res = 0;

        for (int j = 0; j < 8; j++) {
            if (commands.getbit(identifier, index + j) > 0) {
                res = (byte) (res | (0x1 << (7 - j)));
            }
        }
        return res;
    }

    BitFieldArgs.BitFieldType oneBitType = BitFieldArgs.unsigned(1);
    BitFieldArgs.BitFieldType eightBitType = BitFieldArgs.signed(8);

    @Override
    public void set(long[] indexes) {
        BitFieldArgs args = new BitFieldArgs();
        for (long index : indexes) {
            args.set(oneBitType, Math.toIntExact(index), 1);
        }
        commands.bitfield(identifier, args);
    }

    @Override
    public boolean allMatch(long[] indexes) {
        BitFieldArgs args = new BitFieldArgs();
        for (long index : indexes) {
            args.get(oneBitType, Math.toIntExact(index));
        }
        return commands.bitfield(identifier, args).stream().allMatch(a -> a > 0);
    }


    /**
     * @param i     first index of byte
     * @param bytes data
     */
    @Override
    public void write(long i, byte[] bytes) {
        BitFieldArgs args = new BitFieldArgs();
        i = i << 3;
        for (byte b : bytes) {
            if (b != 0) {
                args.set(eightBitType, Math.toIntExact(i), b);
            }
            i += 8;
            if (i % (1 << 20) == 0) {
                commands.bitfield(identifier, args);
                args = new BitFieldArgs();
            }
        }
        commands.bitfield(identifier, args);
    }

    @Override
    public void merge(long i, byte[] bytes) {

        BitFieldArgs readArgs = new BitFieldArgs();
        long index = i << 3;
        int readIndex = 0;
        for (int j = 0; j < bytes.length; j ++) {
            readArgs.get(eightBitType, (int) index);
            index += 8;
            if (index % (1 << 20) == 0) {
                List<Long> res = commands.bitfield(identifier, readArgs);
                for (long b : res) {
                    bytes[readIndex] = (byte) (bytes[readIndex] | b);
                }
                readArgs = new BitFieldArgs();
            }
        }
        List<Long> res = commands.bitfield(identifier, readArgs);
        for (long b : res) {
            bytes[readIndex] = (byte) (bytes[readIndex] | b);
        }
        write(i, bytes);
    }

    @Override
    public void read(long i, byte[] bytes) {
        BitFieldArgs args = new BitFieldArgs();
        i = i << 3;
        for (int j = 0; j < bytes.length; j++) {
            args.get(eightBitType, Math.toIntExact(i));
            i += 8;
        }
        List<Long> res = commands.bitfield(identifier, args);
        int j = 0;
        for (long value : res) {
            bytes[j] = (byte) value;
        }
    }

    @Override
    public Iterator<byte[]> iterator() {
        return new Itr(batch);
    }

    private class Itr implements Iterator<byte[]> {
        int cursor = 0;       // index of next bytes to return
        int batch;            // batch size in bits

        Itr(int batch) {
            this.batch = batch;
        }

        @Override
        public boolean hasNext() {
            return size >> 3 != cursor;
        }

        @Override
        public byte[] next() {
            BitFieldArgs args = new BitFieldArgs();
            int sizeInBytes = (int) (size >> 3);
            int offset = Math.min(batch >> 3, sizeInBytes - cursor); // in bytes
            int bitCursor = cursor << 3; // in bits
            byte[] res = new byte[offset];
            int resIdx = 0;
            int i = 0;
            while (i < offset) {
                args.get(eightBitType, bitCursor);

                if (i % 1000 == 0) {
                    List<Long> bytes = commands.bitfield(identifier, args);
                    for (long b : bytes) {
                        res[resIdx ++] = (byte) b;
                    }
                    args = new BitFieldArgs();
                }
                i ++;
                bitCursor += 8;
            }
            List<Long> bytes = commands.bitfield(identifier, args);
            for (long b : bytes) {
                res[resIdx ++] = (byte) b;
            }
            cursor += offset;
            return res;
        }
    }
}
