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

import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContextKTreeDimension;

public final class BTreeV1DataLeafBB extends AbstractBTreeV1DataBB implements BTreeV1DataLeaf {
    public BTreeV1DataLeafBB(final ByteBuffer buf, final SizingContextKTreeDimension sizingContext) {
        super(buf, sizingContext);
        assert NodeType.DATA == getNodeType();
        assert 0 == getNodeLevel();
    }

    @Override
    public Resolvable<ByteBuffer> getChild(final int index) {
        int pos = 8 + 2 * context().offsetSize();
        pos += index * (8 + (context.dimensionality() + 1) * 8 + context.offsetSize());
        if (pos >= size()) {
            return null;
        }
        final int sizeOfChunkInBytes = getInt(pos);
        pos += 8 + (context.dimensionality() + 1) * 8;
        return getResolvable(pos, sizeOfChunkInBytes);
    }

}
