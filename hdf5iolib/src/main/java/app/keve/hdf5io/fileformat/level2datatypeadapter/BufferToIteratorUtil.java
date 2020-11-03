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
package app.keve.hdf5io.fileformat.level2datatypeadapter;

import java.math.BigInteger;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Iterator;
import java.util.PrimitiveIterator;

import app.keve.hdf5io.util.OfFloat;

public final class BufferToIteratorUtil {
    private abstract static class AbstractBufferToIterator<B extends Buffer, T> implements Iterator<T> {
        protected final B buffer;

        AbstractBufferToIterator(final B buffer) {
            this.buffer = buffer;
        }

        @Override
        public final boolean hasNext() {
            return buffer.hasRemaining();
        }
    }

    public static final class ByteBufferToIntIterator extends AbstractBufferToIterator<ByteBuffer, Integer>
            implements PrimitiveIterator.OfInt {

        ByteBufferToIntIterator(final ByteBuffer byteBuffer) {
            super(byteBuffer);
        }

        @Override
        public int nextInt() {
            return buffer.get();
        }
    }

    public static final class ByteBufferToLongIterator extends AbstractBufferToIterator<ByteBuffer, Long>
            implements PrimitiveIterator.OfLong {

        ByteBufferToLongIterator(final ByteBuffer byteBuffer) {
            super(byteBuffer);
        }

        @Override
        public long nextLong() {
            return buffer.get();
        }
    }

    public static final class UByteBufferToIntIterator extends AbstractBufferToIterator<ByteBuffer, Integer>
            implements PrimitiveIterator.OfInt {

        public UByteBufferToIntIterator(final ByteBuffer byteBuffer) {
            super(byteBuffer);
        }

        @Override
        public int nextInt() {
            return Byte.toUnsignedInt(buffer.get());
        }
    }

    public static final class UByteBufferToLongIterator extends AbstractBufferToIterator<ByteBuffer, Long>
            implements PrimitiveIterator.OfLong {

        UByteBufferToLongIterator(final ByteBuffer byteBuffer) {
            super(byteBuffer);
        }

        @Override
        public long nextLong() {
            return Byte.toUnsignedLong(buffer.get());
        }
    }

    public static final class ShortBufferToIntIterator extends AbstractBufferToIterator<ShortBuffer, Integer>
            implements PrimitiveIterator.OfInt {

        public ShortBufferToIntIterator(final ShortBuffer shortBuffer) {
            super(shortBuffer);
        }

        @Override
        public int nextInt() {
            return buffer.get();
        }
    }

    public static final class ShortBufferToLongIterator extends AbstractBufferToIterator<ShortBuffer, Long>
            implements PrimitiveIterator.OfLong {

        public ShortBufferToLongIterator(final ShortBuffer shortBuffer) {
            super(shortBuffer);
        }

        @Override
        public long nextLong() {
            return buffer.get();
        }
    }

    public static final class UShortBufferToIntIterator extends AbstractBufferToIterator<ShortBuffer, Integer>
            implements PrimitiveIterator.OfInt {

        public UShortBufferToIntIterator(final ShortBuffer shortBuffer) {
            super(shortBuffer);
        }

        @Override
        public int nextInt() {
            return Short.toUnsignedInt(buffer.get());
        }
    }

    public static final class UShortBufferToLongIterator extends AbstractBufferToIterator<ShortBuffer, Long>
            implements PrimitiveIterator.OfLong {

        public UShortBufferToLongIterator(final ShortBuffer shortBuffer) {
            super(shortBuffer);
        }

        @Override
        public long nextLong() {
            return Short.toUnsignedLong(buffer.get());
        }
    }

    public static final class IntBufferToIntIterator extends AbstractBufferToIterator<IntBuffer, Integer>
            implements PrimitiveIterator.OfInt {

        IntBufferToIntIterator(final IntBuffer intBuffer) {
            super(intBuffer);
        }

        @Override
        public int nextInt() {
            return buffer.get();
        }
    }

    public static final class IntBufferToLongIterator extends AbstractBufferToIterator<IntBuffer, Long>
            implements PrimitiveIterator.OfLong {

        public IntBufferToLongIterator(final IntBuffer intBuffer) {
            super(intBuffer);
        }

        @Override
        public long nextLong() {
            return buffer.get();
        }
    }

    public static final class UIntBufferToLongIterator extends AbstractBufferToIterator<IntBuffer, Long>
            implements PrimitiveIterator.OfLong {

        public UIntBufferToLongIterator(final IntBuffer intBuffer) {
            super(intBuffer);
        }

        @Override
        public long nextLong() {
            return Integer.toUnsignedLong(buffer.get());
        }
    }

    public static final class LongBufferToLongIterator extends AbstractBufferToIterator<LongBuffer, Long>
            implements PrimitiveIterator.OfLong {

        public LongBufferToLongIterator(final LongBuffer longBuffer) {
            super(longBuffer);
        }

        @Override
        public long nextLong() {
            return buffer.get();
        }
    }

    public static final class ULongBufferToBigIntegerIterator extends AbstractBufferToIterator<LongBuffer, BigInteger> {

        public ULongBufferToBigIntegerIterator(final LongBuffer longBuffer) {
            super(longBuffer);
        }

        @Override
        public BigInteger next() {
            final long l = buffer.get();
            if (l >= 0L) {
                return BigInteger.valueOf(l);
            } else {
                return BigInteger.valueOf(Integer.toUnsignedLong((int) (l >>> 32))).shiftLeft(32)
                        .or(BigInteger.valueOf(Integer.toUnsignedLong((int) l)));
            }
        }
    }

    public static final class FloatBufferToFloatIterator extends AbstractBufferToIterator<FloatBuffer, Float>
            implements OfFloat {

        public FloatBufferToFloatIterator(final FloatBuffer floatBuffer) {
            super(floatBuffer);
        }

        @Override
        public float nextFloat() {
            return buffer.get();
        }
    }

    public static final class DoubleBufferToDoubleIterator extends AbstractBufferToIterator<DoubleBuffer, Double>
            implements PrimitiveIterator.OfDouble {

        DoubleBufferToDoubleIterator(final DoubleBuffer doubleBuffer) {
            super(doubleBuffer);
        }

        @Override
        public double nextDouble() {
            return buffer.get();
        }
    }

    private BufferToIteratorUtil() {
    }
}
