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

public class FillValueMessageV1BB extends AbstractBB<H5Context> implements FillValueMessageV1 {
    public static final long MIN_SIZE = 4;
    public static final long MAX_SIZE = MAX_MESSAGE_DATA;

    public FillValueMessageV1BB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    @Override
    public long size() {
        return 4 + 4 + getFillValueSize().getAsInt();
    }

    @Override
    public int getVersion() {
        return getUnsignedByte(0);
    }

    @Override
    public SpaceAllocation getSpaceAllocationTime() {
        switch (getUnsignedByte(1)) {
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
    public WriteTime getFillValueWriteTime() {
        switch (getUnsignedByte(2)) {
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
    public boolean isFillValueDefined() {
        return 1 == getByte(3);
    }

    @Override
    public OptionalInt getFillValueSize() {
        return OptionalInt.of(getInt(4));
    }

    @Override
    public ByteBuffer getFillValue() {
        return getEmbeddedData(8, getFillValueSize().getAsInt());
    }

    @Override
    public void setVersion(final int value) {
        setByte(0, value);
    }

    @Override
    public void setSpaceAllocationTime(final SpaceAllocation value) {
        setByte(1, value.ordinal() + 1);
    }

    @Override
    public void setFillValueWriteTime(final WriteTime value) {
        setByte(2, value.ordinal());
    }

    @Override
    public void setFillValueDefined(final boolean value) {
        setByte(3, value ? 1 : 0);
    }

    @Override
    public void setFillValueSize(final OptionalInt value) {
        setInt(4, value.orElse(0));
    }

    @Override
    public void setFillValue(final ByteBuffer buf) {
        if (null == buf) {
            setFillValueSize(OptionalInt.empty());
        } else {
            setFillValueSize(OptionalInt.of(buf.remaining()));
            setEmbeddedData(8, buf.remaining(), buf);
        }
    }

    @Override
    public void initialize() {
        setVersion(1);
        setFillValueWriteTime(WriteTime.ON_ALLOCATION);
        setSpaceAllocationTime(SpaceAllocation.EARLY);
        setFillValueSize(OptionalInt.empty());
    }
}
