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
package app.keve.hdf5io.api;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Iterator;
import java.util.PrimitiveIterator;
import java.util.PrimitiveIterator.OfDouble;
import java.util.PrimitiveIterator.OfInt;
import java.util.PrimitiveIterator.OfLong;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import app.keve.hdf5io.api.datatype.HDF5Datatype;
import app.keve.hdf5io.api.util.ArrayUtil;

/**
 * Type adapter to convert native byte representation in HDF5 file to a java
 * structure.
 * 
 * @author keve
 *
 */
public interface HDF5DatatypeAdapter {
    /**
     * Get the HDF5 datatype that this adapter processes.
     * 
     * @return the datatype
     */
    HDF5Datatype getDatatype();

    /**
     * Get the <i>preferred/most specific</i> Java type that this adapter processes.
     * 
     * @return the java type
     */
    Type getJavaType();

    /**
     * Allocate a buffer to hold the multi dimensional array defined by the dim
     * parameter.
     * 
     * @param dim the dimension sizes.
     * @return the allocated buffer
     */
    ByteBuffer allocate(int... dim);

    /**
     * Return the number of elements that can be read from buf.
     * 
     * @param buf the buffer
     * @return the number of elements that can be read from the buffer.
     */
    int numberOfElements(ByteBuffer buf);

    Object asScalarObject(ByteBuffer buf, int byteOffset);

    default ByteBuffer fromScalarObject(final ByteBuffer buf, final int byteOffset, final Object value) {
        throw new IllegalArgumentException("Implement!");
    }

    /**
     * If the datatype can be interpreted as a java byte, return the buffer.
     * 
     * @param buf the buffer
     * @return the buffer of bytes
     */
    default ByteBuffer asByteBuffer(final ByteBuffer buf) {
        return null;
    }

    /**
     * If the datatype can be interpreted as a java short, return the buffer.
     * 
     * @param buf the buffer
     * @return the buffer of shorts
     */
    default ShortBuffer asShortBuffer(final ByteBuffer buf) {
        return null;
    }

    /**
     * If the datatype can be interpreted as a java int, return the buffer.
     * 
     * @param buf the buffer
     * @return the buffer of ints
     */
    default IntBuffer asIntBuffer(final ByteBuffer buf) {
        return null;
    }

    /**
     * If the datatype can be interpreted as a java long, return the buffer.
     * 
     * @param buf the buffer
     * @return the buffer of longs
     */
    default LongBuffer asLongBuffer(final ByteBuffer buf) {
        return null;
    }

    /**
     * If the datatype can be interpreted as a java float, return the buffer.
     * 
     * @param buf the buffer
     * @return the buffer of floats
     */
    default FloatBuffer asFloatBuffer(final ByteBuffer buf) {
        return null;
    }

    /**
     * If the datatype can be interpreted as a java double, return the buffer.
     * 
     * @param buf the buffer
     * @return the buffer of doubles
     */
    default DoubleBuffer asDoubleBuffer(final ByteBuffer buf) {
        return null;
    }

    /**
     * If the datatype can be converted to a java int, return a primitive iterator
     * over the buffer.
     * 
     * @param buf the buffer
     * @return primitive iterator of ints.
     */
    default PrimitiveIterator.OfInt asIntIterator(final ByteBuffer buf) {
        return null;
    }

    /**
     * Set the data buffer with the elements of the iterator.
     * 
     * @param buf   the target buffer
     * @param value the source stream
     * @return the modified buffer or null if the operation cannot be performed.
     */
    default ByteBuffer fromIntIterator(final ByteBuffer buf, final PrimitiveIterator.OfInt value) {
        return null;
    }

    /**
     * If the datatype can be converted to a java long, return a primitive iterator
     * over the buffer.
     * 
     * @param buf the buffer
     * @return primitive iterator of longs.
     */
    default PrimitiveIterator.OfLong asLongIterator(final ByteBuffer buf) {
        return null;
    }

    /**
     * Set the data buffer with the elements of the iterator.
     * 
     * @param buf   the target buffer
     * @param value the source stream
     * @return the modified buffer or null if the operation cannot be performed.
     */
    default ByteBuffer fromLongIterator(final ByteBuffer buf, final PrimitiveIterator.OfLong value) {
        return null;
    }

    /**
     * If the datatype can be converted to a java double, return a primitive
     * iterator over the buffer.
     * 
     * @param buf the buffer
     * @return primitive iterator of doubles.
     */
    default PrimitiveIterator.OfDouble asDoubleIterator(final ByteBuffer buf) {
        return null;
    }

    /**
     * Set the data buffer with the elements of the iterator.
     * 
     * @param buf   the target buffer
     * @param value the source stream
     * @return the modified buffer or null if the operation cannot be performed.
     */
    default ByteBuffer fromDoubleIterator(final ByteBuffer buf, final PrimitiveIterator.OfDouble value) {
        return null;
    }

    /**
     * Return an iterator over the values in the buffer.
     * 
     * @param buf the buffer
     * @return iterator of values.
     */
    Iterator<?> asIterator(ByteBuffer buf);

