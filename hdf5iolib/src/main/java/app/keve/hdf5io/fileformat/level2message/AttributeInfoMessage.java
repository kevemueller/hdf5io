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
import java.util.OptionalLong;

import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.SizingContext;

public interface AttributeInfoMessage extends H5Object<SizingContext> {
    int FLAG_CREATION_ORDER_TRACKED = 0b01;
    int FLAG_CREATION_ORDER_INDEXED = 0b10;

    int getVersion();

    int getFlags();

    OptionalInt getMaximumCreationIndex();

    long getFractalHeap();

    long getAttributeNameV2Btree();

    OptionalLong getAttributeCreationOrderV2Btree();

    default boolean isCreationOrderTracked() {
        return (getFlags() & FLAG_CREATION_ORDER_TRACKED) > 0;
    }

    default boolean isCreationOrderIndexed() {
        return (getFlags() & FLAG_CREATION_ORDER_INDEXED) > 0;
    }

    static AttributeInfoMessage of(final ByteBuffer buf, final SizingContext sizingContext) {
        switch (buf.get(0)) {
        case 0:
            return new AttributeInfoMessageBB(buf, sizingContext);
        default:
            throw new IllegalArgumentException("Implement version: " + buf.get(0));
        }
    }
}
