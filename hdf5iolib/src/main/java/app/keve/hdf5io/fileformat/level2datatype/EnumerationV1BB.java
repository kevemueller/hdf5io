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
import java.util.Arrays;

import app.keve.hdf5io.fileformat.H5Context;

public class EnumerationV1BB extends AbstractEnumerationBB {

    public EnumerationV1BB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    @Override
    public final long size() {
        return getValueOffset() + getNumberOfMembers() * getElementSize();
    }

    @Override
    public final AbstractDatatypeBB getBaseType() {
        return getEmbedded(8, AbstractDatatypeBB.class);
    }

    @Override
    public final void setBaseType(final AbstractDatatypeBB baseType) {
        setEmbedded(8, baseType);
    }

    private int getValueOffset() {
        int ofs = 8 + (int) getBaseType().size();
        final int sofs = ofs;
        for (int i = 0; i < getNumberOfMembers(); i++) {
            final CharSequence name = getAsciiNulString(ofs);
            ofs += name.length() + 1;
            ofs = sofs + (ofs - sofs + 7 & ~7);
        }
        return ofs;
    }

    @Override
    public final String[] getNames() {
        final String[] names = new String[getNumberOfMembers()];
        int ofs = 8 + (int) getBaseType().size();
        final int sofs = ofs;
        for (int i = 0; i < names.length; i++) {
            final CharSequence name = getAsciiNulString(ofs);
            names[i] = name.toString();
            ofs += name.length() + 1;
            ofs = sofs + (ofs - sofs + 7 & ~7);
        }
        return names;
    }

    @Override
    public final void setNames(final String[] value) {
        int ofs = 8 + (int) getBaseType().size();
        final int sofs = ofs;
        for (final String name : value) {
            final byte[] nameBytes = name.getBytes(StandardCharsets.US_ASCII);
            setBytes(ofs, nameBytes);
            ofs += nameBytes.length;
            setByte(ofs++, 0);
            final int pad = sofs + (ofs - sofs + 7 & ~7);
            while (ofs < pad) {
                setByte(ofs++, 0);
            }
        }
    }

    @Override
    public final ByteBuffer getValueBuf() {
        return getEmbeddedData(getValueOffset(), getNumberOfMembers() * getElementSize());
    }

    @Override
    public final void setValueBuf(final ByteBuffer value) {
        setEmbeddedData(getValueOffset(), getNumberOfMembers() * getElementSize(), value);
    }

    public static class BuilderBB extends AbstractBuilderBB {
        public BuilderBB(final H5Context context) {
            super(context, EnumerationV1BB::new);
        }

    }

    @Override
    public final String toString() {
        final int maxLen = 10;
        return String.format(
                "EnumerationV1BB [size()=%s, getBaseType()=%s, getValueOffset()=%s, getNames()=%s, getValueBuf()=%s, "
                        + "getNumberOfMembers()=%s, getValues()=%s, getVersion()=%s, getDatatypeClass()=%s, getClassBitField()=%s, "
                        + "getElementSize()=%s]",
                size(), getBaseType(), getValueOffset(),
                getNames() != null ? Arrays.asList(getNames()).subList(0, Math.min(getNames().length, maxLen)) : null,
                getValueBuf(), getNumberOfMembers(),
                getValues() != null ? Arrays.asList(getValues()).subList(0, Math.min(getValues().length, maxLen))
                        : null,
                getVersion(), getDatatypeClass(), getClassBitField(), getElementSize());
    }

}
