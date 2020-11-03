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
import java.nio.ByteOrder;
import java.util.OptionalInt;

import app.keve.hdf5io.api.datatype.HDF5FloatingPointNumber;
import app.keve.hdf5io.fileformat.H5Context;

public final class FloatingPointNumberBB extends AbstractDatatypeBB implements HDF5FloatingPointNumber {

    private static final int BF_BYTEORDER_MASK = 0x01;
    private static final int BF_LOPAD_MASK = 0x02;
    private static final int BF_HIPAD_MASK = 0x04;
    private static final int BF_INTPAD_MASK = 0x08;
    private static final int BF_MANTISSA_NORMALIZATION_MASK = 0x30;
    private static final int BF_BYTEORDERX_MASK = 0x40;
    private static final int BF_SIGNLOCATION_MASK = 0xFF00;

    public FloatingPointNumberBB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    @Override
    public long size() {
        return 8 + 12;
    }

    @Override
    public HDF5ByteOrder getByteOrder() {
        if ((getClassBitField() & BF_BYTEORDERX_MASK) > 0) {
            return 0 == (getClassBitField() & BF_BYTEORDER_MASK) ? null : HDF5ByteOrder.VAX_ENDIAN;
        } else {
            return 0 == (getClassBitField() & BF_BYTEORDER_MASK) ? HDF5ByteOrder.LITTLE_ENDIAN
                    : HDF5ByteOrder.BIG_ENDIAN;
        }
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
    public int getIntPadBit() {
        return 0 == (getClassBitField() & BF_INTPAD_MASK) ? 0 : 1;
    }

    @Override
    public int getMantissaNormalization() {
        return (getClassBitField() & BF_MANTISSA_NORMALIZATION_MASK) >> 4;
    }

    @Override
    public int getSignLocation() {
        return (getClassBitField() & BF_SIGNLOCATION_MASK) >> 8;
    }

    @Override
    public int getBitOffset() {
        return getUnsignedShort(8);
    }

    protected void setBitOffset(final int value) {
        assert value >= 0 && value <= 0xFFFF;
        setUnsignedShort(8, value);
    }

    @Override
    public int getBitPrecision() {
        return getUnsignedShort(10);
    }

    protected void setBitPrecision(final int value) {
        assert value >= 0 && value <= 0xFFFF;
        setUnsignedShort(10, value);
    }

    @Override
    public int getExponentLocation() {
        return getUnsignedByte(12);
    }

    protected void setExponentLocation(final int value) {
        assert value >= 0 && value <= 0xFF;
        setByte(12, value);
    }

    @Override
    public int getExponentSize() {
        return getUnsignedByte(13);
    }

    protected void setExponentSize(final int value) {
        assert value >= 0 && value <= 0xFF;
        setByte(13, value);
    }

    @Override
    public int getMantissaLocation() {
        return getUnsignedByte(14);
    }

    protected void setMantissaLocation(final int value) {
        assert value >= 0 && value <= 0xFF;
        setByte(14, value);
    }

    @Override
    public int getMantissaSize() {
        return getUnsignedByte(15);
    }

    protected void setMantissaSize(final int value) {
        assert value >= 0 && value <= 0xFF;
        setByte(15, value);
    }

    @Override
    public int getExponentBias() {
        return getSmallUnsignedInt(16);
    }

    protected void setExponentBias(final int value) {
        setInt(16, value);
    }

    @Override
    public String toString() {
        return String.format(
                "%s FloatingPointNumberBB [size()=%s, getByteOrderHDF5()=%s, getLoPadBit()=%s, getHiPadBit()=%s, "
                        + "getIntPadBit()=%s, getMantissaNormalization()=%s, getSignLocation()=%s, getBitOffset()=%s, "
                        + "getBitPrecision()=%s, getExponentLocation()=%s, getExponentSize()=%s, getMantissaLocation()=%s, "
                        + "getMantissaSize()=%s, getExponentBias()=%s]",
                super.toString(), size(), getByteOrder(), getLoPadBit(), getHiPadBit(), getIntPadBit(),
                getMantissaNormalization(), getSignLocation(), getBitOffset(), getBitPrecision(), getExponentLocation(),
                getExponentSize(), getMantissaLocation(), getMantissaSize(), getExponentBias());
    }

    private static int encodeClassBits(final HDF5ByteOrder hdf5ByteOrder, final int loPadBit, final int hiPadBit,
            final int intPadBit, final int mantissaNormalization, final int signLocation) {
        int classBits = 0;
        switch (hdf5ByteOrder) {
        case LITTLE_ENDIAN:
            classBits |= 0b0000_0000;
            break;
        case BIG_ENDIAN:
            classBits |= 0b0000_0001;
            break;
        case VAX_ENDIAN:
            classBits |= 0b0100_0001;
            break;
        default:
            throw new IllegalArgumentException();
        }
        switch (loPadBit) {
        case 0:
            classBits |= 0b00;
            break;
        case 1:
            classBits |= 0b10;
            break;
        default:
            throw new IllegalArgumentException();
        }
        switch (hiPadBit) {
        case 0:
            classBits |= 0b000;
            break;
        case 1:
            classBits |= 0b100;
            break;
        default:
            throw new IllegalArgumentException();
        }
        switch (intPadBit) {
        case 0:
            classBits |= 0b0000;
            break;
        case 1:
            classBits |= 0b1000;
            break;
        default:
            throw new IllegalArgumentException();
        }
        switch (mantissaNormalization) {
        case 0:
            classBits |= 0b00_0000;
            break;
        case 1:
            classBits |= 0b01_0000;
            break;
        case 2:
            classBits |= 0b10_0000;
            break;
        case 3:
        default:
            throw new IllegalArgumentException();
        }
        classBits |= signLocation << 8;
        return classBits;
    }

    @SuppressWarnings("checkstyle:hiddenfield")
    public static final class BuilderBB implements HDF5FloatingPointNumber.FloatingPointNumberBuilder {
        private final H5Context context;

        private OptionalInt elementSize = OptionalInt.empty();
        private HDF5ByteOrder hdf5ByteOrder = HDF5ByteOrder.BIG_ENDIAN;
        private OptionalInt loPadBit = OptionalInt.empty();
        private OptionalInt bitOffset = OptionalInt.empty();
        private OptionalInt hiPadBit = OptionalInt.empty();
        private OptionalInt bitPrecision = OptionalInt.empty();

        private OptionalInt exponentBias = OptionalInt.empty();

        private OptionalInt mantissaSize = OptionalInt.empty();

        private OptionalInt mantissaLocation = OptionalInt.empty();

        private OptionalInt exponentSize = OptionalInt.empty();

        private OptionalInt exponentLocation = OptionalInt.empty();

        private OptionalInt signLocation = OptionalInt.empty();

        private OptionalInt mantissaNormalization = OptionalInt.empty();

        private OptionalInt intPadBit = OptionalInt.empty();

        public BuilderBB(final H5Context context) {
            this.context = context;
        }

        @Override
        public HDF5FloatingPointNumber build() {
            final ByteBuffer buf = ByteBuffer.allocate(8 + 12).order(ByteOrder.LITTLE_ENDIAN);
            final FloatingPointNumberBB floatingPointNumber = new FloatingPointNumberBB(buf, context);

            final int precision = bitPrecision.orElseGet(() -> elementSize.getAsInt() * 8);
            floatingPointNumber.setDatatypeClass(TypeClass.FLOATING_POINT);
            floatingPointNumber.setVersion(1);
            floatingPointNumber.setClassBitField(encodeClassBits(hdf5ByteOrder, loPadBit.orElse(0), hiPadBit.orElse(0),
                    intPadBit.orElse(0), mantissaNormalization.orElse(2), signLocation.orElse(precision - 1)));
            floatingPointNumber.setElementSize(elementSize.orElse(precision / 8));
            floatingPointNumber.setBitOffset(bitOffset.orElse(0));
            floatingPointNumber.setBitPrecision(precision);
            floatingPointNumber.setExponentLocation(exponentLocation.orElse(mantissaSize.orElseThrow()));
            floatingPointNumber.setExponentSize(exponentSize.orElseThrow());
            floatingPointNumber.setMantissaLocation(mantissaLocation.orElse(0));
            floatingPointNumber.setMantissaSize(mantissaSize.orElseThrow());
            floatingPointNumber.setExponentBias(exponentBias.orElse((1 << exponentSize.orElseThrow() - 1) - 1));
            return floatingPointNumber;
        }

        @Override
        public FloatingPointNumberBuilder withElementSize(final int elementSize) {
            this.elementSize = OptionalInt.of(elementSize);
            return this;
        }

        @Override
        public FloatingPointNumberBuilder withByteOrder(final HDF5ByteOrder hdf5ByteOrder) {
            this.hdf5ByteOrder = hdf5ByteOrder;
            return this;
        }

        @Override
        public FloatingPointNumberBuilder withLoPadBit(final int loPadBit) {
            this.loPadBit = OptionalInt.of(loPadBit);
            return this;
        }

        @Override
        public FloatingPointNumberBuilder withHiPadBit(final int hiPadBit) {
            this.hiPadBit = OptionalInt.of(hiPadBit);
            return this;
        }

        @Override
        public FloatingPointNumberBuilder withBitOffset(final int bitOffset) {
            this.bitOffset = OptionalInt.of(bitOffset);
            return this;
        }

        @Override
        public FloatingPointNumberBuilder withBitPrecision(final int bitPrecision) {
            this.bitPrecision = OptionalInt.of(bitPrecision);
            return this;
        }

        @Override
        public FloatingPointNumberBuilder withIntPadBit(final int intPadBit) {
            this.intPadBit = OptionalInt.of(intPadBit);
            return this;
        }

        @Override
        public FloatingPointNumberBuilder withMantissaNormalization(final int mantissaNormalization) {
            this.mantissaNormalization = OptionalInt.of(mantissaNormalization);
            return this;
        }

        @Override
        public FloatingPointNumberBuilder withSignLocation(final int signLocation) {
            this.signLocation = OptionalInt.of(signLocation);
            return this;
        }

        @Override
        public FloatingPointNumberBuilder withExponentLocation(final int exponentLocation) {
            this.exponentLocation = OptionalInt.of(exponentLocation);
            return this;
        }

        @Override
        public FloatingPointNumberBuilder withExponentSize(final int exponentSize) {
            this.exponentSize = OptionalInt.of(exponentSize);
            return this;
        }

        @Override
        public FloatingPointNumberBuilder withMantissaLocation(final int mantissaLocation) {
            this.mantissaLocation = OptionalInt.of(mantissaLocation);
            return this;
        }

        @Override
        public FloatingPointNumberBuilder withMantissaSize(final int mantissaSize) {
            this.mantissaSize = OptionalInt.of(mantissaSize);
            return this;
        }

        @Override
        public FloatingPointNumberBuilder withExponentBias(final int exponentBias) {
            this.exponentBias = OptionalInt.of(exponentBias);
            return this;
        }

    }

}
