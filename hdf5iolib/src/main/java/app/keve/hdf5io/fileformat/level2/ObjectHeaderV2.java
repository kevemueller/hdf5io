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
import java.time.Instant;
import java.util.OptionalInt;

import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextOHV2;
import app.keve.hdf5io.util.Unsigned;

public interface ObjectHeaderV2 extends ObjectHeader {

    byte[] SIGNATURE = {'O', 'H', 'D', 'R'};
    int SIZE_CHUNK0_MASK = 0x03;
    int FLAG_ATTRIBUTE_CREATION_TRACKED_MASK = 0x04;
    int FLAG_ATTRIBUTE_CREATION_INDEXED_MASK = 0x08;
    int FLAG_STORAGE_PHASE_CHANGE_STORED_MASK = 0x10;
    int FLAG_TIME_MASK = 0x20;

    byte[] getSignature();

    int getFlags();

    default boolean isAttributeCreationOrderTracked() {
        return (getFlags() & FLAG_ATTRIBUTE_CREATION_TRACKED_MASK) > 0;
    }

    default boolean isAttributeCreationOrderIndexed() {
        return (getFlags() & FLAG_ATTRIBUTE_CREATION_INDEXED_MASK) > 0;
    }

    default boolean isNonDefaultStoragePhaseChangeStored() {
        return (getFlags() & FLAG_STORAGE_PHASE_CHANGE_STORED_MASK) > 0;
    }

    default boolean isTimed() {
        return (getFlags() & FLAG_TIME_MASK) > 0;
    }

    OptionalInt getAccessTimeSeconds();

    default Instant getAccessTime() {
        final OptionalInt time = getAccessTimeSeconds();
        return time.isEmpty() ? null : Instant.ofEpochSecond(time.getAsInt());
    }

    OptionalInt getModificationTimeSeconds();

    default Instant getModificationTime() {
        final OptionalInt time = getModificationTimeSeconds();
        return time.isEmpty() ? null : Instant.ofEpochSecond(time.getAsInt());
    }

    OptionalInt getChangeTimeSeconds();

    default Instant getChangeTime() {
        final OptionalInt time = getChangeTimeSeconds();
        return time.isEmpty() ? null : Instant.ofEpochSecond(time.getAsInt());
    }

    OptionalInt getBirthTimeSeconds();

    default Instant getBirthTime() {
        final OptionalInt time = getBirthTimeSeconds();
        return time.isEmpty() ? null : Instant.ofEpochSecond(time.getAsInt());
    }

    OptionalInt getMaximumNumberOfCompactAttributes();

    OptionalInt getMinimumNumberOfDenseAttributes();

    long getSizeOfChunk0();

    @Unsigned
    int getChecksum();

    interface HeaderMessageEntryV2 extends HeaderMessageEntry<SizingContextOHV2> {
        static long size(final SizingContext sizingContext) {
            return 2 + sizingContext.offsetSize();
        }

        OptionalInt getCreationOrder();

        static HeaderMessageEntryV2 of(final ByteBuffer buf, final SizingContext sizingContext) {
            throw new IllegalArgumentException();
        }
    }
}
