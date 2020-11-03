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
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.function.Function;

import app.keve.hdf5io.api.HDF5DatatypeAdapter;
import app.keve.hdf5io.api.datatype.HDF5Datatype;
import app.keve.hdf5io.api.datatype.HDF5VariableLength;
import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.level1.GlobalHeapCollection;

public final class GenericVariableLengthAdapter extends AbstractDatatypeAdapter<HDF5VariableLength> {
    private final Function<VLGlobalHeapReference, ByteBuffer> resolver;
    private final SizingContext sizingContext;
    private final HDF5DatatypeAdapter baseTypeAdapter;
    private final int baseTypeElementSize;

    public GenericVariableLengthAdapter(final HDF5VariableLength datatype,
            final Function<VLGlobalHeapReference, ByteBuffer> resolver, final SizingContext sizingContext) {
        super(datatype);
        final HDF5Datatype baseType = datatype.getBaseType();
        this.baseTypeAdapter = baseType.adapter();
        this.baseTypeElementSize = baseType.getElementSize();
        this.resolver = resolver;
        this.sizingContext = sizingContext;
    }

    @Override
    public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
        final VLGlobalHeapReference r = new VLGlobalHeapReference(
                buf.duplicate().position(byteOffset).slice().order(ByteOrder.LITTLE_ENDIAN), sizingContext);
        return resolveGHR(r);
    }

    @Override
    public Type getJavaType() {
        switch (datatype.getType()) {
        case STRING:
            return String.class;
        case SEQUENCE:
            return Array.newInstance((Class<?>) baseTypeAdapter.getJavaType(), 0).getClass();
        default:
            throw new IllegalArgumentException();
        }
    }

    // Spec problem, this is nowhere...
    public static final class VLGlobalHeapReference extends AbstractSizedBB<SizingContext> {
        public VLGlobalHeapReference(final ByteBuffer buf, final SizingContext sizingContext) {
            super(buf, sizingContext);
        }

        public static long size(final SizingContext sc) {
            return 4 + sc.offsetSize() + 4;
        }

        @Override
        public long size() {
            return 4 + context.offsetSize() + 4;
        }

        public int getLength() {
            return getInt(0);
        }

        public Resolvable<GlobalHeapCollection> getGlobalHeapCollection() {
            return getResolvable(4, GlobalHeapCollection.class, context);
        }

        public int getIndex() {
            return getInt(4 + context.offsetSize());
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("VLGlobalHeapReference{");
            sb.append("length=").append(getLength());
            sb.append(", globalHeapCollection=").append(getGlobalHeapCollection());
            sb.append(", index=").append(getIndex());
            sb.append('}');
            return sb.toString();
        }
    }

    private class VariableLengthIterator implements Iterator<VLGlobalHeapReference> {
        private final ByteBuffer buf;
        private final int end;
        private int ofs;

        VariableLengthIterator(final ByteBuffer buf) {
            this.buf = buf;
            this.ofs = buf.position();
            this.end = buf.limit();
        }

        @Override
        public boolean hasNext() {
            return ofs < end;
        }

        @Override
        public VLGlobalHeapReference next() {
            final VLGlobalHeapReference r = new VLGlobalHeapReference(
                    buf.duplicate().position(ofs).slice().order(ByteOrder.LITTLE_ENDIAN), sizingContext);
            ofs += r.size();
            return r;
        }
    }

    private Object resolveGHR(final VLGlobalHeapReference vlghr) {
        final ByteBuffer hod = resolver.apply(vlghr);
        switch (datatype.getType()) {
        case STRING:
            return datatype.getCharset().decode(hod).toString();
        case SEQUENCE:
            final int numElements = vlghr.getLength();
            final ByteBuffer dataBuf = hod.slice().limit(numElements * baseTypeElementSize);
            return baseTypeAdapter.asStream(dataBuf).toArray();
        default:
            throw new IllegalArgumentException("Implement variable length for " + datatype.getType());
        }

    }

    @Override
    public Iterator<?> asIterator(final ByteBuffer buf) {
        return new VariableLengthIterator(buf);
    }

}
