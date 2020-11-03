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

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.SizingContext;

public final class DataspaceMessageV2BB extends AbstractSizedBB<SizingContext> implements DataspaceMessageV2 {
    public DataspaceMessageV2BB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long minSize(final SizingContext sc) {
        return 4 + MIN_DIMENSIONS * 1 * sc.lengthSize();
    }

    public static long maxSize(final SizingContext sc) {
        return 4 + MAX_DIMENSIONS * 2 * sc.lengthSize();
    }

    @Override
    public long size() {
        return 4 + getDimensionality() * (context.lengthSize() + (isMaxDimensionPresent() ? context.lengthSize() : 0));
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public int getVersion() {
        return getUnsignedByte(0);
    }

    @Override
    public void setVersion(final int value) {
        setByte(0, value);
    }

    @Override
    public int getDimensionality() {
        return getUnsignedByte(1);
    }

    @Override
    public void setDimensionality(final int value) {
        setByte(1, value);
    }

    @Override
    public int getFlags() {
        return getUnsignedByte(2);
    }

    @Override
    public void setFlags(final int value) {
        setByte(2, value);
    }

    @Override
    public DataspaceType getDataspaceType() {
        switch (getByte(3)) {
        case 0:
            return DataspaceType.SCALAR;
        case 1:
            return DataspaceType.SIMPLE;
        case 2:
            return DataspaceType.NULL;
        default:
            throw new IllegalArgumentException("dataspace type " + getByte(3));
        }
    }

    @Override
    public long[] getDimensionSizes() {
        final long[] dimensions = new long[getDimensionality()];
        for (int i = 0; i < dimensions.length; i++) {
            dimensions[i] = getLength(4 + i * context.lengthSize());
        }
        return dimensions;
    }

    @Override
    public void setDimensionSizes(final long... value) {
        for (int i = 0; i < value.length; i++) {
            setLength(4 + i * context.lengthSize(), value[i]);
        }
    }

    @Override
    public long[] getDimensionMaxSizes() {
        if ((getFlags() & MAX_DIMENSION_PRESENT_MASK) > 0) {
            final long[] dimensions = new long[getDimensionality()];
            for (int i = 0; i < dimensions.length; i++) {
                dimensions[i] = getLength(4 + getDimensionality() * context.lengthSize() + i * context.lengthSize());
            }
            return dimensions;
        } else {
            return null;
        }
    }

    @Override
    public void setDimensionMaxSizes(final long... value) {
        if ((getFlags() & MAX_DIMENSION_PRESENT_MASK) > 0) {
            for (int i = 0; i < value.length; i++) {
                setLength(4 + getDimensionality() * context.lengthSize() + i * context.lengthSize(), value[i]);
            }
        } else {
        }
    }

}
