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
import java.time.Instant;

import app.keve.hdf5io.fileformat.H5Context;
import app.keve.hdf5io.fileformat.H5Object;

public interface ObjectModificationTimeMessageV1 extends H5Object<H5Context> {

    long SIZE = 8;

    boolean isValid();

    int getVersion();

    int getSeconds();

    default Instant getInstant() {
        return Instant.ofEpochSecond(getSeconds());
    }

    static ObjectModificationTimeMessageV1 of(final ByteBuffer buf, final H5Context context) {
        switch (buf.get(0)) {
        case 1:
            return new ObjectModificationTimeMessageV1BB(buf, context);
        default:
            throw new IllegalArgumentException();
        }
    }

}
