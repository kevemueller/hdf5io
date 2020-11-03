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

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;

public abstract class AbstractBTreeV1BB<T extends SizingContext> extends AbstractSizedBB<T> implements BTreeV1<T> {
    protected AbstractBTreeV1BB(final ByteBuffer buf, final T sizingContext) {
        super(buf, sizingContext);
    }

    @Override
    public final boolean isValid() {
        return Arrays.equals(SIGNATURE, getSignature());
    }

    @Override
    public final byte[] getSignature() {
        return getBytes(0, 4);
    }

    protected final void setSignature() {
        setBytes(0, SIGNATURE);
    }

    public final int getNodeTypeRaw() {
        return getByte(4);
    }

    @Override
    public final NodeType getNodeType() {
        switch (getNodeTypeRaw()) {
        case 0:
            return NodeType.GROUP;
        case 1:
            return NodeType.DATA;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public final void setNodeType(final NodeType value) {
        setByte(4, value.ordinal());
    }

    @Override
    public final int getNodeLevel() {
        return getByte(5);
    }

    @Override
    public final void setNodeLevel(final int value) {
        setByte(5, value);
    }

    @Override
    public final int getEntriesUsed() {
        return getShort(6);
    }

    @Override
    public final void setEntriesUsed(final int value) {
        setShort(6, value);
    }

    @Override
    public final void setLeftSibling(final Resolvable<? extends BTreeV1<T>> value) {
        setResolvable(8, value);
    }

    @Override
    public final void setRightSibling(final Resolvable<? extends BTreeV1<T>> value) {
        setResolvable(8 + context.offsetSize(), value);
    }

}
