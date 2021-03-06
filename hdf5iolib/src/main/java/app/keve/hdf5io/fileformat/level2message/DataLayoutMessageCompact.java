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

public interface DataLayoutMessageCompact extends DataLayoutMessage {
    static long minSize(final SizingContext sc) {
        return Long.min(AbstractDataLayoutMessageV1BB.minSize(sc), Long.min(AbstractDataLayoutMessageV1BB.minSize(sc),
                Long.min(DataLayoutMessageV3CompactBB.minSize(sc), DataLayoutMessageV3CompactBB.minSize(sc))));
    }

    static long maxSize(final SizingContext sc) {
        return Long.max(AbstractDataLayoutMessageV1BB.maxSize(sc), Long.max(AbstractDataLayoutMessageV1BB.maxSize(sc),
                Long.max(DataLayoutMessageV3CompactBB.maxSize(sc), DataLayoutMessageV3CompactBB.maxSize(sc))));
    }

    int getCompactDataSize();

    ByteBuffer getCompactData();

    static DataLayoutMessageCompact of(final ByteBuffer buf, final SizingContext sizingContext) {
        switch (buf.get(0)) {
        case 1:
            assert 0 == buf.get(2);
            return new DataLayoutMessageV1CompactBB(buf, sizingContext);
        case 2:
            assert 0 == buf.get(2);
            return new DataLayoutMessageV2CompactBB(buf, sizingContext);
        case 3:
            assert 0 == buf.get(1);
            return new DataLayoutMessageV3CompactBB(buf, sizingContext);
        case 4:
            assert 0 == buf.get(1);
            return new DataLayoutMessageV4CompactBB(buf, sizingContext);
        default:
            throw new IllegalArgumentException("Implement data layout message version: " + buf.get(0));
        }
    }
}
