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
package app.keve.hdf5io.fileformat.level0;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.OptionalLong;

import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextHeap;
import app.keve.hdf5io.fileformat.level1.SymbolTableEntry;
import app.keve.hdf5io.fileformat.level1.SymbolTableEntryBB;

public class SuperblockV0BB extends AbstractSuperblockBB implements SuperblockV0 {
    protected int idxOffsets;

    protected SuperblockV0BB(final ByteBuffer buf, final SizingContext sizingContext, final int idxOffsets) {
        super(buf, sizingContext);
        this.idxOffsets = idxOffsets;
    }

    public static long size(final SizingContext sc) {
        return 8 + 8 + 8 + 4 * sc.offsetSize() + SymbolTableEntryBB.size(sc);
    }

    @Override
    public final long size() {
        return idxOffsets + 4 * context.offsetSize() + SymbolTableEntryBB.size(context);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && 0 == getByte(11) && 0 == getByte(15);
    }

    @Override
    public final int getVersionNumberFileFreeSpaceStorage() {
        return getByte(9);
    }

    @Override
    public final void setVersionNumberFileFreeSpaceStorage(final int value) {
        setByte(9, value);
    }

    @Override
    public final int getVersionNumberRootGroupSymbolTableEntry() {
        return getByte(10);
    }

    @Override
    public final void setVersionNumberRootGroupSymbolTableEntry(final int value) {
        setByte(10, value);
    }

    @Override
    public final int getVersionNumberSharedHeaderMessageFormat() {
        return getByte(12);
    }

    @Override
    public final void setVersionNumberSharedHeaderMessageFormat(final int value) {
        setByte(12, value);
    }

    @Override
    public final int getSizeOfOffsets() {
        return getByte(13);
    }

    @Override
    public final void setSizeOfOffsets(final int value) {
        setByte(13, value);
    }

    @Override
    public final int getSizeOfLengths() {
        return getByte(14);
    }

    @Override
    public final void setSizeOfLengths(final int value) {
        setByte(14, value);
    }

    @Override
    public final int getGroupLeafNodeK() {
        return getShort(16);
    }

    @Override
    public final void setGroupLeafNodeK(final int value) {
        setShort(16, value);
    }

    @Override
    public final int getGroupInternalNodeK() {
        return getShort(18);
    }

    @Override
    public final void setGroupInternalNodeK(final int value) {
        setShort(18, value);
    }

    @Override
    public final int getFileConsistencyFlags() {
        return getInt(20);
    }

    @Override
    public final void setFileConsistencyFlags(final int value) {
        setInt(20, value);
    }

    @Override
    public final long getBaseAddress() {
        return getOffset(idxOffsets);
    }

    @Override
    public final void setBaseAddress(final long value) {
        setOffset(idxOffsets, value);
    }

    @Override
    public final OptionalLong getAddressOfFileFreeSpaceInfo() {
        return getOptionalOffset(idxOffsets + context.offsetSize());
    }

    @Override
    public final void setAddressOfFileFreeSpaceInfo(final OptionalLong value) {
        setOptionalOffset(idxOffsets + context.offsetSize(), value);
    }

    @Override
    public final long getEndOfFileAddress() {
        return getOffset(idxOffsets + 2 * context.offsetSize());
    }

    @Override
    public final void setEndOfFileAddress(final long value) {
        setOffset(idxOffsets + 2 * context.offsetSize(), value);
    }

    @Override
    public final Resolvable<FileDriverInfo> getDriverInformationBlock() {
        return getResolvable(idxOffsets + 3 * context.offsetSize(), FileDriverInfo.class, context);
    }

    @Override
    public final void setDriverInformationBlock(final Resolvable<FileDriverInfo> value) {
        setResolvable(idxOffsets + 3 * context.offsetSize(), value);
    }

    @Override
    public final SymbolTableEntry getRootGroupSymbolTableEntry() {
        final SizingContextHeap hsc = SizingContextHeap.of(context, null);
        return getEmbedded(idxOffsets + 4 * context.offsetSize(), 0, SymbolTableEntry.class, hsc);
    }

    @Override
    public void initialize() {
        setFormatSignature();
        setVersionNumber(0);
        setByte(11, 0); // reserved
        setByte(15, 0); // reserved

        setVersionNumberFileFreeSpaceStorage(0);
        setVersionNumberRootGroupSymbolTableEntry(0);
        setVersionNumberSharedHeaderMessageFormat(0);
        setSizeOfOffsets(context.offsetSize());
        setSizeOfLengths(context.lengthSize());
        setGroupInternalNodeK(context.groupInternalNodeK());
        setGroupLeafNodeK(context.groupLeafNodeK());

        setFileConsistencyFlags(0);
        setBaseAddress(0);
        setAddressOfFileFreeSpaceInfo(OptionalLong.empty());
        setEndOfFileAddress(size());
        setDriverInformationBlock(null);
        getRootGroupSymbolTableEntry().initialize();
    }

    public static SuperblockV0 of(final ByteBuffer buf, final SizingContext sizingContext) {
        final ByteBuffer lobuf = buf.order(ByteOrder.LITTLE_ENDIAN); // FIXME: remove
        final SizingContext sc = 0 == sizingContext.offsetSize()
                ? sizingContext.with(lobuf.get(13), lobuf.get(14), lobuf.getShort(18), lobuf.getShort(16))
                : sizingContext;
        return new SuperblockV0BB(lobuf, sc, 24);
    }
}
