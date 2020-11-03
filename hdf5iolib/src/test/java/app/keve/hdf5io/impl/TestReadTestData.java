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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.yaml.snakeyaml.Yaml;

import app.keve.hdf5io.Support;
import app.keve.hdf5io.TestDataFile;
import app.keve.hdf5io.api.HDF5;
import app.keve.hdf5io.api.HDF5Dataset;
import app.keve.hdf5io.api.HDF5Dataset.Chunk;
import app.keve.hdf5io.api.HDF5File;
import app.keve.hdf5io.api.HDF5Group;
import app.keve.hdf5io.api.HDF5Link;
import app.keve.hdf5io.api.util.ArrayUtil;

public final class TestReadTestData {
    private static final HDF5 HDF5;

    static {
        final ServiceLoader<HDF5> hdf5Loader = ServiceLoader.load(HDF5.class);
        HDF5 = hdf5Loader.findFirst().orElse(new HDF5Implementation());
    }

    @ParameterizedTest()
    @ValueSource(strings = {"EARLIEST", "V1_8", "V1_10"})
    public void testToYaml(final String version) throws IOException, Exception {
        final String name = String.format("myData-%s.h5", version);
        final Path p = Paths.get(getClass().getClassLoader().getResource(name).toURI());
        try (LocalHDF5File hdf5File = LocalHDF5File.of(p)) {
            final Yaml yaml = Support.newYaml(hdf5File);
            yaml.dump(hdf5File.getSuperblock(), Files.newBufferedWriter(Path.of(p.toString() + ".yaml")));
        }
    }

    @ParameterizedTest()
    @ValueSource(strings = {"EARLIEST", "V1_8", "V1_10"})
    public void testReadApiTraversal(final String version) throws IOException, Exception {
        final String name = String.format("myData-%s.h5", version);
        final Path p = Paths.get(getClass().getClassLoader().getResource(name).toURI());

        try (HDF5File hdf5File = HDF5.open(p)) {
            final Set<HDF5Link> links = new LinkedHashSet<>();
            hdf5File.getRootGroup().linkIterator().forEachRemaining(links::add);
            assertEquals(4, links.size());
            assertEquals(Set.of("__DATA_TYPES__", "double", "double64", "float64"),
                    links.stream().map(HDF5Link::getName).collect(Collectors.toSet()));

            final HDF5Group doubleDir = hdf5File.getRootGroup().resolve("double").asGroup();
            links.clear();
            doubleDir.linkIterator().forEachRemaining(links::add);
            assertEquals(TestDataFile.DOUBLE_ENTRIES.size(), links.size());
            final Set<String> expectedNames = TestDataFile.DOUBLE_ENTRIES.keySet();
            assertEquals(expectedNames, links.stream().map(HDF5Link::getName).collect(Collectors.toSet()));
        }
    }

    @ParameterizedTest(name = "{0}/{1}")
    @MethodSource("doubleData")
    public void testReadApiData(final String version, final String dsName, final Object expectedValue)
            throws IOException, Exception {
        final String name = String.format("myData-%s.h5", version);
        final Path p = Paths.get(getClass().getClassLoader().getResource(name).toURI());
        try (HDF5File hdf5File = HDF5.open(p)) {
            final HDF5Dataset ds = hdf5File.getRootGroup().resolve("double", dsName).asDataset();
            assertNotNull(ds);
            if ("V1_10".equals(version)
                    && (dsName.endsWith(".chunked") || dsName.endsWith(".deflate") || dsName.endsWith(".sparse"))) {
                // Not implemented yet
                return;
            }
            assertData(expectedValue, ds.getAsObject());

            final Stream<? extends Chunk> chunks = ds.getChunks();
            switch ((int) chunks.count()) {
            case 0:
                fail("must have at least one chunk");
                break;
            case 1:
                final Chunk chunk = ds.getChunks().findFirst().get();
                assertData(expectedValue, chunk.getAsObject());
                break;
            default:
                break;
            }
        }
    }

    private void assertData(final Object expectedValue, final Object data) {
        assertNotNull(expectedValue);
        assertNotNull(data);
        assertEquals(ArrayUtil.rank(expectedValue), ArrayUtil.rank(data), "rank mismatch");
        assertArrayEquals(ArrayUtil.data2DimensionsSimple(expectedValue), ArrayUtil.data2DimensionsSimple(data),
                "dim mismatch");
        assertEquals(ArrayUtil.data2Type(expectedValue), ArrayUtil.data2Type(data), "type mismatch");
        assertTrue(ArrayUtil.deepEquals(expectedValue, data), () -> {
            final StringBuffer sb = new StringBuffer("expected: ");
            sb.append(ArrayUtil.deepToString(expectedValue));
            sb.append(", but got: ");
            sb.append(ArrayUtil.deepToString(data));
            return sb.toString();
        });
    }

    public static Stream<Arguments> doubleData() {
        final Stream<String> versionStream = Stream.of("EARLIEST", "V1_8", "V1_10");
        return versionStream.flatMap(v -> TestDataFile.DOUBLE_ENTRIES.entrySet().stream()
                .map(e -> Arguments.of(v, e.getKey(), e.getValue())));
    }

}
