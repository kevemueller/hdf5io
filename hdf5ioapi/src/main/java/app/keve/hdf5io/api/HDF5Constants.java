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

/**
 * Common HDF5 related constants and enumerations.
 * 
 * @author keve
 *
 */
public final class HDF5Constants {
    private HDF5Constants() {
    }

    /**
     * HDF5 library profiles.
     * 
     * @author keve
     *
     */
    public enum Profile {
        HDFv1_4, HDFv1_6_0, HDFv1_6_1, HDFv1_6_2, HDFv1_6_3, HDFv1_8, HDFv1_10_0, HDFv1_10_1
    }

    /**
     * Member mappings for the multi driver.
     * 
     * @author keve
     *
     */
    public enum MemberMapping {
        SUPERBLOCK_DATA('s'), BTREE_DATA('b'), RAW_DATA('r'), GLOBAL_HEAP_DATA('g'), LOCAL_HEAP_DATA('l'),
        OBJECT_HEADER_DATA('o');

        private final char defaultSuffix;

        MemberMapping(final char defaultSuffix) {
            this.defaultSuffix = defaultSuffix;
        }

        public String getDefaultName() {
            return "%s-" + defaultSuffix + ".h5";
        }

        public static MemberMapping of(final int idx) {
            return values()[idx - 1];
        }

    }
}
