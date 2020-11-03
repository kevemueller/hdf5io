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
package app.keve.hdf5io.fileformat;

import java.util.OptionalInt;

/**
 * Basic sizing context for SizeOfOffsets and SizeOfLengths from superblock.
 */
public interface SizingContext extends H5Context {
    int DEFAULT_INDEXED_STORAGE_INTERNAL_NODE_K = 32;
    int DEFAULT_GROUP_INTERNAL_NODE_K = 16;
    int DEFAULT_GROUP_LEAF_NODE_K = 4;

    int offsetSize();

    int lengthSize();

    int indexedStorageInternalNodeK();

    int groupInternalNodeK();

    int groupLeafNodeK();

    default SizingContext with(final int offsetSize, final int lengthSize, final int groupInternalNodeK,
            final int groupLeafNodeK) {
        return with(offsetSize, lengthSize, indexedStorageInternalNodeK(), groupInternalNodeK, groupLeafNodeK);
    }

    default SizingContext with(final int offsetSize, final int lengthSize, final int indexedStorageInternalNodeK,
            final int groupInternalNodeK, final int groupLeafNodeK) {
        return of(h5Factory(), offsetSize, lengthSize, indexedStorageInternalNodeK, groupInternalNodeK, groupLeafNodeK);
    }

    static SizingContext ofUninitialized(final H5Factory h5Factory) {
        return of(h5Factory, 0, 0, OptionalInt.empty(), OptionalInt.empty(), OptionalInt.empty());
    }

    static SizingContext of(final H5Factory h5Factory, final int offsetSize, final int lengthSize) {
        return of(h5Factory, offsetSize, lengthSize, OptionalInt.empty(), OptionalInt.empty(), OptionalInt.empty());
    }

    static SizingContext of(final H5Factory h5Factory, final int offsetSize, final int lengthSize,
            final OptionalInt indexedStorageInternalNodeK, final OptionalInt groupInternalNodeK,
            final OptionalInt groupLeafNodeK) {
        return of(h5Factory, offsetSize, lengthSize,
                indexedStorageInternalNodeK.orElse(DEFAULT_INDEXED_STORAGE_INTERNAL_NODE_K),
                groupInternalNodeK.orElse(DEFAULT_GROUP_INTERNAL_NODE_K),
                groupLeafNodeK.orElse(DEFAULT_GROUP_LEAF_NODE_K));
    }

    static SizingContext of(final H5Factory h5Factory, final int offsetSize, final int lengthSize,
            final int indexedStorageInternalNodeK, final int groupInternalNodeK, final int groupLeafNodeK) {
        return new SizingContext() {
            @Override
            public H5Factory h5Factory() {
                return h5Factory;
            }

            @Override
            public int offsetSize() {
                return offsetSize;
            }

            @Override
            public int lengthSize() {
                return lengthSize;
            }

            @Override
            public int indexedStorageInternalNodeK() {
                return indexedStorageInternalNodeK;
            }

            @Override
            public int groupInternalNodeK() {
                return groupInternalNodeK;
            }

            @Override
            public int groupLeafNodeK() {
                return groupLeafNodeK;
            }
        };
    }

}
