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

public interface DataspaceMessageV2 extends DataspaceMessage {
    enum DataspaceType {
        SCALAR, SIMPLE, NULL
    }

    static long minSize(final SizingContext sizingContext) {
        return 4 + 1 * sizingContext.lengthSize();
    }

    static long maxSize(final SizingContext sizingContext) {
        return 4 + 255 * 2 * sizingContext.lengthSize();
    }

    DataspaceType getDataspaceType();

    static DataspaceMessageV2 of(final ByteBuffer buf, final SizingContext sizingContext) {
        switch (buf.get(0)) {
        case 2:
            return new DataspaceMessageV2BB(buf, sizingContext);
        default:
            throw new IllegalArgumentException("Implement DataspaceMessage version " + buf.get(0));
        }
    }

}
