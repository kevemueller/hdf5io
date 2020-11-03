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
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.Iterator;
import java.util.PrimitiveIterator.OfLong;

import app.keve.hdf5io.api.HDF5DatatypeAdapter;
import app.keve.hdf5io.api.datatype.HDF5Datatype;
import app.keve.hdf5io.api.datatype.HDF5FixedPointNumber;
import app.keve.hdf5io.fileformat.H5Context;
import app.keve.hdf5io.fileformat.level2datatype.FixedPointNumberBB;

public abstract class AbstractNativeLongAdapter extends AbstractNativeFixedPointNumberAdapter {
    protected AbstractNativeLongAdapter(final HDF5FixedPointNumber datatype) {
        super(datatype);
    }

    @Override
    public final LongBuffer asLongBuffer(final ByteBuffer buf) {
        return buf.order(byteOrder).asLongBuffer();
    }

    @Override
    public final OfLong asLongIterator(final ByteBuffer buf) {
        final LongBuffer intBuffer = asLongBuffer(buf);
        return new BufferToIteratorUtil.LongBufferToLongIterator(intBuffer);
    }

    public static final class NativeLongBEAdapter extends AbstractNativeLongAdapter {
        public NativeLongBEAdapter(final HDF5FixedPointNumber datatype) {
            super(datatype);
        }

        @Override
        public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
            return buf.order(ByteOrder.BIG_ENDIAN).getLong(byteOffset);
        }

        @Override
        public Type getJavaType() {
            return Long.TYPE;
        }

        @Override
        public Iterator<?> asIterator(final ByteBuffer buf) {
            return asLongIterator(buf);
        }

        @Override
        public String toString() {
            return "FixedPointNumber:NativeLongBE";
        }

        public static HDF5DatatypeAdapter instance(final H5Context context) {
            final HDF5FixedPointNumber datatype = new FixedPointNumberBB.BuilderBB(context).withElementSize(8)
                    .withByteOrder(HDF5Datatype.HDF5ByteOrder.BIG_ENDIAN).signed().build();
            return new NativeLongBEAdapter(datatype);
        }
    }

    public static final class NativeLongLEAdapter extends AbstractNativeLongAdapter {
        public NativeLongLEAdapter(final HDF5FixedPointNumber datatype) {
            super(datatype);
        }

        @Override
        public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
            return buf.order(ByteOrder.LITTLE_ENDIAN).getLong(byteOffset);
        }

        @Override
        public Type getJavaType() {
            return Long.TYPE;
        }

        @Override
        public Iterator<?> asIterator(final ByteBuffer buf) {
            return asLongIterator(buf);
        }

        @Override
        public String toString() {
            return "FixedPointNumber:NativeLongLE";
        }

        public static HDF5DatatypeAdapter instance(final H5Context context) {
            final HDF5FixedPointNumber datatype = new FixedPointNumberBB.BuilderBB(context).withElementSize(8)
                    .withByteOrder(HDF5Datatype.HDF5ByteOrder.LITTLE_ENDIAN).signed().build();

            return new NativeLongLEAdapter(datatype);
        }
    }

    private abstract static class AbstractNativeULongAdapter extends AbstractNativeLongAdapter {
        protected AbstractNativeULongAdapter(final HDF5FixedPointNumber datatype) {
            super(datatype);
        }

        @Override
        public final Type getJavaType() {
            return BigInteger.class;
        }

        @Override
        public final Iterator<?> asIterator(final ByteBuffer buf) {
            final LongBuffer longBuffer = asLongBuffer(buf);
            return new BufferToIteratorUtil.ULongBufferToBigIntegerIterator(longBuffer);
        }

        @SuppressWarnings("unchecked")
        @Override
        public final <T> Iterator<T> asIterator(final Class<T> tClass, final ByteBuffer buf) {
            final Iterator<T> it = super.asIterator(tClass, buf);
            if (null != it) {
                return it;
            }
            if (BigInteger.class.equals(tClass)) {
                return (Iterator<T>) asIterator(buf);
            }
            return null;
        }
    }

    public static final class NativeULongBEAdapter extends AbstractNativeULongAdapter {
        public NativeULongBEAdapter(final HDF5FixedPointNumber datatype) {
            super(datatype);
        }

        @Override
        public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
            return BigInteger.valueOf(buf.order(ByteOrder.BIG_ENDIAN).getLong(byteOffset));
        }

        @Override
        public String toString() {
            return "FixedPointNumber:NativeULongBE";
        }

        public static HDF5DatatypeAdapter instance(final H5Context context) {
            final HDF5FixedPointNumber datatype = new FixedPointNumberBB.BuilderBB(context).withElementSize(8)
                    .withByteOrder(HDF5Datatype.HDF5ByteOrder.BIG_ENDIAN).unsigned().build();

            return new NativeULongBEAdapter(datatype);
        }
    }

    public static final class NativeULongLEAdapter extends AbstractNativeULongAdapter {
        public NativeULongLEAdapter(final HDF5FixedPointNumber datatype) {
            super(datatype);
        }

        @Override
        public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
            return BigInteger.valueOf(buf.order(ByteOrder.LITTLE_ENDIAN).getLong(byteOffset));
        }

        @Override
        public String toString() {
            return "FixedPointNumber:NativeULongLE";
        }

        public static HDF5DatatypeAdapter instance(final H5Context context) {
            final HDF5FixedPointNumber datatype = new FixedPointNumberBB.BuilderBB(context).withElementSize(8)
                    .withByteOrder(HDF5Datatype.HDF5ByteOrder.LITTLE_ENDIAN).unsigned().build();

            return new NativeULongLEAdapter(datatype);
        }
    }

    public static final AbstractNativeLongAdapter forType(final HDF5FixedPointNumber fpn) {
        assert 8 == fpn.getElementSize();
        if (0 == fpn.getBitOffset() && 0 == fpn.getHiPadBit() && 0 == fpn.getLoPadBit()
                && fpn.getBitPrecision() == 8 * fpn.getElementSize()) {
            if (fpn.isSigned()) {
                switch (fpn.getByteOrder()) {
                case BIG_ENDIAN:
                    return new AbstractNativeLongAdapter.NativeLongBEAdapter(fpn);
                case LITTLE_ENDIAN:
                    return new AbstractNativeLongAdapter.NativeLongLEAdapter(fpn);
                default:
                    break;
                }
            } else {
                switch (fpn.getByteOrder()) {
                case BIG_ENDIAN:
                    return new AbstractNativeLongAdapter.NativeULongBEAdapter(fpn);
                case LITTLE_ENDIAN:
                    return new AbstractNativeLongAdapter.NativeULongLEAdapter(fpn);
                default:
                    break;
                }
            }
        }
        throw new IllegalArgumentException("No adapter for " + fpn);
    }

}
