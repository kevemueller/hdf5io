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
import java.util.OptionalLong;

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextBTreeV2;
import app.keve.hdf5io.fileformat.level1.BTreeV2;
import app.keve.hdf5io.fileformat.level1.FractalHeap;

public final class LinkInfoMessageV0BB extends AbstractSizedBB<SizingContext> implements LinkInfoMessageV0 {

    public LinkInfoMessageV0BB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long minSize(final SizingContext sc) {
        return 2 + 2 * sc.offsetSize();
    }

    public static long maxSize(final SizingContext sc) {
        return 2 + 8 + 3 * sc.offsetSize();
    }

    @Override
    public long size() {
        return 2 + (isCreationOrderTracked() ? 8 : 0) + 2 * context.offsetSize()
                + (isCreationOrderIndexed() ? context.offsetSize() : 0);
    }

    @Override
    public int getVersion() {
        return getByte(0);
    }

    @Override
    public int getFlags() {
        return getByte(1);
    }

    @Override
    public OptionalLong getMaximumCreationIndex() {
        return isCreationOrderTracked() ? OptionalLong.of(getLong(2)) : OptionalLong.empty();
    }

    @Override
    public Resolvable<FractalHeap> getFractalHeap() {
        final int offset = 2 + (isCreationOrderTracked() ? 8 : 0);
        return getResolvable(offset, FractalHeap.class, context);
    }

    @Override
    public Resolvable<BTreeV2> getBTreeV2NameIndex() {
        final int offset = 2 + (isCreationOrderTracked() ? 8 : 0);
        final SizingContextBTreeV2 context2 = SizingContextBTreeV2.of(context, getFractalHeap());
        return getResolvable(offset + context.offsetSize(), BTreeV2.class, context2);
    }

    @Override
    public Resolvable<BTreeV2> getBTreeV2CreationOrderIndex() {
        final int offset = 2 + (isCreationOrderTracked() ? 8 : 0) + 2 * context.offsetSize();
        final SizingContextBTreeV2 context2 = SizingContextBTreeV2.of(context, getFractalHeap());
        return isCreationOrderIndexed() ? getResolvable(offset, BTreeV2.class, context2) : null;
    }

}
