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

import java.nio.ByteBuffer;
import java.util.EnumMap;

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.SizingContext;

public final class FileSpaceInfoMessageV1BB extends AbstractSizedBB<SizingContext> implements FileSpaceInfoMessageV1 {

    public FileSpaceInfoMessageV1BB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long minSize(final SizingContext sc) {
        return 3 + sc.lengthSize() + 4 + 1 + sc.offsetSize();
    }

    public static long maxSize(final SizingContext sc) {
        return 3 + sc.lengthSize() + 4 + 1 + 13 * sc.offsetSize();
    }

    @Override
    public long size() {
        return 3 + context.lengthSize() + 4 + 2 + context.lengthSize()
                + (isPersistingFreeSpace() ? 12 * context.offsetSize() : 0);
    }

    @Override
    public int getVersion() {
        return getByte(0);
    }

    @Override
    public StrategyV1 getStrategy() {
        switch (getByte(1)) {
        case 0:
            return StrategyV1.H5F_FSPACE_STRATEGY_FSM_AGGR;
        case 1:
            return StrategyV1.H5F_FSPACE_STRATEGY_PAGE;
        case 2:
            return StrategyV1.H5F_FSPACE_STRATEGY_AGGR;
        case 3:
            return StrategyV1.H5F_FSPACE_STRATEGY_NONE;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean isPersistingFreeSpace() {
        return 0 != getByte(2);
    }

    @Override
    public long getFreeSpacePersistingThreshold() {
        return getLength(3);
    }

    @Override
    public long getFileSpacePageSize() {
        return getUnsignedInt(3 + context.lengthSize());
    }

    @Override
    public int getPageEndMetadataThreshold() {
        return getUnsignedShort(3 + context.lengthSize() + 4);
    }

    @Override
    public long getEOA() {
        return getLength(3 + context.lengthSize() + 4 + 2);
    }

    private EnumMap<Manager, Long> readManagers(final int initialOffset) {
        final EnumMap<Manager, Long> map = new EnumMap<>(Manager.class);
        int offset = initialOffset;
        map.put(Manager.H5FD_MEM_SUPER, getOffset(offset));
        offset += context.offsetSize();
        map.put(Manager.H5FD_MEM_BTREE, getOffset(offset));
        offset += context.offsetSize();
        map.put(Manager.H5FD_MEM_DRAW, getOffset(offset));
        offset += context.offsetSize();
        map.put(Manager.H5FD_MEM_GHEAP, getOffset(offset));
        offset += context.offsetSize();
        map.put(Manager.H5FD_MEM_LHEAP, getOffset(offset));
        offset += context.offsetSize();
        map.put(Manager.H5FD_MEM_OHDR, getOffset(offset));
        offset += context.offsetSize();
        return map;
    }

    @Override
    public EnumMap<Manager, Long> getSmallSizeFreeSpaceManagers() {
        if (isPersistingFreeSpace()) {
            final int offset = 3 + context.lengthSize() + 4 + 2 + context.lengthSize();
            return readManagers(offset);
        } else {
            return null;
        }
    }

    @Override
    public EnumMap<Manager, Long> getLargeSizeFreeSpaceManagers() {
        if (isPersistingFreeSpace()) {
            final int offset = 3 + context.lengthSize() + 4 + 2 + context.lengthSize() + 6 * context.offsetSize();
            return readManagers(offset);
        } else {
            return null;
        }
    }

}
