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

public final class ExtensibleArrayIndexBlockV0BB extends AbstractSizedBB<SizingContext>
        implements ExtensibleArrayIndexBlockV0 {
    public ExtensibleArrayIndexBlockV0BB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long size(final SizingContext sc) {
        return 0;
    }

    @Override
    public long size() {
        return available();
    }

    @Override
    public boolean isValid() {
        return Arrays.equals(SIGNATURE, getSignature()) && getChecksum() == JenkinsHash
                .hash(getEmbeddedData(0, 12 + 6 * context.lengthSize() + context.offsetSize()).slice()
                        .order(ByteOrder.LITTLE_ENDIAN), 0);
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
    public Resolvable<ExtensibleArrayIndex> getHeader() {
        return getResolvable(6, ExtensibleArrayIndex.class, context);
    }

    @Override
    public long getElements() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getDataBlockAddresses() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getSecondaryBlockAddresses() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getChecksum() {
        // TODO Auto-generated method stub
        return 0;
    }
}
