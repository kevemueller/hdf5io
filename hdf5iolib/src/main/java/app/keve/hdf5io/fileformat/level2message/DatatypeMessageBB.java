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
import app.keve.hdf5io.fileformat.level2datatype.AbstractDatatypeBB;

public final class DatatypeMessageBB extends AbstractBB<H5Context> implements DatatypeMessage {
    public DatatypeMessageBB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    @Override
    public long size() {
        return getDatatype().size();
    }

    @Override
    public AbstractDatatypeBB getDatatype() {
        return getEmbedded(0, AbstractDatatypeBB.class);
    }

    @Override
    public void setDatatype(final AbstractDatatypeBB value) {
        setEmbedded(0, value);
    }

    @Override
    public void initialize() {
    }
}
