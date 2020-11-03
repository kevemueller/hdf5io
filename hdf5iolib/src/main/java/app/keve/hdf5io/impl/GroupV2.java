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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import app.keve.hdf5io.api.HDF5Dataset.Builder;
import app.keve.hdf5io.api.HDF5FormatException;
import app.keve.hdf5io.api.HDF5Group;
import app.keve.hdf5io.api.HDF5Link;
import app.keve.hdf5io.api.HDF5NamedDatatype;
import app.keve.hdf5io.api.HDF5NamedObject;
import app.keve.hdf5io.api.datatype.HDF5Datatype;
import app.keve.hdf5io.fileformat.H5Resolver;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.level1.BTreeV2;
import app.keve.hdf5io.fileformat.level1.BTreeV2LeafNode;
import app.keve.hdf5io.fileformat.level1.BTreeV2Node;
import app.keve.hdf5io.fileformat.level1.BTreeV2Node.LinkNameRecord;
import app.keve.hdf5io.fileformat.level2.ObjectHeader;
import app.keve.hdf5io.fileformat.level2message.LinkInfoMessage;
import app.keve.hdf5io.fileformat.level2message.LinkMessage;
import app.keve.hdf5io.fileformat.level2message.LinkMessage.HardLinkInformation;
import app.keve.hdf5io.fileformat.level2message.LinkMessage.LinkInformation;
import app.keve.hdf5io.fileformat.level2message.LinkMessage.SoftLinkInformation;

public final class GroupV2 extends AbstractNamedObjectV implements HDF5Group {
    public GroupV2(final long id, final H5Resolver hdf5Resolver, final ObjectHeader objectHeader) {
        super(id, hdf5Resolver, objectHeader);
    }

    private void addLinkMessage(final List<HDF5Link> links, final LinkMessage linkMessage) {
        final String linkName = linkMessage.getLinkName();
        final LinkInformation linkInfo = linkMessage.getLinkInformation();
        if (linkInfo instanceof HardLinkInformation) {
            final Resolvable<ObjectHeader> rObjectHeader = ((HardLinkInformation) linkInfo).getObjectHeader();
            links.add(new Link(hdf5Resolver, linkName, rObjectHeader));
        } else if (linkInfo instanceof SoftLinkInformation) {
            final String linkTarget = ((SoftLinkInformation) linkInfo).getSoftLink();
            links.add(new SoftLink(hdf5Resolver, linkName, linkTarget));
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Iterator<HDF5Link> linkIterator() throws HDF5FormatException {
        final List<HDF5Link> links = new ArrayList<>();
        final HeaderMessageIterator it = new HeaderMessageIterator(hdf5Resolver, objectHeader.headerMessageIterator());
        it.forEachRemaining(hme -> {
            switch (hme.getType()) {
            case LINK_INFO:
                // TODO: replace with BTreeV2Manager.iterator()
                final LinkInfoMessage linkInfoMessage = hme.getMessage();
                final Resolvable<BTreeV2> rNameIndex = linkInfoMessage.getBTreeV2NameIndex();
                if (null != rNameIndex) {
                    final BTreeV2 nameIndex = rNameIndex.resolve(hdf5Resolver);
                    final BTreeV2Node rootNode = nameIndex.getRootNode().resolve(hdf5Resolver);
                    final BTreeV2LeafNode leafNode = (BTreeV2LeafNode) rootNode;
                    leafNode.getRecords().forEach(
                            r -> addLinkMessage(links, ((LinkNameRecord) r).getHeapId().resolve(hdf5Resolver)));
                }
                break;
            case LINK:
                addLinkMessage(links, hme.getMessage());
                break;
            default:
                break;
            }
        });
        return links.iterator();
    }

    @Override
    public HDF5Link addLink(final String name, final HDF5NamedObject target) {
        throw new IllegalArgumentException("implement!");
    }

    @Override
    public HDF5Link addLink(final String name, final String target) {
        throw new IllegalArgumentException("implement!");
    }

    @Override
    public HDF5Group addGroup(final String name) {
        throw new IllegalArgumentException("implement!");
    }

    @Override
    public HDF5NamedDatatype addNamedDatatype(final String name, final HDF5Datatype datatype) {
        throw new IllegalArgumentException("implement!");
    }

    @Override
    public Builder addDataset(final String name) {
        throw new IllegalArgumentException("implement!");
    }
}
