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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import app.keve.hdf5io.fileformat.SizingContext;

public final class ObjectHeaderContinuationBlockV1BB extends AbstractObjectHeader<SizingContext>
        implements ObjectHeaderContinuationBlockV1 {
    // buf MUST be limited to the size of the continuation block
    public ObjectHeaderContinuationBlockV1BB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long minSize(final SizingContext sc) {
        return 0;
    }

    public static long maxSize(final SizingContext sc) {
        return Long.MAX_VALUE;
    }

    @Override
    public long size() {
        return available();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public Iterator<? extends HeaderMessageEntry<?>> headerMessageIterator() {
        return getIteratorByteSized(0, ObjectHeaderV1.HeaderMessageEntryV1.class, available(), 0);
    }

    @Override
    public List<HeaderMessageEntry<?>> getHeaderMessages() {
        final List<HeaderMessageEntry<?>> headerMessages = new ArrayList<>();
        headerMessageIterator().forEachRemaining(headerMessages::add);
        return headerMessages;
    }
}
