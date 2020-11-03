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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import app.keve.hdf5io.api.datatype.HDF5Datatype;
import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.SizingContext;

public class AttributeMessageV1BB extends AbstractSizedBB<SizingContext> implements AttributeMessageV1 {
    protected final int nameOffset;

    protected AttributeMessageV1BB(final ByteBuffer buf, final SizingContext sizingContext, final int nameOffset) {
        super(buf, sizingContext);
        this.nameOffset = nameOffset;
    }

    public AttributeMessageV1BB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
        this.nameOffset = 8;
    }

    public static long minSize(final SizingContext sc) {
        return 8 + 8 + 8 + 8;
    }

    public static long maxSize(final SizingContext sc) {
        return MAX_MESSAGE_DATA;
    }

    @Override
    public long size() {
        int offset = nameOffset;
        offset += getNameSize();
        offset = offset + 7 & ~7;
        offset += getDatatypeSize();
        offset = offset + 7 & ~7;
        offset += getDataspaceSize();
        offset = offset + 7 & ~7;
        offset += getDataspace().getDimensionBytes(getDatatype().getElementSize());
        return offset;
    }

    @Override
    public boolean isValid() {
        return 0 == getByte(1);
    }

    @Override
    public final int getVersion() {
        return getByte(0);
    }

    protected final int getNameSize() {
        return getUnsignedShort(2);
    }

    public final int getDatatypeSize() {
        return getUnsignedShort(4);
    }

    public final int getDataspaceSize() {
        return getUnsignedShort(6);
    }

    /**
     * Return the character set.
     * 
     * @return in this version constant US_ASCII.
     */
    protected Charset getCharset() {
        return StandardCharsets.US_ASCII;
    }

    @Override
    public final String getName() {
        return getString(nameOffset, getNameSize() - 1, getCharset()).toString();
    }

    @Override
    public HDF5Datatype getDatatype() {
        int offset = nameOffset;
        offset += getNameSize();
        offset = offset + 7 & ~7;
        return getEmbedded(offset, getDatatypeSize(), DatatypeMessage.class, context).getDatatype();
    }

    @Override
    public DataspaceMessage getDataspace() {
        int offset = nameOffset;
        offset += getNameSize();
        offset = offset + 7 & ~7;
        offset += getDatatypeSize();
        offset = offset + 7 & ~7;
        return getEmbedded(offset, getDataspaceSize(), DataspaceMessage.class);
    }

    @Override
    public ByteBuffer getData() {
        int offset = nameOffset;
        offset += getNameSize();
        offset = offset + 7 & ~7;
        offset += getDatatypeSize();
        offset = offset + 7 & ~7;
        offset += getDataspaceSize();
        offset = offset + 7 & ~7;
        return getEmbeddedData(offset, available() - offset);
    }
}
