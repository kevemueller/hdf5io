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

import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.level2.ObjectHeader;

public interface SharedMessage extends H5Object<SizingContext> {
    static long minSize(final SizingContext sc) {
        return Long.min(SharedMessageV1BB.size(sc),
                Long.min(SharedMessageV2BB.size(sc), SharedMessageV3BB.minSize(sc)));
    }

    static long maxSize(final SizingContext sc) {
        return Long.max(SharedMessageV1BB.size(sc),
                Long.max(SharedMessageV2BB.size(sc), SharedMessageV3BB.maxSize(sc)));
    }

    int getVersion();

    boolean isValid();

    int getType();

    Resolvable<ObjectHeader> getObjectHeader();

    static SharedMessage of(final ByteBuffer buf, final SizingContext sizingContext) {
        switch (buf.get(0)) {
        case 1:
            return new SharedMessageV1BB(buf, sizingContext);
        case 2:
            return new SharedMessageV2BB(buf, sizingContext);
        case 3:
            return new SharedMessageV3BB(buf, sizingContext);
        default:
            throw new IllegalArgumentException("implement version " + buf.get(0));
        }
    }
}
