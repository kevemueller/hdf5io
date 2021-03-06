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

import app.keve.hdf5io.fileformat.H5Context;

public final class ReferenceV4BB extends ReferenceV2BB {
    protected static final int BF_REFERENCEVERSION_MASK = 0xF0;

    public ReferenceV4BB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    public int getReferenceVersion() {
        return (getClassBitField() & BF_REFERENCEVERSION_MASK) >>> 4;
    }

}
