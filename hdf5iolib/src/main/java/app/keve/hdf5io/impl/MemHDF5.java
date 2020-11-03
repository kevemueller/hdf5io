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

import app.keve.hdf5io.api.HDF5File;
import app.keve.hdf5io.api.HDF5Group;
import app.keve.hdf5io.api.datatype.HDF5Datatype.DatatypeBuilder;
import app.keve.hdf5io.fileformat.H5Registry;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.level2datatype.AbstractDatatypeBB;

public final class MemHDF5 extends H5Heap implements HDF5File {
    private final SizingContext sizingContext;

    public MemHDF5() {
        super(H5Registry.ofDefault());
        sizingContext = SizingContext.of(this, 8, 8);
    }

    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public HDF5Group getRootGroup() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DatatypeBuilder getDatatypeBuilder() {
        return new AbstractDatatypeBB.DatatypeBuilderBB(sizingContext);
    }

    @Override
    public long getPreambleSize() {
        // TODO Auto-generated method stub
        return 0;
    }

}
