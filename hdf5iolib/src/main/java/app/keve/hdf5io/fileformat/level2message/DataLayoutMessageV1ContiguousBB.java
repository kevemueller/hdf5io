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
import java.util.OptionalLong;

import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;

public class DataLayoutMessageV1ContiguousBB extends AbstractDataLayoutMessageV1BB
        implements DataLayoutMessageV1Contiguous {

    public DataLayoutMessageV1ContiguousBB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext, 8 + sizingContext.offsetSize());
    }

    @Override
    public final long size() {
        return dimensionOffset + getDimensionality() * 4;
    }

    @Override
    public final void setDimensionSizes(final long... value) {
        setDimensionality(value.length);
        for (int i = 0; i < value.length; i++) {
            setUnsignedInt(dimensionOffset + i * 4, value[i]);
        }
    }

    @Override
    public final Resolvable<ByteBuffer> getData() {
        final OptionalLong at = getOptionalOffset(8);
        if (at.isEmpty()) {
            return null;
        }
        final long[] dim = getDimensionSizes();
        int size = 1;
        for (final long d : dim) {
            size *= d;
        }
        return getResolvable(8, size);
    }

    public final void setData(final Resolvable<ByteBuffer> data) {
        setResolvableByteBuffer(8, -1, data);
    }

    @Override
    public void initialize() {
        setVersion(1);
        setDimensionSizes();
        setLayoutClass(Layout.CONTIGUOUS);
        setData(null);
    }

}
