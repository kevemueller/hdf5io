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

public final class SoftLink implements HDF5Link {
    private final H5Resolver hdf5Resolver;
    private final String linkName;
    private final String linkTarget;

    public SoftLink(final H5Resolver hdf5Resolver, final String linkName, final String linkTarget) {
        this.hdf5Resolver = hdf5Resolver;
        this.linkName = linkName;
        this.linkTarget = linkTarget;
    }

    @Override
    public String getName() {
        return linkName;
    }

    @Override
    public HDF5NamedObject getTarget() throws HDF5FormatException {
        return null;
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
