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
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import app.keve.hdf5io.fileformat.H5Context;
import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.level2message.FilterPipelineMessageV1BB.FilterDescriptionV1BB;
import app.keve.hdf5io.fileformat.level2message.FilterPipelineMessageV2BB.FilterDescriptionV2BB;

public interface FilterPipelineMessage extends H5Object<H5Context> {

    long MIN_SIZE = Long.min(FilterPipelineMessageV1BB.MIN_SIZE, FilterPipelineMessageV2BB.MIN_SIZE);
    long MAX_SIZE = Long.min(FilterPipelineMessageV1BB.MAX_SIZE, FilterPipelineMessageV2BB.MAX_SIZE);

    boolean isValid();

    int getVersion();

    int getNumberOfFilters();

    Iterator<? extends FilterDescription> filterIterator();

    default List<FilterDescription> getFilters() {
        final ArrayList<FilterDescription> list = new ArrayList<>(getNumberOfFilters());
        filterIterator().forEachRemaining(list::add);
        return list;
    }

    interface FilterDescription extends H5Object<H5Context> {
        long MIN_SIZE = Long.min(FilterDescriptionV1BB.MIN_SIZE, FilterDescriptionV2BB.MIN_SIZE);
        long MAX_SIZE = Long.max(FilterDescriptionV1BB.MAX_SIZE, FilterDescriptionV2BB.MAX_SIZE);

        int FLAG_OPTIONAL_MASK = 0x01;
        int FILTER_RESERVED = 0;
        int FILTER_DEFLATE = 1;
        int FILTER_SHUFFLE = 2;
        int FILTER_FLETCHER32 = 3;
        int FILTER_SZIP = 4;
        int FILTER_NBIT = 5;
        int FILTER_SCALEOFFSET = 6;

        int getFilterIdentification();

        String getName();

        int getFlags();

        default boolean isOptional() {
            return (getFlags() & FLAG_OPTIONAL_MASK) > 0;
        }

        int getNumberOfClientDataValues();

        IntBuffer getClientData();

        static FilterDescription of(final ByteBuffer bb, final H5Context context) {
            throw new IllegalArgumentException();
        }
    }

    static FilterPipelineMessage of(final ByteBuffer buf, final H5Context context) {
        switch (buf.get(0)) {
        case 1:
            return new FilterPipelineMessageV1BB(buf, context);
        case 2:
            return new FilterPipelineMessageV2BB(buf, context);
        default:
            throw new IllegalArgumentException();
        }
    }
}
