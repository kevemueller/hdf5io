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
package app.keve.hdf5io.fileformat.level2message;

import java.nio.ByteBuffer;

import app.keve.hdf5io.fileformat.H5MessageType;
import app.keve.hdf5io.fileformat.SizingContext;

/**
 * IV.A.2.b. The Dataspace Message
 * (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#DataspaceMessage)
 */
public interface DataspaceMessage extends H5Message<SizingContext> {
    int TYPE = 0x0001;

    int MAX_DIMENSION_PRESENT_MASK = 0b1;

    static long minSize(final SizingContext sc) {
        return Long.min(DataspaceMessageV1BB.minSize(sc), DataspaceMessageV2BB.minSize(sc));
    }

    static long maxSize(final SizingContext sc) {
        return Long.max(DataspaceMessageV1BB.maxSize(sc), DataspaceMessageV2BB.maxSize(sc));
    }

    @Override
    default H5MessageType getType() {
        return H5MessageType.DATASPACE;
    }

    boolean isValid();

    int getVersion();

    void setVersion(int value);

    int getFlags();

    void setFlags(int value);

    int getDimensionality();

    void setDimensionality(int value);

    long[] getDimensionSizes();

    void setDimensionSizes(long... value);

    long[] getDimensionMaxSizes();

    void setDimensionMaxSizes(long... value);

    default long getDimensionBytes(final long elementSize) {
        final long[] dim = getDimensionSizes();
        if (0 == dim.length) {
            return 0;
        }
        long l = elementSize;
        for (int i = 0; i < dim.length; i++) {
            l *= dim[i];
        }
        return l;
    }

    default boolean isMaxDimensionPresent() {
        return (getFlags() & MAX_DIMENSION_PRESENT_MASK) > 0;
    }

    default void setMaxDimensionPresent(final boolean value) {
        setFlags(getFlags() & ~MAX_DIMENSION_PRESENT_MASK | (value ? MAX_DIMENSION_PRESENT_MASK : 0));
    }

    static DataspaceMessage of(final ByteBuffer buf, final SizingContext sizingContext) {
        switch (buf.get(0)) {
        case 1:
            return new DataspaceMessageV1BB(buf, sizingContext);
        case 2:
            return new DataspaceMessageV2BB(buf, sizingContext);
        default:
            throw new IllegalArgumentException("Implement DataspaceMessage version " + buf.get(0));
        }
    }

}
