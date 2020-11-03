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
package app.keve.hdf5io.impl;

import app.keve.hdf5io.api.HDF5NamedDatatype;
import app.keve.hdf5io.api.datatype.HDF5Datatype;
import app.keve.hdf5io.fileformat.H5Resolver;
import app.keve.hdf5io.fileformat.level2.ObjectHeader;

public final class NamedDatatypeV0 extends AbstractNamedObjectV implements HDF5NamedDatatype {
    private final HDF5Datatype hdf5Datatype;

    public NamedDatatypeV0(final long id, final H5Resolver hdf5Resolver, final ObjectHeader objectHeader,
            final HDF5Datatype hdf5Datatype) {
        super(id, hdf5Resolver, objectHeader);
        this.hdf5Datatype = hdf5Datatype;
    }

    @Override
    public HDF5Datatype getDatatype() {
        return hdf5Datatype;
    }

}
