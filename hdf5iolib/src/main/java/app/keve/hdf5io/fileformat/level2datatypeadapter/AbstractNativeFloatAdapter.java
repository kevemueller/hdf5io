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
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.PrimitiveIterator.OfInt;

import app.keve.hdf5io.api.HDF5DatatypeAdapter;
import app.keve.hdf5io.api.datatype.HDF5Datatype;
import app.keve.hdf5io.api.datatype.HDF5FloatingPointNumber;
import app.keve.hdf5io.fileformat.H5Context;
import app.keve.hdf5io.fileformat.level2datatype.FloatingPointNumberBB;

public abstract class AbstractNativeFloatAdapter extends AbstractNativeFloatingPointNumberAdapter {

    protected AbstractNativeFloatAdapter(final HDF5FloatingPointNumber datatype) {
        super(datatype);
    }

    @Override
    public final Type getJavaType() {
        return Float.TYPE;
    }

    @Override
    public final FloatBuffer asFloatBuffer(final ByteBuffer buf) {
        return buf.order(byteOrder).asFloatBuffer();
    }

    @Override
    public final OfInt asIntIterator(final ByteBuffer buf) {
        final IntBuffer intBuffer = buf.order(byteOrder).asIntBuffer();
        return new BufferToIteratorUtil.IntBufferToIntIterator(intBuffer);
    }

    @Override
    public final Iterator<?> asIterator(final ByteBuffer buf) {
        final FloatBuffer floatBuffer = buf.order(byteOrder).asFloatBuffer();
        return new BufferToIteratorUtil.FloatBufferToFloatIterator(floatBuffer);
    }

    public static final class NativeFloatBEAdapter extends AbstractNativeFloatAdapter {
        public NativeFloatBEAdapter(final HDF5FloatingPointNumber datatype) {
            super(datatype);
        }

        @Override
        public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
            return buf.order(ByteOrder.BIG_ENDIAN).getFloat(byteOffset);
        }

        @Override
        public ByteBuffer fromScalarObject(final ByteBuffer buf, final int byteOffset, final Object value) {
            buf.order(ByteOrder.BIG_ENDIAN).putFloat(byteOffset, (float) value);
            return buf;
        }

        @Override
        public String toString() {
            return "FixedPointNumber:NativeFloatBE";
        }

        public static HDF5DatatypeAdapter instance(final H5Context context) {
            final HDF5FloatingPointNumber datatype = new FloatingPointNumberBB.BuilderBB(context).withElementSize(4)
                    .withByteOrder(HDF5Datatype.HDF5ByteOrder.BIG_ENDIAN).withExponentLocation(23).withExponentSize(8)
                    .withMantissaLocation(0).withMantissaSize(23).withExponentBias(127).build();
            return new NativeFloatBEAdapter(datatype);
        }
    }

    public static final class NativeFloatLEAdapter extends AbstractNativeFloatAdapter {
        public NativeFloatLEAdapter(final HDF5FloatingPointNumber datatype) {
            super(datatype);
        }

        @Override
        public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
            return buf.order(ByteOrder.LITTLE_ENDIAN).getFloat(byteOffset);
        }

        @Override
        public ByteBuffer fromScalarObject(final ByteBuffer buf, final int byteOffset, final Object value) {
            buf.order(ByteOrder.LITTLE_ENDIAN).putFloat(byteOffset, (float) value);
            return buf;
        }

        @Override
        public String toString() {
            return "FixedPointNumber:NativeFloatLE";
        }

        public static HDF5DatatypeAdapter instance(final H5Context context) {
            final HDF5FloatingPointNumber datatype = new FloatingPointNumberBB.BuilderBB(context).withElementSize(4)
                    .withByteOrder(HDF5Datatype.HDF5ByteOrder.LITTLE_ENDIAN).withExponentLocation(23)
                    .withExponentSize(8).withMantissaLocation(0).withMantissaSize(23).withExponentBias(127).build();
            return new NativeFloatLEAdapter(datatype);
        }
    }

    public static final AbstractNativeFloatAdapter forType(final HDF5FloatingPointNumber fpn) {
        if (fpn.getBitPrecision() == 8 * fpn.getElementSize() && 0 == fpn.getLoPadBit() && 0 == fpn.getHiPadBit()
                && 0 == fpn.getIntPadBit() && 2 == fpn.getMantissaNormalization()
                && fpn.getBitPrecision() - 1 == fpn.getSignLocation() && 23 == fpn.getExponentLocation()
                && 8 == fpn.getExponentSize() && 0 == fpn.getMantissaLocation() && 23 == fpn.getMantissaSize()
                && 127 == fpn.getExponentBias()) {
            switch (fpn.getByteOrder()) {
            case BIG_ENDIAN:
                return new AbstractNativeFloatAdapter.NativeFloatBEAdapter(fpn);
            case LITTLE_ENDIAN:
                return new AbstractNativeFloatAdapter.NativeFloatLEAdapter(fpn);
            default:
                break;
            }
        }
        throw new IllegalArgumentException("No adapter for " + fpn);
    }
}
