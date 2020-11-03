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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.keve.hdf5io.fileformat.level1.LocalHeap;
import app.keve.hdf5io.fileformat.level1.LocalHeap.LocalHeapDataSegment;

public final class LocalHeapManager extends AbstractManager {
    private final Logger logger = LoggerFactory.getLogger(LocalHeapManager.class);

    private final Resolvable<? extends LocalHeap> rLocalHeap;
    private final Resolvable<? extends LocalHeap.LocalHeapDataSegment> rDataSegment;
    private final LocalHeap localHeap;
    private final LocalHeapDataSegment dataSegment;

    public LocalHeapManager(final H5Resolver hdf5Resolver, final Resolvable<? extends LocalHeap> rLocalHeap) {
        this.rLocalHeap = rLocalHeap;
        this.localHeap = rLocalHeap.resolve(hdf5Resolver);
        this.rDataSegment = localHeap.getDataSegment();
        this.dataSegment = rDataSegment.resolve(hdf5Resolver);
    }

    public LocalHeapManager(final H5Resolver resolver, final SizingContext sizingContext) throws IOException {
        final H5Factory h5Factory = sizingContext.h5Factory();
        this.rLocalHeap = h5Factory.allocate(LocalHeap.class, sizingContext);
        this.rDataSegment = h5Factory.allocate(LocalHeap.LocalHeapDataSegment.class, sizingContext);
        h5Factory.markDirty(this);

        this.dataSegment = rDataSegment.resolve(resolver);
        this.localHeap = rLocalHeap.resolve(resolver);
        localHeap.setDataSegmentSize(dataSegment.size());
        localHeap.setDataSegment(rDataSegment);
        localHeap.setOffsetHeadOfFreeList(8);
    }

    public Resolvable<? extends LocalHeap> getLocalHeap() {
        return rLocalHeap;
    }

    public Resolvable<String> addAsciiNulString(final String value) {
        final byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
        final int neededSpace = bytes.length + 1 + 7 & ~7;

        long offset = localHeap.getOffsetHeadOfFreeList();
        while (neededSpace > dataSegment.getFreeBlockSize(offset)) {
            offset = dataSegment.getNextFreeBlockOffset(offset);
            if (1 == offset) {
                throw new IllegalArgumentException();
            }
        }

        long freeBlockSize = dataSegment.getFreeBlockSize(offset);
        final long nextFreeBlockOffset = dataSegment.getNextFreeBlockOffset(offset);

        freeBlockSize -= neededSpace;
        dataSegment.setNextFreeBlockOffset(offset + neededSpace, nextFreeBlockOffset);
        dataSegment.setFreeBlockSize(offset + neededSpace, freeBlockSize);
        localHeap.setOffsetHeadOfFreeList(offset + neededSpace);
        dataSegment.setAsciiNulString(offset, value);

        return localHeap.context().h5Factory().resolvable(rLocalHeap, offset);
    }

    public void commit(final H5Resolver h5Resolver) throws IOException {
        logger.trace("commit manager");
        pack();
//        h5Resolver.commit(rDataSegment);
//        h5Resolver.commit(rLocalHeap);
    }

    public void pack() {
        logger.trace("pack");
        long dataSegmentSize = localHeap.getDataSegmentSize();
        long offsetFreeList = localHeap.getOffsetHeadOfFreeList();
        while (1 != dataSegment.getNextFreeBlockOffset(offsetFreeList)) {
            offsetFreeList = dataSegment.getNextFreeBlockOffset(offsetFreeList);
        }
        final long lastFree = dataSegment.getFreeBlockSize(offsetFreeList);
        final long targetFreeBlockSize = 2 * localHeap.context().lengthSize();
        dataSegment.setFreeBlockSize(offsetFreeList, targetFreeBlockSize);
        dataSegmentSize = dataSegmentSize - lastFree + targetFreeBlockSize;
        localHeap.setDataSegmentSize(dataSegmentSize);
        dataSegment.setSize(dataSegmentSize);
    }

    public static LocalHeapManager of(final H5Resolver hdf5Resolver, final SizingContext sizingContext)
            throws IOException {
        return new LocalHeapManager(hdf5Resolver, sizingContext);
    }

    public static LocalHeapManager of(final H5Resolver hdf5Resolver, final Resolvable<? extends LocalHeap> rLocalHeap) {
        return new LocalHeapManager(hdf5Resolver, rLocalHeap);
    }
}
