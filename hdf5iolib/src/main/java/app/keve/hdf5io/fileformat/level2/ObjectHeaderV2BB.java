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
import java.util.OptionalInt;

import app.keve.hdf5io.fileformat.AbstractBB;
import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.H5Context;
import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextOHV2;
import app.keve.hdf5io.fileformat.level2message.SharedMessage;
import app.keve.hdf5io.util.JenkinsHash;

public final class ObjectHeaderV2BB extends AbstractObjectHeader<SizingContext> implements ObjectHeaderV2 {
    public ObjectHeaderV2BB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long minSize(final SizingContext sc) {
        return 10;
    }

    public static long maxSize(final SizingContext sc) {
        return Long.MAX_VALUE;
    }

    @Override
    public long size() {
        return getMessageChunkOffset() + getSizeOfChunk0() + 4;
    }

    @Override
    public boolean isValid() {
        return Arrays.equals(SIGNATURE, getSignature()) && getChecksum() == JenkinsHash
                .hash(getEmbeddedData(0, (int) size() - 4).order(ByteOrder.LITTLE_ENDIAN), 0);
    }

    @Override
    public byte[] getSignature() {
        return getBytes(0, 4);
    }

    @Override
    public int getVersion() {
        return getByte(4);
    }

    @Override
    public int getFlags() {
        return getByte(5);
    }

    @Override
    public OptionalInt getAccessTimeSeconds() {
        return isTimed() ? OptionalInt.of(getInt(6)) : OptionalInt.empty();
    }

    @Override
    public OptionalInt getModificationTimeSeconds() {
        return isTimed() ? OptionalInt.of(getInt(10)) : OptionalInt.empty();
    }

    @Override
    public OptionalInt getChangeTimeSeconds() {
        return isTimed() ? OptionalInt.of(getInt(14)) : OptionalInt.empty();
    }

    @Override
    public OptionalInt getBirthTimeSeconds() {
        return isTimed() ? OptionalInt.of(getInt(18)) : OptionalInt.empty();
    }

    @Override
    public OptionalInt getMaximumNumberOfCompactAttributes() {
        final int offset = 6 + (isTimed() ? 16 : 0);
        return isNonDefaultStoragePhaseChangeStored() ? OptionalInt.of(getShort(offset)) : OptionalInt.empty();
    }

    @Override
    public OptionalInt getMinimumNumberOfDenseAttributes() {
        final int offset = 6 + (isTimed() ? 16 : 0);
        return isNonDefaultStoragePhaseChangeStored() ? OptionalInt.of(getShort(offset + 2)) : OptionalInt.empty();
    }

    @Override
    public long getSizeOfChunk0() {
        final int offset = 6 + (isTimed() ? 16 : 0) + (isNonDefaultStoragePhaseChangeStored() ? 4 : 0);
        switch (getFlags() & SIZE_CHUNK0_MASK) {
        case 0:
            return getUnsignedByte(offset);
        case 1:
            return getUnsignedShort(offset);
        case 2:
            return getUnsignedInt(offset);
        case 3:
            return getLong(offset);
        default:
            throw new IllegalArgumentException();
        }
    }

    private int getMessageChunkOffset() {
        int offset = 6 + (isTimed() ? 16 : 0) + (isNonDefaultStoragePhaseChangeStored() ? 4 : 0);
        offset += 1 << (getFlags() & SIZE_CHUNK0_MASK);
        return offset;
    }

    @Override
    public Iterator<? extends HeaderMessageEntry<?>> headerMessageIterator() {
        final int offset = getMessageChunkOffset();

        final SizingContextOHV2 contextOHV2 = SizingContextOHV2.of(context, isAttributeCreationOrderTracked());
        if (isAttributeCreationOrderTracked()) {
            return getIteratorByteSized(offset, HeaderMessageEntryV2WithCreationOrderBB.class, getSizeOfChunk0(),
                    (int) HeaderMessageEntryV2WithCreationOrderBB.minSize(null), contextOHV2);
        } else {
            return getIteratorByteSized(offset, HeaderMessageEntryV2NoCreationOrderBB.class, getSizeOfChunk0(),
                    (int) HeaderMessageEntryV2NoCreationOrderBB.minSize(null), contextOHV2);
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

    public abstract static class AbstractHeaderMessageEntryV2BB extends AbstractHeaderMessageEntry<SizingContextOHV2>
            implements HeaderMessageEntryV2 {

        private final int dataOffset;

        public AbstractHeaderMessageEntryV2BB(final ByteBuffer buf, final SizingContextOHV2 sizingContext,
                final int dataOffset) {
            super(buf, sizingContext);
            this.dataOffset = dataOffset;
        }

        public static final long size(final SizingContext sizingContext) {
            return 2 + sizingContext.offsetSize();
        }

        @Override
        public final long size() {
            return dataOffset + getSizeOfMessageData();
        }

        @Override
        public final boolean isValid() {
            return true;
        }

        @Override
        public final int getTypeNumber() {
            return getUnsignedByte(0);
        }

        @Override
        public final void setTypeNumber(final int value) {
            setByte(0, value);
        }

        @Override
        public final int getSizeOfMessageData() {
            return getUnsignedShort(1);
        }

        @Override
        public final void setSizeOfMessageData(final int value) {
            setUnsignedShort(1, value);
        }

        @Override
        public final int getFlags() {
            return getByte(3);
        }

        @Override
        public final void setFlags(final int value) {
            setByte(3, value);
        }

        public static HeaderMessageEntryV2 of(final ByteBuffer buf, final SizingContext sc) {
            throw new IllegalArgumentException();
        }

        @Override
        public final <V extends H5Object<S>, S extends H5Context> V getMessage(final Class<V> vClass) {
            return getEmbedded(dataOffset, getSizeOfMessageData(), vClass, (S) context);
        }

        @Override
        public final <V extends H5Object<S>, S extends H5Context> V getMessage() {
            final Class<V> messageClass = (Class<V>) (isShared() ? SharedMessage.class
                    : context.h5Factory().messageClass(getTypeNumber()));
            if (null == messageClass) {
                return null; // unknown message
            }
            return getEmbedded(dataOffset, getSizeOfMessageData(), messageClass, (S) context);
        }

        @Override
        public final <V extends AbstractBB<S>, S extends H5Context> void setMessage(final V value) {
            setEmbedded(dataOffset, value);
        }

        @Override
        public final void initialize() {
        }
    }

    public static final class HeaderMessageEntryV2NoCreationOrderBB extends AbstractHeaderMessageEntryV2BB {
        public HeaderMessageEntryV2NoCreationOrderBB(final ByteBuffer buf, final SizingContextOHV2 sizingContext) {
            super(buf, sizingContext, 4);
        }

        public static long minSize(final SizingContext notUsed) {
            return 4;
        }

        @Override
        public OptionalInt getCreationOrder() {
            return OptionalInt.empty();
        }
    }

    public static final class HeaderMessageEntryV2WithCreationOrderBB extends AbstractHeaderMessageEntryV2BB {
        public HeaderMessageEntryV2WithCreationOrderBB(final ByteBuffer buf, final SizingContextOHV2 sizingContext) {
            super(buf, sizingContext, 6);
        }

        public static long minSize(final SizingContext notUsed) {
            return 6;
        }

        @Override
        public OptionalInt getCreationOrder() {
            return OptionalInt.of(getShort(4));
        }

    }
}
