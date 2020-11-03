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

import app.keve.hdf5io.fileformat.H5Context;

public class ArrayDatatypeV1BB extends AbstractArrayBB {
    public ArrayDatatypeV1BB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    @Override
    public final long size() {
        return 8 + 4 + 2 * getDimensionality() * 4 + getBaseType().size();
    }

    public final long[] getPermutationIndices() {
        final long[] permutationIndices = new long[getDimensionality()];
        for (int i = 0; i < permutationIndices.length; i++) {
            permutationIndices[i] = getUnsignedInt(8 + 4 + getDimensionality() * 4 + i * 4);
        }
        return permutationIndices;
    }

    @Override
    public final AbstractDatatypeBB getBaseType() {
        return getEmbedded(8 + 4 + 2 * getDimensionality() * 4, AbstractDatatypeBB.class);
    }

    @Override
    public final void setBaseType(final AbstractDatatypeBB value) {
        setEmbedded(8 + 4 + 2 * getDimensionality() * 4, value);
    }

    @Override
    public final long[] getDimensionSizes() {
        final long[] dimensionSizes = new long[getDimensionality()];
        for (int i = 0; i < dimensionSizes.length; i++) {
            dimensionSizes[i] = getUnsignedInt(8 + 4 + i * 4);
        }
        return dimensionSizes;
    }

    @Override
    public final void setDimensionSizes(final long... value) {
        setByte(8, value.length);
        for (int i = 0; i < value.length; i++) {
            setUnsignedInt(8 + 4 + i * 4, value[i]);
        }
    }

    public static final class BuilderBB extends AbstractBuilderBB {
        public BuilderBB(final H5Context context) {
            super(context, ArrayDatatypeV1BB::new);
        }
    }
}
