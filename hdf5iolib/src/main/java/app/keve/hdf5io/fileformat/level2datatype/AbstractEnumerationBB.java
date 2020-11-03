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
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import app.keve.hdf5io.api.HDF5DatatypeAdapter;
import app.keve.hdf5io.api.datatype.HDF5Datatype;
import app.keve.hdf5io.api.datatype.HDF5Enumeration;
import app.keve.hdf5io.fileformat.H5Context;
import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.SizingContext;

public abstract class AbstractEnumerationBB extends AbstractDatatypeBB implements H5Object<H5Context>, HDF5Enumeration {

    protected static final int BF_MEMBERNUM_MASK = 0x00FFFF;

    protected AbstractEnumerationBB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    public final int getNumberOfMembers() {
        return getClassBitField() & BF_MEMBERNUM_MASK;
    }

    @Override
    public final Object[] getValues() {
        final HDF5DatatypeAdapter datatypeAdapter = context.h5Factory().datatypeAdapter(getBaseType(),
                (SizingContext) context);
        return datatypeAdapter.asStream(getValueBuf()).toArray();
    }

    public abstract void setBaseType(AbstractDatatypeBB baseType);

    public abstract void setNames(String[] value);

    public abstract void setValueBuf(ByteBuffer value);

    static int encodeClassBits(final int numberOfMembers) {
        assert numberOfMembers >= 0;
        assert numberOfMembers < UINT16_MAX_VALUE;
        return numberOfMembers;
    }

    @SuppressWarnings("checkstyle:hiddenfield")
    protected abstract static class AbstractBuilderBB implements HDF5Enumeration.EnumerationBuilder {
        protected final H5Context context;
        protected final BiFunction<ByteBuffer, H5Context, ? extends AbstractEnumerationBB> of;

        protected OptionalInt elementSize = OptionalInt.empty();
        protected HDF5Datatype baseType;
        protected String[] names;
        protected ByteBuffer valueBuf;
        protected Object[] values;

        public AbstractBuilderBB(final H5Context context,
                final BiFunction<ByteBuffer, H5Context, ? extends AbstractEnumerationBB> of) {
            this.context = context;
            this.of = of;
        }

        @Override
        public final EnumerationBuilder withElementSize(final int elementSize) {
            this.elementSize = OptionalInt.of(elementSize);
            return this;
        }

        @Override
        public final EnumerationBuilder withBaseType(final HDF5Datatype baseType) {
            this.baseType = baseType;
            return this;
        }

        @Override
        public final EnumerationBuilder withNames(final String[] names) {
            this.names = names;
            return this;
        }

        @Override
        public final EnumerationBuilder withValueBuf(final ByteBuffer values) {
            this.valueBuf = values;
            return this;
        }

        @Override
        public final EnumerationBuilder withValues(final Object[] values) {
            this.values = values;
            return this;
        }

        @Override
        public final HDF5Enumeration build() {
            final AbstractDatatypeBB baseTypeBB = AbstractDatatypeBB.of(baseType, context);
            int namesLength = 0;
            for (final String name : names) {
                namesLength += name.length() + 1 + 7 & ~7;
            }
            final int numberOfMembers = names.length;
            final int eSize = elementSize.orElse(baseType.getElementSize());

            final ByteBuffer buf = ByteBuffer
                    .allocate(8 + (int) baseTypeBB.size() + namesLength + numberOfMembers * eSize)
                    .order(ByteOrder.LITTLE_ENDIAN);
            final AbstractEnumerationBB enumeration = of.apply(buf, context);
            enumeration.setDatatypeClass(TypeClass.ENUMERATED);
            enumeration.setVersion(1);
            enumeration.setClassBitField(encodeClassBits(numberOfMembers));
            enumeration.setElementSize(eSize);
            enumeration.setBaseType(baseTypeBB);
            enumeration.setNames(names);

            if (null != values && null == valueBuf) {
                final HDF5DatatypeAdapter adapter = baseTypeBB.adapter();
                valueBuf = adapter.allocate(values.length);
                valueBuf = adapter.fromStream(valueBuf, Stream.of(values));
                valueBuf.flip();
            }
            enumeration.setValueBuf(Objects.requireNonNull(valueBuf, "must provide values or valueBuf"));
            return enumeration;
        }

    }

}
