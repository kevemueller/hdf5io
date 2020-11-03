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

import java.util.Iterator;

/**
 * Common ancestor of HDF5 named objects.
 * 
 * @author keve
 *
 */
public interface HDF5NamedObject {
    long getObjectId();

    Iterator<HDF5Attribute> attributeIterator();

    HDF5Dataset asDataset() throws HDF5FormatException;

    HDF5Group asGroup() throws HDF5FormatException;

    HDF5NamedDatatype asNamedDatatype() throws HDF5FormatException;

}
