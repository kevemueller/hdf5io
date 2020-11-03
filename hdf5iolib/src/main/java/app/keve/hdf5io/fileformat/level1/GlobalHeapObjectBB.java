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
package app.keve.hdf5io.fileformat.level1;

import java.nio.ByteBuffer;

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.SizingContext;

public final class GlobalHeapObjectBB extends AbstractSizedBB<SizingContext> implements GlobalHeapObject {
    public GlobalHeapObjectBB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long minSize(final SizingContext sc) {
        return 8 + sc.lengthSize();
    }

    public static long maxSize(final SizingContext sc) {
        return 8 == sc.lengthSize() ? Long.MAX_VALUE : 8 + sc.lengthSize() + MAX_LENGTH[sc.lengthSize()];
    }

    @Override
    public boolean isValid() {
        return 0 == getInt(4);
    }

    @Override
    public int getHeapObjectIndex() {
        return getUnsignedShort(0);
    }

    @Override
    public int getReferenceCount() {
        return getUnsignedShort(2);
    }

    @Override
    public long getObjectSize() {
        return getLength(8) - (0 == getHeapObjectIndex() ? 16 : 0);
    }

    @Override
    public long size() {
        int size = 8 + context().lengthSize() + (int) getObjectSize();
        size = size + 7 & ~7;
        return size;
    }

    @Override
    public ByteBuffer getObjectData() {
        return getEmbeddedData(8 + context.lengthSize(), (int) getObjectSize());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("GlobalHeapObjectBB{");
        sb.append("super=").append(super.toString());
        sb.append(", valid=").append(isValid());
        sb.append(", heapObjectIndex=").append(getHeapObjectIndex());
        sb.append(", referenceCount=").append(getReferenceCount());
        sb.append(", objectSize=").append(getObjectSize());
        sb.append(", size=").append(size());
        sb.append(", objectData=").append(getObjectData());
        sb.append('}');
        return sb.toString();
    }

}
