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

import app.keve.hdf5io.fileformat.AbstractBB;
import app.keve.hdf5io.fileformat.H5Context;
import app.keve.hdf5io.fileformat.H5MessageType;
import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.H5ObjectW;
import app.keve.hdf5io.fileformat.SizingContext;

public interface ObjectHeader extends H5Object<SizingContext> {
    static long minSize(final SizingContext sc) {
        return Long.min(ObjectHeaderV1BB.minSize(sc), ObjectHeaderV2BB.minSize(sc));
    }

    static long maxSize(final SizingContext sc) {
        return Long.max(ObjectHeaderV1BB.maxSize(sc), ObjectHeaderV2BB.maxSize(sc));
    }

    boolean isValid();

    int getVersion();

    Iterator<? extends HeaderMessageEntry<?>> headerMessageIterator();

    default List<HeaderMessageEntry<?>> getHeaderMessages() {
        final ArrayList<HeaderMessageEntry<?>> list = new ArrayList<>();
        headerMessageIterator().forEachRemaining(list::add);
        return list;
    }

    static ObjectHeader of(final ByteBuffer buf, final SizingContext sizingContext) {
        switch (buf.get(0)) {
        case 0: // FIXME: not in spec
        case 1:
            return new ObjectHeaderV1BB(buf, sizingContext);
        case 'O':
            switch (buf.get(4)) {
            case 2:
                return new ObjectHeaderV2BB(buf, sizingContext);
            default:
                throw new IllegalArgumentException("Implement version " + buf.get(4));
            }
        default:
            throw new IllegalArgumentException("Implement version " + buf.get(0));
        }
    }

    interface HeaderMessageEntry<T extends SizingContext> extends H5ObjectW<T> {
        int FLAG_CONSTANT_MASK = 0b0000_0001;
        int FLAG_SHARED_MASK = 0b0000_0010;
        int FLAG_NOSHARE_MASK = 0b0000_0100;
        int FLAG_FAIL_UNKNOWN_WRITE_MASK = 0b0000_1000;
        int FLAG_MARK_UNKNOWN_MODIFY_MASK = 0b0001_0000;
        int FLAG_UNKNOWN_MODIFIED_MASK = 0b0010_0000;
        int FLAG_SHAREABLE_MASK = 0b0100_0000;
        int FLAG_FAIL_UNKNOWN_MASK = 0b1000_0000;

        static long minSize(final SizingContext sc) {
            return 8;
        }

        static long maxSize(final SizingContext sc) {
            return MAX_MESSAGE_DATA;
        }

        boolean isValid();

        int getTypeNumber();

        void setTypeNumber(int value);

        default H5MessageType getType() {
            return H5MessageType.of(getTypeNumber());
        }

        default void setType(final H5MessageType value) {
            setTypeNumber(value.typeNum);
        }

        int getSizeOfMessageData();

        void setSizeOfMessageData(int value);

        int getFlags();

        void setFlags(int value);

        default boolean isConstant() {
            return getFlag(getFlags(), FLAG_CONSTANT_MASK);
        }

        default void setConstant(final boolean value) {
            setFlags(setFlag(getFlags(), FLAG_CONSTANT_MASK, value));
        }

        default boolean isShared() {
            return getFlag(getFlags(), FLAG_SHARED_MASK);
        }

        default boolean isNoShare() {
            return getFlag(getFlags(), FLAG_NOSHARE_MASK);
        }

        default boolean isFailUnknownWrite() {
            return getFlag(getFlags(), FLAG_FAIL_UNKNOWN_WRITE_MASK);
        }

        default boolean isMarkUnknownModify() {
            return getFlag(getFlags(), FLAG_MARK_UNKNOWN_MODIFY_MASK);
        }

        default boolean isUnknownModified() {
            return getFlag(getFlags(), FLAG_UNKNOWN_MODIFIED_MASK);
        }

        default boolean isShareable() {
            return getFlag(getFlags(), FLAG_SHAREABLE_MASK);
        }

        default boolean isFailUnknown() {
            return getFlag(getFlags(), FLAG_FAIL_UNKNOWN_MASK);
        }

        <V extends H5Object<S>, S extends H5Context> V getMessage();

        <V extends H5Object<S>, S extends H5Context> V getMessage(Class<V> vClass);

        <V extends AbstractBB<S>, S extends H5Context> void setMessage(V value);

    }
}
