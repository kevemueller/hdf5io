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

import app.keve.hdf5io.fileformat.H5Context;
import app.keve.hdf5io.fileformat.H5MessageType;
import app.keve.hdf5io.fileformat.H5ObjectW;

public interface FillValueMessage extends H5Message<H5Context>, H5ObjectW<H5Context> {
    long MIN_SIZE_ALL = Long.min(FillValueMessageV1BB.MIN_SIZE,
            Long.min(FillValueMessageV2BB.MIN_SIZE, FillValueMessageV3BB.MIN_SIZE));
    long MAX_SIZE_ALL = Long.min(FillValueMessageV1BB.MAX_SIZE,
            Long.min(FillValueMessageV2BB.MAX_SIZE, FillValueMessageV3BB.MAX_SIZE));

    enum SpaceAllocation {
        EARLY, LATE, INCREMENTAL
    }

    enum WriteTime {
        ON_ALLOCATION, NEVER, IF_SET
    }

    @Override
    default H5MessageType getType() {
        return H5MessageType.FILL_VALUE;
    }

    static FillValueMessage of(final ByteBuffer buf, final H5Context context) {
        switch (buf.get(0)) {
        case 1:
            return new FillValueMessageV1BB(buf, context);
        case 2:
            return new FillValueMessageV2BB(buf, context);
        case 3:
            return new FillValueMessageV3BB(buf, context);
        default:
            throw new IllegalArgumentException("Implement FillValueMessage version " + buf.get(0));
        }
    }
}
