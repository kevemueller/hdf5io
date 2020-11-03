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
package app.keve.hdf5io.fileformat.level0;

import java.nio.ByteBuffer;

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.SizingContext;

public abstract class AbstractSuperblockBB extends AbstractSizedBB<SizingContext> implements Superblock {
    protected AbstractSuperblockBB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    @Override
    public final byte[] getFormatSignature() {
        return getBytes8(0);
    }

    @Override
    public final void setFormatSignature() {
        setBytes(0, SIGNATURE);
    }

    @Override
    public final int getVersionNumber() {
        return getByte(8);
    }

    @Override
    public final void setVersionNumber(final int value) {
        setByte(8, value);
    }
}
