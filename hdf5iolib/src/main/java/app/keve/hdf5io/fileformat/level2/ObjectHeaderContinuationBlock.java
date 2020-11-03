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
package app.keve.hdf5io.fileformat.level2;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextOHV2;

public interface ObjectHeaderContinuationBlock<T extends SizingContext> extends H5Object<T> {

    static long minSize(final SizingContext sc) {
        return Long.min(ObjectHeaderContinuationBlockV1BB.minSize(sc), ObjectHeaderContinuationBlockV2BB.minSize(sc));
    }

    static long maxSize(final SizingContext sc) {
        return Long.max(ObjectHeaderContinuationBlockV1BB.maxSize(sc), ObjectHeaderContinuationBlockV2BB.maxSize(sc));
    }

    boolean isValid();

    int getVersion();

    Iterator<? extends ObjectHeader.HeaderMessageEntry<?>> headerMessageIterator();

    List<ObjectHeader.HeaderMessageEntry<?>> getHeaderMessages();

    static ObjectHeaderContinuationBlock of(final ByteBuffer buf, final SizingContext sizingContext) {
        final byte[] signature = new byte[] {buf.get(0), buf.get(1), buf.get(2), buf.get(3)};
        if (Arrays.equals(ObjectHeaderContinuationBlockV2.SIGNATURE, signature)) {
            return new ObjectHeaderContinuationBlockV2BB(buf, (SizingContextOHV2) sizingContext);
        } else {
            return new ObjectHeaderContinuationBlockV1BB(buf, sizingContext);
        }
    }
}
