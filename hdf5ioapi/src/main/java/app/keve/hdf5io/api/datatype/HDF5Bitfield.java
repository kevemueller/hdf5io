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
package app.keve.hdf5io.api.datatype;

/**
 * HDF5 Bitfield datatype.
 * 
 * @author keve
 * 
 * @see <a href=
 *      "https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#DatatypeMessage">HDF5
 *      Specification: Datatype Message</a>
 * 
 *
 */
public interface HDF5Bitfield extends HDF5AtomicDatatype {
    HDF5ByteOrder getByteOrder();

    int getLoPadBit();

    int getHiPadBit();

    int getBitOffset();

    int getBitPrecision();

    /**
     * Builder for bitfield datatype.
     * 
     * @author keve
     *
     */
    interface BitfieldBuilder extends Builder<HDF5Bitfield, BitfieldBuilder> {
        BitfieldBuilder withByteOrder(HDF5ByteOrder hdf5ByteOrder);

        BitfieldBuilder withLoPadBit(int loPadBit);

        BitfieldBuilder withHiPadBit(int hiPadBit);

        BitfieldBuilder withBitOffset(int bitOffset);

        BitfieldBuilder withBitPrecision(int bitPrecision);

        @Override
        default BitfieldBuilder from(final HDF5Bitfield template) {
            withElementSize(template.getElementSize());
            withByteOrder(template.getByteOrder());
            withLoPadBit(template.getLoPadBit());
            withHiPadBit(template.getHiPadBit());
            withBitOffset(template.getBitOffset());
            withBitPrecision(template.getBitPrecision());
            return this;
        }
    }

}
