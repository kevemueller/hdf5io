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

import java.nio.ByteOrder;
import java.util.Objects;

import app.keve.hdf5io.api.HDF5DatatypeAdapter;
import app.keve.hdf5io.api.datatype.HDF5FloatingPointNumber;

public abstract class AbstractNativeFloatingPointNumberAdapter
        extends AbstractDatatypeAdapter<HDF5FloatingPointNumber> {
    protected final ByteOrder byteOrder;

    protected AbstractNativeFloatingPointNumberAdapter(final HDF5FloatingPointNumber datatype) {
        super(datatype);
        byteOrder = Objects.requireNonNull(datatype.getByteOrder().getByteOrder());
    }

    public static HDF5DatatypeAdapter forType(final HDF5FloatingPointNumber fpn) {
        switch (fpn.getElementSize()) {
        case 4:
            return AbstractNativeFloatAdapter.forType(fpn);
        case 8:
            return AbstractNativeDoubleAdapter.forType(fpn);
        default:
            break;
        }
        throw new IllegalArgumentException("No adapter for " + fpn);
    }
}
