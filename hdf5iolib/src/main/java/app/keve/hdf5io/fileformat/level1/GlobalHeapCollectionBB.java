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
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.SizingContext;

public final class GlobalHeapCollectionBB extends AbstractSizedBB<SizingContext> implements GlobalHeapCollection {
    public GlobalHeapCollectionBB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long minSize(final SizingContext sc) {
        return 8 + sc.lengthSize();
    }

    public static long maxSize(final SizingContext sc) {
        return 8 == sc.lengthSize() ? Long.MAX_VALUE : 8 + sc.lengthSize() + MAX_LENGTH[sc.lengthSize()];
    }

    @Override
    public long size() {
        return getCollectionSize();
    }

    @Override
    public boolean isValid() {
        return Arrays.equals(SIGNATURE, getSignature()) && 0 == getByte(5) && 0 == getShort(6);
    }

    @Override
    public byte[] getSignature() {
        return new byte[] {getByte(0), getByte(1), getByte(2), getByte(3)};
    }

    @Override
    public int getVersion() {
        return getUnsignedByte(4);
    }

    @Override
    public long getCollectionSize() {
        return getLength(8);
    }

    @Override
    public GlobalHeapObject getHeapObject(final int index) {
        final Iterator<GlobalHeapObject> it = objectIterator();
        while (it.hasNext()) {
            final GlobalHeapObject gho = it.next();
            if (index == gho.getHeapObjectIndex()) {
                return gho;
            }
        }
        throw new NoSuchElementException();
    }

    @Override
    public Iterator<GlobalHeapObject> objectIterator() {
        return getIteratorByteSized(8 + context.lengthSize(), GlobalHeapObject.class,
                getCollectionSize() - (8 + context.lengthSize()), 8 + context.lengthSize());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("GlobalHeapCollectionBB{");
        sb.append("valid=").append(isValid());
        sb.append(", signature=");
        if (getSignature() == null) {
            sb.append("null");
        } else {
            sb.append('[');
            for (int i = 0; i < getSignature().length; ++i) {
                sb.append(i == 0 ? "" : ", ").append(getSignature()[i]);
            }
            sb.append(']');
        }
        sb.append(", version=").append(getVersion());
        sb.append(", collectionSize=").append(getCollectionSize());
        sb.append(", objectIterator=").append(objectIterator());
        sb.append('}');
        return sb.toString();
    }

}
