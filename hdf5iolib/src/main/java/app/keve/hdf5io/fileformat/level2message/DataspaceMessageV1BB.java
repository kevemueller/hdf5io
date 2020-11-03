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
import java.util.Arrays;

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.SizingContext;

public final class DataspaceMessageV1BB extends AbstractSizedBB<SizingContext> implements DataspaceMessageV1 {
    public DataspaceMessageV1BB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long minSize(final SizingContext sc) {
        return 8 + sc.lengthSize();
    }

    public static long maxSize(final SizingContext sc) {
        return 8 + MAX_DIMENSIONS * 3 * sc.lengthSize();
    }

    @Override
    public long size() {
        return 8 + getDimensionality() * (context.lengthSize() + (isMaxDimensionPresent() ? context.lengthSize() : 0)
                + (isPermutationIndexPresent() ? context.lengthSize() : 0));
    }

    @Override
    public boolean isValid() {
        return 0 == getByte(3) && 0 == getInt(4);
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
        resize();
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
    public long[] getDimensionSizes() {
        final long[] dimensions = new long[getDimensionality()];
        for (int i = 0; i < dimensions.length; i++) {
            dimensions[i] = getLength(8 + i * context.lengthSize());
        }
        return dimensions;
    }

    @Override
    public void setDimensionSizes(final long... value) {
        setDimensionality(value.length);
        for (int i = 0; i < value.length; i++) {
            setLength(8 + i * context.lengthSize(), value[i]);
        }
    }

    @Override
    public long[] getDimensionMaxSizes() {
        if (isMaxDimensionPresent()) {
            final long[] dimensions = new long[getDimensionality()];
            for (int i = 0; i < dimensions.length; i++) {
                dimensions[i] = getLength(8 + getDimensionality() * context.lengthSize() + i * context.lengthSize());
            }
            return dimensions;
        } else {
            return null;
        }
    }

    @Override
    public long[] getPermutationIndex() {
        if (isPermutationIndexPresent()) {
            final long[] dimensions = new long[getDimensionality()];
            for (int i = 0; i < dimensions.length; i++) {
                dimensions[i] = getLength(
                        8 + getDimensionality() * context.lengthSize() * 2 + i * context.lengthSize());
            }
            return dimensions;
        } else {
            return null;
        }
    }

    @Override
    public void setPermutationIndex(final long... value) {
        setPermutationIndexPresent(true);
        for (int i = 0; i < value.length; i++) {
            setLength(8 + getDimensionality() * context.lengthSize() * 2 + i * context.lengthSize(), value[i]);
        }
    }

    @Override
    public void setDimensionMaxSizes(final long... value) {
        setMaxDimensionPresent(true);
        for (int i = 0; i < value.length; i++) {
            setLength(8 + getDimensionality() * context.lengthSize() + i * context.lengthSize(), value[i]);
        }
    }

    @Override
    public void initialize() {
        setVersion(1);
        setFlags(0);
        setByte(3, 0);
        setInt(4, 0);
    }

    @Override
    public String toString() {
        final int maxLen = 10;
        return String.format(
                "DataspaceMessageV1BB [size()=%s, isValid()=%s, getVersion()=%s, "
                        + "getDimensionality()=%s, getFlags()=%s, getDimensionSizes()=%s, getDimensionMaxSizes()=%s, "
                        + "getPermutationIndex()=%s, getBuffer()=%s]",
                size(), isValid(), getVersion(), getDimensionality(), getFlags(),
                getDimensionSizes() != null
                        ? Arrays.toString(
                                Arrays.copyOf(getDimensionSizes(), Math.min(getDimensionSizes().length, maxLen)))
                        : null,
                getDimensionMaxSizes() != null
                        ? Arrays.toString(
                                Arrays.copyOf(getDimensionMaxSizes(), Math.min(getDimensionMaxSizes().length, maxLen)))
                        : null,
                getPermutationIndex() != null
                        ? Arrays.toString(
                                Arrays.copyOf(getPermutationIndex(), Math.min(getPermutationIndex().length, maxLen)))
                        : null,
                getBuffer());
    }

}
