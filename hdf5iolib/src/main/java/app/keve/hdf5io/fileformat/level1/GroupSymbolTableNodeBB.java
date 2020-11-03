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
import java.util.Arrays;
import java.util.Iterator;

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextHeap;

public final class GroupSymbolTableNodeBB extends AbstractSizedBB<SizingContextHeap> implements GroupSymbolTableNode {
    private final int entrySize;

    public GroupSymbolTableNodeBB(final ByteBuffer buf, final SizingContextHeap sizingContext) {
        super(buf, sizingContext);
        this.entrySize = (int) SymbolTableEntryBB.size(sizingContext);
    }

    public static long size(final SizingContext sc) {
        return 8 + 2 * sc.groupLeafNodeK() * SymbolTableEntryBB.size(sc);
    }

    @Override
    public long size() {
        return 8 + 2 * context.groupLeafNodeK() * entrySize;
    }

    @Override
    public boolean isValid() {
        return Arrays.equals(SIGNATURE, getSignature()) && 0 == getByte(5);
    }

    @Override
    public byte[] getSignature() {
        return new byte[] {getByte(0), getByte(1), getByte(2), getByte(3)};
    }

    @Override
    public void setSignature() {
        setBytes(0, SIGNATURE);
    }

    @Override
    public int getVersion() {
        return getUnsignedByte(4);
    }

    @Override
    public void setVersion(final int value) {
        setByte(4, value);
    }

    @Override
    public int getNumberOfSymbols() {
        return getUnsignedShort(6);
    }

    @Override
    public void setNumberOfSymbols(final int value) {
        setUnsignedShort(6, value);
    }

    @Override
    public Iterator<SymbolTableEntry> entryIterator() {
        return getIterator(8, SymbolTableEntry.class, getNumberOfSymbols());
    }

    @Override
    public SymbolTableEntry getEntry(final int idx) {
        return getEmbedded(8 + idx * entrySize, entrySize, SymbolTableEntry.class);
    }

    @Override
    public void setEntry(final int idx, final SymbolTableEntry value) {
        setEmbedded(8 + idx * entrySize, (SymbolTableEntryBB) value);
    }

    @Override
    public void initialize() {
        setSignature();
        setVersion(1);
        setNumberOfSymbols(0);
    }
}
