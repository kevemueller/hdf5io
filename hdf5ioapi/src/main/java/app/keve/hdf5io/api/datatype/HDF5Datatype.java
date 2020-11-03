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
package app.keve.hdf5io.api.datatype;

import java.lang.reflect.Type;
import java.nio.ByteOrder;

import app.keve.hdf5io.api.HDF5DatatypeAdapter;
import app.keve.hdf5io.api.datatype.HDF5Array.ArrayBuilder;
import app.keve.hdf5io.api.datatype.HDF5Bitfield.BitfieldBuilder;
import app.keve.hdf5io.api.datatype.HDF5Compound.CompoundBuilder;
import app.keve.hdf5io.api.datatype.HDF5Enumeration.EnumerationBuilder;
import app.keve.hdf5io.api.datatype.HDF5FixedPointNumber.FixedPointNumberBuilder;
import app.keve.hdf5io.api.datatype.HDF5FloatingPointNumber.FloatingPointNumberBuilder;
import app.keve.hdf5io.api.datatype.HDF5Opaque.OpaqueBuilder;

/**
 * Common ancestor of all HDF5 datatypes.
 * 
 * @author keve
 *
 */
public interface HDF5Datatype {
    TypeClass getDatatypeClass();

    int getElementSize();

    HDF5DatatypeAdapter adapter();

    /**
     * Common builder for datatypes.
     * 
     * @author keve
     *
     * @param <T> the datatype to be built {@link HDF5Datatype}
     * @param <B> the builder type
     */
    interface Builder<T extends HDF5Datatype, B extends Builder<T, B>> {
        B withElementSize(int elementSize);

        /**
         * "Copy constructor" using provided template instance.
         * 
         * @param template the instance to copy all attributes from.
         * 
         * @return self
         */
        B from(T template);

        T build();

    }

    /**
     * Builder for datatype builders.
     * 
     * @author keve
     *
     */
    interface DatatypeBuilder {
        Builder<? extends HDF5Datatype, ?> forType(Type javaType);

        FixedPointNumberBuilder forFixedPointNumber();

        FloatingPointNumberBuilder forFloatingPointNumber();

        BitfieldBuilder forBitfield();

        OpaqueBuilder forOpaque();

        StringBuilder forString();

        ArrayBuilder forArray();

        EnumerationBuilder forEnumeration();

        CompoundBuilder forCompound();
    }

    /**
     * HDF5 datatype classes.
     * 
     * @author keve
     *
     */
    enum TypeClass {
        FIXED_POINT, FLOATING_POINT, TIME, STRING, BIT_FIELD, OPAQUE, COMPOUND, REFERENCE, ENUMERATED, VARIABLE_LENGTH,
        ARRAY;

        public int value() {
            return ordinal();
        }

        public static TypeClass of(final int value) {
            return values()[value];
        }
    }

    /**
     * HDF5 byte orders.
     * 
     * @author keve
     *
     */
    enum HDF5ByteOrder {
        LITTLE_ENDIAN(ByteOrder.LITTLE_ENDIAN), BIG_ENDIAN(ByteOrder.BIG_ENDIAN), VAX_ENDIAN(null);

        private final ByteOrder byteOrder;

        HDF5ByteOrder(final ByteOrder byteOrder) {
            this.byteOrder = byteOrder;
        }

        public ByteOrder getByteOrder() {
            return byteOrder;
        }
    }

    /**
     * HDF5 variable length padding types.
     * 
     * @author keve
     *
     */
    enum PaddingType {
        NULL_TERMINATE, NULL_PAD, SPACE_PAD
    }

}
