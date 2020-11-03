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

public final class FileSpaceInfoMessageV0BB extends AbstractSizedBB<SizingContext> implements FileSpaceInfoMessageV0 {
    public FileSpaceInfoMessageV0BB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long minSize(final SizingContext sc) {
        return 2 + sc.lengthSize();
    }

    public static long maxSize(final SizingContext sc) {
        return 2 + sc.lengthSize() + 6 * sc.offsetSize();
    }

    @Override
    public long size() {
        return 2 + context.lengthSize() + (isPersistingFreeSpace() ? 6 * context.offsetSize() : 0);
    }

    @Override
    public int getVersion() {
        return getByte(0);
    }

    @Override
    public StrategyV0 getStrategy() {
        switch (getByte(1)) {
        case 1:
            return StrategyV0.H5F_FILE_SPACE_ALL_PERSIST;
        case 2:
            return StrategyV0.H5F_FILE_SPACE_ALL;
        case 3:
            return StrategyV0.H5F_FILE_SPACE_AGGR_VFD;
        case 4:
            return StrategyV0.H5F_FILE_SPACE_VFD;
        default:
            throw new IllegalArgumentException("Unknown strategy " + getByte(1));
        }
    }

    @Override
    public boolean isPersistingFreeSpace() {
        return StrategyV0.H5F_FILE_SPACE_ALL_PERSIST == getStrategy();
    }

    @Override
    public long getFreeSpacePersistingThreshold() {
        return getLength(2);
    }

    @Override
    public EnumMap<Manager, Long> getSmallSizeFreeSpaceManagers() {
        if (isPersistingFreeSpace()) {
            final EnumMap<Manager, Long> map = new EnumMap<Manager, Long>(Manager.class);
            map.put(Manager.H5FD_MEM_SUPER, getOffset(2 + context.lengthSize()));
            map.put(Manager.H5FD_MEM_BTREE, getOffset(2 + context.lengthSize() + context.offsetSize()));
            map.put(Manager.H5FD_MEM_DRAW, getOffset(2 + context.lengthSize() + 2 * context.offsetSize()));
            map.put(Manager.H5FD_MEM_GHEAP, getOffset(2 + context.lengthSize() + 3 * context.offsetSize()));
            map.put(Manager.H5FD_MEM_LHEAP, getOffset(2 + context.lengthSize() + 4 * context.offsetSize()));
            map.put(Manager.H5FD_MEM_OHDR, getOffset(2 + context.lengthSize() + 5 * context.offsetSize()));
            return map;
        } else {
            return null;
        }
    }

}
