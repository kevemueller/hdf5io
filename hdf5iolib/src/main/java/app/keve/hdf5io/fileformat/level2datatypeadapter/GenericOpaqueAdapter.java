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

import app.keve.hdf5io.api.datatype.HDF5Opaque;

public final class GenericOpaqueAdapter extends AbstractDatatypeAdapter<HDF5Opaque> {
    private final int elementSize;

    public GenericOpaqueAdapter(final HDF5Opaque datatype) {
        super(datatype);
        elementSize = datatype.getElementSize();
    }

    @Override
    public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
        return buf.duplicate().position(byteOffset).limit(byteOffset + elementSize).slice();
    }

    @Override
    public Type getJavaType() {
        return ByteBuffer.class;
    }

    private static class OpaqueIterator implements Iterator<ByteBuffer> {
        private final ByteBuffer buf;
        private final int elementSize;

        OpaqueIterator(final ByteBuffer buf, final int elementSize) {
            this.buf = buf;
            this.elementSize = elementSize;
        }

        @Override
        public boolean hasNext() {
            return buf.hasRemaining();
        }

        @Override
        public ByteBuffer next() {
            final ByteBuffer next = buf.slice().limit(elementSize);
            buf.position(buf.position() + elementSize);
            return next;
        }
    }

    @Override
    public Iterator<?> asIterator(final ByteBuffer buf) {
        return new OpaqueIterator(buf, elementSize);
    }

}
