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
package app.keve.hdf5io.fileformat.level2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextOHV2;
import app.keve.hdf5io.fileformat.level2.ObjectHeader.HeaderMessageEntry;
import app.keve.hdf5io.fileformat.level2.ObjectHeaderV2BB.HeaderMessageEntryV2NoCreationOrderBB;
import app.keve.hdf5io.fileformat.level2.ObjectHeaderV2BB.HeaderMessageEntryV2WithCreationOrderBB;
import app.keve.hdf5io.fileformat.level2message.ObjectHeaderContinuationMessageBB;
import app.keve.hdf5io.util.JenkinsHash;

public final class ObjectHeaderContinuationBlockV2BB extends AbstractSizedBB<SizingContextOHV2>
        implements ObjectHeaderContinuationBlockV2 {
    public ObjectHeaderContinuationBlockV2BB(final ByteBuffer buf, final SizingContextOHV2 sizingContext) {
        super(buf, sizingContext);
    }

    public static long minSize(final SizingContext sc) {
        return 4 + 4 + ObjectHeaderContinuationMessageBB.size(sc) + 4;
    }

    public static long maxSize(final SizingContext sc) {
        return Long.MAX_VALUE;
    }

    @Override
    public long size() {
        return available();
    }

    @Override
    public boolean isValid() {
        return Arrays.equals(SIGNATURE, getSignature()) && getChecksum() == JenkinsHash
                .hash(getEmbeddedData(0, (int) size() - 4).order(ByteOrder.LITTLE_ENDIAN), 0);
    }

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public byte[] getSignature() {
        return new byte[] {getByte(0), getByte(1), getByte(2), getByte(3)};
    }

    @Override
    public long getSizeOfChunk0() {
        return available() - 4 - 4;
    }

    private int getMessageChunkOffset() {
        return 4;
    }

    @Override
    public Iterator<? extends HeaderMessageEntry<?>> headerMessageIterator() {
        final int offset = getMessageChunkOffset();
        if (context.isCreationOrderTracked()) {
            return getIteratorByteSized(offset, ObjectHeaderV2BB.HeaderMessageEntryV2WithCreationOrderBB.class,
                    getSizeOfChunk0(), (int) HeaderMessageEntryV2WithCreationOrderBB.minSize(null), context);
        } else {
            return getIteratorByteSized(offset, ObjectHeaderV2BB.HeaderMessageEntryV2NoCreationOrderBB.class,
                    getSizeOfChunk0(), (int) HeaderMessageEntryV2NoCreationOrderBB.minSize(null), context);
        }
    }

    @Override
    public List<HeaderMessageEntry<?>> getHeaderMessages() {
        final List<HeaderMessageEntry<?>> headerMessages = new ArrayList<>();
        headerMessageIterator().forEachRemaining(headerMessages::add);
        return headerMessages;
    }

    @Override
    public int getChecksum() {
        return getInt((int) (size() - 4));
    }

}
