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

import app.keve.hdf5io.fileformat.AbstractBB;
import app.keve.hdf5io.fileformat.H5Context;

public final class ObjectModificationTimeMessageV1BB extends AbstractBB<H5Context>
        implements ObjectModificationTimeMessageV1 {

    public ObjectModificationTimeMessageV1BB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    @Override
    public long size() {
        return 8;
    }

    @Override
    public boolean isValid() {
        return 0 == getByte(1) && 0 == getShort(2);
    }

    @Override
    public int getVersion() {
        return getUnsignedByte(0);
    }

    @Override
    public int getSeconds() {
        return getInt(4);
    }
}
