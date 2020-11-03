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
import java.util.Iterator;

import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContextHeap;

public final class BTreeV1GroupLeafBB extends AbstractBTreeV1GroupBB implements BTreeV1GroupLeaf {

    public BTreeV1GroupLeafBB(final ByteBuffer buf, final SizingContextHeap sizingContext) {
        super(buf, sizingContext);
        assert NodeType.GROUP == getNodeType();
        assert 0 == getNodeLevel();
    }

    @Override
    public Resolvable<GroupSymbolTableNode> getChild(final int index) {
        final int pos = 8 + 2 * context().offsetSize() + index * (context().lengthSize() + context().offsetSize());
        return getResolvable(pos + context().lengthSize(), GroupSymbolTableNode.class, context);
    }

    @Override
    public void setChild(final int index, final Resolvable<GroupSymbolTableNode> value) {
        final int pos = 8 + 2 * context().offsetSize() + index * (context().lengthSize() + context().offsetSize());
        setResolvable(pos + context().lengthSize(), value);
    }

    @Override
    public void initialize() {
        setSignature();
        setNodeType(NodeType.GROUP);
        setNodeLevel(0);
        setEntriesUsed(0);
        setLeftSibling(null);
        setRightSibling(null);
    }

    @Override
    public Iterator<Resolvable<GroupSymbolTableNode>> valueIterator() {
        return new Iterator<>() {
            private int idx;

            @Override
            public boolean hasNext() {
                return idx < getEntriesUsed();
            }

            @Override
            public Resolvable<GroupSymbolTableNode> next() {
                return getChild(idx++);
            }
        };
    }

    @Override
    public String toString() {
        return String.format(
                "BTreeV1GroupLeafBB [size()=%s, getLeftSibling()=%s, getRightSibling()=%s, isValid()=%s, getNodeType()=%s, "
                        + "getNodeLevel()=%s, getEntriesUsed()=%s]",
                size(), getLeftSibling(), getRightSibling(), isValid(), getNodeType(), getNodeLevel(),
                getEntriesUsed());
    }

}
