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

import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextHeap;
import app.keve.hdf5io.fileformat.SizingContextKTreeDimension;

public interface BTreeV1<T extends SizingContext> extends H5Object<T> {
    enum NodeType {
        GROUP, DATA
    }

    byte[] SIGNATURE = {'T', 'R', 'E', 'E'};

    static long minSize(final SizingContext sc) {
        return Long.min(BTreeV1Data.minSize(sc), BTreeV1Group.minSize(sc));
    }

    static long maxSize(final SizingContext sc) {
        return Long.max(BTreeV1Data.maxSize(sc), BTreeV1Group.maxSize(sc));
    }

    boolean isValid();

    byte[] getSignature();

    NodeType getNodeType();

    void setNodeType(NodeType value);

    int getNodeLevel();

    void setNodeLevel(int value);

    int getEntriesUsed();

    void setEntriesUsed(int value);

    Resolvable<? extends BTreeV1<T>> getLeftSibling();

    void setLeftSibling(Resolvable<? extends BTreeV1<T>> value);

    Resolvable<? extends BTreeV1<T>> getRightSibling();

    void setRightSibling(Resolvable<? extends BTreeV1<T>> value);

    static <S extends SizingContext> BTreeV1<S> of(final ByteBuffer buf, final S sizingContext) {
        final byte[] signature = new byte[] {buf.get(0), buf.get(1), buf.get(2), buf.get(3)};
        if (Arrays.equals(SIGNATURE, signature)) {
            switch (buf.get(4)) {
            case 0:
                return (BTreeV1<S>) BTreeV1Group.of(buf, (SizingContextHeap) sizingContext);
            case 1:
                return (BTreeV1<S>) BTreeV1Data.of(buf, (SizingContextKTreeDimension) sizingContext);
            default:
                throw new IllegalArgumentException("Implement BTreeV1 type " + buf.get(4));
            }
        }
        throw new IllegalArgumentException();
    }

}
