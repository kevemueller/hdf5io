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

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

import app.keve.hdf5io.api.HDF5Attribute;
import app.keve.hdf5io.api.HDF5NamedObject;
import app.keve.hdf5io.fileformat.H5Resolver;
import app.keve.hdf5io.fileformat.level2.ObjectHeader;

// TODO: might need full path as well to be able to resolve symlinks or at leas root group
public abstract class AbstractNamedObjectV implements HDF5NamedObject {
    protected final H5Resolver hdf5Resolver;
    protected final ObjectHeader objectHeader;
    private final long id;

    public AbstractNamedObjectV(final long id, final H5Resolver hdf5Resolver, final ObjectHeader objectHeader) {
        this.id = id;
        this.hdf5Resolver = hdf5Resolver;
        this.objectHeader = objectHeader;
    }

    @Override
    public final long getObjectId() {
        return id;
    }

    @Override
    public final Iterator<HDF5Attribute> attributeIterator() {
        return Collections.emptyIterator();
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AbstractNamedObjectV)) {
            return false;
        }
        final AbstractNamedObjectV other = (AbstractNamedObjectV) obj;
        return id == other.id;
    }

    protected final <T> T getMessage(final int type, final Class<T> tClass) {
        final HeaderMessageIterator it = new HeaderMessageIterator(hdf5Resolver, objectHeader.headerMessageIterator());
        while (it.hasNext()) {
            final ObjectHeader.HeaderMessageEntry<?> hme = it.next();
            if (type == hme.getTypeNumber()) {
                return (T) hme.getMessage();
            }
        }
        return null;
    }
}
