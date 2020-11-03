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

import app.keve.hdf5io.api.datatype.HDF5Bitfield;
import app.keve.hdf5io.fileformat.H5Context;

public final class BitFieldBB extends AbstractDatatypeBB implements HDF5Bitfield {
    private static final int BF_BYTEORDER_MASK = 0x01;
    private static final int BF_LOPAD_MASK = 0x02;
    private static final int BF_HIPAD_MASK = 0x04;

    public BitFieldBB(final ByteBuffer buf, final H5Context context) {
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
    public int getLoPadBit() {
        return 0 == (getClassBitField() & BF_LOPAD_MASK) ? 0 : 1;
    }

    @Override
    public int getHiPadBit() {
        return 0 == (getClassBitField() & BF_HIPAD_MASK) ? 0 : 1;
    }

    @Override
    public int getBitOffset() {
        return getUnsignedShort(8);
    }

    @Override
    public int getBitPrecision() {
        return getUnsignedShort(10);
    }

    @Override
    public String toString() {
        return String.format(
                "%s, BitFieldBB [size()=%s, getByteOrder()=%s, getLoPadBit()=%s, getHiPadBit()=%s, getBitOffset()=%s,"
                        + " getBitPrecision()=%s]",
                super.toString(), size(), getByteOrder(), getLoPadBit(), getHiPadBit(), getBitOffset(),
                getBitPrecision());
    }

}
