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
import java.util.Iterator;
import java.util.PrimitiveIterator.OfDouble;
import java.util.PrimitiveIterator.OfInt;
import java.util.PrimitiveIterator.OfLong;

import app.keve.hdf5io.api.datatype.HDF5FixedPointNumber;

public final class GenericFixedPointPartAdapter extends AbstractNativeFixedPointNumberAdapter {
    private final AbstractNativeFixedPointNumberAdapter baseAdapter;
    private final int shift;
    private final int mask;

    GenericFixedPointPartAdapter(final HDF5FixedPointNumber datatype,
            final AbstractNativeFixedPointNumberAdapter baseAdapter) {
        super(datatype);
        this.baseAdapter = baseAdapter;
        this.shift = datatype.getBitOffset();
        this.mask = (1 << datatype.getBitPrecision()) - 1;
    }

    @Override
    public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
        return (int) baseAdapter.asScalarObject(buf, byteOffset) >> shift & mask;
    }

    @Override
    public Type getJavaType() {
        return baseAdapter.getJavaType();
    }

    @Override
    public Iterator<?> asIterator(final ByteBuffer buf) {
        final OfInt itInt = asIntIterator(buf);
        if (null != itInt) {
            return itInt;
        }
        final OfLong itLong = asLongIterator(buf);
        if (null != itLong) {
            return itLong;
        }
        final Iterator<?> rawIt = baseAdapter.asIterator(buf);
        final BigInteger biMask = BigInteger.valueOf(mask);
        return new Iterator<BigInteger>() {
            @Override
            public boolean hasNext() {
                return rawIt.hasNext();
            }

            @Override
            public BigInteger next() {
                final BigInteger n = (BigInteger) rawIt.next();
                return n.shiftRight(shift).and(biMask);
            }
        };
    }

    @Override
    public ByteBuffer fromIterator(final ByteBuffer buf, final Iterator<?> value) {
        if (value instanceof OfInt) {
            return fromIntIterator(buf, (OfInt) value);
        } else if (value instanceof OfLong) {
            return fromLongIterator(buf, (OfLong) value);
        } else if (value instanceof OfDouble) {
            return fromDoubleIterator(buf, (OfDouble) value);
        }
        final Iterator<Number> rawIt = new Iterator<>() {
            @Override
            public boolean hasNext() {
                return value.hasNext();
            }

            @Override
            public Number next() {
                final Object n = value.next();
                if (n instanceof Byte) {
                    return ((byte) n) << shift;
                } else if (n instanceof Short) {
                    return ((short) n) << shift;
                } else if (n instanceof Integer) {
                    return ((int) n) << shift;
                } else if (n instanceof Long) {
                    return ((long) n) << shift;
                } else if (n instanceof BigInteger) {
                    return ((BigInteger) n).shiftLeft(shift);
                }
                throw new IllegalArgumentException("Cannot set value from " + n.getClass());
            }
        };
        return baseAdapter.fromIterator(buf, rawIt);
    }

    @Override
    public OfInt asIntIterator(final ByteBuffer buf) {
        final OfInt rawIt = baseAdapter.asIntIterator(buf);
        if (null == rawIt) {
            return null;
        }
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

    @Override
    public ByteBuffer fromIntIterator(final ByteBuffer buf, final OfInt value) {
        final OfInt rawValue = new OfInt() {
            @Override
            public boolean hasNext() {
                return value.hasNext();
            }

            @Override
            public int nextInt() {
                return value.nextInt() << shift;
            }
        };
        return baseAdapter.fromIntIterator(buf, rawValue);
    }

    @Override
    public OfLong asLongIterator(final ByteBuffer buf) {
        final OfLong rawIt = baseAdapter.asLongIterator(buf);
        return new OfLong() {

            @Override
            public boolean hasNext() {
                return rawIt.hasNext();
            }

            @Override
            public long nextLong() {
                return rawIt.nextLong() >> shift & mask;
            }
        };
    }

    @Override
    public String toString() {
        return "FixedPointNumber:NativeUBytePart";
    }

}
