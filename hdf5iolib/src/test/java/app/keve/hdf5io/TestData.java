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

import java.util.Spliterator;
import java.util.Spliterator.OfDouble;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public final class TestData {
    public static final double DOUBLE_DATA_SCALAR = Math.E * Math.PI;
    public static final double[] DOUBLE_DATA_SMALL = new double[24 * 15 * 15];
    public static final double[] DOUBLE_DATA_LARGE = new double[24 * 60 * 60];
    public static final double[][] DOUBLE_DATA2_SMALL = new double[3][24 * 15 * 5];
    public static final double[][] DOUBLE_DATA2_LARGE = new double[5][24 * 60 * 12];

    private TestData() {
    }

    static {
        for (int i = 0; i < DOUBLE_DATA_SMALL.length; i++) {
            DOUBLE_DATA_SMALL[i] = i / 100.0;
        }
        for (int i = 0; i < DOUBLE_DATA_LARGE.length; i++) {
            DOUBLE_DATA_LARGE[i] = i / 4.2;
        }
        for (int i = 0; i < DOUBLE_DATA2_SMALL.length; i++) {
            final int sign = 0 == i % 2 ? 1 : -1;
            for (int j = 0; j < DOUBLE_DATA2_SMALL[i].length; j++) {
                DOUBLE_DATA2_SMALL[i][j] = sign * (i + 1) * (j + 1) / 10.0;
            }
        }
        for (int i = 0; i < DOUBLE_DATA2_LARGE.length; i++) {
            final int sign = 0 == i % 2 ? 1 : -1;
            for (int j = 0; j < DOUBLE_DATA2_LARGE[i].length; j++) {
                DOUBLE_DATA2_LARGE[i][j] = sign * (i + 1) * (j + 1) / 3.2;
            }
        }
    }

    public static Object[] boxed(final double[] array) {
        final OfDouble ds = Spliterators.spliterator(array, Spliterator.ORDERED);
        return StreamSupport.doubleStream(ds, false).boxed().toArray();
    }

}
