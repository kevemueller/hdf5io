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
import app.keve.hdf5io.fileformat.level2.ObjectHeaderContinuationBlock;

public final class ObjectHeaderContinuationMessageBB extends AbstractSizedBB<SizingContext>
        implements ObjectHeaderContinuationMessage {

    public ObjectHeaderContinuationMessageBB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long size(final SizingContext sc) {
        return sc.offsetSize() + sc.lengthSize();
    }

    @Override
    public long size() {
        return context.offsetSize() + context.lengthSize();
    }

    @Override
    public Resolvable<ObjectHeaderContinuationBlock<?>> getContinuationBlock() {
        return getResolvable(0, (int) getLength(), ObjectHeaderContinuationBlock.class, context);
    }

    private long getLength() {
        return getLength(context.offsetSize());
    }

}
