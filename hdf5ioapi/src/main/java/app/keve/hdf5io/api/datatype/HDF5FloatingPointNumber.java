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
 * HDF5 floating point number datatype.
 * 
 * @author keve
 * 
 * @see <a href=
 *      "https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#DatatypeMessage">HDF5
 *      Specification: Datatype Message</a>
 * 
 *
 */
public interface HDF5FloatingPointNumber extends HDF5AtomicDatatype {

    HDF5ByteOrder getByteOrder();

    int getLoPadBit();

    int getHiPadBit();

    int getIntPadBit();

    int getMantissaNormalization();

    int getSignLocation();

    int getBitOffset();

    int getBitPrecision();

    int getExponentLocation();

    int getExponentSize();

    int getMantissaLocation();

    int getMantissaSize();

    int getExponentBias();

    /**
     * Builder for floating point numbers.
     * 
     * @author keve
     *
     */
    interface FloatingPointNumberBuilder extends Builder<HDF5FloatingPointNumber, FloatingPointNumberBuilder> {

        FloatingPointNumberBuilder withByteOrder(HDF5ByteOrder hdf5ByteOrder);

        FloatingPointNumberBuilder withLoPadBit(int loPadBit);

        FloatingPointNumberBuilder withHiPadBit(int hiPadBit);

        FloatingPointNumberBuilder withIntPadBit(int intPadBit);

        FloatingPointNumberBuilder withMantissaNormalization(int mantissaNormalization);

        FloatingPointNumberBuilder withSignLocation(int signLocation);

        FloatingPointNumberBuilder withBitOffset(int bitOffset);

        FloatingPointNumberBuilder withBitPrecision(int bitPrecision);

        FloatingPointNumberBuilder withExponentLocation(int exponentLocation);

        FloatingPointNumberBuilder withExponentSize(int exponentSize);

        FloatingPointNumberBuilder withMantissaLocation(int mantissaLocation);

        FloatingPointNumberBuilder withMantissaSize(int mantissaSize);

        FloatingPointNumberBuilder withExponentBias(int exponentBias);

        @Override
        default FloatingPointNumberBuilder from(final HDF5FloatingPointNumber template) {
            withElementSize(template.getElementSize());
            withByteOrder(template.getByteOrder());
            withLoPadBit(template.getLoPadBit());
            withHiPadBit(template.getHiPadBit());
            withIntPadBit(template.getIntPadBit());
            withMantissaNormalization(template.getMantissaNormalization());
            withSignLocation(template.getSignLocation());
            withBitOffset(template.getBitOffset());
            withBitPrecision(template.getBitPrecision());
            withExponentLocation(template.getExponentLocation());
            withExponentSize(template.getExponentSize());
            withMantissaLocation(template.getMantissaLocation());
            withMantissaSize(template.getMantissaSize());
            withExponentBias(template.getExponentBias());
            return this;
        }
    }
}
