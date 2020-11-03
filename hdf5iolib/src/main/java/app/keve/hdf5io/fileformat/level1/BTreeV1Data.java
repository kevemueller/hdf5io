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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextKTreeDimension;

public interface BTreeV1Data extends BTreeV1<SizingContextKTreeDimension> {

    static long minSize(final SizingContext sc) {
        return Long.min(AbstractBTreeV1DataBB.minSize(sc), AbstractBTreeV1DataBB.minSize(sc));
    }

    static long maxSize(final SizingContext sc) {
        return Long.max(AbstractBTreeV1DataBB.maxSize(sc), AbstractBTreeV1DataBB.maxSize(sc));
    }

    interface DataKey {
        int getSizeOfChunkInBytes();

        int getFilterMask();

        long[] getDimChunkOffset();
    }

    @Override
    Resolvable<BTreeV1Data> getLeftSibling();

    @Override
    Resolvable<BTreeV1Data> getRightSibling();

    DataKey getKey(int index);

    default List<DataKey> getKeys() {
        final List<DataKey> keys = new ArrayList<>(getEntriesUsed() + 1);
        for (int i = 0; i <= getEntriesUsed(); i++) {
            keys.add(getKey(i));
        }
        return keys;
    }

    static BTreeV1Data of(final ByteBuffer buf, final SizingContextKTreeDimension ctx) {
        final byte[] signature = new byte[] {buf.get(0), buf.get(1), buf.get(2), buf.get(3)};
        if (Arrays.equals(BTreeV1.SIGNATURE, signature)) {
            switch (buf.get(4)) {
            case 1:
                switch (buf.get(5)) {
                case 0:
                    return new BTreeV1DataLeafBB(buf, ctx);
                default:
                    return new BTreeV1DataInternalBB(buf, ctx);
                }
            default:
                throw new IllegalArgumentException("BTreeV1Data does not work with " + buf.get(4));
            }
        }
        throw new IllegalArgumentException();
    }
}
