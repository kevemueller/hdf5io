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

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.level2.ObjectHeaderV1BB.HeaderMessageEntryV1BB;
import app.keve.hdf5io.fileformat.level2.ObjectHeaderV2BB.AbstractHeaderMessageEntryV2BB;

public abstract class AbstractObjectHeader<T extends SizingContext> extends AbstractSizedBB<T> implements ObjectHeader {
    protected AbstractObjectHeader(final ByteBuffer buf, final T sizingContext) {
        super(buf, sizingContext);
    }

    public abstract static class AbstractHeaderMessageEntry<T extends SizingContext> extends AbstractSizedBB<T>
            implements HeaderMessageEntry<T> {

        public AbstractHeaderMessageEntry(final ByteBuffer buf, final T sizingContext) {
            super(buf, sizingContext);
        }

        public static long minSize(final SizingContext sc) {
            return Long.min(HeaderMessageEntryV1BB.minSize(sc), AbstractHeaderMessageEntryV2BB.size(sc));
        }

        public static long maxSize(final SizingContext sc) {
            return Long.max(HeaderMessageEntryV1BB.maxSize(sc), AbstractHeaderMessageEntryV2BB.size(sc));
        }

        public static HeaderMessageEntry of(final ByteBuffer buf, final SizingContext sc) {
            throw new IllegalArgumentException();
        }

    }
}
