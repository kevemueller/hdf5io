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
import java.util.OptionalLong;

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextHeap;
import app.keve.hdf5io.fileformat.level2.ObjectHeader;
import app.keve.hdf5io.fileformat.level2message.SymbolTableMessage;

public final class SymbolTableEntryBB extends AbstractSizedBB<SizingContextHeap> implements SymbolTableEntry {
    public SymbolTableEntryBB(final ByteBuffer buf, final SizingContextHeap sizingContext) {
        super(buf, sizingContext);
    }

    public static long size(final SizingContext sc) {
        return 2 * sc.offsetSize() + 4 + 4 + 16;
    }

    @Override
    public long size() {
        return 2 * context.offsetSize() + 4 + 4 + 16;
    }

    @Override
    public boolean isValid() {
        return 0 == getInt(2 * context.offsetSize() + 4);
    }

    @Override
    public Resolvable<String> getLinkName() {
        final long stringOffset = getOffset(0); // why offset, should be length!
        return getResolvableHeapString(context.heap(), stringOffset);
    }

    @Override
    public void setLinkName(final Resolvable<String> string) {
//        final long stringOffset = getOffset(0); // why offset, should be length!
        setResolvableHeapString(0, string);
    }

    @Override
    public Resolvable<? extends ObjectHeader> getObjectHeader() {
        if (CacheType.SYMLINK_IN_SCRATCH == getCacheType()) {
            return null;
        }
        return getResolvable(context.offsetSize(), ObjectHeader.class, context);
    }

    @Override
    public void setObjectHeader(final Resolvable<? extends ObjectHeader> value) {
        if (CacheType.SYMLINK_IN_SCRATCH == getCacheType()) {
            return;
        }
        setResolvable(context.offsetSize(), value);
    }

    @Override
    public void initialize() {
        setLinkName(null);
        setObjectHeader(null);
        setCacheType(CacheType.NO_CACHE);
        setInt(2 * context.offsetSize() + 4, 0);
        setOptionalOffset(context.offsetSize(), OptionalLong.empty());
        setScratchpadSpace(ByteBuffer.wrap(new byte[16]));
    }

    @Override
    public CacheType getCacheType() {
        return CacheType.values()[getInt(2 * context.offsetSize())];
    }

    @Override
    public void setCacheType(final CacheType value) {
        setInt(2 * context.offsetSize(), value.ordinal());
    }

    @Override
    public ByteBuffer getScratchpadSpace() {
        return getEmbeddedData(2 * context.offsetSize() + 8, 16);
    }

    @Override
    public void setScratchpadSpace(final ByteBuffer value) {
        setEmbeddedData(2 * context.offsetSize() + 8, 16, value);
    }

    @Override
    public SymbolTableMessage getScratchSymbolTable() {
        if (CacheType.GOH_IN_SCRATCH == getCacheType()) {
            return getEmbedded(2 * context.offsetSize() + 8, 16, SymbolTableMessage.class, context);
        } else {
            return null;
        }
    }

    @Override
    public void setScratchSymbolTable(final SymbolTableMessage value) {
        throw new IllegalArgumentException();
//        if (CacheType.GOH_IN_SCRATCH == getCacheType()) {
//            return getEmbeddedSized(2 * sizingContext.offsetSize() + 8, 16, SymbolTableMessageBB::of, sizingContext);
//        } else {
//            return null;
//        }
    }

    @Override
    public Resolvable<String> getSymlink() {
        return CacheType.SYMLINK_IN_SCRATCH == getCacheType()
                ? getResolvableHeapString(context.heap(), getUnsignedInt(2 * context.offsetSize() + 8))
                : null;
    }

    @Override
    public void setSymlink(final Resolvable<String> value) {
        setCacheType(CacheType.SYMLINK_IN_SCRATCH);
        setResolvableHeapString(2 * context.offsetSize() + 8, value);
    }

}