    /**
     * Return a typed iterator over the values in the buffer.
     * 
     * @param <T>    the type of the object
     * @param tClass the object's class
     * @param buf    the buffer
     * @return iterator over values
     */
    @SuppressWarnings("unchecked")
    default <T> Iterator<T> asIterator(final Class<T> tClass, final ByteBuffer buf) {
        if (Integer.TYPE.equals(tClass) || Integer.class.equals(tClass)) {
            return (Iterator<T>) asIntIterator(buf);
        }
        if (Long.TYPE.equals(tClass) || Long.class.equals(tClass)) {
            return (Iterator<T>) asLongIterator(buf);
        }
        if (Double.TYPE.equals(tClass) || Double.class.equals(tClass)) {
            return (Iterator<T>) asDoubleIterator(buf);
        }
        if (Object.class.equals(tClass)) {
            return (Iterator<T>) asIterator(buf);
        }
        return null;
    }

    /**
     * Set the data buffer with the elements of the iterator.
     * 
     * @param buf   the target buffer
     * @param value the source stream
     * @return the modified buffer or null if the operation cannot be performed.
     */
    default ByteBuffer fromIterator(final ByteBuffer buf, final Iterator<?> value) {
        if (value instanceof OfInt) {
            return fromIntIterator(buf, (OfInt) value);
        } else if (value instanceof OfLong) {
            return fromLongIterator(buf, (OfLong) value);
        } else if (value instanceof OfDouble) {
            return fromDoubleIterator(buf, (OfDouble) value);
        }
        throw new IllegalArgumentException("Implement fromIterator for " + getClass().getName());
    }

    /**
     * Return an primitive stream of ints over the values in the buffer if they can
     * be converted to int.
     * 
     * @param buf the buffer
     * @return iterator of values.
     */
    default IntStream asIntStream(final ByteBuffer buf) {
        final OfInt it = asIntIterator(buf);
        if (null == it) {
            return null;
        }
        final int n = numberOfElements(buf);
        final Spliterator.OfInt sit = Spliterators.spliterator(it, n, Spliterator.ORDERED);
        return StreamSupport.intStream(sit, false);

    }

    /**
     * Set the data buffer with the elements of the stream.
     * 
     * @param buf   the target buffer
     * @param value the source stream
     * @return the modified buffer or null if the operation cannot be performed.
     */
    default ByteBuffer fromIntStream(final ByteBuffer buf, final IntStream value) {
        return fromIntIterator(buf, value.iterator());
    }

    /**
     * Return an primitive stream of longs over the values in the buffer if they can
     * be converted to long.
     * 
     * @param buf the buffer
     * @return iterator of values.
     */
    default LongStream asLongStream(final ByteBuffer buf) {
        final OfLong it = asLongIterator(buf);
        if (null == it) {
            return null;
        }
        final int n = numberOfElements(buf);
        final Spliterator.OfLong sit = Spliterators.spliterator(it, n, Spliterator.ORDERED);
        return StreamSupport.longStream(sit, false);

    }

    /**
     * Set the data buffer with the elements of the stream.
     * 
     * @param buf   the target buffer
     * @param value the source stream
     * @return the modified buffer or null if the operation cannot be performed.
     */
    default ByteBuffer fromLongStream(final ByteBuffer buf, final LongStream value) {
        return fromLongIterator(buf, value.iterator());
    }

    /**
     * Return an primitive stream of doubles over the values in the buffer if they
     * can be converted to double.
     * 
     * @param buf the buffer
     * @return iterator of values.
     */
    default DoubleStream asDoubleStream(final ByteBuffer buf) {
        final OfDouble it = asDoubleIterator(buf);
        if (null == it) {
            return null;
        }
        final int n = numberOfElements(buf);
        final Spliterator.OfDouble sit = Spliterators.spliterator(it, n, Spliterator.ORDERED);
        return StreamSupport.doubleStream(sit, false);

    }

    /**
     * Set the data buffer with the elements of the stream.
     * 
     * @param buf   the target buffer
     * @param value the source stream
     * @return the modified buffer or null if the operation cannot be performed.
     */
    default ByteBuffer fromDoubleStream(final ByteBuffer buf, final DoubleStream value) {
        return fromDoubleIterator(buf, value.iterator());
    }

    /**
     * Return a stream over the values in the buffer.
     * 
     * @param buf the buffer
     * @return iterator of values.
     */
    default Stream<?> asStream(final ByteBuffer buf) {
        final Iterator<?> it = asIterator(buf);
        if (null == it) {
            return null;
        }
        final int n = numberOfElements(buf);
        final Spliterator<?> sit = Spliterators.spliterator(it, n, Spliterator.ORDERED);
        return StreamSupport.stream(sit, false);

    }

    default ByteBuffer fromStream(final ByteBuffer buf, final Stream<?> value) {
        return fromIterator(buf, value.iterator());
    }

    default Object asObject(final ByteBuffer buf, final int... dim) {
        return ArrayUtil.rowMajorIteratorToMd(Object.class, asIterator(Object.class, buf), dim);
    }

    default ByteBuffer fromObject(final ByteBuffer buf, final Object value) {
        if (0 == ArrayUtil.rank(value)) {
            return fromScalarObject(buf, 0, value);
        } else {
            return fromIterator(buf, ArrayUtil.mdObjectRowMajorIterator(value));
        }
    }

}
