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
package app.keve.hdf5io.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import app.keve.hdf5io.api.HDF5;
import app.keve.hdf5io.api.HDF5File;
import app.keve.hdf5io.api.datatype.HDF5Array;
import app.keve.hdf5io.api.datatype.HDF5Datatype;
import app.keve.hdf5io.api.datatype.HDF5Datatype.DatatypeBuilder;
import app.keve.hdf5io.api.datatype.HDF5Datatype.HDF5ByteOrder;
import app.keve.hdf5io.api.datatype.HDF5Datatype.TypeClass;
import app.keve.hdf5io.api.datatype.HDF5Enumeration;
import app.keve.hdf5io.api.datatype.HDF5FixedPointNumber;
import app.keve.hdf5io.api.datatype.HDF5FixedPointNumber.FixedPointNumberBuilder;
import app.keve.hdf5io.api.datatype.HDF5FloatingPointNumber;
import app.keve.hdf5io.api.datatype.HDF5FloatingPointNumber.FloatingPointNumberBuilder;

public final class TestDatatype {
    private static final HDF5 HDF5;

    static {
        final ServiceLoader<HDF5> sl = ServiceLoader.load(HDF5.class);
        HDF5 = sl.findFirst().get();
    }

    private final HDF5File hdf5File = HDF5.create();

    @ParameterizedTest
    @MethodSource("fixedPoint")
    public void testFixedPoint(final Type javaType, final int bitPrecision, final boolean signed, final HDF5ByteOrder order)
            throws IOException {
        final DatatypeBuilder datatypeBuilder = hdf5File.getDatatypeBuilder();

        // configure the datatype
        final FixedPointNumberBuilder builder = (FixedPointNumberBuilder) datatypeBuilder.forType(javaType);
        builder.withBitPrecision(bitPrecision).withByteOrder(order);
        if (signed) {
            builder.signed();
        } else {
            builder.unsigned();
        }
        final HDF5FixedPointNumber datatype = builder.build();

        // check the configuration
        assertEquals(TypeClass.FIXED_POINT, datatype.getDatatypeClass());
        assertEquals(order, datatype.getByteOrder());
        assertEquals(bitPrecision / 8, datatype.getElementSize());
        assertEquals(bitPrecision, datatype.getBitPrecision());
        assertEquals(0, datatype.getBitOffset());
        assertEquals(0, datatype.getLoPadBit());
        assertEquals(0, datatype.getHiPadBit());
        assertEquals(signed, datatype.isSigned());

        // build another to compare hashCode and equals.
        final FixedPointNumberBuilder secondDatatypeBuilder = datatypeBuilder.forFixedPointNumber().withByteOrder(order)
                .withBitPrecision(bitPrecision);
        if (signed) {
            secondDatatypeBuilder.signed();
        } else {
            secondDatatypeBuilder.unsigned();
        }
        final HDF5FixedPointNumber secondDatatype = secondDatatypeBuilder.build();
        assertEquals(datatype, secondDatatype);
        assertEquals(datatype.hashCode(), secondDatatype.hashCode());

    }

