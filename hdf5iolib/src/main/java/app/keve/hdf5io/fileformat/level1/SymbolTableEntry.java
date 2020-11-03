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

import app.keve.hdf5io.fileformat.H5ObjectW;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContextHeap;
import app.keve.hdf5io.fileformat.level2.ObjectHeader;
import app.keve.hdf5io.fileformat.level2message.SymbolTableMessage;

public interface SymbolTableEntry extends H5ObjectW<SizingContextHeap> {
    enum CacheType {
        NO_CACHE, GOH_IN_SCRATCH, SYMLINK_IN_SCRATCH
    }

    boolean isValid();

    Resolvable<String> getLinkName();

    void setLinkName(Resolvable<String> value);

    Resolvable<? extends ObjectHeader> getObjectHeader();

    void setObjectHeader(Resolvable<? extends ObjectHeader> value);

    CacheType getCacheType();

    void setCacheType(CacheType value);

    ByteBuffer getScratchpadSpace();

    void setScratchpadSpace(ByteBuffer value);

    SymbolTableMessage getScratchSymbolTable();

    void setScratchSymbolTable(SymbolTableMessage value);

    Resolvable<String> getSymlink();

    void setSymlink(Resolvable<String> value);

    static SymbolTableEntry of(final ByteBuffer byteBuffer, final SizingContextHeap sizingContext) {
        return new SymbolTableEntryBB(byteBuffer, sizingContext);
    }
}
