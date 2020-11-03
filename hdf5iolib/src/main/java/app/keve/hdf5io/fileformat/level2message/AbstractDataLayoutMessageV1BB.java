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
package app.keve.hdf5io.fileformat.level2message;

import java.nio.ByteBuffer;

import app.keve.hdf5io.fileformat.SizingContext;

public abstract class AbstractDataLayoutMessageV1BB extends AbstractDataLayoutMessageBB {
    protected final int dimensionOffset;

    protected AbstractDataLayoutMessageV1BB(final ByteBuffer buf, final SizingContext sizingContext,
            final int dimensionOffset) {
        super(buf, sizingContext);
        this.dimensionOffset = dimensionOffset;
    }

    public static final long minSize(final SizingContext sc) {
        return 8 + 1 * 4;
    }

    public static final long maxSize(final SizingContext sc) {
//        return 8 + sc.offsetSize() + MAX_DIMENSIONS * 4 + ;
        return MAX_MESSAGE_DATA;
    }

    @Override
    public final boolean isValid() {
        return 0 == getByte(3) && 0 == getInt(4);
    }

    @Override
    public final Layout getLayoutClass() {
        switch (getByte(2)) {
        case 0:
            return Layout.COMPACT;
        case 1:
            return Layout.CONTIGUOUS;
        case 2:
            return Layout.CHUNKED;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public final void setLayoutClass(final Layout value) {
        setByte(2, value.ordinal());
    }

//    @Override
    public final int getDimensionality() {
        return Layout.CHUNKED == getLayoutClass() ? getUnsignedByte(1) - 1 : getUnsignedByte(1);
    }

    public final void setDimensionality(final int value) {
        setByte(1, Layout.CHUNKED == getLayoutClass() ? value + 1 : value);
        resize();
    }

//    @Override
    public final long[] getDimensionSizes() {
        final long[] dimensionSizes = new long[getDimensionality()];
        for (int i = 0; i < dimensionSizes.length; i++) {
            dimensionSizes[i] = getUnsignedInt(dimensionOffset + i * 4);
        }
        return dimensionSizes;
    }

}
