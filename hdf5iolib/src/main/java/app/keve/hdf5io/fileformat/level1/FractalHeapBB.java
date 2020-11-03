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
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextFractalHeap;
import app.keve.hdf5io.util.JenkinsHash;

public final class FractalHeapBB extends AbstractSizedBB<SizingContext> implements FractalHeap {
    public FractalHeapBB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long minSize(final SizingContext sc) {
        return 20 + 12 * sc.lengthSize() + 3 * sc.offsetSize();
    }

    public static long maxSize(final SizingContext sc) {
        return 20 + 12 * sc.lengthSize() + 3 * sc.offsetSize() + UINT32_MAX_VALUE;
    }

    @Override
    public long size() {
        return available();
    }

    @Override
    public boolean isValid() {
        return Arrays.equals(SIGNATURE, getSignature());
    }

    @Override
    public byte[] getSignature() {
        return getBytes(0, 4);
    }

    @Override
    public int getVersion() {
        return getUnsignedByte(4);
    }

    @Override
    public int getHeapIdLength() {
        return getUnsignedShort(5);
    }

    @Override
    public int getIOFiltersEncodedLength() {
        return getUnsignedShort(7);
    }

    @Override
    public int getFlags() {
        return getUnsignedByte(9);
    }

    @Override
    public int getMaximumSizeOfManagedObjects() {
        return getInt(10);
    }

    @Override
    public long getNextHugeObjectId() {
        return getLength(14);
    }

    @Override
    public long getBTreeV2HugeObjects() {
        return getOffset(14 + context.lengthSize());
    }

    @Override
    public long getFreeSpaceAmount() {
        return getLength(14 + context.lengthSize() + context.offsetSize());
    }

    @Override
    public long getFreeSpaceManager() {
        return getOffset(14 + 2 * context.lengthSize() + context.offsetSize());
    }

    @Override
    public long getManagedSpaceAmount() {
        return getLength(14 + 2 * context.lengthSize() + 2 * context.offsetSize());
    }

    @Override
    public long getAllocatedManagedSpaceAmount() {
        return getLength(14 + 3 * context.lengthSize() + 2 * context.offsetSize());
    }

    @Override
    public long getDirectBlockAllocationIterator() {
        return getLength(14 + 4 * context.lengthSize() + 2 * context.offsetSize());
    }

    @Override
    public long getManagedObjectNumber() {
        return getLength(14 + 5 * context.lengthSize() + 2 * context.offsetSize());
    }

    @Override
    public long getHugeObjectsSize() {
        return getLength(14 + 6 * context.lengthSize() + 2 * context.offsetSize());
    }

    @Override
    public long getHugeObjectsNumber() {
        return getLength(14 + 7 * context.lengthSize() + 2 * context.offsetSize());
    }

    @Override
    public long getTinyObjectsSize() {
        return getLength(14 + 8 * context.lengthSize() + 2 * context.offsetSize());
    }

    @Override
    public long getTinyObjectsNumber() {
        return getLength(14 + 9 * context.lengthSize() + 2 * context.offsetSize());
    }

    @Override
    public int getTableWidth() {
        return getUnsignedShort(14 + 10 * context.lengthSize() + 2 * context.offsetSize());
    }

    @Override
    public long getStartingBlockSize() {
        return getLength(16 + 10 * context.lengthSize() + 2 * context.offsetSize());
    }

    @Override
    public long getMaximumDirectBlockSize() {
        return getLength(16 + 11 * context.lengthSize() + 2 * context.offsetSize());
    }

    @Override
    public int getMaximumHeapSize() {
        return getUnsignedShort(16 + 12 * context.lengthSize() + 2 * context.offsetSize());
    }

    @Override
    public int getStartingNumberOfRowsIndirectBlock() {
        return getUnsignedShort(18 + 12 * context.lengthSize() + 2 * context.offsetSize());
    }

