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

public final class CompoundV2BB extends AbstractCompoundBB {

    public CompoundV2BB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    @Override
    public Iterator<MemberV2BB> memberIterator() {
        return getIterator(8, MemberV2BB.class, getNumberOfMembers());
    }

    public static final class MemberV2BB extends AbstractH5Member {
        public static final long MIN_SIZE = 8 + 4 + 8;
        public static final long MAX_SIZE = 8 + 4 + UINT16_MAX_VALUE;

        public MemberV2BB(final ByteBuffer buf, final H5Context context) {
            super(buf, context);
        }

        public static MemberV2BB of(final ByteBuffer buf, final H5Context context) {
            return new MemberV2BB(buf, context);
        }

        @Override
        public long size() {
            return getNameSize() + 4 + getMemberType().size();
        }

        private int getNameSize() {
            return getAsciiNulString(0).length() + 1 + 7 & ~7;
        }

        @Override
        public String getName() {
            return getAsciiNulString(0).toString();
        }

        @Override
        public int getByteOffset() {
            return getSmallUnsignedInt(getNameSize());
        }

        @Override
        public AbstractDatatypeBB getMemberType() {
            return getEmbedded(getNameSize() + 4, AbstractDatatypeBB.class);
        }
    }
}
