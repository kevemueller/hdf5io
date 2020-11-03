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

import app.keve.hdf5io.api.datatype.HDF5Opaque;
import app.keve.hdf5io.fileformat.H5Context;

public final class OpaqueDatatypeBB extends AbstractDatatypeBB implements HDF5Opaque {
    private static final int BF_LENGTH_MASK = 0xFF;

    public OpaqueDatatypeBB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    @Override
    public long size() {
        // FIXME: Does the taglength include the 8 byte padding or not?
        return 8 + getTagLength();
    }

    private int getTagLength() {
        return getClassBitField() & BF_LENGTH_MASK;
    }

    @Override
    public CharSequence getTag() {
        // spec problem: no NUL byte if there is no padding
        return 0 == getTagLength() ? "" : getAsciiNulString(8, getTagLength());
    }

}
