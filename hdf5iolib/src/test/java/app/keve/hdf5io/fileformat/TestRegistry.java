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
package app.keve.hdf5io.fileformat;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import app.keve.hdf5io.fileformat.H5Registry.H5ObjectInfo;
import app.keve.hdf5io.fileformat.level2message.H5Message;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

public final class TestRegistry {
    private static final String CL_FOR = ") for ";
    private final H5Registry registry = H5Registry.ofDefault();

    @ParameterizedTest(name = "{0}")
    @MethodSource("h5ObjectInterfaces")
    public void testClass(final String h5ObjectName, final ClassInfo h5ObjectClassInfo) throws ClassNotFoundException {
        final Set<String> skipNames = Set.of(H5ObjectW.class.getName(), H5Object.class.getName(),
                H5Message.class.getName());
        final String h5ObjectClassName = h5ObjectClassInfo.getName();

        if (skipNames.contains(h5ObjectClassName)) {
            return; // skip
        }

        if (h5ObjectClassName.contains("level2datatype")) {
            return; // TODO: skip for now
        }
        System.err.println(h5ObjectClassName);
        @SuppressWarnings({"unchecked", "rawtypes"})
        final H5ObjectInfo info = registry.info((Class<H5Object>) Class.forName(h5ObjectClassInfo.getName()));
        assertNotNull(info, "info unavailable for " + h5ObjectName);
//        final FieldInfo sizeField = h5ObjectClassInfo.getFieldInfo("SIZE");
//        final FieldInfo minSizeField = h5ObjectClassInfo.getFieldInfo("MIN_SIZE");
//        final FieldInfo minSizeAllField = h5ObjectClassInfo.getFieldInfo("MIN_SIZE_ALL");
//        final FieldInfo maxSizeField = h5ObjectClassInfo.getFieldInfo("MAX_SIZE");
//        final FieldInfo maxSizeAllField = h5ObjectClassInfo.getFieldInfo("MAX_SIZE_ALL");
//        assertTrue(null != sizeField || null != minSizeField && null != maxSizeField
//                || null != minSizeAllField && null != maxSizeAllField);
    }

    @ParameterizedTest
    @MethodSource("registry")
    public <T extends H5Object<S>, S extends H5Context> void testSize(final Class<T> tClass, final H5ObjectInfo info) {
        System.err.println(info);
        final SizingContext sizingContext = SizingContext.of(null, 8, 8);
        final long minSize = info.minSize(sizingContext);
        final long maxSize = info.maxSize(sizingContext);
        System.err.format("size: %d -- %d\n", minSize, maxSize);

        assertTrue(minSize >= 0, "invalid negative minSize (" + minSize + CL_FOR + tClass.getName());
        assertTrue(minSize < Long.MAX_VALUE, "invalid minSize (" + minSize + CL_FOR + tClass.getName());

        assertTrue(maxSize >= 0, "invalid negative maxSize (" + maxSize + CL_FOR + tClass.getName());
        assertTrue(maxSize >= minSize, "invalid maxSize (" + maxSize + CL_FOR + tClass.getName());
    }

    @Test
    public void testMaxLength() {
        for (int i = 0; i < H5Object.MAX_LENGTH.length; i++) {
            System.out.format("%d - %d\n", i, H5Object.MAX_LENGTH[i]);
        }
    }

    public static Stream<Arguments> h5ObjectInterfaces() {
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages("app.keve.hdf5io").scan()) {
            final ClassInfoList h5ObjectClasses = scanResult.getClassesImplementing(H5Object.class.getName());
            return h5ObjectClasses.stream().filter(ClassInfo::isInterface)
                    .sorted(Comparator.comparing(ClassInfo::getSimpleName))
                    .map(e -> Arguments.of(e.getSimpleName(), e));
        }
    }

    public static Stream<Arguments> registry() {
        final H5Registry registry = H5Registry.ofDefault();
        return registry.allInfo().entrySet().stream().map(e -> Arguments.of(e.getKey(), e.getValue()));
    }

}
