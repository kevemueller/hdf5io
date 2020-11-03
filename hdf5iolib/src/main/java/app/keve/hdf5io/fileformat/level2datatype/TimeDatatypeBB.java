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

import app.keve.hdf5io.api.datatype.HDF5Time;
import app.keve.hdf5io.fileformat.H5Context;

public final class TimeDatatypeBB extends AbstractDatatypeBB implements HDF5Time {
    private static final int BF_BYTEORDER_MASK = 0x01;

    public TimeDatatypeBB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    @Override
    public long size() {
        return 8 + 4;
    }

    @Override
    public HDF5ByteOrder getByteOrder() {
        return 0 == (getClassBitField() & BF_BYTEORDER_MASK) ? HDF5ByteOrder.LITTLE_ENDIAN : HDF5ByteOrder.BIG_ENDIAN;
    }

    @Override
    public int getBitPrecision() {
        return getSmallUnsignedInt(8);
    }
}
