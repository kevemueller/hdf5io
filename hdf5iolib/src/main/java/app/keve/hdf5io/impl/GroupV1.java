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
package app.keve.hdf5io.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import app.keve.hdf5io.api.HDF5Dataset.Builder;
import app.keve.hdf5io.api.HDF5FormatException;
import app.keve.hdf5io.api.HDF5Group;
import app.keve.hdf5io.api.HDF5Link;
import app.keve.hdf5io.api.HDF5NamedDatatype;
import app.keve.hdf5io.api.HDF5NamedObject;
import app.keve.hdf5io.api.datatype.HDF5Datatype;
import app.keve.hdf5io.fileformat.BTreeV1GroupManager;
import app.keve.hdf5io.fileformat.H5Factory;
import app.keve.hdf5io.fileformat.H5MessageType;
import app.keve.hdf5io.fileformat.H5Resolver;
import app.keve.hdf5io.fileformat.LocalHeapManager;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextHeap;
import app.keve.hdf5io.fileformat.level1.BTreeV1GroupLeaf;
import app.keve.hdf5io.fileformat.level1.GroupSymbolTableNode;
import app.keve.hdf5io.fileformat.level1.SymbolTableEntry;
import app.keve.hdf5io.fileformat.level2.ObjectHeader;
import app.keve.hdf5io.fileformat.level2.ObjectHeader.HeaderMessageEntry;
import app.keve.hdf5io.fileformat.level2.ObjectHeaderV1;
import app.keve.hdf5io.fileformat.level2datatype.AbstractDatatypeBB;
import app.keve.hdf5io.fileformat.level2message.DatatypeMessage;
import app.keve.hdf5io.fileformat.level2message.SymbolTableMessage;
import app.keve.hdf5io.fileformat.level2message.SymbolTableMessageBB;

public final class GroupV1 extends AbstractNamedObjectV implements HDF5Group {
    public GroupV1(final long id, final H5Resolver hdf5Resolver, final ObjectHeader objectHeader) {
        super(id, hdf5Resolver, objectHeader);
    }

    private SymbolTableMessage getSymbolTableMessage() throws HDF5FormatException {
        final SymbolTableMessage symbolTableMessage = getMessage(H5MessageType.SYMBOL_TABLE.typeNum,
                SymbolTableMessage.class);
        if (null == symbolTableMessage) {
            throw new HDF5FormatException("Must have a SymbolTableMessage in an old style group!");
        }
        return symbolTableMessage;
    }

    @Override
    public Iterator<HDF5Link> linkIterator() throws HDF5FormatException {
        final SymbolTableMessage symbolTableMessage = getSymbolTableMessage();
        final BTreeV1GroupManager bTreeV1GroupManager = new BTreeV1GroupManager(hdf5Resolver,
                symbolTableMessage.getBTree());

        final List<HDF5Link> links = new ArrayList<>();
        bTreeV1GroupManager.groupIterator().forEachRemaining(rGroupSymbolTableEntry -> {
            final GroupSymbolTableNode groupSymbolTable = rGroupSymbolTableEntry.child.resolve(hdf5Resolver);
            groupSymbolTable.entryIterator().forEachRemaining(ste -> {
                final String linkName = ste.getLinkName().resolve(hdf5Resolver);
                if (null == ste.getObjectHeader()) {
                    final String symLink = ste.getSymlink().resolve(hdf5Resolver);
                    // check if absolute
                    // pass base (root or self)
                    links.add(new SoftLink(hdf5Resolver, linkName, symLink));
                } else {
                    links.add(new Link(hdf5Resolver, linkName, ste.getObjectHeader()));
                }
            });
        });
        return links.iterator();
    }

    @Override
    public String toString() {
//        try {
        return String.format("GroupV1 [getObjectId()=%s, getLinks()=%s]", getObjectId(), /* getLinks() */"");
//        } catch (HDF5FormatException e) {
//            return String.format("GroupV1 [getObjectId()=%s, getLinks()=%s]", getObjectId(), e.toString());
//        }
    }

    private SymbolTableEntry addEntry(final String name) throws IOException {
        final SymbolTableMessage symbolTableMessage = getSymbolTableMessage();
        final BTreeV1GroupManager bTreeV1GroupManager = new BTreeV1GroupManager(hdf5Resolver,
                symbolTableMessage.getBTree());

        final LocalHeapManager localHeapManager = LocalHeapManager.of(hdf5Resolver, symbolTableMessage.getLocalHeap());
        final Resolvable<String> rName = localHeapManager.addAsciiNulString(name);
        final SymbolTableEntry newSymbolTableEntry = bTreeV1GroupManager.add(rName, name);
        return newSymbolTableEntry;
    }

    private Map.Entry<SymbolTableEntry, Resolvable<String>> addEntry(final String name, final String additionalName)
            throws IOException {
        final SymbolTableMessage symbolTableMessage = getSymbolTableMessage();
        final BTreeV1GroupManager bTreeV1GroupManager = new BTreeV1GroupManager(hdf5Resolver,
                symbolTableMessage.getBTree());

        final LocalHeapManager localHeapManager = LocalHeapManager.of(hdf5Resolver, symbolTableMessage.getLocalHeap());
        final Resolvable<String> rName = localHeapManager.addAsciiNulString(name);
        final Resolvable<String> rAdditionalName = localHeapManager.addAsciiNulString(additionalName);

        final SymbolTableEntry newSymbolTableEntry = bTreeV1GroupManager.add(rName, name);
        return Map.entry(newSymbolTableEntry, rAdditionalName);
    }

