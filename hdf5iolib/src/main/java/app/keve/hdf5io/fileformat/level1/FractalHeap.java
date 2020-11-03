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

import java.nio.ByteBuffer;
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextFractalHeap;

public interface FractalHeap extends H5Object<SizingContext> {
    byte[] SIGNATURE = {'F', 'R', 'H', 'P'};

    int FLAG_HUGE_OBJECT_WRAP_MASK = 0b01;
    int FLAG_BLOCK_CHECKSUM_MASK = 0b10;

    boolean isValid();

    byte[] getSignature();

    int getVersion();

    int getHeapIdLength();

    int getIOFiltersEncodedLength();

    int getFlags();

    default boolean isHugeObjectWrap() {
        return (getFlags() & FLAG_HUGE_OBJECT_WRAP_MASK) > 0;
    }

    default boolean isBlockChecksum() {
        return (getFlags() & FLAG_BLOCK_CHECKSUM_MASK) > 0;
    }

    int getMaximumSizeOfManagedObjects();

    long getNextHugeObjectId();

    long getBTreeV2HugeObjects();

    long getFreeSpaceAmount();

    long getFreeSpaceManager();

    long getManagedSpaceAmount();

    long getAllocatedManagedSpaceAmount();

    long getDirectBlockAllocationIterator();

    long getManagedObjectNumber();

    long getHugeObjectsSize();

    long getHugeObjectsNumber();

    long getTinyObjectsSize();

    long getTinyObjectsNumber();

    int getTableWidth();

    long getStartingBlockSize();

    long getMaximumDirectBlockSize();

    int getMaximumHeapSize();

    int getStartingNumberOfRowsIndirectBlock();

    Resolvable<? extends HeapBlock> getRootBlock();

    int getCurrentNumberOfRowsIndirectBlock();

    OptionalLong getFilteredBlockSize();

    OptionalInt getIOFilterMask();

    Object getIOFilterInformation();

    int getChecksum();

    interface HeapBlock extends H5Object<SizingContextFractalHeap> {

    }

    interface DirectBlock extends HeapBlock {
        byte[] SIGNATURE = {'F', 'H', 'D', 'B'};

        boolean isValid();

        byte[] getSignature();

        int getVersion();

        Resolvable<FractalHeap> getHeapHeader();

        long getBlockOffset();

        OptionalInt getChecksum();

        ByteBuffer getObjectData();

        ByteBuffer getObjectData(int idx, int length);
    }

}
