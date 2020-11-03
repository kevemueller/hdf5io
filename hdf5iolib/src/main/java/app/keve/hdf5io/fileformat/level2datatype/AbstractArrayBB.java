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
import java.nio.ByteOrder;
import java.util.OptionalInt;
import java.util.function.BiFunction;

import app.keve.hdf5io.api.datatype.HDF5Array;
import app.keve.hdf5io.api.datatype.HDF5Datatype;
import app.keve.hdf5io.fileformat.H5Context;

public abstract class AbstractArrayBB extends AbstractDatatypeBB implements HDF5Array {
    protected AbstractArrayBB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    protected final int getDimensionality() {
        return getUnsignedByte(8);
    }

    public abstract void setBaseType(AbstractDatatypeBB baseType);

    public abstract void setDimensionSizes(long... value);

    @SuppressWarnings("checkstyle:hiddenfield")
    protected abstract static class AbstractBuilderBB implements ArrayBuilder {
        private final H5Context context;
        private final BiFunction<ByteBuffer, H5Context, ? extends AbstractArrayBB> of;

        private OptionalInt elementSize = OptionalInt.empty();
        private long[] dim;
        private HDF5Datatype baseType;

        public AbstractBuilderBB(final H5Context context,
                final BiFunction<ByteBuffer, H5Context, ? extends AbstractArrayBB> of) {
            this.context = context;
            this.of = of;
        }

        @Override
        public final HDF5Array build() {
            final AbstractDatatypeBB baseTypeBB = AbstractDatatypeBB.of(baseType, context);
            final ByteBuffer buf = ByteBuffer.allocate(8 + 1 + dim.length * 4 + (int) baseTypeBB.size())
                    .order(ByteOrder.LITTLE_ENDIAN);
            final AbstractArrayBB array = of.apply(buf, context);

            array.setDatatypeClass(TypeClass.ARRAY);
            array.setVersion(3);
            array.setClassBitField(0);
            array.setElementSize(elementSize.orElseGet(() -> {
                int size = baseType.getElementSize();
                for (final long d : dim) {
                    size *= d;
                }
                return size;
            }));
            array.setDimensionSizes(dim);
            array.setBaseType(baseTypeBB);

            return array;
        }

        @Override
        public final ArrayBuilder withElementSize(final int elementSize) {
            this.elementSize = OptionalInt.of(elementSize);
            return this;
        }

        @Override
        public final ArrayBuilder withDimensionSizes(final long... dim) {
            this.dim = dim;
            return this;
        }

        @Override
        public final ArrayBuilder withBaseType(final HDF5Datatype baseType) {
            this.baseType = baseType;
            return this;
        }

    }
}
