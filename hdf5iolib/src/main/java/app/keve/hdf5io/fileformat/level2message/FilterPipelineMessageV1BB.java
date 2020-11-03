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
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import app.keve.hdf5io.fileformat.AbstractBB;
import app.keve.hdf5io.fileformat.H5Context;

public final class FilterPipelineMessageV1BB extends AbstractBB<H5Context> implements FilterPipelineMessageV1 {
    public static final long MIN_SIZE = 8;
    public static final long MAX_SIZE = MAX_MESSAGE_DATA;

    public FilterPipelineMessageV1BB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    @Override
    public long size() {
        final AtomicLong size = new AtomicLong(8);
        filterIterator().forEachRemaining(e -> size.addAndGet(e.size()));
        return size.get();
    }

    @Override
    public boolean isValid() {
        return 0 == getShort(2) && 0 == getInt(4);
    }

    @Override
    public int getVersion() {
        return getByte(0);
    }

    @Override
    public int getNumberOfFilters() {
        return getUnsignedByte(1);
    }

    @Override
    public Iterator<? extends FilterDescription> filterIterator() {
        return getIterator(8, FilterDescriptionV1.class, getNumberOfFilters());
    }

    public static final class FilterDescriptionV1BB extends AbstractBB<H5Context> implements FilterDescriptionV1 {
        public static final long MIN_SIZE = 8 + 8 + 0;
        public static final long MAX_SIZE = MAX_MESSAGE_DATA;

        public FilterDescriptionV1BB(final ByteBuffer buf, final H5Context context) {
            super(buf, context);
        }

        @Override
        public int getFilterIdentification() {
            return getUnsignedShort(0);
        }

        private int getNameLength() {
            return getUnsignedShort(2);
        }

        @Override
        public String getName() {
            return 0 == getNameLength() ? "" : getAsciiNulString(8).toString();
        }

        @Override
        public int getFlags() {
            return getUnsignedShort(4);
        }

        @Override
        public int getNumberOfClientDataValues() {
            return getUnsignedShort(6);
        }

        @Override
        public IntBuffer getClientData() {
            int offset = 8;
            offset += getNameLength();
            offset = offset + 7 & ~7;
            return getEmbeddedData(offset, getNumberOfClientDataValues() * 4).order(ByteOrder.LITTLE_ENDIAN)
                    .asIntBuffer();
        }

        @Override
        public long size() {
            int offset = 8;
            offset += getNameLength();
            offset = offset + 7 & ~7;
            offset += getNumberOfClientDataValues() * 4;
            offset = offset + 7 & ~7;
            return offset;
        }
    }
}
