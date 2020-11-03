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

import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.SizingContext;

public interface FileSpaceInfoMessage extends H5Object<SizingContext> {
    static long minSize(final SizingContext sc) {
        return Long.min(FileSpaceInfoMessageV0BB.minSize(sc), FileSpaceInfoMessageV1BB.minSize(sc));
    }

    static long maxSize(final SizingContext sc) {
        return Long.max(FileSpaceInfoMessageV0BB.maxSize(sc), FileSpaceInfoMessageV1BB.maxSize(sc));
    }

    enum Manager {
        H5FD_MEM_SUPER, H5FD_MEM_BTREE, H5FD_MEM_DRAW, H5FD_MEM_GHEAP, H5FD_MEM_LHEAP, H5FD_MEM_OHDR
    }

    int getVersion();

    boolean isPersistingFreeSpace();

    long getFreeSpacePersistingThreshold();

    EnumMap<Manager, Long> getSmallSizeFreeSpaceManagers();

    static FileSpaceInfoMessage of(final ByteBuffer buf, final SizingContext sizingContext) {
        switch (buf.get(0)) {
        case 0:
            return new FileSpaceInfoMessageV0BB(buf, sizingContext);
        case 1:
            return new FileSpaceInfoMessageV1BB(buf, sizingContext);
        default:
            throw new IllegalArgumentException("Implement version: " + buf.get(0));
        }
    }
}
