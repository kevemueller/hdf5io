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
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.SizingContext;

public final class AttributeInfoMessageBB extends AbstractSizedBB<SizingContext> implements AttributeInfoMessage {
    public AttributeInfoMessageBB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long minSize(final SizingContext sc) {
        return 2 + 0 + 2 * sc.offsetSize() + 0;
    }

    public static long maxSize(final SizingContext sc) {
        return 2 + 2 + 3 * sc.offsetSize();
    }

    @Override
    public long size() {
        return 2 + (isCreationOrderTracked() ? 2 : 0) + 2 * context.offsetSize()
                + (isCreationOrderIndexed() ? context.offsetSize() : 0);
    }

    @Override
    public int getVersion() {
        return getUnsignedByte(0);
    }

    @Override
    public int getFlags() {
        return getUnsignedByte(1);
    }

    @Override
    public OptionalInt getMaximumCreationIndex() {
        return isCreationOrderTracked() ? OptionalInt.of(getUnsignedShort(2)) : OptionalInt.empty();
    }

    @Override
    public long getFractalHeap() {
        return getOffset(2 + (isCreationOrderTracked() ? 2 : 0));
    }

    @Override
    public long getAttributeNameV2Btree() {
        return getOffset(2 + (isCreationOrderTracked() ? 2 : 0) + context.offsetSize());
    }

    @Override
    public OptionalLong getAttributeCreationOrderV2Btree() {
        return isCreationOrderIndexed()
                ? OptionalLong.of(getOffset(2 + (isCreationOrderTracked() ? 2 : 0) + 2 * context.offsetSize()))
                : OptionalLong.empty();
    }

}
