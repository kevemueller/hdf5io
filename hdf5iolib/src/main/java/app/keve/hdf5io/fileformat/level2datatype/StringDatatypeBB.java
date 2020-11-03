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

import app.keve.hdf5io.api.datatype.HDF5String;
import app.keve.hdf5io.fileformat.H5Context;

public final class StringDatatypeBB extends AbstractDatatypeBB implements HDF5String {
    private static final int BF_PADDING_MASK = 0x0F;
    private static final int BF_CHARSET_MASK = 0xF0;

    public StringDatatypeBB(final ByteBuffer of, final H5Context context) {
        super(of, context);
    }

    @Override
    public long size() {
        return 8;
    }

    @Override
    public PaddingType getPaddingType() {
        switch (getClassBitField() & BF_PADDING_MASK) {
        case 0:
            return PaddingType.NULL_TERMINATE;
        case 1:
            return PaddingType.NULL_PAD;
        case 2:
            return PaddingType.SPACE_PAD;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Charset getCharset() {
        switch (getClassBitField() & BF_CHARSET_MASK) {
        case 0 << 4:
            return StandardCharsets.US_ASCII;
        case 1 << 4:
            return StandardCharsets.UTF_8;
        default:
            throw new IllegalArgumentException();
        }
    }

}
