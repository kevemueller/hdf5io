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
import java.nio.LongBuffer;
import java.util.Iterator;
import java.util.PrimitiveIterator.OfLong;
import java.util.function.LongFunction;

import app.keve.hdf5io.api.datatype.HDF5Reference;

public final class GenericReferenceAdapter extends AbstractDatatypeAdapter<HDF5Reference> {
    private final LongFunction<Object> resolver;

    public GenericReferenceAdapter(final HDF5Reference datatype, final LongFunction<Object> resolver) {
        super(datatype);
        this.resolver = resolver;
    }

    @Override
    public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
        return buf.order(ByteOrder.LITTLE_ENDIAN).getLong(byteOffset);
    }

    @Override
    public Type getJavaType() {
        return Object.class;
    }

    @Override
    public LongBuffer asLongBuffer(final ByteBuffer buf) {
        return buf.order(ByteOrder.LITTLE_ENDIAN).asLongBuffer();
    }

    @Override
    public OfLong asLongIterator(final ByteBuffer buf) {
        final LongBuffer longBuffer = asLongBuffer(buf);
        return new BufferToIteratorUtil.LongBufferToLongIterator(longBuffer);
    }

    @Override
    public Iterator<?> asIterator(final ByteBuffer buf) {
        final OfLong rawIt = asLongIterator(buf);
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return rawIt.hasNext();
            }

            @Override
            public Object next() {
                return resolver.apply(rawIt.nextLong());
            }
        };

    }

    public static GenericReferenceAdapter forType(final HDF5Reference datatype, final LongFunction<Object> resolver) {
        return new GenericReferenceAdapter(datatype, resolver);
    }

}
