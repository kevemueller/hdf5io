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

public interface DataLayoutMessageContiguous extends DataLayoutMessage {
    static long minSize(final SizingContext sc) {
        return Long.min(DataLayoutMessageV1ContiguousBB.minSize(sc),
                Long.min(DataLayoutMessageV2ContiguousBB.minSize(sc),
                        Long.min(DataLayoutMessageV3ContiguousBB.size(sc), DataLayoutMessageV4ContiguousBB.size(sc))));
    }

    static long maxSize(final SizingContext sc) {
        return Long.max(DataLayoutMessageV1ContiguousBB.maxSize(sc),
                Long.max(DataLayoutMessageV2ContiguousBB.maxSize(sc),
                        Long.max(DataLayoutMessageV3ContiguousBB.size(sc), DataLayoutMessageV4ContiguousBB.size(sc))));
    }

    Resolvable<ByteBuffer> getData();

    void setData(Resolvable<ByteBuffer> value);

    static DataLayoutMessageContiguous of(final ByteBuffer buf, final SizingContext sizingContext) {
        switch (buf.get(0)) {
        case 1:
            assert 1 == buf.get(2);
            return new DataLayoutMessageV1ContiguousBB(buf, sizingContext);
        case 2:
            assert 1 == buf.get(2);
            return new DataLayoutMessageV2ContiguousBB(buf, sizingContext);
        case 3:
            assert 1 == buf.get(1);
            return new DataLayoutMessageV3ContiguousBB(buf, sizingContext);
        case 4:
            assert 1 == buf.get(1);
            return new DataLayoutMessageV4ContiguousBB(buf, sizingContext);
        default:
            throw new IllegalArgumentException("Implement data layout message version: " + buf.get(0));
        }
    }
}
