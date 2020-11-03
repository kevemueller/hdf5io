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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import app.keve.hdf5io.fileformat.level1.BTreeV1.NodeType;
import app.keve.hdf5io.fileformat.level1.BTreeV1Group;
import app.keve.hdf5io.fileformat.level1.BTreeV1GroupInternal;
import app.keve.hdf5io.fileformat.level1.BTreeV1GroupLeaf;
import app.keve.hdf5io.fileformat.level1.GroupSymbolTableNode;
import app.keve.hdf5io.fileformat.level1.SymbolTableEntry;

public final class BTreeV1GroupManager extends AbstractBTreeV1Manager {
    private final H5Resolver hdf5Resolver;
    private final BTreeV1Group node;

    public BTreeV1GroupManager(final H5Resolver hdf5Resolver, final Resolvable<BTreeV1Group> rNode) {
        this.hdf5Resolver = hdf5Resolver;
        this.node = rNode.resolve(hdf5Resolver);
        assert NodeType.GROUP == node.getNodeType();
    }

    public static final class TreeEntryGroup {
        public final Resolvable<String> leftKey;
        public final Resolvable<GroupSymbolTableNode> child;
        public final Resolvable<String> rightKey;

        public TreeEntryGroup(final Resolvable<String> leftKey, final Resolvable<GroupSymbolTableNode> resolvable,
                final Resolvable<String> rightKey) {
            this.leftKey = leftKey;
            this.child = resolvable;
            this.rightKey = rightKey;
        }

        @Override
        public String toString() {
            return String.format("TreeEntryGroup[leftKey=%s, child=%s, rightKey=%s]", leftKey, child, rightKey);
        }
    }

    public BTreeV1Group getRootNode() {
        return node;
    }

    public Iterator<TreeEntryGroup> groupIterator() {
        return groupIterator(node);
    }

    private Iterator<TreeEntryGroup> groupIterator(final BTreeV1Group bTreeNode) {
        if (0 == bTreeNode.getNodeLevel()) {
            final BTreeV1GroupLeaf leafNode = (BTreeV1GroupLeaf) bTreeNode;
            final int numEntries = leafNode.getEntriesUsed();
            return new Iterator<>() {
                private int i;

                @Override
                public boolean hasNext() {
                    return i < numEntries;
                }

                @Override
                public TreeEntryGroup next() {
                    final TreeEntryGroup treeEntry = new TreeEntryGroup(leafNode.getKey(i), leafNode.getChild(i),
                            leafNode.getKey(i + 1));
                    i++;
                    return treeEntry;
                }

            };
        } else {
            final List<Resolvable<BTreeV1Group>> children = ((BTreeV1GroupInternal) bTreeNode).getChildren();
            return new Iterator<>() {
                private Iterator<TreeEntryGroup> currentIterator;

                @Override
                public boolean hasNext() {
                    if (!children.isEmpty()) {
                        return true;
                    }
                    return currentIterator.hasNext();
                }

                @Override
                public TreeEntryGroup next() {
                    if (null == currentIterator || !currentIterator.hasNext()) {
                        final Resolvable<BTreeV1Group> child = children.remove(0);
                        currentIterator = groupIterator(child.resolve(hdf5Resolver));
                    }
                    return currentIterator.next();
                }
            };
        }
    }

    private SymbolTableEntry addEntry(final GroupSymbolTableNode childNode, final Resolvable<String> rName,
            final String name) {
        /* Insert an entry with the given name */
        final int num = childNode.getNumberOfSymbols();
        int index = 0;
        loop: while (index < num) {
            final String childName = childNode.getEntry(index).getLinkName().resolve(hdf5Resolver);
            switch (name.compareTo(childName)) {
            case 0:
                throw new IllegalArgumentException("name exists");
            case -1:
                break loop;
            default:
                break;
            }
            index++;
        }
        // shift subsequent nodes
        for (int j = num; j > index; j--) {
            childNode.setEntry(j, childNode.getEntry(j - 1));
        }
        childNode.setNumberOfSymbols(num + 1);
        final SymbolTableEntry symbolTableEntry = childNode.getEntry(index);
        symbolTableEntry.initialize();
        symbolTableEntry.setLinkName(rName);
        return symbolTableEntry;
    }

    public SymbolTableEntry add(final Resolvable<String> rName, final String name) throws IOException {
        /*
         * Find the index of the child where the node shall be added. key[index+1].name
         * > name; Add the name to the existing child. If already full, split the child.
         * TODO: clean up!!
         */
        int index = 0;
        loop: while (index < node.getEntriesUsed()) {
            final Resolvable<String> rNextKey = node.getKey(index + 1);
            final String nextKey = rNextKey.resolve(hdf5Resolver);

            switch (name.compareTo(nextKey)) {
            case 0:
                throw new IllegalArgumentException("name exists");
            case 1:
                break loop;
            default:
                // advance further
            }
            index++;
        }
        final BTreeV1GroupLeaf leafNode = (BTreeV1GroupLeaf) node;

        final Resolvable<GroupSymbolTableNode> rChildNode;
        if (index == node.getEntriesUsed()) {
            leafNode.setEntriesUsed(leafNode.getEntriesUsed() + 1);
            rChildNode = leafNode.context().h5Factory().allocate(GroupSymbolTableNode.class, node.context());
            leafNode.setChild(index, rChildNode);
        } else {
            rChildNode = leafNode.getChild(index);
        }
        final GroupSymbolTableNode childNode = rChildNode.resolve(hdf5Resolver);
        final SymbolTableEntry addedEntry = addEntry(childNode, rName, name);
        final Resolvable<String> lastName = childNode.getEntry(childNode.getNumberOfSymbols() - 1).getLinkName();
        leafNode.setKey(index + 1, lastName);
        return addedEntry;

        // index points to the insertion point
//        for (int j = node.getEntriesUsed(); j >= index; j--) {
//            leafNode.setKey(j + 1, node.getKey(j));
//            leafNode.setChild(j + 1, leafNode.getChild(j));
//        }
//        leafNode.setKey(index + 1, rName);
//        leafNode.setChild(index, rGroupSymbolTableNode);
    }

}
