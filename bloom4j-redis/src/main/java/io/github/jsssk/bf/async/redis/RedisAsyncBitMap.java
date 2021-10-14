package io.github.jsssk.bf.async.redis;


import io.github.jsssk.bf.async.AsyncBitMap;
import io.lettuce.core.BitFieldArgs;
import io.lettuce.core.api.async.RedisKeyAsyncCommands;
import io.lettuce.core.api.async.RedisStringAsyncCommands;

import java.util.function.BiConsumer;

public class RedisAsyncBitMap
    <C extends RedisKeyAsyncCommands<String, ?> & RedisStringAsyncCommands<String, ?>>
    implements AsyncBitMap {

    final C commands;

    final long size;

    final String identifier;

    public RedisAsyncBitMap(String identifier, C commands, long size) {
        this.commands = commands;
        this.size = size;
        this.identifier = identifier;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public void clear(BiConsumer<Void, Throwable> callback) {
        delete(callback);
    }

    @Override
    public void delete(BiConsumer<Void, Throwable> callback) {
        commands.del(identifier).whenComplete((a, t) -> callback.accept(null, t));
    }

    @Override
    public void set(long index, BiConsumer<Void, Throwable> callback) {
        commands.setbit(identifier, index, 1).whenComplete((a, t) -> callback.accept(null, t));
    }

    @Override
    public void get(long index, BiConsumer<Boolean, Throwable> callback) {
        commands.getbit(identifier, index).whenComplete((a, t) -> {
            if (t != null) {
                callback.accept(null, t);
            } else {
                callback.accept(a > 0, null);
            }
        });
    }

    BitFieldArgs.BitFieldType oneBitType = BitFieldArgs.unsigned(1);

    @Override
    public void set(long[] indexes, BiConsumer<Void, Throwable> callback) {
        BitFieldArgs args = new BitFieldArgs();
        for (long index : indexes) {
            args.set(oneBitType, Math.toIntExact(index), 1);
        }
        commands.bitfield(identifier, args).whenComplete((list, t) -> callback.accept(null, t));
    }

    @Override
    public void allMatch(long[] indexes, BiConsumer<Boolean, Throwable> callback) {
        BitFieldArgs args = new BitFieldArgs();
        for (long index : indexes) {
            args.get(oneBitType, Math.toIntExact(index));
        }
        commands.bitfield(identifier, args).whenComplete((list, t) -> {
            if (t != null) {
                callback.accept(null, t);
            } else {
                boolean res = true;
                for (long i : list) {
                    if (i == 0) {
                        res = false;
                        break;
                    }
                }
                callback.accept(res, null);
            }
        });
    }
}

