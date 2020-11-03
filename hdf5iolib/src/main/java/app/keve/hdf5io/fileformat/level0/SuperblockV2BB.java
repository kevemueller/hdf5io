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

import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.level2.ObjectHeader;
import app.keve.hdf5io.util.JenkinsHash;
import app.keve.hdf5io.util.Unsigned;

public class SuperblockV2BB extends AbstractSuperblockBB implements SuperblockV2 {
    protected SuperblockV2BB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static final long size(final SizingContext sc) {
        return 12 + 4 * sc.offsetSize() + 4;
    }

    @Override
    public final long size() {
        return 12 + 4 * context.offsetSize() + 4;
    }

    @Override
    public final boolean isValid() {
        return super.isValid() && getChecksum() == JenkinsHash
                .hash(getEmbeddedData(0, (int) (size() - 4)).order(ByteOrder.LITTLE_ENDIAN), 0);
    }

    @Override
    public final int getSizeOfOffsets() {
        return getByte(9);
    }

    @Override
    public final void setSizeOfOffsets(final int value) {
        setByte(9, value);
    }

    @Override
    public final int getSizeOfLengths() {
        return getByte(10);
    }

    @Override
    public final void setSizeOfLengths(final int value) {
        setByte(10, value);
    }

    @Override
    public final int getFileConsistencyFlags() {
        return getByte(11);
    }

    @Override
    public final void setFileConsistencyFlags(final int value) {
        setInt(11, value);
    }

    @Override
    public final long getBaseAddress() {
        return getOffset(12);
    }

    @Override
    public final void setBaseAddress(final long value) {
        setOffset(12, value);
    }

    @Override
    public final Resolvable<ObjectHeader> getSuperblockExtension() {
        return getResolvable(12 + context.offsetSize(), ObjectHeader.class, context);
    }

    @Override
    public final void setSuperblockExtension(final Resolvable<ObjectHeader> value) {
        setResolvable(12 + context.offsetSize(), value);
    }

    @Override
    public final long getEndOfFileAddress() {
        return getOffset(12 + 2 * context.offsetSize());
    }

    @Override
    public final void setEndOfFileAddress(final long value) {
        setOffset(12 + 2 * context.offsetSize(), value);
    }

    @Override
    public final Resolvable<ObjectHeader> getRootGroupObjectHeader() {
        return getResolvable(12 + 3 * context.offsetSize(), ObjectHeader.class, context);
    }

    @Override
    public final void setRootGroupObjectHeader(final Resolvable<ObjectHeader> value) {
        setResolvable(12 + 3 * context.offsetSize(), value);
    }

    @Override
    @Unsigned
    public final int getChecksum() {
        return getInt(12 + 4 * getSizeOfOffsets());
    }

    @Override
    public final void setChecksum() {
        final int checksum = JenkinsHash.hash(getEmbeddedData(0, (int) (size() - 4)).order(ByteOrder.LITTLE_ENDIAN), 0);
        setInt(12 + 4 * getSizeOfOffsets(), checksum);
    }

    @Override
    public void initialize() {
        setFormatSignature();
        setVersionNumber(2);
        setSizeOfOffsets(context.offsetSize());
        setSizeOfLengths(context.lengthSize());

        setFileConsistencyFlags(0);
        setBaseAddress(0);
        setSuperblockExtension(null);
        setEndOfFileAddress(size());
        setRootGroupObjectHeader(null);
        setChecksum();
    }

    @Override
    public final void pack() {
        super.pack();
        setChecksum();
    }

    public static SuperblockV2 of(final ByteBuffer buf, final SizingContext sizingContext) {
        final ByteBuffer lobuf = buf.order(ByteOrder.LITTLE_ENDIAN);
        final SizingContext sc = 0 == sizingContext.offsetSize() ? sizingContext.with(lobuf.get(9), lobuf.get(10),
                SizingContext.DEFAULT_GROUP_INTERNAL_NODE_K, SizingContext.DEFAULT_GROUP_LEAF_NODE_K) : sizingContext;
        return new SuperblockV2BB(lobuf, sc);
    }

}
