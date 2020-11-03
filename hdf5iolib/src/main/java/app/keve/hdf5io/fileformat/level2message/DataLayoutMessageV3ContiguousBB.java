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

import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;

public class DataLayoutMessageV3ContiguousBB extends AbstractDataLayoutMessageV3BB
        implements DataLayoutMessageV3Contiguous {
    public DataLayoutMessageV3ContiguousBB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static final long size(final SizingContext sizingContext) {
        return 2 + sizingContext.offsetSize() + sizingContext.lengthSize();
    }

    @Override
    public final long size() {
        return 2 + context.offsetSize() + context.lengthSize();
    }

    @Override
    public final Resolvable<ByteBuffer> getData() {
        final long length = getLength(2 + context.offsetSize());
        return getResolvable(2, (int) length);
    }

    @Override
    public final void setData(final Resolvable<ByteBuffer> value) {
        setResolvableByteBuffer(2, 2 + context.offsetSize(), value);
    }
}
