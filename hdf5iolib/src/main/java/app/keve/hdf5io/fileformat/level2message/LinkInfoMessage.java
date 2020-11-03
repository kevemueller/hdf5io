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
import java.util.OptionalLong;

import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.level1.BTreeV2;
import app.keve.hdf5io.fileformat.level1.FractalHeap;

/**
 * IV.A.2.c. The Link Info Message
 * (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#LinkInfoMessage)
 *
 */
public interface LinkInfoMessage extends H5Object<SizingContext> {
    int FLAG_CREATION_ORDER_TRACKED_MASK = 0b01;
    int FLAG_CREATION_ORDER_INDEXED_MASK = 0b10;

    static long minSize(final SizingContext sc) {
        return LinkInfoMessageV0BB.minSize(sc);
    }

    static long maxSize(final SizingContext sc) {
        return LinkInfoMessageV0BB.maxSize(sc);
    }

    int getVersion();

    int getFlags();

    default boolean isCreationOrderTracked() {
        return (getFlags() & FLAG_CREATION_ORDER_TRACKED_MASK) > 0;
    }

    default boolean isCreationOrderIndexed() {
        return (getFlags() & FLAG_CREATION_ORDER_INDEXED_MASK) > 0;
    }

    OptionalLong getMaximumCreationIndex();

    Resolvable<FractalHeap> getFractalHeap();

    Resolvable<BTreeV2> getBTreeV2NameIndex();

    Resolvable<BTreeV2> getBTreeV2CreationOrderIndex();

    static LinkInfoMessage of(final ByteBuffer buf, final SizingContext sizingContext) {
        switch (buf.get(0)) {
        case 0:
            return new LinkInfoMessageV0BB(buf, sizingContext);
        default:
            throw new IllegalArgumentException("Implement version " + buf.get(0));
        }
    }
}
