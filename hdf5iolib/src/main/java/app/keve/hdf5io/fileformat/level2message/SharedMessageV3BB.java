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
import app.keve.hdf5io.fileformat.level2.ObjectHeader;

public final class SharedMessageV3BB extends AbstractSizedBB<SizingContext> implements SharedMessageV3 {
    public SharedMessageV3BB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long minSize(final SizingContext sc) {
        return 3 + Integer.min(sc.offsetSize(), 8);
    }

    public static long maxSize(final SizingContext sc) {
        return 3 + Integer.max(sc.offsetSize(), 8);
    }

    @Override
    public long size() {
        return 2 + (TypeV3.SHARED == getTypeV3() ? 8 : context.offsetSize());
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public int getVersion() {
        return getByte(0);
    }

    @Override
    public int getType() {
        return getByte(1);
    }

    @Override
    public TypeV3 getTypeV3() {
        switch (getType()) {
        case 0:
            return TypeV3.NOT_SHARED_NOT_SHAREABLE;
        case 1:
            return TypeV3.SHARED;
        case 2:
            return TypeV3.COMMITTED;
        case 3:
            return TypeV3.NOT_SHARED_SHAREABLE;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Resolvable<ObjectHeader> getObjectHeader() {
        if (TypeV3.SHARED == getTypeV3()) {
            return null;
        }
        return getResolvable(3, ObjectHeader.class, context);
    }

    @Override
    public OptionalLong getFractalHeapId() {
        return TypeV3.SHARED == getTypeV3() ? OptionalLong.of(getLong(2)) : OptionalLong.empty();
    }

}
