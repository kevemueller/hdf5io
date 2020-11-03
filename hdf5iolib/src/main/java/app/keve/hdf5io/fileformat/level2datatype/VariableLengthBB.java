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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import app.keve.hdf5io.api.datatype.HDF5VariableLength;
import app.keve.hdf5io.fileformat.H5Context;

public final class VariableLengthBB extends AbstractDatatypeBB implements HDF5VariableLength {
    private static final int BF_TYPE_MASK = 0x00F;
    private static final int BF_PADDING_MASK = 0x0F0;
    private static final int BF_CHARSET_MASK = 0xF00;

    public VariableLengthBB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    @Override
    public long size() {
        return 8 + getBaseType().size();
    }

    @Override
    public Type getType() {
        switch (getClassBitField() & BF_TYPE_MASK) {
        case 0:
            return Type.SEQUENCE;
        case 1:
            return Type.STRING;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public PaddingType getPaddingType() {
        switch (getClassBitField() & BF_PADDING_MASK) {
        case 0 << 4:
            return PaddingType.NULL_TERMINATE;
        case 1 << 4:
            return PaddingType.NULL_PAD;
        case 2 << 4:
            return PaddingType.SPACE_PAD;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Charset getCharset() {
        switch (getClassBitField() & BF_CHARSET_MASK) {
        case 0 << 8:
            return StandardCharsets.US_ASCII;
        case 1 << 8:
            return StandardCharsets.UTF_8;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public AbstractDatatypeBB getBaseType() {
        return getEmbedded(8, AbstractDatatypeBB.class);
    }

    @Override
    public String toString() {
        return String.format(
                "VariableLengthBB [size()=%s, getType()=%s, getPaddingType()=%s, getCharset()=%s, getBaseType()=%s, "
                        + "getVersion()=%s, getDatatypeClass()=%s, getClassBitField()=%s, getElementSize()=%s]",
                size(), getType(), getPaddingType(), getCharset(), getBaseType(), getVersion(), getDatatypeClass(),
                getClassBitField(), getElementSize());
    }

}
