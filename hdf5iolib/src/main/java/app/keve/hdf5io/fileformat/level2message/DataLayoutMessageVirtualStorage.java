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

import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.level1.GlobalHeapCollection;

public interface DataLayoutMessageVirtualStorage extends DataLayoutMessage {
    static long size(final SizingContext sc) {
        return DataLayoutMessageV4VirtualStorageBB.size(sc);
    }

    Resolvable<GlobalHeapCollection> getGlobalHeapCollection();

    long getIndex();

    static DataLayoutMessageVirtualStorage of(final ByteBuffer buf, final SizingContext sizingContext) {
        switch (buf.get(0)) {
        case 4:
            assert 3 == buf.get(1);
            return new DataLayoutMessageV4VirtualStorageBB(buf, sizingContext);
        default:
            throw new IllegalArgumentException("Implement data layout message virtual storage version: " + buf.get(0));
        }
    }
}
