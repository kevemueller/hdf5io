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
import java.util.Iterator;

import app.keve.hdf5io.fileformat.H5Context;

public final class CompoundV3BB extends AbstractCompoundBB {

    public CompoundV3BB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    @Override
    public Iterator<MemberV3BB> memberIterator() {
        return getIterator(8, MemberV3BB::new, getNumberOfMembers());
    }

    public final class MemberV3BB extends AbstractH5Member {
        private final int numByteOffsetBytes;

        public MemberV3BB(final ByteBuffer buf, final H5Context context) {
            super(buf, context);
            numByteOffsetBytes = (64 - Long.numberOfLeadingZeros(getElementSize()) + 7 & ~7) / 8;
        }

        @Override
        public long size() {
            return getNameSize() + numByteOffsetBytes + getMemberType().size();
        }

        private int getNameSize() {
            return getAsciiNulString(0).length() + 1;
        }

        @Override
        public String getName() {
            return getAsciiNulString(0).toString();
        }

        @Override
        public int getByteOffset() {
            return (int) getUnsignedNumber(getNameSize(), numByteOffsetBytes);
        }

        @Override
        public AbstractDatatypeBB getMemberType() {
            return getEmbedded(getNameSize() + numByteOffsetBytes, AbstractDatatypeBB.class);
        }
    }
}
