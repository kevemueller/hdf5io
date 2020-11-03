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

import app.keve.hdf5io.fileformat.SizingContext;

public final class SuperblockV1BB extends SuperblockV0BB implements SuperblockV1 {
    private SuperblockV1BB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext, 28);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && 0 == getShort(26);
    }

    @Override
    public int getIndexedStorageInternalNodeK() {
        return getShort(24);
    }

    @Override
    public void setIndexedStorageInternalNodeK(final int value) {
        setShort(24, value);
    }

    public static SuperblockV1 of(final ByteBuffer buf, final SizingContext sizingContext) {
        final ByteBuffer lobuf = buf.order(ByteOrder.LITTLE_ENDIAN); // FIXME: remove
        final SizingContext sc = 0 == sizingContext.offsetSize() ? sizingContext.with(lobuf.get(13), lobuf.get(14),
                lobuf.getShort(24), lobuf.getShort(18), lobuf.getShort(16)) : sizingContext;
        return new SuperblockV1BB(lobuf, sc);
    }

    @Override
    public void initialize() {
        super.initialize();
        setIndexedStorageInternalNodeK(SizingContext.DEFAULT_INDEXED_STORAGE_INTERNAL_NODE_K);
    }
}
