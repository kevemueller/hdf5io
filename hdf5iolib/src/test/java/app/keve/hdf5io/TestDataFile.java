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

import java.util.LinkedHashMap;
import java.util.Map;

import app.keve.hdf5io.api.util.ArrayUtil;

public final class TestDataFile {
    public static final Map<String, Object> DOUBLE_ENTRIES;

    static {
        DOUBLE_ENTRIES = new LinkedHashMap<>();
        /* 0D */
        DOUBLE_ENTRIES.put("data.scalar", TestData.DOUBLE_DATA_SCALAR);
        /* 1D */
        DOUBLE_ENTRIES.put("dataSmall.contiguous", TestData.DOUBLE_DATA_SMALL);
        DOUBLE_ENTRIES.put("dataSmall.chunked", TestData.DOUBLE_DATA_SMALL);
        DOUBLE_ENTRIES.put("dataSmall.compact", TestData.DOUBLE_DATA_SMALL);
        DOUBLE_ENTRIES.put("dataSmall.deflate", TestData.DOUBLE_DATA_SMALL);
        DOUBLE_ENTRIES.put("dataLarge.contiguous", TestData.DOUBLE_DATA_LARGE);
        DOUBLE_ENTRIES.put("dataLarge.chunked", TestData.DOUBLE_DATA_LARGE);
        DOUBLE_ENTRIES.put("dataLarge.deflate", TestData.DOUBLE_DATA_LARGE);
        // this is the expanded version, the file is written without the sparse sections
        final double[] doubleSparse = new double[4 * TestData.DOUBLE_DATA_SMALL.length];
        System.arraycopy(TestData.DOUBLE_DATA_SMALL, 0, doubleSparse, 0, TestData.DOUBLE_DATA_SMALL.length);
        System.arraycopy(TestData.DOUBLE_DATA_SMALL, 0, doubleSparse, 3 * TestData.DOUBLE_DATA_SMALL.length,
                TestData.DOUBLE_DATA_SMALL.length);
        DOUBLE_ENTRIES.put("dataLarge.sparse", doubleSparse);

        /* 2D */
        DOUBLE_ENTRIES.put("data2Small.contiguous", TestData.DOUBLE_DATA2_SMALL);
        DOUBLE_ENTRIES.put("data2Small.chunked", TestData.DOUBLE_DATA2_SMALL);
        DOUBLE_ENTRIES.put("data2Small.compact", TestData.DOUBLE_DATA2_SMALL);
        DOUBLE_ENTRIES.put("data2Small.deflate", TestData.DOUBLE_DATA2_SMALL);
        DOUBLE_ENTRIES.put("data2Large.contiguous", TestData.DOUBLE_DATA2_LARGE);
        DOUBLE_ENTRIES.put("data2Large.chunked", TestData.DOUBLE_DATA2_LARGE);
        DOUBLE_ENTRIES.put("data2Large.deflate", TestData.DOUBLE_DATA2_LARGE);

        // this is the expanded version, the file is written without the sparse sections
        final double[][] double2Sparse = new double[4 * TestData.DOUBLE_DATA2_SMALL.length][5
                * TestData.DOUBLE_DATA2_SMALL[0].length];
        ArrayUtil.copyTo(double2Sparse, TestData.DOUBLE_DATA2_SMALL, 0, 0);
        ArrayUtil.copyTo(double2Sparse, TestData.DOUBLE_DATA2_SMALL, TestData.DOUBLE_DATA2_SMALL.length,
                TestData.DOUBLE_DATA2_SMALL[0].length);
        DOUBLE_ENTRIES.put("data2Large.sparse", double2Sparse);
    }

    private TestDataFile() {
    }
}
