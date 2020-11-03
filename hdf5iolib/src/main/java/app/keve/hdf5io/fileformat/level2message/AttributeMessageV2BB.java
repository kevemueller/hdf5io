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
import app.keve.hdf5io.fileformat.SizingContext;

public class AttributeMessageV2BB extends AttributeMessageV1BB implements AttributeMessageV2 {

    protected AttributeMessageV2BB(final ByteBuffer buf, final SizingContext sizingContext, final int nameOffset) {
        super(buf, sizingContext, nameOffset);
    }

    public AttributeMessageV2BB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext, 8);
    }

    @Override
    public final boolean isValid() {
        return true;
    }

    @Override
    public final long size() {
        long size = nameOffset;
        size += getNameSize();
        size += getDatatypeSize();
        size += getDataspaceSize();
        if (!isDatatypeShared() && !isDataspaceShared()) {
            size += getDataspace().getDimensionBytes(getDatatype().getElementSize());
        } else {
            size = available(); // we really can't tell, we would need to resolve the message, iterate the
                                // objectheader&continuations, look up the message entry, ...
        }
        return size;
    }

    @Override
    public final int getFlags() {
        return getByte(1);
    }

    @Override
    public final HDF5Datatype getDatatype() {
        int offset = nameOffset;
        offset += getNameSize();
        if (isDatatypeShared()) {
            return null;
        }
        return getEmbedded(offset, getDatatypeSize(), DatatypeMessage.class, context).getDatatype();
    }

    @Override
    public final SharedMessage getSharedDatatype() {
        int offset = nameOffset;
        offset += getNameSize();
        if (isDatatypeShared()) {
            return getEmbedded(offset, SharedMessage.class);
        }
        return null;
    }

    @Override
    public final DataspaceMessage getDataspace() {
        int offset = nameOffset;
        offset += getNameSize();
        offset += getDatatypeSize();
        if (isDataspaceShared()) {
            return null;
        }
        return getEmbedded(offset, getDataspaceSize(), DataspaceMessage.class);
    }

    @Override
    public final SharedMessage getSharedDataspace() {
        int offset = nameOffset;
        offset += getNameSize();
        offset += getDatatypeSize();
        if (isDataspaceShared()) {
            return getEmbedded(offset, SharedMessage.class);
        }
        return null;
    }

    @Override
    public final ByteBuffer getData() {
        int offset = nameOffset;
        offset += getNameSize();
        offset += getDatatypeSize();
        offset += getDataspaceSize();
        // getDatatype().getElementSize() * getDataspace().getDimensionSizes()
        return getEmbeddedData(offset, available() - offset);
    }

}