    @Override
    public Resolvable<? extends HeapBlock> getRootBlock() {
        final SizingContextFractalHeap context2 = SizingContextFractalHeap.of(context, getMaximumHeapSize(),
                isBlockChecksum(), (int) getStartingBlockSize());
        return getResolvable(20 + 12 * context.lengthSize() + 2 * context.offsetSize(), DirectBlock.class, context2);
    }

    @Override
    public int getCurrentNumberOfRowsIndirectBlock() {
        return getUnsignedShort(20 + 12 * context.lengthSize() + 3 * context.offsetSize());
    }

    @Override
    public OptionalLong getFilteredBlockSize() {
        return OptionalLong.empty();
    }

    @Override
    public OptionalInt getIOFilterMask() {
        return OptionalInt.empty();
    }

    @Override
    public Object getIOFilterInformation() {
        return null;
    }

    @Override
    public int getChecksum() {
        return 0;
    }

    public abstract static class AbstractHeapBlockBB extends AbstractSizedBB<SizingContextFractalHeap>
            implements HeapBlock {
        public AbstractHeapBlockBB(final ByteBuffer buf, final SizingContextFractalHeap sizingContext) {
            super(buf, sizingContext);
        }

        public static long minSize(final SizingContext context) {
            return DirectBlockBB.minSize(context);
        }

        public static long maxSize(final SizingContext context) {
            return DirectBlockBB.maxSize(context);
        }

        public static final HeapBlock of(final ByteBuffer buf, final SizingContextFractalHeap context) {
            throw new IllegalArgumentException();
        }
    }

    public static final class DirectBlockBB extends AbstractHeapBlockBB implements DirectBlock {
        public DirectBlockBB(final ByteBuffer buf, final SizingContextFractalHeap sizingContext) {
            super(buf, sizingContext);
        }

        public static long minSize(final SizingContext context) {
            return 5 + context.offsetSize();
        }

        public static long maxSize(final SizingContext context) {
            return 5 + context.offsetSize() + UINT32_MAX_VALUE; // TODO:
        }

        @Override
        public boolean isValid() {
            if (!Arrays.equals(SIGNATURE, getSignature())) {
                return false;
            }
            if (context.blockChecksum()) {
                // what a mess, to compute the checksum we need to make a copy of the block...
                final ByteBuffer chkBuf = ByteBuffer.wrap(getBytes(0, (int) size()));
                chkBuf.putInt(getChecksumOffset(), 0);
                final int checksum = JenkinsHash.hash(chkBuf.order(ByteOrder.LITTLE_ENDIAN), 0);
                return getChecksum().getAsInt() == checksum;
            }
            return true;
        }

        @Override
        public long size() {
            return context.directBlockSize();
        }

        @Override
        public byte[] getSignature() {
            return getBytes(0, 4);
        }

        @Override
        public int getVersion() {
            return getUnsignedByte(4);
        }

        @Override
        public Resolvable<FractalHeap> getHeapHeader() {
            return getResolvable(5, FractalHeap.class, context);
        }

        @Override
        public long getBlockOffset() {
            final int sizeInBytes = (context.maximumHeapSize() + 7) / 8;
            return getUnsignedNumber(5 + context.offsetSize(), sizeInBytes);
        }

        private int getChecksumOffset() {
            int ofs = 5 + context.offsetSize();
            ofs += (context.maximumHeapSize() + 7) / 8;
            return ofs;
        }

        @Override
        public OptionalInt getChecksum() {
            return context.blockChecksum() ? OptionalInt.of(getInt(getChecksumOffset())) : OptionalInt.empty();
        }

        // TODO: offsets are given towards the start of the buffer, so this does not
        // actually count
        @Override
        public ByteBuffer getObjectData() {
            final int ofs = getChecksumOffset() + (context.blockChecksum() ? 4 : 0);
            return getEmbeddedData(ofs, context.directBlockSize() - ofs);
        }

        @Override
        public ByteBuffer getObjectData(final int idx, final int length) {
            return getEmbeddedData(idx, length);
        }

    }
}
