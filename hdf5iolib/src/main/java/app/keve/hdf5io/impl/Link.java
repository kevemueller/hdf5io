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

import app.keve.hdf5io.api.HDF5FormatException;
import app.keve.hdf5io.api.HDF5Link;
import app.keve.hdf5io.api.HDF5NamedObject;
import app.keve.hdf5io.fileformat.H5Resolver;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.level2.ObjectHeader;
import app.keve.hdf5io.fileformat.level2message.DatatypeMessage;

public final class Link implements HDF5Link {
    private final H5Resolver hdf5Resolver;
    private final String linkName;
    private final Resolvable<? extends ObjectHeader> rObjectHeader;

    public Link(final H5Resolver hdf5Resolver, final String linkName,
            final Resolvable<? extends ObjectHeader> rObjectHeader) {
        this.hdf5Resolver = hdf5Resolver;
        this.linkName = linkName;
        this.rObjectHeader = rObjectHeader;
    }

    @Override
    public String getName() {
        return linkName;
    }

    @Override
    public HDF5NamedObject getTarget() throws HDF5FormatException {
        final ObjectHeader objectHeader = rObjectHeader.resolve(hdf5Resolver);
        final HeaderMessageIterator it = new HeaderMessageIterator(hdf5Resolver, objectHeader.headerMessageIterator());
        DatatypeMessage datatypeMessage = null;
        while (it.hasNext()) {
            final ObjectHeader.HeaderMessageEntry<?> hme = it.next();
            switch (hme.getType()) {
            case DATA_LAYOUT:
                return new DatasetV(rObjectHeader.getAddress(), hdf5Resolver, objectHeader);
            case DATATYPE:
                datatypeMessage = hme.getMessage();
                break;
            case SYMBOL_TABLE:
                return new GroupV1(rObjectHeader.getAddress(), hdf5Resolver, objectHeader);
            case LINK_INFO:
            case LINK:
                return new GroupV2(rObjectHeader.getAddress(), hdf5Resolver, objectHeader);
            default:
                break;
            }
        }
        if (null != datatypeMessage) {
            return new NamedDatatypeV0(rObjectHeader.getAddress(), hdf5Resolver, objectHeader,
                    datatypeMessage.getDatatype());
        }
        throw new HDF5FormatException("Could not determine HDF5NamedObject from header messages");
    }

    @Override
    public String toString() {
        try {
            return String.format("Link [getName()=%s, getTarget()=%s]", getName(), getTarget());
        } catch (final HDF5FormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
