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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextBTreeV2Node;
import app.keve.hdf5io.fileformat.level2message.LinkMessage;

public interface BTreeV2Node extends H5Object<SizingContextBTreeV2Node> {

    static long minSize(final SizingContext sc) {
        return BTreeV2LeafNodeBB.minSize(sc);
    }

    static long maxSize(final SizingContext sc) {
        return BTreeV2LeafNodeBB.maxSize(sc);
    }

    static BTreeV2Node of(final ByteBuffer buf, final SizingContextBTreeV2Node sc) {
        final byte[] signature = new byte[] {buf.get(0), buf.get(1), buf.get(2), buf.get(3)};
        if (Arrays.equals(BTreeV2LeafNode.SIGNATURE, signature)) {
            return new BTreeV2LeafNodeBB(buf, sc);
        }
        throw new IllegalArgumentException("Implement node " + new String(signature, StandardCharsets.US_ASCII));
    }

    interface Record extends H5Object<SizingContextBTreeV2Node> {

    }

    interface LinkNameRecord extends Record {
        int getHash();

        Resolvable<LinkMessage> getHeapId();
    }
}
