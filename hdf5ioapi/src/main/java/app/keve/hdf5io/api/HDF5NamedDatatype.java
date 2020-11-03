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

import app.keve.hdf5io.api.datatype.HDF5Datatype;

/**
 * A HDF5 datatype named object.
 * 
 * @author keve
 *
 */
public interface HDF5NamedDatatype extends HDF5NamedObject {
    @Override
    default HDF5Dataset asDataset() {
        throw new IllegalArgumentException();
    }

    @Override
    default HDF5Group asGroup() {
        throw new IllegalArgumentException();
    }

    @Override
    default HDF5NamedDatatype asNamedDatatype() {
        return this;
    }

    HDF5Datatype getDatatype();

}
