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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import app.keve.hdf5io.api.HDF5DatatypeAdapter;
import app.keve.hdf5io.api.datatype.HDF5Compound;
import app.keve.hdf5io.api.datatype.HDF5Compound.Member;

public final class GenericCompoundToMapAdapter extends AbstractDatatypeAdapter<HDF5Compound> {
    private final int elementSize;
    private final List<Member> members;
    private final HDF5DatatypeAdapter[] memberTypeAdapter;

    public GenericCompoundToMapAdapter(final HDF5Compound datatype) {
        super(datatype);
        this.elementSize = datatype.getElementSize();
        this.members = datatype.getMembers();
        this.memberTypeAdapter = new HDF5DatatypeAdapter[members.size()];
        for (int i = 0; i < memberTypeAdapter.length; i++) {
            memberTypeAdapter[i] = members.get(i).getMemberType().adapter();
        }
    }

    @Override
    public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
        return toMap(byteOffset, buf);
    }

    @Override
    public Type getJavaType() {
        return Map.class;
    }

    private Map<String, Object> toMap(final int byteOffset, final ByteBuffer buf) {
        final LinkedHashMap<String, Object> map = new LinkedHashMap<>(members.size());
        for (int i = 0; i < members.size(); i++) {
            final Object v = memberTypeAdapter[i].asScalarObject(buf, byteOffset + members.get(i).getByteOffset());
            map.put(members.get(i).getName(), v);
        }
        return map;
    }

    private class CompoundToMapIterator implements Iterator<Map<String, Object>> {
        private final ByteBuffer buf;
        private int offset;

        CompoundToMapIterator(final ByteBuffer buf) {
            this.buf = buf;
            offset = buf.position();
        }

        @Override
        public boolean hasNext() {
            return buf.remaining() >= datatype.getElementSize();
        }

        @Override
        public Map<String, Object> next() {
            final Map<String, Object> map = toMap(offset, buf);
            offset += elementSize;
            buf.position(offset);
            return map;
        }
    }

    @Override
    public Iterator<?> asIterator(final ByteBuffer buf) {
        return new CompoundToMapIterator(buf.duplicate());
    }
}
