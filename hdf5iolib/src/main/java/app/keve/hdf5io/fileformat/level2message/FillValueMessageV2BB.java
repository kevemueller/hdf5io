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

import app.keve.hdf5io.fileformat.H5Context;

public final class FillValueMessageV2BB extends FillValueMessageV1BB implements FillValueMessageV2 {

    public FillValueMessageV2BB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    @Override
    public long size() {
        return 4 + (isFillValueDefined() ? 4 + getFillValueSize().getAsInt() : 0);
    }

    @Override
    public OptionalInt getFillValueSize() {
        return isFillValueDefined() ? OptionalInt.of(getInt(4)) : OptionalInt.empty();
    }

    @Override
    public void setFillValueSize(final OptionalInt value) {
        if (value.isEmpty()) {
            setFillValueDefined(false);
        } else {
            setFillValueDefined(true);
            setInt(4, value.getAsInt());
        }
    }

    @Override
    public ByteBuffer getFillValue() {
        return isFillValueDefined() ? getEmbeddedData(8, getFillValueSize().getAsInt()) : null;
    }

    @Override
    public void initialize() {
        setVersion(2);
        setFillValueSize(OptionalInt.empty());
    }
}
