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

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextBTreeV2;
import app.keve.hdf5io.fileformat.SizingContextBTreeV2Node;
import app.keve.hdf5io.util.JenkinsHash;

public final class BTreeV2BB extends AbstractSizedBB<SizingContextBTreeV2> implements BTreeV2 {
    public BTreeV2BB(final ByteBuffer buf, final SizingContextBTreeV2 sizingContext) {
        super(buf, sizingContext);
    }

    public static long size(final SizingContext sc) {
        return 22 + sc.offsetSize() + sc.lengthSize();
    }

    @Override
    public long size() {
        return size(context);
    }

    @Override
    public boolean isValid() {
        return Arrays.equals(SIGNATURE, getSignature()) && JenkinsHash
                .hash(getEmbeddedData(0, (int) size() - 4).order(ByteOrder.LITTLE_ENDIAN), 0) == getChecksum();
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
    public int getType() {
        return getUnsignedByte(5);
    }

    @Override
    public long getNodeSize() {
        return getUnsignedInt(6);
    }

    @Override
    public int getRecordSize() {
        return getUnsignedShort(10);
    }

    @Override
    public int getDepth() {
        return getUnsignedShort(12);
    }

    @Override
    public int getSplitPercent() {
        return getUnsignedByte(14);
    }

    @Override
    public int getMergePercent() {
        return getUnsignedByte(15);
    }

    @Override
    public Resolvable<? extends BTreeV2Node> getRootNode() {
        final SizingContextBTreeV2Node context2 = SizingContextBTreeV2Node.of(context, context.fractalHeap(),
                getRecordSize(), getRootNumberOfRecords());
        return 0 == getDepth() ? getResolvable(16, BTreeV2LeafNode.class, context2) : null;
    }

    @Override
    public int getRootNumberOfRecords() {
        return getUnsignedShort(16 + context.offsetSize());
    }

    @Override
    public long getTotalNumberOfRecords() {
        return getLength(16 + context.offsetSize() + 2);
    }

    @Override
    public int getChecksum() {
        return getInt(18 + context.offsetSize() + context.lengthSize());
    }

}
