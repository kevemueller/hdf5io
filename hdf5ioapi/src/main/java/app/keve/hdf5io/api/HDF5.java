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
import java.nio.ByteBuffer;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import app.keve.hdf5io.api.HDF5Constants.Profile;

/**
 * The main entrypoint to the HDF5 API.
 * 
 * @author keve
 *
 */
public interface HDF5 {
    HDF5File open(Path file, OpenOption... options) throws IOException;

    @Deprecated
    HDF5File create();

    HDF5FileBuilder builder();

    /**
     * Builder to finetune HDF5 file creation parameters.
     * 
     * @author keve
     *
     */
    interface HDF5FileBuilder {
        HDF5File build() throws IOException;

        HDF5FileBuilder withBacking(Path path, OpenOption... options);

        HDF5FileBuilder withMultiBacking(Path path, OpenOption... options);

        HDF5FileBuilder withFamilyBacking(long memberSize, Path path, OpenOption... options);

        HDF5FileBuilder withProfile(Profile profile);

        HDF5FileBuilder withPreamble(ByteBuffer buf);

        HDF5FileBuilder withOffsetSize(int offsetSize);

        HDF5FileBuilder withLengthSize(int lengthSize);

        HDF5FileBuilder withIndexedStorageInternalNodeK(int k);

        HDF5FileBuilder withGroupInternalNodeK(int k);

        HDF5FileBuilder withGroupLeafNodeK(int k);
    }
}
