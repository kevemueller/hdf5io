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
import java.util.List;

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.level1.ExtensibleArrayIndex;
import app.keve.hdf5io.fileformat.level1.FixedArrayIndex;

public final class DataLayoutMessageV4ChunkedBB extends AbstractDataLayoutMessageV4BB
        implements DataLayoutMessageV4Chunked {
    public DataLayoutMessageV4ChunkedBB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long minSize(final SizingContext sc) {
        return 2 + 3 + MIN_DIMENSIONS * 1 + 1 + sc.offsetSize();
    }

    public static long maxSize(final SizingContext sc) {
        return 2 + 3 + MAX_DIMENSIONS * 8 + 1 + ChunkIndexBTreeV2BB.size(sc);
    }

    @Override
    public long size() {
        int size = 2;
        size += 3;
        size += getDimensionality() * getDimensionSizeEncodedLength();
        size += 1;
        switch (getChunkIndexingType()) {
        case SINGLE:
            size += context.lengthSize();
            size += 4;
            size += context.offsetSize();
            break;
        case IMPLICIT:
            size += ChunkIndexImplicitBB.size(context);
            break;
        case FIXED_ARRAY:
            size += ChunkIndexFixedArrayBB.size(context);
            break;
        case EXTENSIBLE_ARRAY:
            size += ChunkIndexExtensibleArrayBB.size(context);
            break;
        case VERSION2_BTREE:
            size += ChunkIndexBTreeV2BB.size(context);
            break;
        default:
            throw new IllegalArgumentException("Implement " + getChunkIndexingType());
        }
        return size;
    }

    @Override
    public int getFlags() {
        return getUnsignedByte(2);
    }

    @Override
    public int getDimensionality() {
        return getUnsignedByte(2 + 1);
    }

    @Override
    public int getDimensionSizeEncodedLength() {
        return getUnsignedByte(2 + 2);
    }

    @Override
    public long[] getDimensionSizes() {
        final long[] dimensionSizes = new long[getDimensionality()];
        final int nb = getDimensionSizeEncodedLength();
        int idx = 5;
        for (int i = 0; i < dimensionSizes.length; i++) {
            dimensionSizes[i] = getUnsignedNumber(idx, nb);
            idx += nb;
        }
        return dimensionSizes;
    }

    @Override
    public ChunkIndexingType getChunkIndexingType() {
        int idx = 5;
        idx += getDimensionality() * getDimensionSizeEncodedLength();

        switch (getByte(idx)) {
        case 1:
            return ChunkIndexingType.SINGLE;
        case 2:
            return ChunkIndexingType.IMPLICIT;
        case 3:
            return ChunkIndexingType.FIXED_ARRAY;
        case 4:
            return ChunkIndexingType.EXTENSIBLE_ARRAY;
        case 5:
            return ChunkIndexingType.VERSION2_BTREE;
        default:
            throw new IllegalArgumentException("Unknown chunk type " + getByte(idx));
        }
    }

    @Override
    public ChunkIndexingInformation getChunkIndexingInformation() {
        int idx = 5;
        idx += getDimensionality() * getDimensionSizeEncodedLength();
        idx += 1;
        switch (getChunkIndexingType()) {
        case IMPLICIT:
            return getEmbedded(idx, ImplicitIndexingInformation.class);
        case FIXED_ARRAY:
            return getEmbedded(idx, FixedArrayIndexingInformation.class);
        case EXTENSIBLE_ARRAY:
            return getEmbedded(idx, ExtensibleArrayIndexingInformation.class);
        case VERSION2_BTREE:
            return getEmbedded(idx, Version2BTreeIndexingInformation.class);
        default:
            throw new IllegalArgumentException("Implement v4 chunk indexing type " + getChunkIndexingType());
        }
    }

    public abstract static class AbstractChunkIndexingInformationBB extends AbstractSizedBB<SizingContext>
            implements ChunkIndexingInformation {

        public AbstractChunkIndexingInformationBB(final ByteBuffer buf, final SizingContext sizingContext) {
            super(buf, sizingContext);
        }

        public static long minSize(final SizingContext sc) {
            return Long.min(ChunkIndexSingleChunkBB.size(sc),
                    Long.min(ChunkIndexImplicitBB.size(sc), Long.min(ChunkIndexFixedArrayBB.size(sc),
                            Long.min(ChunkIndexExtensibleArrayBB.size(sc), ChunkIndexBTreeV2BB.size(sc)))));
        }

        public static long maxSize(final SizingContext sc) {
            return Long.max(ChunkIndexSingleChunkBB.size(sc),
                    Long.max(ChunkIndexImplicitBB.size(sc), Long.max(ChunkIndexFixedArrayBB.size(sc),
                            Long.max(ChunkIndexExtensibleArrayBB.size(sc), ChunkIndexBTreeV2BB.size(sc)))));
        }

        public static ChunkIndexingInformation of(final ByteBuffer buf, final SizingContext sc) {
            throw new IllegalArgumentException();
        }

    }

    public static final class ChunkIndexSingleChunkBB extends AbstractChunkIndexingInformationBB
            implements SingleChunkIndexingInformation {

        public ChunkIndexSingleChunkBB(final ByteBuffer buf, final SizingContext sizingContext) {
            super(buf, sizingContext);
        }

        public static long size(final SizingContext sc) {
            return sc.lengthSize() + 4;
        }

        @Override
        public long size() {
            return context.lengthSize() + 4;
        }

        @Override
        public long getFilteredChunkSize() {
            return getLength(0);
        }

        @Override
        public int getFilters() {
            return getSmallUnsignedInt(context.lengthSize());
        }
    }

    public static final class ChunkIndexImplicitBB extends AbstractChunkIndexingInformationBB
            implements ImplicitIndexingInformation {

        public ChunkIndexImplicitBB(final ByteBuffer buf, final SizingContext sizingContext) {
            super(buf, sizingContext);
        }

        public static long size(final SizingContext sc) {
            return sc.offsetSize();
        }

        @Override
        public long size() {
            return context.offsetSize();
        }

        @Override
        public long getIndex() {
            return getOffset(0);
        }

    }

    public static final class ChunkIndexFixedArrayBB extends AbstractChunkIndexingInformationBB
            implements FixedArrayIndexingInformation {
        public ChunkIndexFixedArrayBB(final ByteBuffer buf, final SizingContext sizingContext) {
            super(buf, sizingContext);
        }

        public static long size(final SizingContext sc) {
            return 1 + sc.offsetSize();
        }

        @Override
        public long size() {
            return 1 + context.offsetSize();
        }

        @Override
        public int getPageBits() {
            return getUnsignedByte(0);
        }

        @Override
        public Resolvable<FixedArrayIndex> getIndex() {
            return getResolvable(1, FixedArrayIndex.class, context);
        }
    }

    public static final class ChunkIndexExtensibleArrayBB extends AbstractChunkIndexingInformationBB
            implements ExtensibleArrayIndexingInformation {
        public ChunkIndexExtensibleArrayBB(final ByteBuffer buf, final SizingContext sizingContext) {
            super(buf, sizingContext);
        }

        public static long size(final SizingContext sc) {
            return 5 + sc.offsetSize();
        }

        @Override
        public long size() {
            return 5 + context.offsetSize();
        }

        @Override
        public int getMaxBits() {
            return getUnsignedByte(0);
        }

        @Override
        public int getIndexElements() {
            return getUnsignedByte(1);
        }

        @Override
        public int getMinPointers() {
            return getUnsignedByte(2);
        }

        @Override
        public int getMinElements() {
            return getUnsignedByte(3);
        }

        @Override
        public int getPageBits() {
            // specification problem: short vs. byte
            return getUnsignedByte(4);
        }

        @Override
        public Resolvable<ExtensibleArrayIndex> getIndex() {
            return getResolvable(5, ExtensibleArrayIndex.class, context);
        }

    }

    public static final class ChunkIndexBTreeV2BB extends AbstractChunkIndexingInformationBB
            implements Version2BTreeIndexingInformation {
        public ChunkIndexBTreeV2BB(final ByteBuffer buf, final SizingContext sizingContext) {
            super(buf, sizingContext);
        }

        public static long size(final SizingContext sc) {
            return 6 + sc.offsetSize();
        }

        @Override
        public long size() {
            return 6 + context.offsetSize();
        }

        @Override
        public long getNodeSize() {
            return getUnsignedInt(0);
        }

        @Override
        public int getSplitPercent() {
            return getUnsignedByte(4);
        }

        @Override
        public int getMergePercent() {
            return getUnsignedByte(5);
        }

        @Override
        public long getIndex() {
            return getOffset(6);
        }

    }
}
