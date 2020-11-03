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
import java.util.Arrays;
import java.util.Iterator;

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextBTreeV2;
import app.keve.hdf5io.fileformat.SizingContextSOHMT;

public final class SharedObjectHeaderMessageTableBB extends AbstractSizedBB<SizingContextSOHMT>
        implements SharedObjectHeaderMessageTable {
    private final int elementSize;

    public SharedObjectHeaderMessageTableBB(final ByteBuffer buf, final SizingContextSOHMT sizingContext) {
        super(buf, sizingContext);
        this.elementSize = (int) AbstractSharedObjectHeaderMessageIndexBB.size(context);
    }

    public static long minSize(final SizingContext sc) {
        return 4 + 4;
    }

    public static long maxSize(final SizingContext sc) {
        return Long.MAX_VALUE;
    }

    @Override
    public long size() {
        return 4 + context.numberOfIndices() * elementSize + 4;
    }

    @Override
    public boolean isValid() {
        return Arrays.equals(SIGNATURE, getSignature()) && true /* FIXME: hash */;
    }

    @Override
    public byte[] getSignature() {
        return getBytes(0, 4);
    }

    @Override
    public Iterator<SharedObjectHeaderMessageIndex> indexIterator() {
        return getIterator(4, SharedObjectHeaderMessageIndex.class, context.numberOfIndices(), context);
    }

    @Override
    public SharedObjectHeaderMessageIndex getIndex(final int index) {
        return getEmbedded(4 + index * elementSize, elementSize, SharedObjectHeaderMessageIndex.class, context);
    }

    @Override
    public int getChecksum() {
        return getInt((int) size() - 4);
    }

    public abstract static  class AbstractSharedObjectHeaderMessageIndexBB extends AbstractSizedBB<SizingContext>
            implements SharedObjectHeaderMessageIndex {

        public AbstractSharedObjectHeaderMessageIndexBB(final ByteBuffer buf, final SizingContext sizingContext) {
            super(buf, sizingContext);
        }

        public static long size(final SizingContext sc) {
            return 14 + 2 * sc.offsetSize();
        }

        @Override
        public final long size() {
            return 14 + 2 * context.offsetSize();

        }

        @Override
        public final boolean isValid() {
            return true;
        }

        @Override
        public final int getVersion() {
            return getByte(0);
        }

        @Override
        public final IndexType getIndexType() {
            // spec problem: values not documented
            switch (getByte(1)) {
            case 0:
                return IndexType.LIST;
            case 1:
                return IndexType.BTREE;
            default:
                throw new IllegalArgumentException();
            }
        }

        @Override
        public final int getMessageTypeFlags() {
            return getUnsignedShort(2);
        }

        @Override
        public final long getMinimumMessageSize() {
            return getUnsignedInt(4);
        }

        @Override
        public final int getListCutoff() {
            return getUnsignedShort(8);
        }

        @Override
        public final int getBTreeV2Cutoff() {
            return getUnsignedShort(10);
        }

        @Override
        public final int getNumberOfMessages() {
            return getUnsignedShort(12);
        }

        @Override
        public final Resolvable<FractalHeap> getFractalHeap() {
            return getResolvable(14 + context.offsetSize(), 0, FractalHeap.class, context);
        }

        public static final SharedObjectHeaderMessageIndex of(final ByteBuffer buf, final SizingContext context) {
            switch (buf.get(1)) {
            case 0:
                return new SharedObjectHeaderMessageIndexRecordListBB(buf, context);
            case 1:
                return new SharedObjectHeaderMessageIndexBTreeBB(buf, context);
            default:
                throw new IllegalArgumentException();
            }
        }
    }

    public static final class SharedObjectHeaderMessageIndexRecordListBB
            extends AbstractSharedObjectHeaderMessageIndexBB implements SharedObjectHeaderMessageIndexRecordList {

        public SharedObjectHeaderMessageIndexRecordListBB(final ByteBuffer buf, final SizingContext sizingContext) {
            super(buf, sizingContext);
        }

        @Override
        public long getIndexAddress() {
            return getOffset(14);
        }
    }

    public static final class SharedObjectHeaderMessageIndexBTreeBB extends AbstractSharedObjectHeaderMessageIndexBB
            implements SharedObjectHeaderMessageIndexBTree {

        public SharedObjectHeaderMessageIndexBTreeBB(final ByteBuffer buf, final SizingContext sizingContext) {
            super(buf, sizingContext);
        }

        @Override
        public Resolvable<BTreeV2> getIndex() {
            final SizingContextBTreeV2 context2 = SizingContextBTreeV2.of(context, getFractalHeap());
            return getResolvable(14, 0, BTreeV2.class, context2);
        }
    }
}
