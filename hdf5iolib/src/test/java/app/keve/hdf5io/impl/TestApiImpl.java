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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import app.keve.hdf5io.Support;
import app.keve.hdf5io.TestData;
import app.keve.hdf5io.api.HDF5;
import app.keve.hdf5io.api.HDF5Dataset;
import app.keve.hdf5io.api.HDF5File;
import app.keve.hdf5io.api.HDF5FormatException;
import app.keve.hdf5io.api.HDF5Group;
import app.keve.hdf5io.api.HDF5NamedObject;
import app.keve.hdf5io.api.datatype.HDF5Array;
import app.keve.hdf5io.api.datatype.HDF5Datatype;
import app.keve.hdf5io.fileformat.H5Resolver;

public final class TestApiImpl {
    private static final String SIMPLE2_H5 = "simple2.h5";
    private static final String DOTYAML = ".yaml";
    private final HDF5 hdf5 = new HDF5Implementation();

    @Disabled("Mem HDF is not a priority")
    @Test
    public void testBuild() throws IOException {
        final HDF5File hdf5File = hdf5.builder().build();
        System.err.println(hdf5File);
    }

    @Test
    public void testBuildExisting() throws IOException {
        final Path path = Path.of("simple.h5");
        final HDF5File hdf5File = hdf5.builder().withBacking(path, StandardOpenOption.READ).build();
        System.err.println(hdf5File.getRootGroup());
        assertEquals(0, hdf5File.getRootGroup().getLinks().size());
    }

    @Test
    public void testBuildExistingFailPreamble() throws IOException {
        assertThrows(HDF5FormatException.class, () -> {
            final Path path = Path.of("simple.h5");
            final HDF5File hdf5File = hdf5.builder()
                    .withBacking(path, StandardOpenOption.READ, StandardOpenOption.WRITE)
                    .withPreamble(ByteBuffer.wrap("Hello world!".getBytes())).build();
            System.err.println(hdf5File.getRootGroup());
            assertEquals(0, hdf5File.getRootGroup().getLinks().size());
        });
    }

    @Test
    public void testCreateNew() throws Exception {
        final Path path = Path.of(SIMPLE2_H5);
        try (HDF5File hdf5File = hdf5.builder().withBacking(path, StandardOpenOption.READ, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).build()) {
            System.err.println(hdf5File.getRootGroup());
            assertEquals(0, hdf5File.getPreambleSize());
            assertEquals(0, hdf5File.getRootGroup().getLinks().size());
        }
        try (HDF5File hdf5File = hdf5.open(path, StandardOpenOption.READ)) {
            assertEquals(0, hdf5File.getPreambleSize());
            assertEquals(0, hdf5File.getRootGroup().getLinks().size());
        }
    }

    @Test
    public void testCreateNewWithPreamble() throws Exception {
        final Path path = Path.of(SIMPLE2_H5);
        try (HDF5File hdf5File = hdf5.builder()
                .withBacking(path, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING)
                .withPreamble(ByteBuffer.wrap("Hello world!".getBytes())).build()) {
            System.err.println(hdf5File.getRootGroup());
            assertEquals(512, hdf5File.getPreambleSize());
            assertEquals(0, hdf5File.getRootGroup().getLinks().size());
        }
        try (HDF5File hdf5File = hdf5.open(path, StandardOpenOption.READ)) {
            assertEquals(512, hdf5File.getPreambleSize());
            assertEquals(0, hdf5File.getRootGroup().getLinks().size());
        }
    }

    @Test
    public void testCreateNewWithSubGroup() throws Exception {
        final Path path = Path.of(SIMPLE2_H5);
        try (HDF5File hdf5File = hdf5.builder().withBacking(path, StandardOpenOption.READ, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).build()) {

            final HDF5Group rootGroup = hdf5File.getRootGroup();
            System.err.println(rootGroup);

            final HDF5Group group1 = rootGroup.addGroup("group1");

            assertEquals(0, hdf5File.getPreambleSize());
            assertEquals(1, hdf5File.getRootGroup().getLinks().size());

            System.err.println(hdf5File.getRootGroup().getLinks());

            final HDF5NamedObject group1Alt = rootGroup.resolve("group1");
            assertEquals(group1, group1Alt);
        }
        try (HDF5File hdf5File = hdf5.open(path, StandardOpenOption.READ)) {
            final Yaml yaml = Support.newYaml((H5Resolver) hdf5File);
            yaml.dump(((LocalHDF5File) hdf5File).getSuperblock(),
                    Files.newBufferedWriter(Path.of(path.toString() + DOTYAML)));
            assertEquals(0, hdf5File.getPreambleSize());
            assertEquals(1, hdf5File.getRootGroup().getLinks().size());

            final HDF5NamedObject group1 = hdf5File.getRootGroup().resolve("group1");
            assertNotNull(group1);
        }
    }