    @ParameterizedTest
    @MethodSource("floatingPoint")
    public void testFloatingPoint(final Type javaType, final int bitPrecision, final HDF5ByteOrder order) throws IOException {
        final DatatypeBuilder datatypeBuilder = hdf5File.getDatatypeBuilder();

        // configure the datatype
        final FloatingPointNumberBuilder builder = (FloatingPointNumberBuilder) datatypeBuilder.forType(javaType);
        builder.withBitPrecision(bitPrecision).withByteOrder(order);
        final HDF5FloatingPointNumber datatype = builder.build();

        // check the configuration
        assertEquals(TypeClass.FLOATING_POINT, datatype.getDatatypeClass());
        assertEquals(order, datatype.getByteOrder());
        assertEquals(bitPrecision / 8, datatype.getElementSize());
        assertEquals(bitPrecision, datatype.getBitPrecision());
        assertEquals(0, datatype.getBitOffset());
        assertEquals(0, datatype.getLoPadBit());
        assertEquals(0, datatype.getHiPadBit());
        assertEquals(0, datatype.getIntPadBit());
        assertEquals(2, datatype.getMantissaNormalization());

        final FloatingPointNumberBuilder secondDatatypeBuilder = datatypeBuilder.forFloatingPointNumber()
                .withByteOrder(order).withBitPrecision(bitPrecision).withMantissaNormalization(2);

        switch (bitPrecision) {
        case 32:
            assertEquals(31, datatype.getSignLocation());
            assertEquals(23, datatype.getExponentLocation());
            assertEquals(8, datatype.getExponentSize());
            assertEquals(0, datatype.getMantissaLocation());
            assertEquals(23, datatype.getMantissaSize());
            assertEquals(127, datatype.getExponentBias());
            secondDatatypeBuilder.withExponentSize(8).withMantissaSize(23);
            break;
        case 64:
            assertEquals(63, datatype.getSignLocation());
            assertEquals(52, datatype.getExponentLocation());
            assertEquals(11, datatype.getExponentSize());
            assertEquals(0, datatype.getMantissaLocation());
            assertEquals(52, datatype.getMantissaSize());
            assertEquals(1023, datatype.getExponentBias());
            secondDatatypeBuilder.withExponentSize(11).withMantissaSize(52);
            break;
        default:
            throw new IllegalArgumentException();
        }

        final HDF5FloatingPointNumber secondDatatype = secondDatatypeBuilder.build();

        assertEquals(datatype, secondDatatype);
        assertEquals(datatype.hashCode(), secondDatatype.hashCode());
    }

    @Test
    public void testArray() throws IOException {
        final DatatypeBuilder datatypeBuilder = hdf5File.getDatatypeBuilder();

        final HDF5Datatype baseType = datatypeBuilder.forType(Integer.TYPE).build();
        final HDF5Array arrayType = datatypeBuilder.forArray().withBaseType(baseType).withDimensionSizes(3, 4, 5)
                .build();
        System.err.println(arrayType);

        assertEquals(TypeClass.ARRAY, arrayType.getDatatypeClass());
        assertEquals(4 * 3 * 4 * 5, arrayType.getElementSize());
        assertEquals(baseType, arrayType.getBaseType());
        assertArrayEquals(new long[] {3, 4, 5}, arrayType.getDimensionSizes());

        final HDF5Array secondDatatype = datatypeBuilder.forArray()
                .withBaseType(datatypeBuilder.forType(Short.TYPE).build()).withDimensionSizes(3, 4, 5).build();
        assertNotEquals(arrayType, secondDatatype);

        final HDF5Array thirdDatatype = datatypeBuilder.forArray()
                .withBaseType(datatypeBuilder.forType(Integer.TYPE).build()).withDimensionSizes(5, 4, 3).build();
        assertNotEquals(arrayType, thirdDatatype);

        final HDF5Array fourthDatatype = datatypeBuilder.forArray()
                .withBaseType(datatypeBuilder.forType(Integer.TYPE).build()).withDimensionSizes(3, 4, 5).build();
        assertEquals(arrayType, fourthDatatype);
        assertEquals(arrayType.hashCode(), fourthDatatype.hashCode());
    }

    public enum TestEnum {
        MERCURY, VENUS, EARTH, MARS, JUPITER, SATURN, URANUS, NEPTUNE
    }

