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
import java.nio.DoubleBuffer;
import java.util.Iterator;
import java.util.PrimitiveIterator.OfDouble;
import java.util.stream.Stream;

import app.keve.hdf5io.api.HDF5DatatypeAdapter;
import app.keve.hdf5io.api.datatype.HDF5Datatype;
import app.keve.hdf5io.api.datatype.HDF5FloatingPointNumber;
import app.keve.hdf5io.api.util.ArrayUtil;
import app.keve.hdf5io.fileformat.H5Context;
import app.keve.hdf5io.fileformat.level2datatype.FloatingPointNumberBB;

public abstract class AbstractNativeDoubleAdapter extends AbstractNativeFloatingPointNumberAdapter {
    protected AbstractNativeDoubleAdapter(final HDF5FloatingPointNumber datatype) {
        super(datatype);
    }

    @Override
    public final Type getJavaType() {
        return Double.TYPE;
    }

    @Override
    public final DoubleBuffer asDoubleBuffer(final ByteBuffer buf) {
        return buf.order(byteOrder).asDoubleBuffer();
    }

    @Override
    public final OfDouble asDoubleIterator(final ByteBuffer buf) {
        return new BufferToIteratorUtil.DoubleBufferToDoubleIterator(asDoubleBuffer(buf));
    }

    @Override
    public final Iterator<?> asIterator(final ByteBuffer buf) {
        return asDoubleIterator(buf);
    }

    @Override
    public final Stream<?> asStream(final ByteBuffer buf) {
        return asDoubleStream(buf).boxed();
    }

    @Override
    public final Object asObject(final ByteBuffer dataBuf, final int... dim) {
        if (0 == dim.length) {
            return asScalarObject(dataBuf, 0);
        } else {
            final DoubleBuffer buffer = asDoubleBuffer(dataBuf);
            return ArrayUtil.bufferToMd(buffer, dim);
        }
    }

    @Override
    public final ByteBuffer fromObject(final ByteBuffer dataBuf, final Object value) {
        final DoubleBuffer buffer = asDoubleBuffer(dataBuf);
        if (value instanceof Number) {
            buffer.put(((Number) value).doubleValue());
        } else {
            ArrayUtil.mdRowMajorBuffer(buffer, value);
        }
        return dataBuf;
    }

    public static final class NativeDoubleBEAdapter extends AbstractNativeDoubleAdapter {
        public NativeDoubleBEAdapter(final HDF5FloatingPointNumber datatype) {
            super(datatype);
        }

        @Override
        public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
            return buf.order(ByteOrder.BIG_ENDIAN).getDouble(byteOffset);
        }

        @Override
        public ByteBuffer fromScalarObject(final ByteBuffer buf, final int byteOffset, final Object value) {
            buf.order(ByteOrder.BIG_ENDIAN).putDouble(byteOffset, (double) value);
            return buf;
        }

        @Override
        public String toString() {
            return "FixedPointNumber:NativeDoubleBE";
        }

        public static HDF5DatatypeAdapter instance(final H5Context context) {
            final HDF5FloatingPointNumber datatype = new FloatingPointNumberBB.BuilderBB(context).withElementSize(8)
                    .withByteOrder(HDF5Datatype.HDF5ByteOrder.BIG_ENDIAN).withExponentLocation(52).withExponentSize(11)
                    .withMantissaLocation(0).withMantissaSize(52).withExponentBias(1023).build();
            return new NativeDoubleBEAdapter(datatype);
        }
    }

    public static final class NativeDoubleLEAdapter extends AbstractNativeDoubleAdapter {
        public NativeDoubleLEAdapter(final HDF5FloatingPointNumber datatype) {
            super(datatype);
        }

        @Override
        public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
            return buf.order(ByteOrder.LITTLE_ENDIAN).getDouble(byteOffset);
        }

        @Override
        public ByteBuffer fromScalarObject(final ByteBuffer buf, final int byteOffset, final Object value) {
            buf.order(ByteOrder.LITTLE_ENDIAN).putDouble(byteOffset, (double) value);
            return buf;
        }

        @Override
        public String toString() {
            return "FixedPointNumber:NativeDoubleLE";
        }

        public static HDF5DatatypeAdapter instance(final H5Context context) {
            final HDF5FloatingPointNumber datatype = new FloatingPointNumberBB.BuilderBB(context).withElementSize(8)
                    .withByteOrder(HDF5Datatype.HDF5ByteOrder.LITTLE_ENDIAN).withExponentLocation(52)
                    .withExponentSize(11).withMantissaLocation(0).withMantissaSize(52).withExponentBias(1023).build();
            return new NativeDoubleLEAdapter(datatype);
        }
    }

    public static final AbstractNativeDoubleAdapter forType(final HDF5FloatingPointNumber fpn) {
        if (fpn.getBitPrecision() == 8 * fpn.getElementSize() && 0 == fpn.getLoPadBit() && 0 == fpn.getHiPadBit()
                && 0 == fpn.getIntPadBit() && 2 == fpn.getMantissaNormalization()
                && fpn.getBitPrecision() - 1 == fpn.getSignLocation() && 52 == fpn.getExponentLocation()
                && 11 == fpn.getExponentSize() && 0 == fpn.getMantissaLocation() && 52 == fpn.getMantissaSize()
                && 1023 == fpn.getExponentBias()) {
            switch (fpn.getByteOrder()) {
            case BIG_ENDIAN:
                return new AbstractNativeDoubleAdapter.NativeDoubleBEAdapter(fpn);
            case LITTLE_ENDIAN:
                return new AbstractNativeDoubleAdapter.NativeDoubleLEAdapter(fpn);
            default:
                break;
            }
        }
        throw new IllegalArgumentException("No adapter for " + fpn);
    }
}
