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
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PrimitiveIterator.OfDouble;
import java.util.PrimitiveIterator.OfInt;
import java.util.PrimitiveIterator.OfLong;
import java.util.concurrent.atomic.AtomicInteger;

import app.keve.hdf5io.api.HDF5DatatypeAdapter;
import app.keve.hdf5io.api.datatype.HDF5Enumeration;

// this assumes we have an adapter for the basetype
// in fact we could work without decoding the basetype (comparing the byte patterns)
public final class GenericEnumToLabelAdapter extends AbstractDatatypeAdapter<HDF5Enumeration> {
    private final HDF5DatatypeAdapter baseTypeAdapter;
    private final Map<Object, String> labelMap;

    public GenericEnumToLabelAdapter(final HDF5Enumeration datatype) {
        super(datatype);
        this.baseTypeAdapter = datatype.getBaseType().adapter();
        final String[] labels = datatype.getNames();
        this.labelMap = new LinkedHashMap<>(labels.length);

        final ByteBuffer buf = datatype.getValueBuf();
        final Iterator<?> valueStream = baseTypeAdapter.asIterator(buf);
        final AtomicInteger i = new AtomicInteger();
        valueStream.forEachRemaining(v -> {
            labelMap.put(v, labels[i.getAndIncrement()]);
        });
    }

    @Override
    public Type getJavaType() {
        return String.class;
    }

    @Override
    public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
        return labelMap.get(baseTypeAdapter.asScalarObject(buf, byteOffset));
    }

    @Override
    public ShortBuffer asShortBuffer(final ByteBuffer buf) {
        return baseTypeAdapter.asShortBuffer(buf);
    }

    @Override
    public IntBuffer asIntBuffer(final ByteBuffer buf) {
        return baseTypeAdapter.asIntBuffer(buf);
    }

    @Override
    public LongBuffer asLongBuffer(final ByteBuffer buf) {
        return baseTypeAdapter.asLongBuffer(buf);
    }

    @Override
    public FloatBuffer asFloatBuffer(final ByteBuffer buf) {
        return baseTypeAdapter.asFloatBuffer(buf);
    }

    @Override
    public DoubleBuffer asDoubleBuffer(final ByteBuffer buf) {
        return baseTypeAdapter.asDoubleBuffer(buf);
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
    public Iterator<String> asIterator(final ByteBuffer buf) {
        final Iterator<?> baseIt = baseTypeAdapter.asIterator(buf);
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return baseIt.hasNext();
            }

            @Override
            public String next() {
                return labelMap.get(baseIt.next());
            }

        };
    }

}
