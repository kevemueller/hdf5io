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

import app.keve.hdf5io.api.datatype.HDF5FixedPointNumber;
import app.keve.hdf5io.fileformat.H5Context;

public final class FixedPointNumberBB extends AbstractDatatypeBB implements HDF5FixedPointNumber {
    private static final int BF_BYTEORDER_MASK = 0x01;
    private static final int BF_LOPAD_MASK = 0x02;
    private static final int BF_HIPAD_MASK = 0x04;
    private static final int BF_SIGNED_MASK = 0x08;

    public FixedPointNumberBB(final ByteBuffer buf, final H5Context context) {
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
    public boolean isSigned() {
        return (getClassBitField() & BF_SIGNED_MASK) > 0;
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

    private static int encodeClassBits(final HDF5ByteOrder hdf5ByteOrder, final int loPadBit, final int hiPadBit,
            final boolean signed) {
        int classBits = 0;
        switch (hdf5ByteOrder) {
        case LITTLE_ENDIAN:
            classBits |= 0b0;
            break;
        case BIG_ENDIAN:
            classBits |= 0b1;
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
        if (signed) {
            classBits |= 0b1000;
        }
        return classBits;
    }

    @Override
    public String toString() {
        return String.format(
                "%s, FixedPointNumberBB [size()=%s, getByteOrder()=%s, getLoPadBit()=%s, getHiPadBit()=%s, isSigned()=%s, "
                        + "getBitOffset()=%s, getBitPrecision()=%s]",
                super.toString(), size(), getByteOrder(), getLoPadBit(), getHiPadBit(), isSigned(), getBitOffset(),
                getBitPrecision());
    }

    @SuppressWarnings("checkstyle:hiddenfield")
    public static final class BuilderBB implements FixedPointNumberBuilder {
        private final H5Context context;

        private OptionalInt elementSize = OptionalInt.empty();
        private HDF5ByteOrder hdf5ByteOrder = HDF5ByteOrder.BIG_ENDIAN;
        private OptionalInt loPadBit = OptionalInt.empty();
        private OptionalInt bitOffset = OptionalInt.empty();
        private Boolean signed = true;
        private OptionalInt hiPadBit = OptionalInt.empty();
        private OptionalInt bitPrecision = OptionalInt.empty();

        public BuilderBB(final H5Context context) {
            this.context = context;
        }

        @Override
        public HDF5FixedPointNumber build() {
            final ByteBuffer buf = ByteBuffer.allocate(8 + 4).order(ByteOrder.LITTLE_ENDIAN);
            final FixedPointNumberBB fixedPointNumber = new FixedPointNumberBB(buf, context);
            fixedPointNumber.setDatatypeClass(TypeClass.FIXED_POINT);
            fixedPointNumber.setVersion(1);
            fixedPointNumber
                    .setClassBitField(encodeClassBits(hdf5ByteOrder, loPadBit.orElse(0), hiPadBit.orElse(0), signed));
            final int precision = bitPrecision.orElseGet(() -> elementSize.orElseThrow() * 8);
            fixedPointNumber.setElementSize(elementSize.orElse((precision + 7) / 8));
            fixedPointNumber.setBitOffset(bitOffset.orElse(0));
            fixedPointNumber.setBitPrecision(precision);
            return fixedPointNumber;
        }

        @Override
        public FixedPointNumberBuilder withElementSize(final int elementSize) {
            this.elementSize = OptionalInt.of(elementSize);
            return this;
        }

        @Override
        public FixedPointNumberBuilder withByteOrder(final HDF5ByteOrder hdf5ByteOrder) {
            this.hdf5ByteOrder = hdf5ByteOrder;
            return this;
        }

        @Override
        public FixedPointNumberBuilder withLoPadBit(final int loPadBit) {
            this.loPadBit = OptionalInt.of(loPadBit);
            return this;
        }

        @Override
        public FixedPointNumberBuilder withHiPadBit(final int hiPadBit) {
            this.hiPadBit = OptionalInt.of(hiPadBit);
            return this;
        }

        @Override
        public FixedPointNumberBuilder signed() {
            this.signed = true;
            return this;
        }

        @Override
        public FixedPointNumberBuilder unsigned() {
            this.signed = false;
            return this;
        }

        @Override
        public FixedPointNumberBuilder withBitOffset(final int bitOffset) {
            this.bitOffset = OptionalInt.of(bitOffset);
            return this;
        }

        @Override
        public FixedPointNumberBuilder withBitPrecision(final int bitPrecision) {
            this.bitPrecision = OptionalInt.of(bitPrecision);
            return this;
        }

    }

}
