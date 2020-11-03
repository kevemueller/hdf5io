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
import java.util.OptionalInt;

import app.keve.hdf5io.fileformat.AbstractBB;
import app.keve.hdf5io.fileformat.H5Context;

public final class FillValueMessageV3BB extends AbstractBB<H5Context> implements FillValueMessageV3 {
    public static final long MIN_SIZE = 2;
    public static final long MAX_SIZE = MAX_MESSAGE_DATA;

    public FillValueMessageV3BB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    @Override
    public long size() {
        return 2 + (isFillValueDefined() ? 4 + getFillValueSize().getAsInt() : 0);
    }

    @Override
    public int getVersion() {
        return getUnsignedByte(0);
    }

    @Override
    public void setVersion(final int value) {
        setByte(0, value);
    }

    @Override
    public int getFlags() {
        return getUnsignedByte(1);
    }

    @Override
    public void setFlags(final int value) {
        setByte(1, value);
    }

    @Override
    public SpaceAllocation getSpaceAllocationTime() {
        switch (getFlags() & SPACE_ALLOCATION_TIME_MASK) {
        case 1:
            return SpaceAllocation.EARLY;
        case 2:
            return SpaceAllocation.LATE;
        case 3:
            return SpaceAllocation.INCREMENTAL;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void setSpaceAllocationTime(final SpaceAllocation value) {
        setFlags(getFlags() & ~SPACE_ALLOCATION_TIME_MASK | value.ordinal() + 1);
    }

    @Override
    public WriteTime getFillValueWriteTime() {
        switch ((getFlags() & FILL_VALUE_WRITE_TIME_MASK) >>> 2) {
        case 0:
            return WriteTime.ON_ALLOCATION;
        case 1:
            return WriteTime.NEVER;
        case 2:
            return WriteTime.IF_SET;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void setFillValueWriteTime(final WriteTime value) {
        setFlags(getFlags() & ~FILL_VALUE_WRITE_TIME_MASK | value.ordinal() << 2);
    }

    @Override
    public boolean isFillValueDefined() {
        return (getFlags() & FILL_VALUE_DEFINED_MASK) > 0;
    }

    @Override
    public void setFillValueDefined(final boolean value) {
        setFlags(getFlags() & ~FILL_VALUE_DEFINED_MASK | (value ? FILL_VALUE_DEFINED_MASK : 0));
    }

    @Override
    public boolean isFillValueUndefined() {
        return (getFlags() & FILL_VALUE_UNDEFINED_MASK) > 0;
    }

    @Override
    public void setFillValueUndefined(final boolean value) {
        setFlags(getFlags() & ~FILL_VALUE_UNDEFINED_MASK | (value ? FILL_VALUE_UNDEFINED_MASK : 0));
    }

    @Override
    public OptionalInt getFillValueSize() {
        return isFillValueDefined() ? OptionalInt.of(getInt(2)) : OptionalInt.empty();
    }

    @Override
    public void setFillValueSize(final OptionalInt value) {
        if (value.isEmpty()) {
            setFillValueDefined(false);
        } else {
            setFillValueDefined(true);
            setInt(2, value.getAsInt());
        }
    }

    @Override
    public ByteBuffer getFillValue() {
        return isFillValueDefined() ? getEmbeddedData(6, getFillValueSize().getAsInt()) : null;
    }

    @Override
    public void setFillValue(final ByteBuffer value) {
        if (null == value) {
            setFillValueSize(OptionalInt.empty());
        } else {
            setFillValueSize(OptionalInt.of(value.remaining()));
            setEmbeddedData(6, value.remaining(), value);
        }
    }

    @Override
    public void initialize() {
        setVersion(3);
        setFlags(0);
        setFillValueSize(OptionalInt.empty());
    }
}
