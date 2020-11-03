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
package app.keve.hdf5io.fileformat.level0;

import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.level2.ObjectHeader;
import app.keve.hdf5io.util.Unsigned;

public interface SuperblockV2 extends Superblock {
    int FLAG_FILE_CONSISTENCY_WRITE_ACCESS_MASK = 0b001;
    int FLAG_FILE_CONSISTENCY_RESERVED_MASK = 0b010;
    int FLAG_FILE_CONSISTENCY_SWMR_MASK = 0b100;

    default boolean isFileConsistencyOpenedForWrite() {
        return getFlag(getFileConsistencyFlags(), FLAG_FILE_CONSISTENCY_WRITE_ACCESS_MASK);
    }

    default void setFileConsistencyOpenedForWrite(final boolean value) {
        setFileConsistencyFlags(setFlag(getFileConsistencyFlags(), FLAG_FILE_CONSISTENCY_WRITE_ACCESS_MASK, value));
    }

    default boolean isFileConsistencyOpenedSWMR() {
        return getFlag(getFileConsistencyFlags(), FLAG_FILE_CONSISTENCY_SWMR_MASK);
    }

    default void setFileConsistencyOpenedSWMR(final boolean value) {
        setFileConsistencyFlags(setFlag(getFileConsistencyFlags(), FLAG_FILE_CONSISTENCY_SWMR_MASK, value));
    }

    Resolvable<ObjectHeader> getSuperblockExtension();

    void setSuperblockExtension(Resolvable<ObjectHeader> value);

    Resolvable<ObjectHeader> getRootGroupObjectHeader();

    void setRootGroupObjectHeader(Resolvable<ObjectHeader> value);

    @Unsigned
    int getChecksum();

    void setChecksum();
}
