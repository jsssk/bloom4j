package io.github.jsssk.bf.sync.file;

import io.github.jsssk.bf.sync.BitMap;
import io.github.jsssk.bf.util.BitUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

public class FileBitMap implements BitMap, Externalizable {

    Path path;

    FileChannel w;

    MappedByteBuffer read;
    MappedByteBuffer write;


    // size of bits.
    long size;

    int batch = 1 << 20;

    public FileBitMap(Path path) {
        File f = path.toFile();
        if (!f.exists()) {
            throw new RuntimeException(path + " not exists.");
        }
        this.path = path;
        size = f.length() << 3;
        try {
            long fileSize = size >> 3;
            w = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
            if (w.size() < fileSize) {
                w.position(fileSize);
                w.write(ByteBuffer.wrap(new byte[]{0}));
            }
            read = w.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
            write = w.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public FileBitMap(Path path, long size) {
        if ((size & 0x7) > 0) {
            throw new IllegalArgumentException("size must be a multiple of 8.");
        }
        this.size = size;
        this.path = path;
        init();
    }

    public FileBitMap() {
    }


    private void init() {

        File f = path.toFile();
        if (! f.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                f.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            long fileSize = size >> 3; // in bytes
            w = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
            if (w.size() < fileSize) {
                w.position(fileSize - 1);
                w.write(ByteBuffer.wrap(new byte[]{0}));
            }
            read = w.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
            write = w.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public void delete() {
        File file = path.toFile();
        if (file.delete()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void clear() {
        delete();
        init();
    }

    @Override
    public void set(long index) {
        try {
            byte b;
            long i = index >> 3;

            if (w.size() < i) {
                b = 0;
            } else {
                b = read.get((int) i);
            }

            long remainder = index & 0x7;
            b = BitUtils.setBit(b, (int) remainder);
            write.put((int) i, b);
            if (i >= size) {
                size = w.size();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean get(long index) {
        boolean res;
        try {
            byte b;
            long i = index >> 3;
            long remainder = index & 0x7;

            b = read.get((int) i);
            res = BitUtils.getBit(b, (int) remainder) > 0;
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(long i, byte[] bytes) {
        try {
            w.position(i);
            w.write(ByteBuffer.wrap(bytes));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void merge(long i, byte[] bytes) {
        try {
            for (int j = 0; j < bytes.length; j ++) {
                byte origin = write.get((int) i + j);
                bytes[j] = (byte) (origin | bytes[j]);
            }
            w.position(i);
            w.write(ByteBuffer.wrap(bytes));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void read(long i, byte[] bytes) {
        try {
            w.position(i);
            ByteBuffer bf = ByteBuffer.allocate(bytes.length);
            w.read(bf);
            System.arraycopy(bf.array(), 0, bytes, 0, bytes.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream openInputStream() {
        write.force();
        try {
            return new FileInputStream(path.toFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterator<byte[]> iterator() {
        write.force();
        return new Itr(batch);
    }

    // just for serializing
    // start
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(size);
        out.writeInt(batch);

        long remaining = size >> 3;
        int step = 1 << 15;
        w.position(0);
        while (remaining > 0) {
            ByteBuffer buffer = ByteBuffer.allocate(step);
            int byteSize = w.read(buffer);
            remaining -= byteSize;
            out.write(buffer.array());
        }
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        size = in.readLong();
        batch = in.readInt();
        long remaining = size >> 3;
        path = Files.createTempFile("bf", "file-bitmap");
        FileChannel wChannel = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
        int step = 512;
        byte[] buffer = new byte[step];
        while (remaining > 0) {
            int byteSize = in.read(buffer);
            remaining -= byteSize;
            wChannel.write(ByteBuffer.wrap(buffer, 0, byteSize));
        }
        init();
    }

    // just for serializing
    // end

    private class Itr implements Iterator<byte[]> {
        int cursor = 0;       // index of next byte to return
        int batch;            // batch size in bits

        Itr(int batch) {
            this.batch = batch;
        }

        @Override
        public boolean hasNext() {
            return (size >> 3) > cursor;
        }

        @Override
        public byte[] next() {
            try {
                w.position(cursor);
                int offset = batch >> 3;// in bytes
                int sizeInBytes = (int) (size >> 3);
                offset = cursor + offset > sizeInBytes ? sizeInBytes - cursor : offset;
                ByteBuffer buffer = ByteBuffer.allocate(offset);
                w.read(buffer);
                cursor += offset;
                return buffer.array();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
