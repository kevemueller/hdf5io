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
package app.keve.hdf5io.fileformat.level2message;

import java.nio.ByteBuffer;

import app.keve.hdf5io.fileformat.SizingContext;

public class DataLayoutMessageV1CompactBB extends AbstractDataLayoutMessageV1BB implements DataLayoutMessageV1Compact {

    public DataLayoutMessageV1CompactBB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext, 8);
    }

    @Override
    public final long size() {
        return dimensionOffset + getDimensionality() * 4 + 4 + getCompactDataSize();
    }

    @Override
    public final int getCompactDataSize() {
        return getSmallUnsignedInt(dimensionOffset + getDimensionality() * 4);
    }

    @Override
    public final ByteBuffer getCompactData() {
        return getEmbeddedData(dimensionOffset + getDimensionality() * 4 + 4, getCompactDataSize());
    }

}
