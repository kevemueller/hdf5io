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
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import app.keve.hdf5io.api.datatype.HDF5Compound;
import app.keve.hdf5io.fileformat.AbstractBB;
import app.keve.hdf5io.fileformat.H5Context;
import app.keve.hdf5io.fileformat.H5Object;

public abstract class AbstractCompoundBB extends AbstractDatatypeBB implements HDF5Compound {
    protected static final int BF_MEMBERNUM_MASK = 0x00FFFF;

    protected AbstractCompoundBB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    @Override
    public abstract Iterator<? extends AbstractH5Member> memberIterator();

    @Override
    public final long size() {
        final AtomicLong size = new AtomicLong(8);
        memberIterator().forEachRemaining(m -> size.addAndGet(m.size()));
        return size.get();
    }

    @Override
    public final int getNumberOfMembers() {
        return getClassBitField() & BF_MEMBERNUM_MASK;
    }

    public abstract static class AbstractH5Member extends AbstractBB<H5Context> implements H5Object<H5Context>, Member {
        protected AbstractH5Member(final ByteBuffer buf, final H5Context context) {
            super(buf, context);
        }

        @Override
        public String toString() {
            return String.format("size()=%s, name=%s, byteOffset=%s, datatype=%s", size(), getName(), getByteOffset(),
                    getMemberType());
        }

    }

    @Override
    public final String toString() {
        return String.format(
                "AbstractCompoundBB [members()=%s, size()=%s, getNumberOfMembers()=%s, getVersion()=%s, getDatatypeClass()=%s, "
                        + "getClassBitField()=%s, getElementSize()=%s]",
                getMembers(), size(), getNumberOfMembers(), getVersion(), getDatatypeClass(), getClassBitField(),
                getElementSize());
    }

}
