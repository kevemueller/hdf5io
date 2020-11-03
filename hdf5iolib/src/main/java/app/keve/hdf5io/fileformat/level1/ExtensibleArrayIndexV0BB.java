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
import java.nio.ByteOrder;
import java.util.Arrays;

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.util.JenkinsHash;

public final class ExtensibleArrayIndexV0BB extends AbstractSizedBB<SizingContext> implements ExtensibleArrayIndexV0 {
    public ExtensibleArrayIndexV0BB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long size(final SizingContext sc) {
        return 12 + 6 * sc.lengthSize() + sc.offsetSize() + 4;
    }

    @Override
    public long size() {
        return 12 + 6 * context.lengthSize() + context.offsetSize() + 4;
    }

    @Override
    public boolean isValid() {
        return Arrays.equals(SIGNATURE, getSignature()) && getChecksum() == JenkinsHash.hash(
                getEmbeddedData(0, 12 + 6 * context.lengthSize() + context.offsetSize()).order(ByteOrder.LITTLE_ENDIAN),
                0);
    }

    @Override
    public byte[] getSignature() {
        return getBytes(0, 4);
    }

    @Override
    public int getVersion() {
        return getUnsignedByte(4);
    }

    @Override
    public int getClientID() {
        return getUnsignedByte(5);
    }

    @Override
    public int getElementSize() {
        return getUnsignedByte(6);
    }

    @Override
    public int getMaxNelmtsBits() {
        return getUnsignedByte(7);
    }

    @Override
    public int getIndexBlkElmts() {
        return getUnsignedByte(8);
    }

    @Override
    public int getDataBlkMinElmts() {
        return getUnsignedByte(9);
    }

    @Override
    public int getSecondaryBlkMinDataPtrs() {
        return getUnsignedByte(10);
    }

    @Override
    public int getMaxDataBlkPageNelmtsBits() {
        return getUnsignedByte(11);
    }

    @Override
    public long getNumSecondaryBlks() {
        return getLength(12);
    }

    @Override
    public long getSecondaryBlkSize() {
        return getLength(12 + context.lengthSize());
    }

    @Override
    public long getNumDataBlks() {
        return getLength(12 + 2 * context.lengthSize());
    }

    @Override
    public long getDataBlkSize() {
        return getLength(12 + 3 * context.lengthSize());
    }

    @Override
    public long getMaxIndexSet() {
        return getLength(12 + 4 * context.lengthSize());
    }

    @Override
    public long getNumElements() {
        return getLength(12 + 5 * context.lengthSize());
    }

    @Override
    public Resolvable<ExtensibleArrayIndexBlock> getIndexBlock() {
        return getResolvable(12 + 6 * context.lengthSize(), ExtensibleArrayIndexBlock.class, context);
    }

    @Override
    public int getChecksum() {
        return getInt(12 + 6 * context.lengthSize() + context.offsetSize());
    }
}
