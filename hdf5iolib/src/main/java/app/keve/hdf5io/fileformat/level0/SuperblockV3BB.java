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

public final class SuperblockV3BB extends SuperblockV2BB implements SuperblockV3 {
    private SuperblockV3BB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    @Override
    public void initialize() {
        super.initialize();
        setVersionNumber(3);
        setChecksum();
    }

    public static SuperblockV3 of(final ByteBuffer buf, final SizingContext sizingContext) {
        final ByteBuffer lobuf = buf.order(ByteOrder.LITTLE_ENDIAN);
        final SizingContext sc = 0 == sizingContext.offsetSize() ? sizingContext.with(lobuf.get(9), lobuf.get(10),
                SizingContext.DEFAULT_GROUP_INTERNAL_NODE_K, SizingContext.DEFAULT_GROUP_LEAF_NODE_K) : sizingContext;
        return new SuperblockV3BB(lobuf, sc);
    }

}
