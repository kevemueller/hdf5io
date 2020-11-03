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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.Stream;

import app.keve.hdf5io.api.HDF5DatatypeAdapter;
import app.keve.hdf5io.api.datatype.HDF5Array;
import app.keve.hdf5io.api.datatype.HDF5Array.ArrayBuilder;
import app.keve.hdf5io.api.datatype.HDF5Bitfield.BitfieldBuilder;
import app.keve.hdf5io.api.datatype.HDF5Compound.CompoundBuilder;
import app.keve.hdf5io.api.datatype.HDF5Datatype;
import app.keve.hdf5io.api.datatype.HDF5Enumeration.EnumerationBuilder;
import app.keve.hdf5io.api.datatype.HDF5FixedPointNumber;
import app.keve.hdf5io.api.datatype.HDF5FixedPointNumber.FixedPointNumberBuilder;
import app.keve.hdf5io.api.datatype.HDF5FloatingPointNumber;
import app.keve.hdf5io.api.datatype.HDF5FloatingPointNumber.FloatingPointNumberBuilder;
import app.keve.hdf5io.api.datatype.HDF5Opaque.OpaqueBuilder;
import app.keve.hdf5io.fileformat.AbstractBB;
import app.keve.hdf5io.fileformat.H5Context;
import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.level2datatype.AbstractEnumerationBB.AbstractBuilderBB;

public abstract class AbstractDatatypeBB extends AbstractBB<H5Context> implements HDF5Datatype, H5Object<H5Context> {
    public static final long MIN_SIZE = 8;
    public static final long MAX_SIZE = UINT32_MAX_VALUE;

    protected AbstractDatatypeBB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    public final int getVersion() {
        return getUnsignedByte(0) >>> 4;
    }

    protected final void setVersion(final int version) {
        assert version >= 0 && version <= 0xF;
        setByte(0, getUnsignedByte(0) & 0x0F | version << 4);
    }

    @Override
    public final TypeClass getDatatypeClass() {
        return TypeClass.of(getUnsignedByte(0) & 0x0F);
    }

    protected final void setDatatypeClass(final TypeClass datatypeClass) {
        setByte(0, getUnsignedByte(0) & 0xF0 | datatypeClass.value());
    }

    public final int getClassBitField() {
        return getUnsignedByte(1) | getUnsignedByte(2) << 8 | getUnsignedByte(3) << 16;
    }

    protected final void setClassBitField(final int classBitField) {
        assert classBitField >= 0 && classBitField <= 0x00FFFFFF;
        setByte(1, classBitField);
        setByte(2, classBitField >>> 8);
        setByte(3, classBitField >>> 16);
    }

    @Override
    public final int getElementSize() {
        return getSmallUnsignedInt(4);
    }

    protected final void setElementSize(final long elementSize) {
        setUnsignedInt(4, elementSize);
    }

    @Override
    public final HDF5DatatypeAdapter adapter() {
        return context.h5Factory().datatypeAdapter(this, (SizingContext) context);
    }

    @Override
    public final boolean equals(final Object obj) {
        if (null == obj) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass().equals(obj.getClass())) {
            final AbstractDatatypeBB that = (AbstractDatatypeBB) obj;
            return this.size() == that.size()
                    && Arrays.equals(this.getBytes(0, (int) size()), that.getBytes(0, (int) that.size()));
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Arrays.hashCode(getBytes(0, (int) size()));
    }

    @Override
    public String toString() {
        return String.format(
                "AbstractDatatypeBB [getDatatypeClass()=%s, getVersion()=%s, getClassBitField()=%s, getElementSize()=%s]",
                getDatatypeClass(), getVersion(), getClassBitField(), getElementSize());
    }

    public static final class DatatypeBuilderBB implements DatatypeBuilder {
        private final H5Context context;

        public DatatypeBuilderBB(final H5Context context) {
            this.context = context;
        }

        @Override
        public FixedPointNumberBuilder forFixedPointNumber() {
            return new FixedPointNumberBB.BuilderBB(context);
        }

        @Override
        public FloatingPointNumberBuilder forFloatingPointNumber() {
            return new FloatingPointNumberBB.BuilderBB(context);
        }

        @Override
        public BitfieldBuilder forBitfield() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public OpaqueBuilder forOpaque() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public StringBuilder forString() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ArrayBuilder forArray() {
            return new ArrayDatatypeV3BB.BuilderBB(context);
        }

        @Override
        public EnumerationBuilder forEnumeration() {
            return new EnumerationV1BB.BuilderBB(context);
        }

