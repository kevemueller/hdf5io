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
import app.keve.hdf5io.fileformat.SizingContextHeap;

public interface BTreeV1Group extends BTreeV1<SizingContextHeap> {

    static long minSize(final SizingContext sc) {
        return Long.min(BTreeV1GroupLeafBB.size(sc), AbstractBTreeV1GroupBB.size(sc));
    }

    static long maxSize(final SizingContext sc) {
        return Long.max(BTreeV1GroupLeafBB.size(sc), AbstractBTreeV1GroupBB.size(sc));
    }

    Resolvable<BTreeV1Group> getLeftSibling();

//    void setLeftSibling(Resolvable<? extends BTreeV1Group> value);

    Resolvable<BTreeV1Group> getRightSibling();

//    void setRightSibling(Resolvable<? extends BTreeV1Group> value);

    // range 0 .. getEntriesUsed
    Resolvable<String> getKey(int index);

    void setKey(int index, Resolvable<String> value);

    default List<Resolvable<String>> getKeys() {
        final List<Resolvable<String>> keys = new ArrayList<>(getEntriesUsed() + 1);
        for (int i = 0; i <= getEntriesUsed(); i++) {
            keys.add(getKey(i));
        }
        return keys;
    }

    static BTreeV1Group of(final ByteBuffer buf, final SizingContextHeap context) {
        final byte[] signature = new byte[] {buf.get(0), buf.get(1), buf.get(2), buf.get(3)};
        if (Arrays.equals(BTreeV1.SIGNATURE, signature)) {
            switch (buf.get(4)) {
            case 0:
                switch (buf.get(5)) {
                case 0:
                    return new BTreeV1GroupLeafBB(buf, context);
                default:
                    return new BTreeV1GroupInternalBB(buf, context);
                }
            default:
                throw new IllegalArgumentException("BTreeV1Group does not work with " + buf.get(4));
            }
        }
        throw new IllegalArgumentException();
    }
}
