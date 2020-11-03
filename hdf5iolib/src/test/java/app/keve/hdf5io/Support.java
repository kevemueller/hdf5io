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
package app.keve.hdf5io;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import app.keve.hdf5io.fileformat.H5Resolver;
import app.keve.hdf5io.impl.LocalHDF5File;

public final class Support {
    private Support() {

    }

    public static Yaml newYaml(final H5Resolver hdf5File) {
        final DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setWidth(320);
        dumperOptions.setAllowReadOnlyProperties(true);
        final HDF5ObjectRepresenter hdf5Representer = new HDF5ObjectRepresenter(hdf5File);
        return new Yaml(hdf5Representer, dumperOptions) {
            @Override
            public void dump(final Object data, final Writer output) {
                super.dump(data, output);
                final Throwable lastThrowable = hdf5Representer.getLastThrowable();
                if (null != lastThrowable) {
                    throw new Error(lastThrowable);
                }
            }
        };
    }

    public static void dumpH5ToYaml(final Path h5p) throws Exception {
        try (LocalHDF5File hdf5File = LocalHDF5File.of(h5p)) {
            final Yaml yaml = Support.newYaml(hdf5File);
            yaml.dump(hdf5File.getSuperblock(), Files.newBufferedWriter(Path.of(h5p.toString() + ".yaml")));
        }
    }
}