    @Test
    public void testEnum() throws IOException {
        final DatatypeBuilder datatypeBuilder = hdf5File.getDatatypeBuilder();
        final HDF5Enumeration enumType = (HDF5Enumeration) datatypeBuilder.forType(TestEnum.class).build();
        System.err.println(enumType);

        assertEquals(TypeClass.ENUMERATED, enumType.getDatatypeClass());
        assertEquals(1, enumType.getElementSize());
        final HDF5FixedPointNumber baseType = datatypeBuilder.forFixedPointNumber().withBitPrecision(3).unsigned()
                .build();
        assertEquals(baseType, enumType.getBaseType());
        assertArrayEquals(Stream.of(TestEnum.values()).map(TestEnum::name).toArray(), enumType.getNames());
        assertArrayEquals(Stream.of(TestEnum.values()).map(TestEnum::ordinal).toArray(), enumType.getValues());

        final ByteBuffer buf = ByteBuffer.allocate(8);
        for (int i = 0; i < 8; i++) {
            buf.put((byte) i);
        }
        buf.flip();

        final HDF5Enumeration enumType2 = datatypeBuilder.forEnumeration().withBaseType(baseType)
                .withNames(Stream.of(TestEnum.values()).map(TestEnum::name).toArray(String[]::new)).withValueBuf(buf)
                .build();
        assertEquals(enumType, enumType2);

        final HDF5Enumeration enumType3 = datatypeBuilder.forEnumeration()
                .withBaseType(datatypeBuilder.forType(Integer.TYPE).build())
                .withNames(Stream.of(TestEnum.values()).map(TestEnum::name).toArray(String[]::new)).withValueBuf(buf)
                .build();
        assertNotEquals(enumType, enumType3);

        final ByteBuffer intBBuf = ByteBuffer.allocate(8 * 4);
        final IntBuffer intBuf = intBBuf.asIntBuffer();
        for (int i = 0; i < 8; i++) {
            intBuf.put(i);
        }
        intBuf.flip();

        final HDF5Enumeration enumType4 = datatypeBuilder.forEnumeration()
                .withBaseType(datatypeBuilder.forType(Integer.TYPE).build())
                .withNames(Stream.of(TestEnum.values()).map(TestEnum::name).toArray(String[]::new))
                .withValueBuf(intBBuf).build();
        assertEquals(TypeClass.ENUMERATED, enumType4.getDatatypeClass());
        assertEquals(4, enumType4.getElementSize());
        final HDF5FixedPointNumber baseTypeI = datatypeBuilder.forFixedPointNumber().withBitPrecision(32).signed()
                .build();
        assertEquals(baseTypeI, enumType4.getBaseType());
        assertArrayEquals(Stream.of(TestEnum.values()).map(TestEnum::name).toArray(), enumType4.getNames());
        assertArrayEquals(Stream.of(TestEnum.values()).map(TestEnum::ordinal).toArray(), enumType4.getValues());

    }

    public static Stream<Arguments> fixedPoint() {
        final Map<?, ?> types = Map.of(Byte.TYPE, Byte.SIZE, Short.TYPE, Short.SIZE, Integer.TYPE, Integer.SIZE,
                Long.TYPE, Long.SIZE, Byte.class, Byte.SIZE, Short.class, Short.SIZE, Integer.class, Integer.SIZE,
                Long.class, Long.SIZE);
        final List<Boolean> sign = List.of(true, false);
        final List<HDF5ByteOrder> order = List.of(HDF5ByteOrder.BIG_ENDIAN, HDF5ByteOrder.LITTLE_ENDIAN);
        return types.entrySet().stream().flatMap(
                e -> sign.stream().flatMap(s -> order.stream().map(o -> Arguments.of(e.getKey(), e.getValue(), s, o))));
    }

    public static Stream<Arguments> floatingPoint() {
        final Map<?, ?> types = Map.of(Float.TYPE, Float.SIZE, Double.TYPE, Double.SIZE, Float.class, Float.SIZE,
                Double.class, Double.SIZE);
        final List<HDF5ByteOrder> order = List.of(HDF5ByteOrder.BIG_ENDIAN, HDF5ByteOrder.LITTLE_ENDIAN);
        return types.entrySet().stream()
                .flatMap(e -> order.stream().map(o -> Arguments.of(e.getKey(), e.getValue(), o)));
    }

}
