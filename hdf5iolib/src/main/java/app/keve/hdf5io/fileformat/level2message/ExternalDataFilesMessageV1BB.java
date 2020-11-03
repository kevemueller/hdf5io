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
import java.util.Iterator;

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextHeap;
import app.keve.hdf5io.fileformat.level1.LocalHeap;

public final class ExternalDataFilesMessageV1BB extends AbstractSizedBB<SizingContext>
        implements ExternalDataFilesMessageV1 {
    public ExternalDataFilesMessageV1BB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long minSize(final SizingContext sc) {
        return 8 + sc.offsetSize() + 0;
    }

    public static long maxSize(final SizingContext sc) {
        return MAX_MESSAGE_DATA;
    }

    @Override
    public long size() {
        return 8 + context.offsetSize() + getAllocatedSlots() * 3 * context.lengthSize();
    }

    @Override
    public boolean isValid() {
        return 0 == getByte(1) && 0 == getShort(2);
    }

    @Override
    public int getVersion() {
        return getByte(0);
    }

    @Override
    public int getAllocatedSlots() {
        return getUnsignedShort(4);
    }

    @Override
    public int getUsedSlots() {
        return getUnsignedShort(6);
    }

    @Override
    public Resolvable<LocalHeap> getHeap() {
        return getResolvable(8, LocalHeap.class, context);
    }

    @Override
    public Iterator<? extends SlotDefinition> slotsIterator() {
        final SizingContextHeap sch = SizingContextHeap.of(context, getHeap());
        return getIterator(8 + context.offsetSize(), SlotDefinitionV1.class, getUsedSlots(), sch);
    }

    public static final class SlotDefinitionV1BB extends AbstractSizedBB<SizingContextHeap>
            implements SlotDefinitionV1 {
        public SlotDefinitionV1BB(final ByteBuffer buf, final SizingContextHeap sizingContext) {
            super(buf, sizingContext);
        }

        public static long size(final SizingContext sc) {
            return 3 * sc.lengthSize();
        }

        @Override
        public long size() {
            return 3 * context.lengthSize();
        }

        @Override
        public Resolvable<String> getName() {
            return getResolvableHeapString(context.heap(), getLength(0));
        }

        @Override
        public long getExternalFileOffset() {
            return getLength(context.lengthSize());
        }

        @Override
        public long getExternalFileDataSize() {
            return getLength(2 * context.lengthSize());
        }

    }

}
