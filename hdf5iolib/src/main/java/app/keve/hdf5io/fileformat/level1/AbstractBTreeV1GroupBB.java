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
import app.keve.hdf5io.fileformat.SizingContextHeap;

public abstract class AbstractBTreeV1GroupBB extends AbstractBTreeV1BB<SizingContextHeap> implements BTreeV1Group {

    public AbstractBTreeV1GroupBB(final ByteBuffer buf, final SizingContextHeap sizingContext) {
        super(buf, sizingContext);
    }

    public static final long size(final SizingContext sc) {
        final int twoK = 2 * sc.groupInternalNodeK();
        return 4 + 4 + 2 * sc.offsetSize() + (twoK + 1) * sc.lengthSize() + twoK * sc.offsetSize();
    }

    @Override
    public final long size() {
        return size(context);
    }

    @Override
    public final Resolvable<BTreeV1Group> getLeftSibling() {
        return getResolvable(8, BTreeV1Group.class, context);
    }

    @Override
    public final Resolvable<BTreeV1Group> getRightSibling() {
        return getResolvable(8 + context.offsetSize(), BTreeV1Group.class, context);
    }

    @Override
    public final Resolvable<String> getKey(final int index) {
        final int pos = 8 + 2 * context().offsetSize() + index * (context.lengthSize() + context().offsetSize());
        final long stringOffset = getLength(pos);
        return getResolvableHeapString(context.heap(), stringOffset);
    }

    @Override
    public final void setKey(final int index, final Resolvable<String> value) {
        final int pos = 8 + 2 * context().offsetSize() + index * (context.lengthSize() + context().offsetSize());
        setResolvableHeapString(pos, value);
    }

}
