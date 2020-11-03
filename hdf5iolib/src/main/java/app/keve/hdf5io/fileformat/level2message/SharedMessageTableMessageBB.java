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

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextSOHMT;
import app.keve.hdf5io.fileformat.level1.SharedObjectHeaderMessageTable;

public final class SharedMessageTableMessageBB extends AbstractSizedBB<SizingContext>
        implements SharedMessageTableMessage {
    public SharedMessageTableMessageBB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long size(final SizingContext sc) {
        return 2 + sc.offsetSize();
    }

    @Override
    public long size() {
        return 2 + context.offsetSize();
    }

    @Override
    public int getVersion() {
        return getByte(0);
    }

    @Override
    public Resolvable<SharedObjectHeaderMessageTable> getSOHMTableAddress() {
        final SizingContextSOHMT context2 = SizingContextSOHMT.of(context, getNumberOfIndices());
        return getResolvable(1, SharedObjectHeaderMessageTable.class, context2);
    }

    @Override
    public int getNumberOfIndices() {
        return getUnsignedByte(1 + context.offsetSize());
    }

}
