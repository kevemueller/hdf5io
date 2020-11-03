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
import java.util.Iterator;
import java.util.PrimitiveIterator.OfInt;
import java.util.PrimitiveIterator.OfLong;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import app.keve.hdf5io.api.datatype.HDF5FixedPointNumber;
import app.keve.hdf5io.api.util.ArrayUtil;

public abstract class AbstractNativeByteAdapter extends AbstractNativeFixedPointNumberAdapter {
    protected AbstractNativeByteAdapter(final HDF5FixedPointNumber datatype) {
        super(datatype);
    }

    @Override
    public final ByteBuffer fromIntIterator(final ByteBuffer buf, final OfInt value) {
        value.forEachRemaining((IntConsumer) i -> buf.put((byte) i));
        return buf;
    }

    @Override
    public final Iterator<?> asIterator(final ByteBuffer buf) {
        return asIntIterator(buf);
    }

    @Override
    public final ByteBuffer fromIterator(final ByteBuffer buf, final Iterator<?> value) {
        value.forEachRemaining(o -> buf.put((byte) toIntMapper(o)));
        return buf;
    }

    @Override
    public final ByteBuffer fromIntStream(final ByteBuffer buf, final IntStream value) {
        value.forEachOrdered(i -> buf.put((byte) i));
        return buf;
    }

    @Override
    public final Stream<?> asStream(final ByteBuffer buf) {
        return asIntStream(buf).boxed();
    }

    @Override
    public final ByteBuffer fromStream(final ByteBuffer dataBuf, final Stream<?> value) {
        return fromIntStream(dataBuf, value.mapToInt(AbstractNativeByteAdapter::toIntMapper));
    }

    @Override
    public final Object asObject(final ByteBuffer buf, final int... dim) {
        return ArrayUtil.rowMajorIteratorToMd(asIntIterator(buf), dim);
    }

    private static class NativeByteAdapter extends AbstractNativeByteAdapter {
        NativeByteAdapter(final HDF5FixedPointNumber datatype) {
            super(datatype);
        }

        @Override
        public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
            return buf.get(byteOffset);
        }

        @Override
        public Type getJavaType() {
            return Byte.TYPE;
        }

        @Override
        public final OfInt asIntIterator(final ByteBuffer buf) {
            return new BufferToIteratorUtil.ByteBufferToIntIterator(buf);
        }

        @Override
        public final OfLong asLongIterator(final ByteBuffer buf) {
            return new BufferToIteratorUtil.ByteBufferToLongIterator(buf);
        }

        @Override
        public String toString() {
            return "FixedPointNumber:NativeByte";
        }
    }

    static class NativeUByteAdapter extends AbstractNativeByteAdapter {
        NativeUByteAdapter(final HDF5FixedPointNumber datatype) {
            super(datatype);
        }

        @Override
        public Type getJavaType() {
            return Integer.TYPE;
        }

        @Override
        public final OfInt asIntIterator(final ByteBuffer buf) {
            return new BufferToIteratorUtil.UByteBufferToIntIterator(buf);
        }

        @Override
        public final OfLong asLongIterator(final ByteBuffer buf) {
            return new BufferToIteratorUtil.UByteBufferToLongIterator(buf);
        }

        @Override
        public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
            return Byte.toUnsignedInt(buf.get(byteOffset));
        }

        @Override
        public String toString() {
            return "FixedPointNumber:NativeUByte";
        }

    }

    public static AbstractNativeFixedPointNumberAdapter forType(final HDF5FixedPointNumber fpn) {
        assert 1 == fpn.getElementSize();
        // we don't care for ByteOrder when dealing with single bytes
        if (0 == fpn.getBitOffset() && 0 == fpn.getHiPadBit() && 0 == fpn.getLoPadBit()
                && fpn.getBitPrecision() == 8 * fpn.getElementSize()) {
            if (fpn.isSigned()) {
                return new NativeByteAdapter(fpn);
            } else {
                return new NativeUByteAdapter(fpn);
            }
        } else {
            if (fpn.isSigned()) {
                return new GenericFixedPointPartAdapter(fpn, new NativeByteAdapter(fpn));
            } else {
                return new GenericFixedPointPartAdapter(fpn, new NativeUByteAdapter(fpn));
            }
        }
//        throw new IllegalArgumentException("No adapter for " + fpn);
    }
}
