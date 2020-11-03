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
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicLong;

import app.keve.hdf5io.fileformat.AbstractBB;
import app.keve.hdf5io.fileformat.H5Context;

public final class FilterPipelineMessageV2BB extends AbstractBB<H5Context> implements FilterPipelineMessageV2 {
    public static final long MIN_SIZE = 2;
    public static final long MAX_SIZE = MAX_MESSAGE_DATA;

    public FilterPipelineMessageV2BB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    @Override
    public long size() {
        final AtomicLong size = new AtomicLong(2);
        filterIterator().forEachRemaining(e -> size.addAndGet(e.size()));
        return size.get();
    }

    @Override
    public boolean isValid() {
        return true;
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
        return getIterator(2, FilterDescriptionV2.class, getNumberOfFilters());
    }

    public static final class FilterDescriptionV2BB extends AbstractBB<H5Context> implements FilterDescriptionV2 {
        public static final long MIN_SIZE = 6;
        public static final long MAX_SIZE = MAX_MESSAGE_DATA;

        public FilterDescriptionV2BB(final ByteBuffer buf, final H5Context context) {
            super(buf, context);
        }

        @Override
        public int getFilterIdentification() {
            return getUnsignedShort(0);
        }

        private OptionalInt getNameLength() {
            return getFilterIdentification() < 256 ? OptionalInt.empty() : OptionalInt.of(getUnsignedShort(2));
        }

        @Override
        public int getFlags() {
            int offset = 2;
            offset += getFilterIdentification() >= 256 ? 2 : 0;
            return getUnsignedShort(offset);
        }

        @Override
        public int getNumberOfClientDataValues() {
            int offset = 2;
            offset += getFilterIdentification() >= 256 ? 2 : 0;
            offset += 2;
            return getUnsignedShort(offset);
        }

        @Override
        public String getName() {
            if (getFilterIdentification() < 256 || 0 == getNameLength().getAsInt()) {
                return "";
            }
            int offset = 2;
            offset += getFilterIdentification() >= 256 ? 2 : 0;
            offset += 4;
            return getAsciiNulString(offset).toString();
        }

        @Override
        public IntBuffer getClientData() {
            int offset = 2;
            offset += getFilterIdentification() >= 256 ? 2 : 0;
            offset += 4;
            offset += getNameLength().orElse(0);
            return getEmbeddedData(offset, getNumberOfClientDataValues() * 4).order(ByteOrder.LITTLE_ENDIAN)
                    .asIntBuffer();
        }

        @Override
        public long size() {
            int offset = 2;
            offset += getFilterIdentification() >= 256 ? 2 : 0;
            offset += 4;
            offset += getNameLength().orElse(0);
            offset += getNumberOfClientDataValues() * 4;
            return offset;
        }
    }

}
