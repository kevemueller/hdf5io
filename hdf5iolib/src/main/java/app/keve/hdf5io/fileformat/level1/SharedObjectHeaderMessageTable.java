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
package app.keve.hdf5io.fileformat.level1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextSOHMT;
import app.keve.hdf5io.util.Unsigned;

public interface SharedObjectHeaderMessageTable extends H5Object<SizingContextSOHMT> {
    byte[] SIGNATURE = {'S', 'M', 'T', 'B'};

    boolean isValid();

    byte[] getSignature();

    Iterator<SharedObjectHeaderMessageIndex> indexIterator();

    default List<SharedObjectHeaderMessageIndex> getIndices() {
        final List<SharedObjectHeaderMessageIndex> indices = new ArrayList<>();
        indexIterator().forEachRemaining(indices::add);
        return indices;
    }

    SharedObjectHeaderMessageIndex getIndex(int index);

    @Unsigned
    int getChecksum();

    interface SharedObjectHeaderMessageIndex extends H5Object<SizingContext> {
        int FLAGS_TYPE_DATASPACE = 0b0_0001;
        int FLAGS_TYPE_DATATYPE = 0b0_0010;
        int FLAGS_TYPE_FILLVALUE = 0b0_0100;
        int FLAGS_TYPE_FILTER_PIPELINE = 0b0_1000;
        int FLAGS_TYPE_ATTRIBUTE = 0b1_0000;

        enum IndexType {
            LIST, BTREE
        }

        boolean isValid();

        int getVersion();

        IndexType getIndexType();

        int getMessageTypeFlags();

        default boolean isIndexingDataspace() {
            return getFlag(getMessageTypeFlags(), FLAGS_TYPE_DATASPACE);
        }

        default boolean isIndexingDatatype() {
            return getFlag(getMessageTypeFlags(), FLAGS_TYPE_DATATYPE);
        }

        default boolean isIndexingFillValue() {
            return getFlag(getMessageTypeFlags(), FLAGS_TYPE_FILLVALUE);
        }

        default boolean isIndexingFilterPipeline() {
            return getFlag(getMessageTypeFlags(), FLAGS_TYPE_FILTER_PIPELINE);
        }

        default boolean isIndexingAttribute() {
            return getFlag(getMessageTypeFlags(), FLAGS_TYPE_ATTRIBUTE);
        }

        long getMinimumMessageSize();

        int getListCutoff();

        int getBTreeV2Cutoff();

        int getNumberOfMessages();

        Resolvable<FractalHeap> getFractalHeap();
    }

    interface SharedObjectHeaderMessageIndexRecordList extends SharedObjectHeaderMessageIndex {
        // TODO:
        long getIndexAddress();
    }

    interface SharedObjectHeaderMessageIndexBTree extends SharedObjectHeaderMessageIndex {
        Resolvable<BTreeV2> getIndex();
    }

}
