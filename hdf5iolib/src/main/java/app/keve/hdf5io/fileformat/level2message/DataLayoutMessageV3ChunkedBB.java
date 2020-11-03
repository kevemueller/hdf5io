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

import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextKTreeDimension;
import app.keve.hdf5io.fileformat.level1.BTreeV1Data;

// TODO: SizingContext -> SizingContextKTree
public final class DataLayoutMessageV3ChunkedBB extends AbstractDataLayoutMessageV3BB
        implements DataLayoutMessageV3Chunked {
    public DataLayoutMessageV3ChunkedBB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long minSize(final SizingContext sc) {
        return 2 + 1 + sc.offsetSize() + 1 * 4 + 4;
    }

    public static long maxSize(final SizingContext sc) {
        return 2 + 1 + sc.offsetSize() + MAX_DIMENSIONS * 4 + 4;
    }

    @Override
    public long size() {
        return 2 + 1 + context.offsetSize() + getDimensionality() * 4 + 4;
    }

    @Override
    public int getDimensionality() {
        // FIXME: Specification problem we need to subtract 1 to get this working.
        return getByte(2) - 1;
    }

    @Override
    public Resolvable<BTreeV1Data> getData() {
        final SizingContextKTreeDimension scKD = SizingContextKTreeDimension.of(context, getDimensionality());
        return getResolvable(3, BTreeV1Data.class, scKD);
    }

    @Override
    public long[] getDimensionSizes() {
        final long[] dim = new long[getDimensionality()];
        for (int i = 0; i < dim.length; i++) {
            dim[i] = getUnsignedInt(3 + context.offsetSize() + i * 4);
        }
        return dim;
    }

    @Override
    public long getDatasetElementSize() {
        return getUnsignedInt(3 + context.offsetSize() + getDimensionality() * 4);
    }

    @Override
    public String toString() {
        final int maxLen = 10;
        return String.format(
                "DataLayoutMessageV3ChunkedBB [size()=%s, getDimensionality()=%s, getData()=%s, getDimensionSizes()=%s, "
                        + "getDatasetElementSize()=%s, isValid()=%s, getLayoutClass()=%s, getVersion()=%s, getBuffer()=%s]",
                size(), getDimensionality(), getData(),
                getDimensionSizes() != null
                        ? Arrays.toString(
                                Arrays.copyOf(getDimensionSizes(), Math.min(getDimensionSizes().length, maxLen)))
                        : null,
                getDatasetElementSize(), isValid(), getLayoutClass(), getVersion(), getBuffer());
    }

}
