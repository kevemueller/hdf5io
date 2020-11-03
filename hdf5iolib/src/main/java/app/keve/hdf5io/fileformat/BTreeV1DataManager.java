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
package app.keve.hdf5io.fileformat;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import app.keve.hdf5io.fileformat.level1.BTreeV1.NodeType;
import app.keve.hdf5io.fileformat.level1.BTreeV1Data;
import app.keve.hdf5io.fileformat.level1.BTreeV1Data.DataKey;
import app.keve.hdf5io.fileformat.level1.BTreeV1DataInternal;
import app.keve.hdf5io.fileformat.level1.BTreeV1DataLeaf;

public final class BTreeV1DataManager extends AbstractBTreeV1Manager {
    private final H5Resolver hdf5Resolver;
    private final BTreeV1Data node;

    public BTreeV1DataManager(final H5Resolver hdf5Resolver, final Resolvable<BTreeV1Data> rNode) {
        this.hdf5Resolver = hdf5Resolver;
        this.node = rNode.resolve(hdf5Resolver);
        assert NodeType.DATA == node.getNodeType();
    }

    public static final class TreeEntryData {
        public final DataKey leftKey;
        public final Resolvable<ByteBuffer> child;
        public final DataKey rightKey;

        public TreeEntryData(final DataKey leftKey, final Resolvable<ByteBuffer> child, final DataKey rightKey) {
            this.leftKey = leftKey;
            this.child = child;
            this.rightKey = rightKey;
        }

        @Override
        public String toString() {
            return String.format("TreeEntryData[leftKey=%s, child=%s, rightKey=%s]", leftKey, child, rightKey);
        }

    }

    public boolean isSingleChunk() {
        return 0 == node.getNodeLevel() && 1 == node.getEntriesUsed();
    }

    public int getNumberOfChunks() {
        return getNumberOfChunks(node);
    }

    private int getNumberOfChunks(final BTreeV1Data bTreeNode) {
        if (0 == bTreeNode.getNodeLevel()) {
            return bTreeNode.getEntriesUsed();
        } else {
            return ((BTreeV1DataInternal) bTreeNode).getChildren().stream().parallel().map(rn -> rn.resolve(hdf5Resolver))
                    .collect(Collectors.summingInt(this::getNumberOfChunks));
        }
    }

    public Iterator<TreeEntryData> chunkIterator() {
        return chunkIterator(node);
    }

    private Iterator<TreeEntryData> chunkIterator(final BTreeV1Data bTreeNode) {
        if (0 == bTreeNode.getNodeLevel()) {
            final BTreeV1DataLeaf leafNode = (BTreeV1DataLeaf) bTreeNode;
            final int numEntries = leafNode.getEntriesUsed();
            return new Iterator<>() {
                private int i;

                @Override
                public boolean hasNext() {
                    return i < numEntries;
                }

                @Override
                public TreeEntryData next() {
                    final TreeEntryData treeEntry = new TreeEntryData(leafNode.getKey(i), leafNode.getChild(i),
                            leafNode.getKey(i + 1));
                    i++;
                    return treeEntry;
                }

            };
        } else {
            final List<Resolvable<BTreeV1Data>> children = ((BTreeV1DataInternal) bTreeNode).getChildren();
            return new Iterator<>() {
                private Iterator<TreeEntryData> currentIterator;

                @Override
                public boolean hasNext() {
                    if (!children.isEmpty()) {
                        return true;
                    }
                    return currentIterator.hasNext();
                }

                @Override
                public TreeEntryData next() {
                    if (null == currentIterator || !currentIterator.hasNext()) {
                        final Resolvable<BTreeV1Data> child = children.remove(0);
                        currentIterator = chunkIterator(child.resolve(hdf5Resolver));
                    }
                    return currentIterator.next();
                }
            };
        }
    }

}
