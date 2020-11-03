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

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.Iterator;
import java.util.PrimitiveIterator;
import java.util.PrimitiveIterator.OfInt;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import app.keve.hdf5io.api.datatype.HDF5Bitfield;

public abstract class AbstractBitfieldAdapter extends AbstractDatatypeAdapter<HDF5Bitfield> {

    private AbstractBitfieldAdapter(final HDF5Bitfield datatype) {
        super(datatype);
    }

    @Override
    public IntStream asIntStream(final ByteBuffer buf) {
        final PrimitiveIterator.OfInt ofIntP = new BufferToIteratorUtil.UByteBufferToIntIterator(buf);
        final Spliterator.OfInt ofIntS = Spliterators.spliterator(ofIntP, buf.remaining(), Spliterator.ORDERED);
        return StreamSupport.intStream(ofIntS, false);
    }

    private static class BitfieldAdapter8 extends AbstractBitfieldAdapter {
        private final int shift;
        private final int mask;
        private final int precision;

        BitfieldAdapter8(final HDF5Bitfield datatype) {
            super(datatype);
            this.shift = datatype.getBitOffset();
            this.mask = (1 << datatype.getBitPrecision()) - 1;
            this.precision = datatype.getBitPrecision();
        }

        @Override
        public Type getJavaType() {
            return BitSet.class;
        }

        @Override
        public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
            final BitSet bs = BitSet.valueOf(buf.duplicate().position(byteOffset).limit(byteOffset + 1));
            return bs.get(shift, shift + precision);
        }

        @Override
        public Iterator<?> asIterator(final ByteBuffer buf) {
            return new Bitfield8Iterator(buf);
        }

        @Override
        public <T> Iterator<T> asIterator(final Class<T> tClass, final ByteBuffer buf) {
            if (BitSet.class.isAssignableFrom(tClass) || Object.class.isAssignableFrom(tClass)) {
                return (Iterator<T>) new Bitfield8Iterator(buf);
            }
            if (Integer.TYPE == tClass || Integer.class.isAssignableFrom(tClass)) {
                return (Iterator<T>) asIntIterator(buf);
            }
            throw new IllegalArgumentException();
        }

        @Override
        public OfInt asIntIterator(final ByteBuffer buf) {
            final OfInt rawIt = super.asIntIterator(buf);
            return new OfInt() {

                @Override
                public boolean hasNext() {
                    return rawIt.hasNext();
                }

                @Override
                public int nextInt() {
                    return rawIt.nextInt() >> shift & mask;
                }
            };
        }

        private class Bitfield8Iterator implements Iterator<BitSet> {
            private final ByteBuffer buf;
            private final int end;
            private int ofs;

            Bitfield8Iterator(final ByteBuffer buf) {
                this.buf = buf.duplicate();
                this.ofs = buf.position();
                this.end = buf.limit();
            }

            @Override
            public boolean hasNext() {
                return ofs < end;
            }

            @Override
            public BitSet next() {
                final BitSet bs = BitSet.valueOf(buf.position(ofs).limit(ofs + 1));
                ofs++;
                return bs.get(shift, shift + precision);
            }

        }

        @Override
        public String toString() {
            return "Bitfield8";
        }
    }

    public static AbstractBitfieldAdapter forType(final HDF5Bitfield bitfieldType) {
        switch (bitfieldType.getElementSize()) {
        case 1:
            return new BitfieldAdapter8(bitfieldType);
        default:
            throw new IllegalArgumentException("Implement bitfield adapter for " + bitfieldType);
        }
    }
}
