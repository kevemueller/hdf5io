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
package app.keve.hdf5io.fileformat.level2datatype;

import java.nio.ByteBuffer;

import app.keve.hdf5io.api.datatype.HDF5Reference;
import app.keve.hdf5io.fileformat.H5Context;

/**
 * Reference datatype version 1.
 * 
 * @author keve
 * 
 * @see <a href=
 *      "https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#ReferenceEncodeDP">Spec</a>
 * 
 *
 */
public class ReferenceV1BB extends AbstractDatatypeBB implements HDF5Reference {
    protected static final int BF_REFERENCETYPE_MASK = 0x0F;

    public ReferenceV1BB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    @Override
    public final long size() {
        return 8; // context.offsetSize()
    }

    @Override
    public final ReferenceType getType() {
        return ReferenceType.of(getTypeRaw());
    }

    public final int getTypeRaw() {
        return getClassBitField() & BF_REFERENCETYPE_MASK;
    }
}
