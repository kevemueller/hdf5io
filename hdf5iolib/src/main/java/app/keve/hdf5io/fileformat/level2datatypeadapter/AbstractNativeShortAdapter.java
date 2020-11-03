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
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Iterator;
import java.util.PrimitiveIterator.OfInt;
import java.util.PrimitiveIterator.OfLong;

import app.keve.hdf5io.api.HDF5DatatypeAdapter;
import app.keve.hdf5io.api.datatype.HDF5Datatype;
import app.keve.hdf5io.api.datatype.HDF5FixedPointNumber;
import app.keve.hdf5io.fileformat.H5Context;
import app.keve.hdf5io.fileformat.level2datatype.FixedPointNumberBB;

public abstract class AbstractNativeShortAdapter extends AbstractNativeFixedPointNumberAdapter {
    protected AbstractNativeShortAdapter(final HDF5FixedPointNumber datatype) {
        super(datatype);
    }

    @Override
    public final ShortBuffer asShortBuffer(final ByteBuffer buf) {
        return buf.order(byteOrder).asShortBuffer();
    }

    @Override
    public final Iterator<?> asIterator(final ByteBuffer buf) {
        return asIntIterator(buf);
    }

    public static final class NativeShortBEAdapter extends AbstractNativeShortAdapter {
        public NativeShortBEAdapter(final HDF5FixedPointNumber datatype) {
            super(datatype);
        }

        @Override
        public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
            return buf.order(ByteOrder.BIG_ENDIAN).getShort(byteOffset);
        }

        @Override
        public Type getJavaType() {
            return Short.TYPE;
        }

        @Override
        public OfInt asIntIterator(final ByteBuffer buf) {
            final ShortBuffer shortBuffer = asShortBuffer(buf);
            return new BufferToIteratorUtil.ShortBufferToIntIterator(shortBuffer);
        }

        @Override
        public OfLong asLongIterator(final ByteBuffer buf) {
            final ShortBuffer shortBuffer = asShortBuffer(buf);
            return new BufferToIteratorUtil.ShortBufferToLongIterator(shortBuffer);
        }

        @Override
        public String toString() {
            return "FixedPointNumber:NativeShortBE";
        }

