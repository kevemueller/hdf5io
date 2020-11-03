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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import app.keve.hdf5io.fileformat.H5ObjectW;
import app.keve.hdf5io.fileformat.SizingContextHeap;

public interface GroupSymbolTableNode extends H5ObjectW<SizingContextHeap> {

    byte[] SIGNATURE = {'S', 'N', 'O', 'D'};

    boolean isValid();

    byte[] getSignature();

    void setSignature();

    int getVersion();

    void setVersion(int value);

    int getNumberOfSymbols();

    void setNumberOfSymbols(int value);

    Iterator<SymbolTableEntry> entryIterator();

    default List<SymbolTableEntry> getSymbolTableEntries() {
        final ArrayList<SymbolTableEntry> list = new ArrayList<>(getNumberOfSymbols());
        entryIterator().forEachRemaining(list::add);
        return list;
    }

    SymbolTableEntry getEntry(int idx);

    void setEntry(int idx, SymbolTableEntry value);

    static GroupSymbolTableNode of(final ByteBuffer buf, final SizingContextHeap sizingContext) {
        return new GroupSymbolTableNodeBB(buf, sizingContext);
    }
}
