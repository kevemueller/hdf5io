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

import app.keve.hdf5io.api.datatype.HDF5Datatype;
import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.SizingContext;

public interface AttributeMessage extends H5Object<SizingContext> {

    static long minSize(final SizingContext sc) {
        return Long.min(AttributeMessageV1BB.minSize(sc),
                Long.min(AttributeMessageV2BB.minSize(sc), AttributeMessageV3BB.minSize(sc)));
    }

    static long maxSize(final SizingContext sc) {
        return Long.max(AttributeMessageV1BB.maxSize(sc),
                Long.max(AttributeMessageV2BB.maxSize(sc), AttributeMessageV3BB.maxSize(sc)));
    }

    boolean isValid();

    int getVersion();

    String getName();

    /**
     * Get the datatype.
     * 
     * @return the datatype.
     */
    HDF5Datatype getDatatype();

    /**
     * Get the dataspace information.
     * 
     * @return the dataspace information.
     */
    DataspaceMessage getDataspace();

    /**
     * Get the data's buffer.
     * 
     * @return the buffer.
     */
    ByteBuffer getData();

    static AttributeMessage of(final ByteBuffer buf, final SizingContext sizingContext) {
        switch (buf.get(0)) {
        case 1:
            return new AttributeMessageV1BB(buf, sizingContext);
        case 2:
            return new AttributeMessageV2BB(buf, sizingContext);
        case 3:
            return new AttributeMessageV3BB(buf, sizingContext);
        default:
            throw new IllegalArgumentException("implement version " + buf.get(0));
        }
    }
}
