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

import app.keve.hdf5io.api.datatype.HDF5FixedPointNumber;

public abstract class AbstractNativeFixedPointNumberAdapter extends AbstractDatatypeAdapter<HDF5FixedPointNumber> {
    protected final ByteOrder byteOrder;

    protected AbstractNativeFixedPointNumberAdapter(final HDF5FixedPointNumber datatype) {
        super(datatype);
        byteOrder = Objects.requireNonNull(datatype.getByteOrder().getByteOrder());
    }

    public static AbstractNativeFixedPointNumberAdapter forType(final HDF5FixedPointNumber fpn) {
        switch (fpn.getElementSize()) {
        case 1:
            return AbstractNativeByteAdapter.forType(fpn);
        case 2:
            return AbstractNativeShortAdapter.forType(fpn);
        case 4:
            return AbstractNativeIntAdapter.forType(fpn);
        case 8:
            return AbstractNativeLongAdapter.forType(fpn);
        default:
            throw new IllegalArgumentException("No adapter for " + fpn);
        }
    }
}