        public static HDF5DatatypeAdapter instance(final H5Context context) {
            final HDF5FixedPointNumber datatype = new FixedPointNumberBB.BuilderBB(context).withElementSize(2)
                    .withByteOrder(HDF5Datatype.HDF5ByteOrder.BIG_ENDIAN).signed().build();

            return new NativeShortBEAdapter(datatype);
        }
    }

    public static final class NativeShortLEAdapter extends AbstractNativeShortAdapter {
        public NativeShortLEAdapter(final HDF5FixedPointNumber datatype) {
            super(datatype);
        }

        @Override
        public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
            return buf.order(ByteOrder.LITTLE_ENDIAN).getShort(byteOffset);
        }

        @Override
        public Type getJavaType() {
            return Short.TYPE;
        }

        @Override
        public OfInt asIntIterator(final ByteBuffer buf) {
            final ShortBuffer shortBuffer = asShortBuffer(buf);
            return new BufferToIteratorUtil.ShortBufferToIntIterator(shortBuffer);
        }

        @Override
        public OfLong asLongIterator(final ByteBuffer buf) {
            final ShortBuffer shortBuffer = asShortBuffer(buf);
            return new BufferToIteratorUtil.ShortBufferToLongIterator(shortBuffer);
        }

        @Override
        public String toString() {
            return "FixedPointNumber:NativeShortLE";
        }

        public static HDF5DatatypeAdapter instance(final H5Context context) {
            final HDF5FixedPointNumber datatype = new FixedPointNumberBB.BuilderBB(context).withElementSize(2)
                    .withByteOrder(HDF5Datatype.HDF5ByteOrder.LITTLE_ENDIAN).signed().build();

            return new NativeShortLEAdapter(datatype);
        }
    }

    public static final class NativeUShortBEAdapter extends AbstractNativeShortAdapter {
        public NativeUShortBEAdapter(final HDF5FixedPointNumber datatype) {
            super(datatype);
        }

        @Override
        public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
            return Short.toUnsignedInt(buf.order(ByteOrder.BIG_ENDIAN).getShort(byteOffset));
        }

        @Override
        public Type getJavaType() {
            return Integer.TYPE;
        }

        @Override
        public OfInt asIntIterator(final ByteBuffer buf) {
            final ShortBuffer shortBuffer = asShortBuffer(buf);
            return new BufferToIteratorUtil.UShortBufferToIntIterator(shortBuffer);
        }

        @Override
        public OfLong asLongIterator(final ByteBuffer buf) {
            final ShortBuffer shortBuffer = asShortBuffer(buf);
            return new BufferToIteratorUtil.UShortBufferToLongIterator(shortBuffer);
        }

        @Override
        public String toString() {
            return "FixedPointNumber:NativeUShortBE";
        }

        public static HDF5DatatypeAdapter instance(final H5Context context) {
            final HDF5FixedPointNumber datatype = new FixedPointNumberBB.BuilderBB(context).withElementSize(2)
                    .withByteOrder(HDF5Datatype.HDF5ByteOrder.BIG_ENDIAN).unsigned().build();

            return new NativeUShortBEAdapter(datatype);
        }
    }

    public static final class NativeUShortLEAdapter extends AbstractNativeShortAdapter {
        public NativeUShortLEAdapter(final HDF5FixedPointNumber datatype) {
            super(datatype);
        }

        @Override
        public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
            return Short.toUnsignedInt(buf.order(ByteOrder.LITTLE_ENDIAN).getShort(byteOffset));
        }

        @Override
        public Type getJavaType() {
            return Integer.TYPE;
        }

        @Override
        public OfInt asIntIterator(final ByteBuffer buf) {
            final ShortBuffer shortBuffer = asShortBuffer(buf);
            return new BufferToIteratorUtil.UShortBufferToIntIterator(shortBuffer);
        }

        @Override
        public OfLong asLongIterator(final ByteBuffer buf) {
            final ShortBuffer shortBuffer = asShortBuffer(buf);
            return new BufferToIteratorUtil.UShortBufferToLongIterator(shortBuffer);
        }

        @Override
        public String toString() {
            return "FixedPointNumber:NativeUShortLE";
        }

        public static HDF5DatatypeAdapter instance(final H5Context context) {
            final HDF5FixedPointNumber datatype = new FixedPointNumberBB.BuilderBB(context).withElementSize(2)
                    .withByteOrder(HDF5Datatype.HDF5ByteOrder.LITTLE_ENDIAN).unsigned().build();
            return new NativeUShortLEAdapter(datatype);
        }
    }

    public static AbstractNativeShortAdapter forType(final HDF5FixedPointNumber fpn) {
        assert 2 == fpn.getElementSize();
        if (0 == fpn.getBitOffset() && 0 == fpn.getHiPadBit() && 0 == fpn.getLoPadBit()
                && fpn.getBitPrecision() == 8 * fpn.getElementSize()) {
            if (fpn.isSigned()) {
                switch (fpn.getByteOrder()) {
                case BIG_ENDIAN:
                    return new AbstractNativeShortAdapter.NativeShortBEAdapter(fpn);
                case LITTLE_ENDIAN:
                    return new AbstractNativeShortAdapter.NativeShortLEAdapter(fpn);
                default:
                    break;
                }
            } else {
                switch (fpn.getByteOrder()) {
                case BIG_ENDIAN:
                    return new AbstractNativeShortAdapter.NativeUShortBEAdapter(fpn);
                case LITTLE_ENDIAN:
                    return new AbstractNativeShortAdapter.NativeUShortLEAdapter(fpn);
                default:
                    break;
                }
            }
        }
        throw new IllegalArgumentException("No adapter for " + fpn);
    }
}
