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
import java.util.Iterator;

import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextBTreeV2Node;
import app.keve.hdf5io.util.JenkinsHash;

public final class BTreeV2LeafNodeBB extends AbstractBTreeV2NodeBB implements BTreeV2LeafNode {
    public BTreeV2LeafNodeBB(final ByteBuffer buf, final SizingContextBTreeV2Node sizingContext) {
        super(buf, sizingContext);
    }

    public static long minSize(final SizingContext sc) {
        return 6L + 0 * UINT16_MAX_VALUE + 4;
    }

    public static long maxSize(final SizingContext sc) {
        return 6L + (long) UINT16_MAX_VALUE * (long) UINT16_MAX_VALUE + 4;
    }

    @Override
    public boolean isValid() {
        return Arrays.equals(SIGNATURE, getSignature()) && JenkinsHash
                .hash(getEmbeddedData(0, (int) size() - 4).order(ByteOrder.LITTLE_ENDIAN), 0) == getChecksum();
    }

    @Override
    public long size() {
        return 6 + context.recordNum() * context.recordSize() + 4;
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
    public Iterator<? extends Record> iteratorRecords() {
        return getIterator(6, LinkNameRecord.class, context.recordNum(), context);
    }

    @Override
    public int getChecksum() {
        return getInt(6 + context.recordNum() * context.recordSize());
    }

}
