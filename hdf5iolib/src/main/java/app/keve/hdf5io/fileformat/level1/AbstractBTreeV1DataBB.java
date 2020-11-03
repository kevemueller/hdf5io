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
package app.keve.hdf5io.fileformat.level1;

import java.nio.ByteBuffer;

import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextKTreeDimension;

public abstract class AbstractBTreeV1DataBB extends AbstractBTreeV1BB<SizingContextKTreeDimension>
        implements BTreeV1Data {

    public AbstractBTreeV1DataBB(final ByteBuffer buf, final SizingContextKTreeDimension sizingContext) {
        super(buf, sizingContext);
        assert NodeType.DATA == getNodeType();
    }

    public static final long minSize(final SizingContext sc) {
        final int k2 = sc.indexedStorageInternalNodeK() * 2;
        return 4 + 4 + 2 * sc.offsetSize() + (k2 + 1) * (4 + 4 + (MIN_DIMENSIONS + 1) * 8) + k2 * sc.offsetSize();
    }

    public static final long maxSize(final SizingContext sc) {
        final int k2 = sc.indexedStorageInternalNodeK() * 2;
        return 4 + 4 + 2 * sc.offsetSize() + (k2 + 1) * (4 + 4 + (MAX_DIMENSIONS + 1) * 8) + k2 * sc.offsetSize();
    }

    @Override
    public final long size() {
        final int k2 = context.indexedStorageInternalNodeK() * 2;
        return 4 + 4 + 2 * context.offsetSize() + (k2 + 1) * (4 + 4 + (context.dimensionality() + 1) * 8)
                + k2 * context.offsetSize();
    }

    @Override
    public final Resolvable<BTreeV1Data> getLeftSibling() {
        return getResolvable(8, BTreeV1Data.class, context);
    }

    @Override
    public final Resolvable<BTreeV1Data> getRightSibling() {
        return getResolvable(8 + context.offsetSize(), BTreeV1Data.class, context);
    }

    public static final class DataKeyJ implements DataKey {
        public final int sizeOfChunkInBytes;
        public final int filterMask;
        public final long[] dimChunkOffset;

        public DataKeyJ(final int sizeOfChunkInBytes, final int filterMask, final long[] dimChunkOffset) {
            this.sizeOfChunkInBytes = sizeOfChunkInBytes;
            this.filterMask = filterMask;
            this.dimChunkOffset = dimChunkOffset;
        }

        @Override
        public int getSizeOfChunkInBytes() {
            return sizeOfChunkInBytes;
        }

        @Override
        public int getFilterMask() {
            return filterMask;
        }

        @Override
        public long[] getDimChunkOffset() {
            return dimChunkOffset;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("DataKeyJ{");
            sb.append("sizeOfChunkInBytes=").append(sizeOfChunkInBytes);
            sb.append(", filterMask=").append(filterMask);
            sb.append(", dimChunkOffset=");
            if (dimChunkOffset == null) {
                sb.append("null");
            } else {
                sb.append('[');
                for (int i = 0; i < dimChunkOffset.length; ++i) {
                    sb.append(i == 0 ? "" : ", ").append(dimChunkOffset[i]);
                }
                sb.append(']');
            }
            sb.append('}');
            return sb.toString();
        }
    }

    @Override
    public final DataKey getKey(final int index) {
        final int dimensionality = context.dimensionality();
        int pos = 8 + 2 * context().offsetSize();
        pos += index * (8 + (context.dimensionality() + 1) * 8 + context.offsetSize());
        final int sizeOfChunkInBytes = getInt(pos);
        pos += 4;
        final int filterMask = getInt(pos);
        pos += 4;

        final long[] dimChunkOffset = new long[dimensionality + 1];
        for (int j = 0; j < dimChunkOffset.length; j++) {
            dimChunkOffset[j] = getLong(pos);
            pos += 8;
        }

        return new DataKeyJ(sizeOfChunkInBytes, filterMask, dimChunkOffset);
    }
}
