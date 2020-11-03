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
import java.util.Arrays;
import java.util.Iterator;

import app.keve.hdf5io.fileformat.H5Context;

public final class CompoundV1BB extends AbstractCompoundBB {

    public CompoundV1BB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    @Override
    public Iterator<MemberV1BB> memberIterator() {
        return getIterator(8, MemberV1BB.class, getNumberOfMembers());
    }

    public static final class MemberV1BB extends AbstractH5Member {
        public static final long MIN_SIZE = 8 + 16 + 4 * 4 + 8;
        public static final long MAX_SIZE = UINT16_MAX_VALUE;

        public MemberV1BB(final ByteBuffer buf, final H5Context context) {
            super(buf, context);
        }

        @Override
        public long size() {
            return getNameSize() + 16 + 4 * 4 + getMemberType().size();
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

        private int getDimensionality() {
            return getUnsignedByte(getNameSize() + 4);
        }

        public int getDimensionPermutation() {
            return getSmallUnsignedInt(getNameSize() + 8);
        }

        public long[] getDimensionSizes() {
            final long[] dimensionSizes = new long[getDimensionality()];
            for (int i = 0; i < Integer.min(4, dimensionSizes.length); i++) {
                dimensionSizes[i] = getUnsignedInt(getNameSize() + 16 + i * 4);
            }
            return dimensionSizes;
        }

        @Override
        public AbstractDatatypeBB getMemberType() {
            return getEmbedded(getNameSize() + 16 + 4 * 4, AbstractDatatypeBB.class);
        }

        @Override
        public String toString() {
            final int maxLen = 10;
            return String.format(
                    "MemberV1BB [%s, getNameSize()=%s, getName()=%s, getByteOffset()=%s, getDimensionality()=%s, "
                            + "getDimensionPermutation()=%s, getDimensionSizes()=%s]",
                    super.toString(), getNameSize(), getName(), getByteOffset(), getDimensionality(),
                    getDimensionPermutation(),
                    getDimensionSizes() != null
                            ? Arrays.toString(
                                    Arrays.copyOf(getDimensionSizes(), Math.min(getDimensionSizes().length, maxLen)))
                            : null);
        }
    }
}
