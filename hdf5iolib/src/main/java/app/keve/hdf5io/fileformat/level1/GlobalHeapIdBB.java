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

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;

public final class GlobalHeapIdBB extends AbstractSizedBB<SizingContext> implements GlobalHeapId {
    public GlobalHeapIdBB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long minSize(final SizingContext sc) {
        return sc.offsetSize() + 4;
    }

    public static long maxSize(final SizingContext sc) {
        return sc.offsetSize() + 4 + UINT32_MAX_VALUE;
    }

    @Override
    public long size() {
        return context.offsetSize() + 4;
    }

    @Override
    public Resolvable<GlobalHeapCollection> getCollection() {
        return getResolvable(0, GlobalHeapCollection.class, context);
    }

    @Override
    public int getObjectIndex() {
        return getInt(context.offsetSize());
    }

}
