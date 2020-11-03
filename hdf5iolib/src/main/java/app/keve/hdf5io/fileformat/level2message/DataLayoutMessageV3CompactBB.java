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

public class DataLayoutMessageV3CompactBB extends AbstractDataLayoutMessageV3BB implements DataLayoutMessageV3Compact {

    public DataLayoutMessageV3CompactBB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static final long minSize(final SizingContext sc) {
        return 2 + 2 + 0;
    }

    public static final long maxSize(final SizingContext sc) {
        return MAX_MESSAGE_DATA;
    }

    @Override
    public final long size() {
        return 2 + 2 + getCompactDataSize();
    }

    @Override
    public final int getCompactDataSize() {
        return getUnsignedShort(2);
    }

    @Override
    public final ByteBuffer getCompactData() {
        return getEmbeddedData(4, getCompactDataSize());
    }

    @Override
    public String toString() {
        return String.format(
                "DataLayoutMessageV3CompactBB [size()=%s, getCompactDataSize()=%s, getCompactData()=%s, isValid()=%s, "
                        + "getLayoutClass()=%s, getVersion()=%s, getBuffer()=%s]",
                size(), getCompactDataSize(), getCompactData(), isValid(), getLayoutClass(), getVersion(), getBuffer());
    }

}
