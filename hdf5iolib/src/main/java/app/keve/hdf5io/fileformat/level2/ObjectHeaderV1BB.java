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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import app.keve.hdf5io.fileformat.AbstractBB;
import app.keve.hdf5io.fileformat.H5Context;
import app.keve.hdf5io.fileformat.H5MessageType;
import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.level2message.H5Message;
import app.keve.hdf5io.fileformat.level2message.ObjectHeaderContinuationMessageBB;
import app.keve.hdf5io.fileformat.level2message.SharedMessage;

public final class ObjectHeaderV1BB extends AbstractObjectHeader<SizingContext> implements ObjectHeaderV1 {
    public ObjectHeaderV1BB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long minSize(final SizingContext sc) {
        return 16;
    }

    public static long maxSize(final SizingContext sc) {
        return 16 + UINT32_MAX_VALUE;
    }

    @Override
    public long size() {
        return 16 + getObjectHeaderSize();
    }

    @Override
    public boolean isValid() {
        return 0 == getByte(1) && 0 == getInt(12);
    }

    @Override
    public int getVersion() {
        return getByte(0);
    }

    @Override
    public int getTotalNumberOfHeaderMessages() {
        return getShort(2);
    }

    @Override
    public void setTotalNumberOfHeaderMessages(final int value) {
        setShort(2, value);
    }

    @Override
    public int getObjectReferenceCount() {
        return getInt(4);
    }

    @Override
    public void setObjectReferenceCount(final int value) {
        setInt(4, value);
    }

    @Override
    public int getObjectHeaderSize() {
        return getInt(8);
    }

    @Override
    public void setObjectHeaderSize(final int value) {
        setInt(8, value);
    }

    @Override
    public Iterator<? extends HeaderMessageEntry<?>> headerMessageIterator() {
        return getIteratorByteSized(16, HeaderMessageEntryV1.class, getObjectHeaderSize(), 0);
    }

    @Override
    public List<HeaderMessageEntry<?>> getHeaderMessages() {
        final List<HeaderMessageEntry<?>> headerMessages = new ArrayList<>();
        headerMessageIterator().forEachRemaining(headerMessages::add);
        return headerMessages;
    }

    @Override
    public void initialize() {
        setByte(0, 1); // version
        setByte(1, 0); // reserved
        setShort(2, 1); // 1 header message
        setInt(4, 1); // reference count
        setInt(8, available() - 16); // message size
        setInt(12, 0); // reserved
        final HeaderMessageEntryV1 me = getEmbedded(16, getObjectHeaderSize(), HeaderMessageEntryV1.class);
        me.initialize();
        me.setType(H5MessageType.NIL);
        me.setSizeOfMessageData(getObjectHeaderSize() - 8);
    }

    @Override
    public void pack() {
        HeaderMessageEntry<?> lastMessageEntry = null;
        final Iterator<? extends HeaderMessageEntry<?>> it = headerMessageIterator();
        while (it.hasNext()) {
            lastMessageEntry = it.next();
        }
        if (null != lastMessageEntry && H5MessageType.NIL == lastMessageEntry.getType()) {
            // reduce the last NILMessage to hold only a ObjectHeaderExtensionMessage
            final int currentSize = lastMessageEntry.getSizeOfMessageData();
            final int targetSize = (int) ObjectHeaderContinuationMessageBB.size(context);
//            context.h5Factory().H5MessageType.OBJECT_HEADER_CONTINUATION.messageClass;
            final int saved = currentSize - targetSize;
            lastMessageEntry.setSizeOfMessageData(targetSize);
            setObjectHeaderSize(getObjectHeaderSize() - saved);
            resize();
        }
    }

    @Override
    public HeaderMessageEntryV1 addHeaderMessage(final long dataSize) {
        final int paddedDataSize = (int) dataSize + 7 & ~7;
        final Iterator<Entry<Integer, HeaderMessageEntryV1>> it = getOffsetIteratorByteSized(16,
                HeaderMessageEntryV1.class, getObjectHeaderSize(), 0);
        while (it.hasNext()) {
            final Entry<Integer, HeaderMessageEntryV1> ohme = it.next();
            final HeaderMessageEntryV1 hme = ohme.getValue();
            if (H5MessageType.NIL == hme.getType() && hme.getSizeOfMessageData() >= paddedDataSize) {
                // split this message
                final int nextOffset = ohme.getKey() + (int) HeaderMessageEntryV1BB.minSize(context) + paddedDataSize;

                final HeaderMessageEntryV1 nextHME = getEmbedded(nextOffset, (int) hme.size() - paddedDataSize - 8,
                        HeaderMessageEntryV1.class);
                nextHME.initialize();
                nextHME.setType(H5MessageType.NIL);
                nextHME.setSizeOfMessageData((int) hme.size() - paddedDataSize - 8 - 8);

                hme.setSizeOfMessageData(paddedDataSize);
                setShort(2, 1 + getShort(2));
                return hme;
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public HeaderMessageEntry<SizingContext> addHeaderMessage(final H5Message<? extends H5Context> message) {
        final HeaderMessageEntryV1 hme = addHeaderMessage(message.size());
        hme.setType(message.getType());
        final AbstractBB<?> wMessage = (AbstractBB<?>) message;
        wMessage.pack();
        hme.setMessage(wMessage);
        return hme;
    }

    public static final class HeaderMessageEntryV1BB extends AbstractHeaderMessageEntry<SizingContext>
            implements HeaderMessageEntryV1 {
        public HeaderMessageEntryV1BB(final ByteBuffer buf, final SizingContext sizingContext) {
            super(buf, sizingContext);
        }

        public static long minSize(final SizingContext sc) {
            return 8;
        }

        public static long maxSize(final SizingContext sc) {
            return MAX_MESSAGE_DATA;
        }

        @Override
        public long size() {
            return 8 + getSizeOfMessageData();
        }

        @Override
        public boolean isValid() {
            return 0 == getByte(5) && 0 == getShort(6);
        }

        @Override
        public int getTypeNumber() {
            return getUnsignedShort(0);
        }

        @Override
        public void setTypeNumber(final int value) {
            setUnsignedShort(0, value);
        }

        @Override
        public int getSizeOfMessageData() {
            return getUnsignedShort(2);
        }

        @Override
        public void setSizeOfMessageData(final int value) {
            setUnsignedShort(2, value);
        }

        @Override
        public int getFlags() {
            return getByte(4);
        }

        @Override
        public void setFlags(final int value) {
            setByte(4, value);
        }

        @Override
        public <V extends H5Object<S>, S extends H5Context> V getMessage(final Class<V> vClass) {
            return getEmbedded(8, getSizeOfMessageData(), vClass, (S) context);
        }

        @Override
        public <V extends H5Object<S>, S extends H5Context> V getMessage() {
            final Class<V> messageClass = (Class<V>) (isShared() ? SharedMessage.class
                    : context.h5Factory().messageClass(getTypeNumber()));
            return getEmbedded(8, getSizeOfMessageData(), messageClass, (S) context);
        }

        @Override
        public <V extends AbstractBB<S>, S extends H5Context> void setMessage(final V value) {
            setEmbedded(8, value);
        }

        @Override
        public void initialize() {
            setByte(5, 0);
            setShort(6, 0);
        }

    }

}