        @Override
        public CompoundBuilder forCompound() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Builder<?, ?> forType(final Type javaType) {
            if (javaType.equals(Byte.TYPE) || javaType.equals(Byte.class)) {
                final FixedPointNumberBB.BuilderBB builder = new FixedPointNumberBB.BuilderBB(context);
                return builder.withElementSize(1).signed().withByteOrder(HDF5ByteOrder.BIG_ENDIAN);
            } else if (javaType.equals(Short.TYPE) || javaType.equals(Short.class)) {
                final FixedPointNumberBB.BuilderBB builder = new FixedPointNumberBB.BuilderBB(context);
                return builder.withElementSize(2).signed().withByteOrder(HDF5ByteOrder.BIG_ENDIAN);
            } else if (javaType.equals(Integer.TYPE) || javaType.equals(Integer.class)) {
                final FixedPointNumberBB.BuilderBB builder = new FixedPointNumberBB.BuilderBB(context);
                return builder.withElementSize(4).signed().withByteOrder(HDF5ByteOrder.BIG_ENDIAN);
            } else if (javaType.equals(Long.TYPE) || javaType.equals(Long.class)) {
                final FixedPointNumberBB.BuilderBB builder = new FixedPointNumberBB.BuilderBB(context);
                return builder.withElementSize(8).signed().withByteOrder(HDF5ByteOrder.BIG_ENDIAN);
            } else if (javaType.equals(Float.TYPE) || javaType.equals(Float.class)) {
                final FloatingPointNumberBB.BuilderBB builder = new FloatingPointNumberBB.BuilderBB(context);
                return builder.withElementSize(4).withByteOrder(HDF5ByteOrder.BIG_ENDIAN).withMantissaSize(23)
                        .withExponentSize(8);
            } else if (javaType.equals(Double.TYPE) || javaType.equals(Double.class)) {
                final FloatingPointNumberBB.BuilderBB builder = new FloatingPointNumberBB.BuilderBB(context);
                return builder.withElementSize(8).withByteOrder(HDF5ByteOrder.BIG_ENDIAN).withMantissaSize(52)
                        .withExponentSize(11);

            } else if (javaType instanceof Class<?>) {
                final Class<?> javaClass = (Class<?>) javaType;
                if (javaClass.isEnum()) {
                    final Class<Enum<?>> enumClass = (Class<Enum<?>>) javaClass;
                    try {
                        final Method valuesMethod = enumClass.getMethod("values");
                        final Enum<?>[] values = (Enum<?>[]) valuesMethod.invoke(null);
                        final String[] names = Stream.of(values).map(Enum::name).toArray(String[]::new);
                        final Object[] ordinals = Stream.of(values).map(Enum::ordinal).toArray();

                        final int bitPrecision = 64 - Long.numberOfLeadingZeros(names.length - 1);
                        final AbstractBuilderBB builder = new EnumerationV1BB.BuilderBB(context);
                        final HDF5FixedPointNumber baseType = new FixedPointNumberBB.BuilderBB(context).unsigned()
                                .withByteOrder(HDF5ByteOrder.BIG_ENDIAN).withBitPrecision(bitPrecision).build();
                        builder.withBaseType(baseType);
                        builder.withNames(names);
                        builder.withValues(ordinals);
                        return builder;
                    } catch (NoSuchMethodException | SecurityException | IllegalAccessException
                            | IllegalArgumentException | InvocationTargetException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            }

            return null;
        }
    }

    public static AbstractDatatypeBB of(final HDF5Datatype datatype, final H5Context context) {
        if (datatype instanceof AbstractDatatypeBB) {
            return (AbstractDatatypeBB) datatype;
        }
        switch (datatype.getDatatypeClass()) {
        case FIXED_POINT:
            return (AbstractDatatypeBB) new FixedPointNumberBB.BuilderBB(context).from((HDF5FixedPointNumber) datatype)
                    .build();
        case FLOATING_POINT:
            return (AbstractDatatypeBB) new FloatingPointNumberBB.BuilderBB(context)
                    .from((HDF5FloatingPointNumber) datatype).build();
        case ARRAY:
            return (AbstractDatatypeBB) new ArrayDatatypeV3BB.BuilderBB(context).from((HDF5Array) datatype).build();
        default:
            throw new IllegalArgumentException("Implement copy contstructors!");
        }
    }

    public static AbstractDatatypeBB of(final ByteBuffer buf, final H5Context context) {
        final int versionClass = Byte.toUnsignedInt(buf.get(0));
        final int version = versionClass >>> 4;
        final int datatypeClass = versionClass & 0x0F;
        switch (datatypeClass) {
        case 0:
            return new FixedPointNumberBB(buf, context);
        case 1:
            return new FloatingPointNumberBB(buf, context);
        case 2:
            return new TimeDatatypeBB(buf, context);
        case 3:
            return new StringDatatypeBB(buf, context);
        case 4:
            return new BitFieldBB(buf, context);
        case 5:
            return new OpaqueDatatypeBB(buf, context);
        case 6:
            switch (version) {
            case 1:
                return new CompoundV1BB(buf, context);
            case 2:
                return new CompoundV2BB(buf, context);
            case 3:
                return new CompoundV3BB(buf, context);
            default:
                throw new IllegalArgumentException("Implement compound version: " + version);
            }
        case 7:
            switch (version) {
            case 1:
                return new ReferenceV1BB(buf, context);
            case 2:
                return new ReferenceV2BB(buf, context);
            case 3:
                return new ReferenceV3BB(buf, context);
            case 4:
                return new ReferenceV4BB(buf, context);
            default:
                throw new IllegalArgumentException("Implement reference version: " + version);
            }
        case 8:
            switch (version) {
            case 0: // FIXME not in spec!
            case 1:
                return new EnumerationV1BB(buf, context);
            case 2:
                return new EnumerationV2BB(buf, context);
            case 3:
                return new EnumerationV3BB(buf, context);
            default:
                throw new IllegalArgumentException("Implement Enumeration version: " + version);
            }
        case 9:
            return new VariableLengthBB(buf, context);
        case 10:
            switch (version) {
            case 1: // FIXME: not in spec!
                return new ArrayDatatypeV1BB(buf, context);
            case 2:
                return new ArrayDatatypeV2BB(buf, context);
            case 3:
                return new ArrayDatatypeV3BB(buf, context);
            default:
                throw new IllegalArgumentException("Implement Array version: " + version);
            }
        default:
            throw new IllegalArgumentException("Implement datatype " + datatypeClass + " version " + version);
        }
    }
}
