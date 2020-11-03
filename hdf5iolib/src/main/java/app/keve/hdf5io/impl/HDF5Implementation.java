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
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.OptionalInt;

import app.keve.hdf5io.api.HDF5;
import app.keve.hdf5io.api.HDF5Constants.Profile;
import app.keve.hdf5io.api.HDF5File;
import app.keve.hdf5io.api.HDF5FormatException;
import app.keve.hdf5io.fileformat.H5Registry;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.level0.Superblock;

public final class HDF5Implementation implements HDF5 {

    @SuppressWarnings("checkstyle:hiddenfield")
    private static final class Builder implements HDF5FileBuilder {
        private Profile profile;
        private ByteBuffer preamble;
        private OptionalInt offsetSize = OptionalInt.empty();
        private OptionalInt lengthSize = OptionalInt.empty();
        private OptionalInt indexedStorageInternalNodeK = OptionalInt.empty();
        private OptionalInt groupLeafNodeK = OptionalInt.empty();
        private OptionalInt groupInternalNodeK = OptionalInt.empty();
        private Path path;
        private OpenOption[] options = new OpenOption[0];

        private boolean allowTruncate() {
            for (final OpenOption option : options) {
                if (StandardOpenOption.TRUNCATE_EXISTING == option) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public HDF5File build() throws IOException {
            final H5Registry h5Registry = null == profile ? H5Registry.ofDefault() : H5Registry.of(profile);
            if (null != path && Files.exists(path) && !allowTruncate()) {
                final LocalHDF5File existingFile = LocalHDF5File.of(h5Registry, path, options);
                final Superblock superblock = existingFile.getSuperblock();
                final SizingContext sizingContext = superblock.context();
                if (offsetSize.isPresent() && offsetSize.getAsInt() != sizingContext.offsetSize()) {
                    throw new HDF5FormatException("Cannot change offsetSize on existing HDF5 file.");
                }
                if (lengthSize.isPresent() && lengthSize.getAsInt() != sizingContext.lengthSize()) {
                    throw new HDF5FormatException("Cannot change lengthSize on existing HDF5 file.");
                }
                if (indexedStorageInternalNodeK.isPresent()
                        && indexedStorageInternalNodeK.getAsInt() != sizingContext.indexedStorageInternalNodeK()) {
                    throw new HDF5FormatException(
                            "Cannot change indexedStorageInternalNodeK on existing HDF5 file.");
                }
                if (groupInternalNodeK.isPresent()
                        && groupInternalNodeK.getAsInt() != sizingContext.groupInternalNodeK()) {
                    throw new HDF5FormatException("Cannot change groupInternalNodeK on existing HDF5 file.");
                }
                if (groupLeafNodeK.isPresent() && groupLeafNodeK.getAsInt() != sizingContext.groupLeafNodeK()) {
                    throw new HDF5FormatException("Cannot change groupLeafNodeK on existing HDF5 file.");
                }
                if (null != preamble) {
                    existingFile.writePreamble(preamble);
                }
                return existingFile;
            }
            final LocalHDF5File newFile = LocalHDF5File.ofNew(h5Registry, path, options, preamble,
                    offsetSize.orElse(8), lengthSize.orElse(8), indexedStorageInternalNodeK, groupInternalNodeK,
                    groupLeafNodeK);

            return newFile;
        }

        @Override
        public HDF5FileBuilder withProfile(final Profile profile) {
            this.profile = profile;
            return this;
        }

        @Override
        public HDF5FileBuilder withPreamble(final ByteBuffer preamble) {
            this.preamble = preamble;
            return this;
        }

        @Override
        public HDF5FileBuilder withOffsetSize(final int offsetSize) {
            this.offsetSize = OptionalInt.of(offsetSize);
            return this;
        }

        @Override
        public HDF5FileBuilder withLengthSize(final int lengthSize) {
            this.lengthSize = OptionalInt.of(lengthSize);
            return this;
        }

        @Override
        public HDF5FileBuilder withIndexedStorageInternalNodeK(final int k) {
            this.indexedStorageInternalNodeK = OptionalInt.of(k);
            return this;
        }

        @Override
        public HDF5FileBuilder withGroupLeafNodeK(final int k) {
            this.groupLeafNodeK = OptionalInt.of(k);
            return this;
        }

        @Override
        public HDF5FileBuilder withGroupInternalNodeK(final int k) {
            this.groupInternalNodeK = OptionalInt.of(k);
            return this;
        }

        @Override
        public HDF5FileBuilder withBacking(final Path path, final OpenOption... options) {
            this.path = path;
            this.options = options;
            return this;
        }

        @Override
        public HDF5FileBuilder withMultiBacking(final Path path, final OpenOption... options) {
            throw new IllegalArgumentException("implement!");
        }

        @Override
        public HDF5FileBuilder withFamilyBacking(final long memberSize, final Path path,
                final OpenOption... options) {
            throw new IllegalArgumentException("implement!");
        }
    }

    @Override
    public HDF5File open(final Path file, final OpenOption... options) throws IOException {
        return LocalHDF5File.of(file);
    }

    @Override
    public HDF5File create() {
        return new MemHDF5();
    }

    @Override
    public HDF5FileBuilder builder() {
        return new Builder();
    }

}
