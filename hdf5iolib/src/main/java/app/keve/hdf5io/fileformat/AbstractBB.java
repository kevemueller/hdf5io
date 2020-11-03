/*
 * Copyright 2020 Keve Müller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.keve.hdf5io.fileformat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.keve.hdf5io.fileformat.level1.FractalHeap;
import app.keve.hdf5io.fileformat.level1.LocalHeap;

public abstract class AbstractBB<T extends H5Context> {
    protected final T context;
    private final Logger logger = LoggerFactory.getLogger(AbstractBB.class);
    private final ByteBuffer buf;

    protected AbstractBB(final ByteBuffer buf, final T context) {
        this.buf = buf;
        this.context = Objects.requireNonNull(context);
        assert 0 == buf.position();
        assert ByteOrder.LITTLE_ENDIAN.equals(buf.order());
    }

    public final T context() {
        return context;
    }

    public final void resize() {
        buf.limit((int) size());
    }

    /**
     * Pack the contents of this element to its smallest size.
     */
    public void pack() {
        resize();
    }

    protected final int available() {
        return buf.remaining();
    }

    protected final int align8Bytes(final int idx) {
        return (idx + 7 & ~7) - idx;
    }

    public abstract long size();

    // FIXME: Debug purposes only
    @Deprecated
    public final ByteBuffer getBuffer() {
        return buf.asReadOnlyBuffer();
    }

    protected final byte getByte(final int idx) {
        return buf.get(idx);
    }

    protected final void setByte(final int idx, final int value) {
        buf.put(idx, (byte) value);
    }

    protected final int getUnsignedByte(final int idx) {
        return Byte.toUnsignedInt(buf.get(idx));
    }

    protected final short getShort(final int idx) {
        return buf.getShort(idx);
    }

    protected final void setShort(final int idx, final int value) {
        buf.putShort(idx, (short) value);
    }

    protected final int getUnsignedShort(final int idx) {
        return Short.toUnsignedInt(buf.getShort(idx));
    }

    protected final void setUnsignedShort(final int idx, final int value) {
        buf.putShort(idx, (short) value);
    }

    protected final int getInt(final int idx) {
        return buf.getInt(idx);
    }

    protected final void setInt(final int idx, final int value) {
        buf.putInt(idx, value);
    }

    protected final long getUnsignedInt(final int idx) {
        return Integer.toUnsignedLong(buf.getInt(idx));
    }

    // accept [0..Integer.MAX_VALUE]
    protected final int getSmallUnsignedInt(final int idx) {
        final int i = buf.getInt(idx);
        assert i >= 0;
        return i;
    }

    protected final void setUnsignedInt(final int idx, final long value) {
        assert value >= 0L && value <= 0xFFFFFFFFL;
        buf.putInt(idx, (int) value);
    }

    protected final long getLong(final int idx) {
        return buf.getLong(idx);
    }

    protected final void setLong(final int idx, final long value) {
        buf.putLong(idx, value);
    }

    protected final long getUnsignedNumber(final int idx, final int sizeInBytes) {
        switch (sizeInBytes) {
        case 1:
            return Byte.toUnsignedLong(buf.get(idx));
        case 2:
            return Short.toUnsignedLong(buf.getShort(idx));
        case 3:
            return Short.toUnsignedLong(buf.getShort(idx)) | Byte.toUnsignedLong(buf.get(idx + 2)) << 16;
        case 4:
            return Integer.toUnsignedLong(buf.getInt(idx));
        case 5:
            return Integer.toUnsignedLong(buf.getShort(idx)) | Byte.toUnsignedLong(buf.get(idx + 4)) << 32;
        case 6:
            return Integer.toUnsignedLong(buf.getShort(idx)) | Short.toUnsignedLong(buf.get(idx + 4)) << 32;
        case 7:
            return Integer.toUnsignedLong(buf.getShort(idx)) | Short.toUnsignedLong(buf.get(idx + 4)) << 32
                    | Byte.toUnsignedLong(buf.get(idx + 6)) << 48;
        case 8:
            return buf.getLong(idx);
        default:
            throw new IllegalArgumentException("Unsupported size:" + sizeInBytes);
        }
    }

    protected final void setUnsignedNumber(final int idx, final int sizeInBytes, final long value) {
        switch (sizeInBytes) {
        case 1:
            setByte(idx, (int) value);
            break;
        case 2:
            setShort(idx, (int) value);
            break;
        case 3:
            setShort(idx, (int) value);
            setByte(idx + 2, (int) (value >>> 16));
            break;
        case 4:
            setInt(idx, (int) value);
            break;
        case 8:
            setLong(idx, value);
            break;
        default:
            throw new IllegalArgumentException("Unsupported size:" + sizeInBytes);
        }
    }

    protected final byte[] getBytes(final int idx, final int num) {
        final byte[] bytes = new byte[num];
        for (int i = 0; i < num; i++) {
            bytes[i] = buf.get(idx + i);
        }
        return bytes;
    }

    protected final byte[] getBytes8(final int idx) {
        return getBytes(idx, 8);
    }

    protected final void setBytes(final int idx, final byte[] value) {
        final int end = idx + value.length;
        for (int i = idx, j = 0; i < end; i++) {
            buf.put(i, value[j++]);
        }
    }

    protected final ByteBuffer getEmbeddedData(final int idx, final int length) {
        return buf.duplicate().position(idx).limit(idx + length);
    }

    protected final void setEmbeddedData(final int idx, final int length, final ByteBuffer value) {
        buf.duplicate().position(idx).limit(idx + length).put(value);
    }

    protected final <E extends H5Object<W>, W extends H5Context> E getEmbedded(final int idx, final int length,
            final Class<E> eClass, final W contextW) {
        logger.trace("getEmbedded: {}:{} of {}", idx, length, eClass.getSimpleName());
        final ByteBuffer ebuf = buf.duplicate().position(idx).slice().order(ByteOrder.LITTLE_ENDIAN);
        if (0 != length) {
            ebuf.limit(length);
        }
        final BiFunction<ByteBuffer, W, E> of = context.h5Factory().of(eClass);
        final E t = of.apply(ebuf, contextW);
        if (t.size() < 0) {
            throw new IllegalArgumentException(
                    "Calculated size " + t.size() + " of " + t.getClass() + " for " + eClass + " invalid");
        }
        if (t.size() > ebuf.capacity()) {
            throw new IllegalArgumentException("Calculated size " + t.size() + " of " + t.getClass() + " for " + eClass
                    + " exceed buffer capacity of " + ebuf.capacity());
        }
        ebuf.limit((int) t.size());
        return t;
    }

    /**
     * Instantiate an embedded H5Object instance directly.
     * 
     * @deprecated Use the Class&lt;E&gt; version that enforces h5Factory.
     * @param <E>      the H5Object type
     * @param <W>      the H5Context type
     * @param idx      the index into the buffer
     * @param length   the length of the embedded type, or 0 if unknown
     * @param of       the constructor function
     * @param contextW the context instance
     * @return the instantiated instance
     */
    @Deprecated
    private <E extends H5Object<W>, W extends H5Context> E getEmbedded(final int idx, final int length,
            final BiFunction<ByteBuffer, W, E> of, final W contextW) {
        final ByteBuffer ebuf = buf.duplicate().position(idx).slice().order(ByteOrder.LITTLE_ENDIAN);
        if (0 != length) {
            ebuf.limit(length);
        }
        final E t = of.apply(ebuf, contextW);
        ebuf.limit((int) t.size());
        return t;
    }

    protected final <E extends H5Object<T>> E getEmbedded(final int idx, final int length, final Class<E> eClass) {
        return getEmbedded(idx, length, eClass, context);
    }

    protected final <E extends H5Object<T>> E getEmbedded(final int idx, final Class<E> eClass) {
        return getEmbedded(idx, 0, eClass, context);
    }

    protected final void setEmbedded(final int idx, final AbstractBB<?> object) {
        final int length = (int) object.size();
        buf.duplicate().position(idx).limit(idx + length).put(object.buf.duplicate());
    }

    protected final Resolvable<String> getResolvableHeapString(final Resolvable<? extends LocalHeap> heap,
            final long stringOffset) {
        return context.h5Factory().resolvable(heap, stringOffset);
    }

    protected final <E extends H5Object<W>, W extends H5Context> Resolvable<E> getResolvable(
            final Resolvable<FractalHeap> fractalHeap, final ByteBuffer embeddedData, final Class<E> tClass,
            final W sizingContext) {
        return context.h5Factory().resolvable(fractalHeap, embeddedData, tClass, sizingContext);
    }

    protected final <V extends H5Object<T>> Iterator<V> getIterator(final int idx, final Class<V> vClass,
            final int maxNumber) {
        return getIterator(idx, vClass, maxNumber, context);
    }

    @Deprecated
    protected final <V extends H5Object<T>> Iterator<V> getIterator(final int idx,
            final BiFunction<ByteBuffer, T, V> of, final int maxNumber) {
        return getIterator(idx, of, maxNumber, context);
    }

    protected final <V extends H5Object<W>, W extends H5Context> Iterator<V> getIterator(final int idx,
            final Class<V> vClass, final int maxNumber, final W sizingContextW) {
        return new Iterator<>() {
            private int i;
            private int offset = idx;

            @Override
            public boolean hasNext() {
                return i < maxNumber;
            }

            @Override
            public V next() {
                logger.trace("Iterator:next {}:{}", i, offset);
                final V next = getEmbedded(offset, 0, vClass, sizingContextW);
                offset += next.size();
                i++;
                return next;
            }
        };
    }

    @Deprecated
    private <V extends H5Object<W>, W extends H5Context> Iterator<V> getIterator(final int idx,
            final BiFunction<ByteBuffer, W, V> of, final int maxNumber, final W sizingContextW) {
        return new Iterator<>() {
            private int i;
            private int offset = idx;

            @Override
            public boolean hasNext() {
                return i < maxNumber;
            }

            @Override
            public V next() {
                final V next = getEmbedded(offset, 0, of, sizingContextW);
                offset += next.size();
                i++;
                return next;
            }
        };
    }

    protected final <V extends H5Object<W>, W extends H5Context> Iterator<V> getIteratorByteSized(final int idx,
            final Class<V> vClass, final long maxBytes, final int minSize, final W contextW) {
        return new Iterator<>() {
            private final int end = idx + (int) maxBytes;
            private int offset = idx;

            @Override
            public boolean hasNext() {
                return offset + minSize < end;
            }

            @Override
            public V next() {
                final V next = getEmbedded(offset, 0, vClass, contextW);
                offset += next.size();
                assert offset <= end;
                return next;
            }
        };
    }

    protected final <V extends H5Object<T>> Iterator<V> getIteratorByteSized(final int idx, final Class<V> vClass,
            final long maxBytes, final int minSize) {
        return new Iterator<>() {
            private final int end = idx + (int) maxBytes;
            private int offset = idx;

            @Override
            public boolean hasNext() {
                return offset + minSize < end;
            }

            @Override
            public V next() {
                final V next = getEmbedded(offset, 0, vClass, context);
                offset += next.size();
                assert offset <= end : "iterator " + offset + " > " + end;
                return next;
            }
        };
    }

    protected final <V extends H5Object<T>> Iterator<Map.Entry<Integer, V>> getOffsetIteratorByteSized(final int idx,
            final Class<V> vClass, final long maxBytes, final int minSize) {
        return new Iterator<>() {
            private final int end = idx + (int) maxBytes;
            private int offset = idx;

            @Override
            public boolean hasNext() {
                return offset + minSize < end;
            }

            @Override
            public Entry<Integer, V> next() {
                final V next = getEmbedded(offset, 0, vClass, context);
                final Entry<Integer, V> e = Map.entry(offset, next);
                offset += next.size();
                assert offset <= end;
                return e;
            }
        };
    }

    protected final CharSequence getString(final int idx, final int length, final Charset charset) {
        return charset.decode(getEmbeddedData(idx, length));
    }

    protected final CharSequence getAsciiString(final int idx, final int length) {
        return getString(idx, length, StandardCharsets.US_ASCII);
    }

    protected final CharSequence getAsciiNulString(final int idx) {
        int offset = idx;
        while (0 != buf.get(offset)) {
            offset++;
        }
        final ByteBuffer sbuf = buf.duplicate().position(idx).limit(offset);
        return StandardCharsets.US_ASCII.decode(sbuf);
    }

    protected final CharSequence getAsciiNulString(final int idx, final int maxLength) {
        int offset = idx;
        final int end = offset + maxLength;
        while (offset < end && 0 != buf.get(offset)) {
            offset++;
        }
        final ByteBuffer sbuf = buf.duplicate().position(idx).limit(offset);
        return StandardCharsets.US_ASCII.decode(sbuf);
    }

    @Override
    public String toString() {
        return String.format("%s [size()=%s, getBuffer()=%s]", getClass().getSimpleName(), size(), getBuffer());
    }
}
