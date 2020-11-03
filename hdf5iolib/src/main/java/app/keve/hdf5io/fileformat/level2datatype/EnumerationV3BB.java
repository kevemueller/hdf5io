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
package app.keve.hdf5io.fileformat.level2datatype;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import app.keve.hdf5io.fileformat.H5Context;

public final class EnumerationV3BB extends AbstractEnumerationBB {

    protected EnumerationV3BB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    @Override
    public long size() {
        return getValueOffset() + getNumberOfMembers() * getBaseType().getElementSize();
    }

    private int getValueOffset() {
        int ofs = 8 + (int) getBaseType().size();
        for (int i = 0; i < getNumberOfMembers(); i++) {
            final CharSequence name = getAsciiNulString(ofs);
            ofs += name.length() + 1;
        }
        return ofs;
    }

    @Override
    public AbstractDatatypeBB getBaseType() {
        return getEmbedded(8, AbstractDatatypeBB.class);
    }

    @Override
    public void setBaseType(final AbstractDatatypeBB baseType) {
        setEmbedded(8, baseType);
    }

    @Override
    public String[] getNames() {
        final String[] names = new String[getNumberOfMembers()];
        int ofs = 8 + (int) getBaseType().size();
        for (int i = 0; i < names.length; i++) {
            final CharSequence name = getAsciiNulString(ofs);
            names[i] = name.toString();
            ofs += name.length() + 1;
        }
        return names;
    }

    @Override
    public void setNames(final String[] value) {
        int ofs = 8 + (int) getBaseType().size();
        for (int i = 0; i < value.length; i++) {
            final byte[] nameBytes = value[i].getBytes(StandardCharsets.US_ASCII);
            setBytes(ofs, nameBytes);
            ofs += nameBytes.length;
            setByte(ofs++, 0);
        }
    }

    @Override
    public ByteBuffer getValueBuf() {
        return getEmbeddedData(getValueOffset(), getNumberOfMembers() * getElementSize());
    }

    @Override
    public void setValueBuf(final ByteBuffer value) {
        setEmbeddedData(getValueOffset(), getNumberOfMembers() * getElementSize(), value);
    }

    public static class BuilderBB extends AbstractBuilderBB {
        public BuilderBB(final H5Context context) {
            super(context, EnumerationV3BB::new);
        }
    }

}
