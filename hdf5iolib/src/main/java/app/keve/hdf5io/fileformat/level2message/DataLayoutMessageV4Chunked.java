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

import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.level1.ExtensibleArrayIndex;
import app.keve.hdf5io.fileformat.level1.FixedArrayIndex;

public interface DataLayoutMessageV4Chunked extends DataLayoutMessageChunked {
    int FLAG_DONT_FILTER_PARTIAL_BOUND_CHUNKS_MASK = 0b01;
    int FLAG_SINGLE_INDEX_WITH_FILTER_MASK = 0b10;

    enum ChunkIndexingType {
        SINGLE, IMPLICIT, FIXED_ARRAY, EXTENSIBLE_ARRAY, VERSION2_BTREE
    }

    int getFlags();

    int getDimensionSizeEncodedLength();

    ChunkIndexingType getChunkIndexingType();

    ChunkIndexingInformation getChunkIndexingInformation();

    interface ChunkIndexingInformation extends H5Object<SizingContext> {

    }

    interface SingleChunkIndexingInformation extends ChunkIndexingInformation {
        long getFilteredChunkSize();

        int getFilters();
    }

    interface ImplicitIndexingInformation extends ChunkIndexingInformation {
//        Resolvable<ImplicitArrayIndex, SizingContext> getIndex();
        long getIndex();
    }

    interface FixedArrayIndexingInformation extends ChunkIndexingInformation {
        int getPageBits();

        Resolvable<FixedArrayIndex> getIndex();
    }

    interface ExtensibleArrayIndexingInformation extends ChunkIndexingInformation {
        int getMaxBits();

        int getIndexElements();

        int getMinPointers();

        int getMinElements();

        int getPageBits();

        Resolvable<ExtensibleArrayIndex> getIndex();
    }

    interface Version2BTreeIndexingInformation extends ChunkIndexingInformation {
        long getNodeSize();

        int getSplitPercent();

        int getMergePercent();

        long getIndex();
    }

    default boolean isDontFilterPartialBoundChunks() {
        return (getFlags() & FLAG_DONT_FILTER_PARTIAL_BOUND_CHUNKS_MASK) > 0;
    }

    default boolean isSingleIndexWithFilter() {
        return (getFlags() & FLAG_SINGLE_INDEX_WITH_FILTER_MASK) > 0;
    }
}
