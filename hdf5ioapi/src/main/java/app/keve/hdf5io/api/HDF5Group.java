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
package app.keve.hdf5io.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import app.keve.hdf5io.api.datatype.HDF5Datatype;

/**
 * A HDF5 group named object.
 * 
 * @author keve
 *
 */
public interface HDF5Group extends HDF5NamedObject {

    HDF5Link addLink(String name, HDF5NamedObject target) throws IOException;

    HDF5Link addLink(String name, String target) throws IOException;

    HDF5Group addGroup(String name) throws IOException;

    HDF5NamedDatatype addNamedDatatype(String name, HDF5Datatype datatype) throws IOException;

    HDF5Dataset.Builder addDataset(String name) throws IOException;

    Iterator<HDF5Link> linkIterator() throws HDF5FormatException;

    default List<HDF5Link> getLinks() throws HDF5FormatException {
        final ArrayList<HDF5Link> links = new ArrayList<>();
        linkIterator().forEachRemaining(links::add);
        return links;
    }

    @Override
    default HDF5Dataset asDataset() {
        throw new IllegalArgumentException();
    }

    @Override
    default HDF5Group asGroup() {
        return this;
    }

    @Override
    default HDF5NamedDatatype asNamedDatatype() {
        throw new IllegalArgumentException();
    }

    default HDF5NamedObject resolve(final String... names) throws HDF5FormatException {
        HDF5NamedObject current = this;
        name: for (final String name : names) {
            for (final Iterator<HDF5Link> it = ((HDF5Group) current).linkIterator(); it.hasNext();) {
                final HDF5Link link = it.next();
                if (name.equals(link.getName())) {
                    current = link.getTarget();
                    continue name;
                }
            }
            throw new NoSuchElementException();
        }
        return current;
    }
}