    @Test
    public void testCreateNewWithLinks() throws Exception {
        final Path path = Path.of(SIMPLE2_H5);
        try (HDF5File hdf5File = hdf5.builder().withBacking(path, StandardOpenOption.READ, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).build()) {

            final HDF5Group rootGroup = hdf5File.getRootGroup();
            final HDF5Group simple = rootGroup.addGroup("simple");
            final HDF5Group first = simple.addGroup("first");
            first.addGroup("firstChild");
            simple.addGroup("second");
            simple.addGroup("third");

            simple.addLink("firstL", first);
            rootGroup.addLink("simpleSoft", "simple/second");

//            assertEquals(0, hdf5File.getPreambleSize());
//            assertEquals(1, hdf5File.getRootGroup().getLinks().size());

            System.err.println(hdf5File.getRootGroup().getLinks());

//            HDF5NamedObject group1Alt = rootGroup.resolve("group1");
//            assertEquals(group1, group1Alt);
        }
        try (HDF5File hdf5File = hdf5.open(path, StandardOpenOption.READ)) {
            final Yaml yaml = Support.newYaml((H5Resolver) hdf5File);
            yaml.dump(((LocalHDF5File) hdf5File).getSuperblock(),
                    Files.newBufferedWriter(Path.of(path.toString() + DOTYAML)));
//            assertEquals(0, hdf5File.getPreambleSize());
//            assertEquals(1, hdf5File.getRootGroup().getLinks().size());
//
//            HDF5NamedObject group1 = hdf5File.getRootGroup().resolve("group1");
//            assertNotNull(group1);
        }
    }

    public enum MyEnum {
        MERCURY, VENUS, EARTH, MARS, JUPITER, SATURN, URANUS, NEPTUNE
    }

    @Test
    public void testNamedDatatype() throws Exception {
        final Path path = Path.of(SIMPLE2_H5);
        try (HDF5File hdf5File = hdf5.builder().withBacking(path, StandardOpenOption.READ, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).build()) {

            final HDF5Group rootGroup = hdf5File.getRootGroup();

            final HDF5Datatype myEnumType = hdf5File.getDatatypeBuilder().forType(MyEnum.class).build();
            final HDF5Array myEnumArrayType = hdf5File.getDatatypeBuilder().forArray().withBaseType(myEnumType)
                    .withDimensionSizes(5, 8, 13, 21).build();
            rootGroup.addNamedDatatype("myEnum", myEnumType);
            rootGroup.addNamedDatatype("myEnumArray", myEnumArrayType);

            assertEquals(2, hdf5File.getRootGroup().getLinks().size());

            final HDF5Datatype myEnumType2 = rootGroup.resolve("myEnum").asNamedDatatype().getDatatype();
            assertEquals(myEnumType, myEnumType2);
            final HDF5Datatype myEnumArrayType2 = rootGroup.resolve("myEnumArray").asNamedDatatype().getDatatype();
            assertEquals(myEnumArrayType, myEnumArrayType2);

        }
        try (HDF5File hdf5File = hdf5.open(path, StandardOpenOption.READ)) {
            final Yaml yaml = Support.newYaml((H5Resolver) hdf5File);
            yaml.dump(((LocalHDF5File) hdf5File).getSuperblock(),
                    Files.newBufferedWriter(Path.of(path.toString() + DOTYAML)));

            final HDF5Group rootGroup = hdf5File.getRootGroup();

            assertEquals(2, rootGroup.getLinks().size());

            final HDF5Datatype myEnumType = hdf5File.getDatatypeBuilder().forType(MyEnum.class).build();
            final HDF5Array myEnumArrayType = hdf5File.getDatatypeBuilder().forArray().withBaseType(myEnumType)
                    .withDimensionSizes(5, 8, 13, 21).build();
            final HDF5Datatype myEnumType2 = rootGroup.resolve("myEnum").asNamedDatatype().getDatatype();
            assertEquals(myEnumType, myEnumType2);
            final HDF5Datatype myEnumArrayType2 = rootGroup.resolve("myEnumArray").asNamedDatatype().getDatatype();
            assertEquals(myEnumArrayType, myEnumArrayType2);
        }
    }

    @Test
    public void testDataset() throws Exception {
        final Path path = Path.of(SIMPLE2_H5);
        try (HDF5File hdf5File = hdf5.builder().withBacking(path, StandardOpenOption.READ, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).build()) {

            final HDF5Group rootGroup = hdf5File.getRootGroup();
            final HDF5Dataset simple1D = rootGroup.addDataset("simple1D").forData(TestData.DOUBLE_DATA_SMALL).build();
            assertNotNull(simple1D);
            final HDF5Dataset simple2D = rootGroup.addDataset("simple2D").forData(TestData.DOUBLE_DATA2_SMALL).build();
            assertNotNull(simple2D);

            final HDF5Dataset simpleScalar = rootGroup.addDataset("simpleScalar").forData(TestData.DOUBLE_DATA_SCALAR)
                    .build();
            assertNotNull(simpleScalar);
        }
        try (HDF5File hdf5File = hdf5.open(path, StandardOpenOption.READ)) {
            final Yaml yaml = Support.newYaml((H5Resolver) hdf5File);
            yaml.dump(((LocalHDF5File) hdf5File).getSuperblock(),
                    Files.newBufferedWriter(Path.of(path.toString() + DOTYAML)));
            final HDF5Group rootGroup = hdf5File.getRootGroup();
            assertEquals(3, rootGroup.getLinks().size());

            final HDF5Dataset simpleScalar = rootGroup.resolve("simpleScalar").asDataset();
            assertNotNull(simpleScalar);
            Object data = simpleScalar.getAsObject();
            assertEquals(TestData.DOUBLE_DATA_SCALAR, data);

            final HDF5Dataset simple1D = rootGroup.resolve("simple1D").asDataset();
            assertNotNull(simple1D);
            data = simple1D.getAsObject();
            assertArrayEquals(TestData.DOUBLE_DATA_SMALL, (double[]) data);
        }
    }
}