    @Override
    public HDF5Link addLink(final String name, final HDF5NamedObject target) throws IOException {
        final SymbolTableEntry newSymbolTableEntry = addEntry(name);

        final AbstractNamedObjectV namedTarget = (AbstractNamedObjectV) target;
        final Resolvable<ObjectHeader> targetObjectHeader = objectHeader.context().h5Factory()
                .resolvable(namedTarget.getObjectId(), 0, ObjectHeader.class, objectHeader.context());
        newSymbolTableEntry.setObjectHeader(targetObjectHeader);
        ((ObjectHeaderV1) namedTarget.objectHeader)
                .setObjectReferenceCount(((ObjectHeaderV1) namedTarget.objectHeader).getObjectReferenceCount() + 1);
        return new Link(hdf5Resolver, name, targetObjectHeader);
    }

    @Override
    public HDF5Link addLink(final String linkName, final String linkTarget) throws IOException {
        final Entry<SymbolTableEntry, Resolvable<String>> newSymbolTableEntry = addEntry(linkName, linkTarget);
        newSymbolTableEntry.getKey().setSymlink(newSymbolTableEntry.getValue());
        return new SoftLink(hdf5Resolver, linkName, linkTarget);
    }

    @Override
    public HDF5Group addGroup(final String name) throws IOException {
        final SymbolTableEntry newSymbolTableEntry = addEntry(name);

        // unify with LocalHDF5File initialization
        final H5Factory h5Factory = objectHeader.context().h5Factory();
        final Resolvable<ObjectHeaderV1> rNewObjectHeader = h5Factory.allocate(ObjectHeaderV1.class,
                objectHeader.context());
        final ObjectHeaderV1 newObjectHeader = rNewObjectHeader.resolve(hdf5Resolver);

        final LocalHeapManager newHeapManager = LocalHeapManager.of(hdf5Resolver, objectHeader.context());
        final SizingContextHeap newHeapContext = SizingContextHeap.of(objectHeader.context(),
                newHeapManager.getLocalHeap());

        newSymbolTableEntry.setObjectHeader(rNewObjectHeader);

        final Resolvable<BTreeV1GroupLeaf> rNewBTreeV1Group = h5Factory.allocate(BTreeV1GroupLeaf.class,
                newHeapContext);

        final long messageDataSize = SymbolTableMessageBB.size(objectHeader.context());
        final HeaderMessageEntry<SizingContext> hme = newObjectHeader.addHeaderMessage(messageDataSize);
        hme.setType(H5MessageType.SYMBOL_TABLE);
        final SymbolTableMessage newSymbolTableMessage = hme.getMessage();
        newSymbolTableMessage.initialize();
        hme.setConstant(true);
        newSymbolTableMessage.setLocalHeap(newHeapManager.getLocalHeap());
        newSymbolTableMessage.setBTree(rNewBTreeV1Group);

        return new GroupV1(rNewObjectHeader.getAddress(), hdf5Resolver, newObjectHeader);
    }

    @Override
    public HDF5NamedDatatype addNamedDatatype(final String name, final HDF5Datatype hdf5Datatype) throws IOException {
        final SymbolTableEntry newSymbolTableEntry = addEntry(name);

        final H5Factory h5Factory = objectHeader.context().h5Factory();

        final Resolvable<ObjectHeaderV1> rNewObjectHeader = h5Factory.allocate(ObjectHeaderV1.class,
                objectHeader.context());
        final ObjectHeaderV1 newObjectHeader = rNewObjectHeader.resolve(hdf5Resolver);
        newSymbolTableEntry.setObjectHeader(rNewObjectHeader);

        final AbstractDatatypeBB h5Datatype = AbstractDatatypeBB.of(hdf5Datatype, objectHeader.context());
        final long messageDataSize = h5Datatype.size();
        final HeaderMessageEntry<SizingContext> hme = newObjectHeader.addHeaderMessage(messageDataSize);
        hme.setType(H5MessageType.DATATYPE);
        final DatatypeMessage newDatatypeMessage = hme.getMessage();
        newDatatypeMessage.setDatatype(h5Datatype);

        hdf5Resolver.commit(rNewObjectHeader);

        return new NamedDatatypeV0(rNewObjectHeader.getAddress(), hdf5Resolver, newObjectHeader, hdf5Datatype);
    }

    @Override
    public Builder addDataset(final String name) throws IOException {
        final SymbolTableEntry newSymbolTableEntry = addEntry(name);

        final H5Factory h5Factory = objectHeader.context().h5Factory();

        final Resolvable<ObjectHeaderV1> rNewObjectHeader = h5Factory.allocate(ObjectHeaderV1.class,
                objectHeader.context());
        final ObjectHeaderV1 newObjectHeader = rNewObjectHeader.resolve(hdf5Resolver);
        newSymbolTableEntry.setObjectHeader(rNewObjectHeader);
        return new DatasetV.BuilderV(rNewObjectHeader.getAddress(), hdf5Resolver, newObjectHeader);
    }

}
