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
 * HDF5 Fixed point number datatype.
 * 
 * @author keve
 * 
 * @see <a href=
 *      "https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#DatatypeMessage">HDF5
 *      Specification: Datatype Message</a>
 * 
 *
 */
public interface HDF5FixedPointNumber extends HDF5AtomicDatatype {
    HDF5ByteOrder getByteOrder();

    int getLoPadBit();

    int getHiPadBit();

    boolean isSigned();

    int getBitOffset();

    int getBitPrecision();

    /**
     * Builder for fixed point number.
     * 
     * @author keve
     *
     */
    interface FixedPointNumberBuilder extends Builder<HDF5FixedPointNumber, FixedPointNumberBuilder> {
        FixedPointNumberBuilder withByteOrder(HDF5ByteOrder hdf5ByteOrder);

        FixedPointNumberBuilder withLoPadBit(int loPadBit);

        FixedPointNumberBuilder withHiPadBit(int hiPadBit);

        FixedPointNumberBuilder signed();

        FixedPointNumberBuilder unsigned();

        FixedPointNumberBuilder withBitOffset(int bitOffset);

        FixedPointNumberBuilder withBitPrecision(int bitPrecision);

        @Override
        default FixedPointNumberBuilder from(final HDF5FixedPointNumber template) {
            withElementSize(template.getElementSize());
            withByteOrder(template.getByteOrder());
            withLoPadBit(template.getLoPadBit());
            withHiPadBit(template.getHiPadBit());
            if (template.isSigned()) {
                signed();
            } else {
                unsigned();
            }
            withBitOffset(template.getBitOffset());
            withBitPrecision(template.getBitPrecision());
            return this;
        }
    }
}
