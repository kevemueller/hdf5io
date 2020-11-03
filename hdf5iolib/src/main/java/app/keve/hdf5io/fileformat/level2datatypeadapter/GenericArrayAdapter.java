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

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Iterator;
import java.util.PrimitiveIterator.OfDouble;
import java.util.PrimitiveIterator.OfInt;
import java.util.PrimitiveIterator.OfLong;
import java.util.stream.Collectors;

import app.keve.hdf5io.api.HDF5DatatypeAdapter;
import app.keve.hdf5io.api.datatype.HDF5Array;

public final class GenericArrayAdapter extends AbstractDatatypeAdapter<HDF5Array> {
    private final HDF5DatatypeAdapter baseTypeAdapter;

    public GenericArrayAdapter(final HDF5Array datatype) {
        super(datatype);
        this.baseTypeAdapter = datatype.getBaseType().adapter();
    }

    @Override
    public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
        final ByteBuffer arrayBuf = buf.duplicate().position(byteOffset).slice().limit(datatype.getElementSize());
        return baseTypeAdapter.asStream(arrayBuf).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Type getJavaType() {
        return Array.newInstance((Class<?>) baseTypeAdapter.getJavaType(), 0).getClass();
    }

    @Override
    public ShortBuffer asShortBuffer(final ByteBuffer preparedBuf) {
        return baseTypeAdapter.asShortBuffer(preparedBuf);
    }

    @Override
    public IntBuffer asIntBuffer(final ByteBuffer preparedBuf) {
        return baseTypeAdapter.asIntBuffer(preparedBuf);
    }

    @Override
    public LongBuffer asLongBuffer(final ByteBuffer preparedBuf) {
        return baseTypeAdapter.asLongBuffer(preparedBuf);
    }

    @Override
    public FloatBuffer asFloatBuffer(final ByteBuffer preparedBuf) {
        return baseTypeAdapter.asFloatBuffer(preparedBuf);
    }

    @Override
    public DoubleBuffer asDoubleBuffer(final ByteBuffer preparedBuf) {
        return baseTypeAdapter.asDoubleBuffer(preparedBuf);
    }

    @Override
    public OfInt asIntIterator(final ByteBuffer buf) {
        return baseTypeAdapter.asIntIterator(buf);
    }

    @Override
    public OfLong asLongIterator(final ByteBuffer buf) {
        return baseTypeAdapter.asLongIterator(buf);
    }

    @Override
    public OfDouble asDoubleIterator(final ByteBuffer buf) {
        return baseTypeAdapter.asDoubleIterator(buf);
    }

    @Override
    public Iterator<?> asIterator(final ByteBuffer buf) {
        throw new IllegalArgumentException("Implement!");
    }

}
