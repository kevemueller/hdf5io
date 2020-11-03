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
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.PrimitiveIterator.OfInt;
import java.util.PrimitiveIterator.OfLong;

import app.keve.hdf5io.api.HDF5DatatypeAdapter;
import app.keve.hdf5io.api.datatype.HDF5Datatype;
import app.keve.hdf5io.api.datatype.HDF5FixedPointNumber;
import app.keve.hdf5io.fileformat.H5Context;
import app.keve.hdf5io.fileformat.level2datatype.FixedPointNumberBB;

public abstract class AbstractNativeIntAdapter extends AbstractNativeFixedPointNumberAdapter {
    protected AbstractNativeIntAdapter(final HDF5FixedPointNumber datatype) {
        super(datatype);
    }

    @Override
    public final IntBuffer asIntBuffer(final ByteBuffer buf) {
        return buf.order(byteOrder).asIntBuffer();
    }

    @Override
    public final OfInt asIntIterator(final ByteBuffer buf) {
        final IntBuffer intBuffer = asIntBuffer(buf);
        return new BufferToIteratorUtil.IntBufferToIntIterator(intBuffer);
    }

    public static final class NativeIntBEAdapter extends AbstractNativeIntAdapter {
        public NativeIntBEAdapter(final HDF5FixedPointNumber datatype) {
            super(datatype);
        }

        @Override
        public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
            return buf.order(ByteOrder.BIG_ENDIAN).getInt(byteOffset);
        }

        @Override
        public Type getJavaType() {
            return Integer.TYPE;
        }

        @Override
        public OfLong asLongIterator(final ByteBuffer buf) {
            final IntBuffer intBuffer = asIntBuffer(buf);
            return new BufferToIteratorUtil.IntBufferToLongIterator(intBuffer);
        }

        @Override
        public Iterator<?> asIterator(final ByteBuffer buf) {
            return asIntIterator(buf);
        }

        @Override
        public String toString() {
            return "FixedPointNumber:NativeIntBE";
        }

        public static HDF5DatatypeAdapter instance(final H5Context context) {
            final HDF5FixedPointNumber datatype = new FixedPointNumberBB.BuilderBB(context).withElementSize(4)
                    .withByteOrder(HDF5Datatype.HDF5ByteOrder.BIG_ENDIAN).signed().build();
            return new NativeIntBEAdapter(datatype);
        }
    }

    public static final class NativeIntLEAdapter extends AbstractNativeIntAdapter {
        public NativeIntLEAdapter(final HDF5FixedPointNumber datatype) {
            super(datatype);
        }

        @Override
        public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
            return buf.order(ByteOrder.LITTLE_ENDIAN).getInt(byteOffset);
        }

        @Override
        public Type getJavaType() {
            return Integer.TYPE;
        }

        @Override
        public OfLong asLongIterator(final ByteBuffer buf) {
            final IntBuffer intBuffer = asIntBuffer(buf);
            return new BufferToIteratorUtil.IntBufferToLongIterator(intBuffer);
        }

        @Override
        public Iterator<?> asIterator(final ByteBuffer buf) {
            return asIntIterator(buf);
        }

        @Override
        public String toString() {
            return "FixedPointNumber:NativeIntLE";
        }

        public static HDF5DatatypeAdapter instance(final H5Context context) {
            final HDF5FixedPointNumber datatype = new FixedPointNumberBB.BuilderBB(context).withElementSize(4)
                    .withByteOrder(HDF5Datatype.HDF5ByteOrder.LITTLE_ENDIAN).signed().build();

            return new NativeIntLEAdapter(datatype);
        }
    }

    public static final class NativeUIntBEAdapter extends AbstractNativeIntAdapter {
        public NativeUIntBEAdapter(final HDF5FixedPointNumber datatype) {
            super(datatype);
        }

        @Override
        public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
            return Integer.toUnsignedLong(buf.order(ByteOrder.BIG_ENDIAN).getInt(byteOffset));
        }

        @Override
        public Type getJavaType() {
            return Long.TYPE;
        }

        @Override
        public OfLong asLongIterator(final ByteBuffer buf) {
            final IntBuffer intBuffer = asIntBuffer(buf);
            return new BufferToIteratorUtil.UIntBufferToLongIterator(intBuffer);
        }

        @Override
        public Iterator<?> asIterator(final ByteBuffer buf) {
            return asLongIterator(buf);
        }

        @Override
        public String toString() {
            return "FixedPointNumber:NativeUShortBE";
        }

        public static HDF5DatatypeAdapter instance(final H5Context context) {
            final HDF5FixedPointNumber datatype = new FixedPointNumberBB.BuilderBB(context).withElementSize(4)
                    .withByteOrder(HDF5Datatype.HDF5ByteOrder.BIG_ENDIAN).unsigned().build();

            return new NativeUIntBEAdapter(datatype);
        }
    }

    public static final class NativeUIntLEAdapter extends AbstractNativeIntAdapter {
        public NativeUIntLEAdapter(final HDF5FixedPointNumber datatype) {
            super(datatype);
        }

        @Override
        public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
            return Integer.toUnsignedLong(buf.order(ByteOrder.LITTLE_ENDIAN).getInt(byteOffset));
        }

        @Override
        public Type getJavaType() {
            return Long.TYPE;
        }

        @Override
        public OfLong asLongIterator(final ByteBuffer buf) {
            final IntBuffer intBuffer = asIntBuffer(buf);
            return new BufferToIteratorUtil.UIntBufferToLongIterator(intBuffer);
        }

        @Override
        public Iterator<?> asIterator(final ByteBuffer buf) {
            return asLongIterator(buf);
        }

        @Override
        public String toString() {
            return "FixedPointNumber:NativeUShortLE";
        }

        public static HDF5DatatypeAdapter instance(final H5Context context) {
            final HDF5FixedPointNumber datatype = new FixedPointNumberBB.BuilderBB(context).withElementSize(4)
                    .withByteOrder(HDF5Datatype.HDF5ByteOrder.LITTLE_ENDIAN).unsigned().build();
            return new NativeUIntLEAdapter(datatype);
        }
    }

    public static final AbstractNativeIntAdapter forType(final HDF5FixedPointNumber fpn) {
        assert 4 == fpn.getElementSize();
        if (0 == fpn.getBitOffset() && 0 == fpn.getHiPadBit() && 0 == fpn.getLoPadBit()
                && fpn.getBitPrecision() == 8 * fpn.getElementSize()) {
            if (fpn.isSigned()) {
                switch (fpn.getByteOrder()) {
                case BIG_ENDIAN:
                    return new AbstractNativeIntAdapter.NativeIntBEAdapter(fpn);
                case LITTLE_ENDIAN:
                    return new AbstractNativeIntAdapter.NativeIntLEAdapter(fpn);
                default:
                    break;
                }
            } else {
                switch (fpn.getByteOrder()) {
                case BIG_ENDIAN:
                    return new AbstractNativeIntAdapter.NativeUIntBEAdapter(fpn);
                case LITTLE_ENDIAN:
                    return new AbstractNativeIntAdapter.NativeUIntLEAdapter(fpn);
                default:
                    break;
                }
            }
        }
        throw new IllegalArgumentException("No adapter for " + fpn);
    }
}
