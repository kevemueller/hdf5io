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

import app.keve.hdf5io.api.datatype.HDF5Array;
import app.keve.hdf5io.fileformat.H5Context;

public final class ArrayDatatypeV3BB extends AbstractArrayBB implements HDF5Array {
    public ArrayDatatypeV3BB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    @Override
    public long size() {
        return 8 + 1 + getDimensionality() * 4 + getBaseType().size();
    }

    @Override
    public AbstractDatatypeBB getBaseType() {
        return getEmbedded(8 + 1 + getDimensionality() * 4, AbstractDatatypeBB.class);
    }

    @Override
    public void setBaseType(final AbstractDatatypeBB baseType) {
        setEmbedded(8 + 1 + getDimensionality() * 4, baseType);
    }

    @Override
    public long[] getDimensionSizes() {
        final long[] dimensionSizes = new long[getDimensionality()];
        for (int i = 0; i < dimensionSizes.length; i++) {
            dimensionSizes[i] = getUnsignedInt(8 + 1 + i * 4);
        }
        return dimensionSizes;
    }

    @Override
    public void setDimensionSizes(final long... value) {
        setByte(8, value.length);
        for (int i = 0; i < value.length; i++) {
            setUnsignedInt(8 + 1 + i * 4, value[i]);
        }
    }

    @Override
    public String toString() {
        final int maxLen = 10;
        return String.format(
                "ArrayDatatypeV3BB [size()=%s, getBaseType()=%s, getDimensionality()=%s, getDimensionSizes()=%s, getVersion()=%s, "
                        + "getDatatypeClass()=%s, getClassBitField()=%s, getElementSize()=%s]",
                size(), getBaseType(), getDimensionality(),
                getDimensionSizes() != null
                        ? Arrays.toString(
                                Arrays.copyOf(getDimensionSizes(), Math.min(getDimensionSizes().length, maxLen)))
                        : null,
                getVersion(), getDatatypeClass(), getClassBitField(), getElementSize());
    }

    public static final class BuilderBB extends AbstractBuilderBB {
        public BuilderBB(final H5Context context) {
            super(context, ArrayDatatypeV3BB::new);
        }
    }
}
