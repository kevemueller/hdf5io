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
package app.keve.hdf5io.api;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.stream.Stream;

import app.keve.hdf5io.api.datatype.HDF5Datatype;

/**
 * A HDF5 dataset named object.
 * 
 * @author keve
 *
 */
public interface HDF5Dataset extends HDF5NamedObject, HDF5Data {
    @Override
    default HDF5Dataset asDataset() {
        return this;
    }

    @Override
    default HDF5Group asGroup() {
        throw new IllegalArgumentException();
    }

    @Override
    default HDF5NamedDatatype asNamedDatatype() {
        throw new IllegalArgumentException();
    }

    HDF5Datatype getDatatype();

    long[] getDimensionSizes();

    long[] getDimensionMaxSizes();

    Stream<? extends Chunk> getChunks();

    /**
     * A HDF5 chunk, i.e. a subset of a dataset.
     * 
     * @author keve
     *
     */
    interface Chunk extends HDF5Data {
        long[] getOffset();

        long[] getSize();
    }

    /**
     * Builder for data sets.
     * 
     * @author keve
     *
     */
    interface Builder {
        HDF5Dataset build() throws IOException;

        Builder forData(Object data);

        Builder withDatatype(HDF5Datatype datatype);

        Builder withFillValue(Object value);

        Builder withFillValue(ByteBuffer value);

        Builder withDimensions(long... dim);

        Builder withMaxDimensions(long... dim);
    }
}
