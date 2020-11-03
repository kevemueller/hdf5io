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

import app.keve.hdf5io.fileformat.SizingContext;

public abstract class AbstractDataLayoutMessageV3BB extends AbstractDataLayoutMessageBB {
    protected AbstractDataLayoutMessageV3BB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    @Override
    public final boolean isValid() {
        return true;
    }

    @Override
    public Layout getLayoutClass() {
        switch (getByte(1)) {
        case 0:
            return Layout.COMPACT;
        case 1:
            return Layout.CONTIGUOUS;
        case 2:
            return Layout.CHUNKED;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public final void setLayoutClass(final Layout value) {
        setByte(1, value.ordinal());
    }
}
