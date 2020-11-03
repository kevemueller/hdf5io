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

import app.keve.hdf5io.api.datatype.HDF5String;

public final class GenericStringAdapter extends AbstractDatatypeAdapter<HDF5String> {
    private final int elementSize;

    public GenericStringAdapter(final HDF5String datatype) {
        super(datatype);
        this.elementSize = datatype.getElementSize();
    }

    @Override
    public Object asScalarObject(final ByteBuffer buf, final int byteOffset) {
        final ByteBuffer bb = buf.duplicate().position(byteOffset).limit(byteOffset + elementSize).slice();
        return datatype.getCharset().decode(bb);
    }

    @Override
    public Type getJavaType() {
        return String.class;
    }

    @Override
    public Iterator<?> asIterator(final ByteBuffer buf) {
        throw new IllegalArgumentException();
    }
}
