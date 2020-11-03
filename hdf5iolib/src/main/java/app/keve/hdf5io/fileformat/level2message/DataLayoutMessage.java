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

import app.keve.hdf5io.fileformat.H5MessageType;
import app.keve.hdf5io.fileformat.SizingContext;

public interface DataLayoutMessage extends H5Message<SizingContext> {

    enum Layout {
        COMPACT, CONTIGUOUS, CHUNKED, VIRTUAL_STORAGE
    }

    int TYPE = 0x0008;

    static long minSize(final SizingContext sc) {
        return Long.min(DataLayoutMessageCompact.minSize(sc), Long.min(DataLayoutMessageContiguous.minSize(sc),
                Long.min(DataLayoutMessageChunked.minSize(sc), DataLayoutMessageVirtualStorage.size(sc))));
    }

    static long maxSize(final SizingContext sc) {
        return Long.max(DataLayoutMessageCompact.maxSize(sc), Long.max(DataLayoutMessageContiguous.maxSize(sc),
                Long.max(DataLayoutMessageChunked.maxSize(sc), DataLayoutMessageVirtualStorage.size(sc))));
    }

    @Override
    default H5MessageType getType() {
        return H5MessageType.DATA_LAYOUT;
    }

    boolean isValid();

    int getVersion();

    void setVersion(int value);

    Layout getLayoutClass();

    void setLayoutClass(Layout value);

    static DataLayoutMessage of(final ByteBuffer buf, final SizingContext sizingContext) {
        switch (buf.get(0)) {
        case 1:
            switch (buf.get(2)) {
            case 0:
                return new DataLayoutMessageV1CompactBB(buf, sizingContext);
            case 1:
                return new DataLayoutMessageV1ContiguousBB(buf, sizingContext);
            case 2:
                return new DataLayoutMessageV1ChunkedBB(buf, sizingContext);
            default:
                throw new IllegalArgumentException("Datalayout implementV1 " + buf.get(2));
            }
        case 2:
            switch (buf.get(2)) {
            case 0:
                return new DataLayoutMessageV2CompactBB(buf, sizingContext);
            case 1:
                return new DataLayoutMessageV2ContiguousBB(buf, sizingContext);
            case 2:
                return new DataLayoutMessageV2ChunkedBB(buf, sizingContext);
            default:
                throw new IllegalArgumentException("Datalayout implementV2 " + buf.get(2));
            }
        case 3:
            switch (buf.get(1)) {
            case 0:
                return new DataLayoutMessageV3CompactBB(buf, sizingContext);
            case 1:
                return new DataLayoutMessageV3ContiguousBB(buf, sizingContext);
            case 2:
                return new DataLayoutMessageV3ChunkedBB(buf, sizingContext);
            default:
                throw new IllegalArgumentException("Datalayout implementV3 " + buf.get(1));
            }
        case 4:
            switch (buf.get(1)) {
            case 0:
                return new DataLayoutMessageV4CompactBB(buf, sizingContext);
            case 1:
                return new DataLayoutMessageV4ContiguousBB(buf, sizingContext);
            case 2:
                return new DataLayoutMessageV4ChunkedBB(buf, sizingContext);
            case 3:
                return new DataLayoutMessageV4VirtualStorageBB(buf, sizingContext);
            default:
                throw new IllegalArgumentException("Datalayout implementV4 " + buf.get(1));
            }
        default:
            throw new IllegalArgumentException("Implement data layout message version: " + buf.get(0));
        }
    }
}
