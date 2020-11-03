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
import java.util.Map;

import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContextHeap;

public final class BTreeV1GroupInternalBB extends AbstractBTreeV1GroupBB implements BTreeV1GroupInternal {

    public BTreeV1GroupInternalBB(final ByteBuffer buf, final SizingContextHeap sizingContext) {
        super(buf, sizingContext);
        assert NodeType.GROUP == getNodeType();
        assert 0 != getNodeLevel();
    }

    @Override
    public Resolvable<BTreeV1Group> getChild(final int index) {
        throw new IllegalArgumentException("Implement!");
    }

    @Override
    public Map<Resolvable<String>, Resolvable<BTreeV1Group>> getEntryNodes() {
        throw new IllegalArgumentException("Implement!");
    }

    @Override
    public Iterator<Resolvable<BTreeV1Group>> valueIterator() {
        throw new IllegalArgumentException("Implement!");
    }

}
