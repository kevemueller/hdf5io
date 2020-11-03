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

import java.nio.ByteBuffer;

import app.keve.hdf5io.api.HDF5DatatypeAdapter;
import app.keve.hdf5io.api.datatype.HDF5Datatype;

public abstract class AbstractDatatypeAdapter<T extends HDF5Datatype> implements HDF5DatatypeAdapter {
    protected final T datatype;

    public AbstractDatatypeAdapter(final T datatype) {
        this.datatype = datatype;
    }

    @Override
    public final T getDatatype() {
        return datatype;
    }

    @Override
    public final ByteBuffer allocate(final int... dim) {
        int size = datatype.getElementSize();
        for (final int d : dim) {
            size *= d;
        }
        return ByteBuffer.allocate(size);
    }

    @Override
    public final int numberOfElements(final ByteBuffer buf) {
        return buf.remaining() / datatype.getElementSize();
    }

    protected static final int toIntMapper(final Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        throw new IllegalArgumentException("Cannot convert " + value + " of class " + value.getClass() + " to int");
    }

}
