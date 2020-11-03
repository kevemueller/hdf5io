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
package app.keve.hdf5io.api.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public final class TestArrayUtil {
    private static final String HASH = "#";
    private static final double D = Math.PI;
    private static final double[] D0 = new double[0];
    private static final double[] D1 = new double[5];
    private static final double[][] D2 = new double[5][8];
    private static final double[][][] D3 = new double[5][8][13];
    private static final Object O0 = new Object[0];
    private static final Object[] O1 = new Object[5];
    private static final Object[][] O2 = new Object[5][8];
    private static final Object[][][] O3 = new Object[5][8][13];

    static {
        for (int i = 0; i < D1.length; i++) {
            D1[i] = i;
            O1[i] = HASH + D1[i];
        }
        for (int i = 0; i < D2.length; i++) {
            for (int j = 0; j < D2[i].length; j++) {
                D2[i][j] = i * D2[i].length + j;
                O2[i][j] = HASH + D2[i][j];
            }
        }
        for (int i = 0; i < D3.length; i++) {
            for (int j = 0; j < D3[i].length; j++) {
                for (int k = 0; k < D3[i][j].length; k++) {
                    D3[i][j][k] = i * D3[i].length * D3[i][j].length + j * D3[i][j].length + k;
                    O3[i][j][k] = HASH + D3[i][j][k];
                }
            }
        }

    }

    @Test
    public void testSizeScalar() {
        assertEquals(Double.class, ArrayUtil.data2Type(D));
        assertArrayEquals(new int[0], ArrayUtil.data2DimensionsSimple(D));
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("allD")
    public void testSizeD(final Object o, final int[] expectedDim) {
        assertEquals(Double.TYPE, ArrayUtil.data2Type(o));
        assertEquals(expectedDim.length, ArrayUtil.rank(o));
        assertArrayEquals(expectedDim, ArrayUtil.data2DimensionsSimple(o));
        assertArrayEquals(expectedDim, ArrayUtil.data2MinDimensions(o));
        assertArrayEquals(expectedDim, ArrayUtil.data2MaxDimensions(o));

        DoubleBuffer buffer = DoubleBuffer.allocate(ArrayUtil.size(expectedDim));
        buffer = ArrayUtil.mdRowMajorBuffer(buffer, o).flip();
        assertEquals(buffer.remaining(), ArrayUtil.size(expectedDim));
        final Object bufO = ArrayUtil.bufferToMd(buffer, expectedDim);
        assertTrue(ArrayUtil.deepEquals(o, bufO));
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("allO")
    public void testSizeO(final Object o, final int[] expectedDim) {
        assertEquals(Object.class, ArrayUtil.data2Type(o));
        assertEquals(expectedDim.length, ArrayUtil.rank(o));
        assertArrayEquals(expectedDim, ArrayUtil.data2DimensionsSimple(o));
        assertArrayEquals(expectedDim, ArrayUtil.data2MinDimensions(o));
        assertArrayEquals(expectedDim, ArrayUtil.data2MaxDimensions(o));
    }

    @Test
    public void testSizeMixed() {
        final double[][] dX = new double[3][];
        for (int i = 0; i < dX.length; i++) {
            dX[i] = new double[(i + 1) * 5];
        }
        assertEquals(2, ArrayUtil.rank(dX));
        assertEquals(Double.TYPE, ArrayUtil.data2Type(dX));
        assertArrayEquals(new int[] {3, 5}, ArrayUtil.data2DimensionsSimple(dX));
        assertArrayEquals(new int[] {3, 5}, ArrayUtil.data2MinDimensions(dX));
        assertArrayEquals(new int[] {3, 15}, ArrayUtil.data2MaxDimensions(dX));
    }

    @Test
    public void testSizeMixedZero() {
        final double[][][] dX = new double[3][][];
        for (int i = 0; i < dX.length; i++) {
            dX[i] = new double[(i + 1) * 5][i * 3];
        }

        assertEquals(3, ArrayUtil.rank(dX));
        assertEquals(Double.TYPE, ArrayUtil.data2Type(dX));
        assertArrayEquals(new int[] {3, 5, 0}, ArrayUtil.data2DimensionsSimple(dX));
        assertArrayEquals(new int[] {3, 5, 0}, ArrayUtil.data2MinDimensions(dX));
        assertArrayEquals(new int[] {3, 15, 6}, ArrayUtil.data2MaxDimensions(dX));
    }

    @Test
    public void testSizeMixedZero2() {
        final double[][][] dX = new double[3][][];
        for (int i = 0; i < dX.length; i++) {
            dX[i] = new double[Math.abs(i - 1) * 5][i * 3];
        }
        assertEquals(3, ArrayUtil.rank(dX));
        assertEquals(Double.TYPE, ArrayUtil.data2Type(dX));
        assertArrayEquals(new int[] {3, 5, 0}, ArrayUtil.data2DimensionsSimple(dX));
        assertArrayEquals(new int[] {3, 0, 0}, ArrayUtil.data2MinDimensions(dX));
        assertArrayEquals(new int[] {3, 5, 6}, ArrayUtil.data2MaxDimensions(dX));
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("allD")
    public void testRowMajorD(final Object o, final int[] expectedDim) {
        final List<Double> expectedList = new ArrayList<>();
        IntStream.range(0, ArrayUtil.size(expectedDim)).asDoubleStream().forEach(expectedList::add);

        final List<Double> dlist = new ArrayList<>();
        ArrayUtil.mdDoubleRowMajorIterator(o).forEachRemaining((Consumer<? super Double>) dlist::add);
        assertEquals(ArrayUtil.size(expectedDim), dlist.size());
        assertEquals(expectedList, dlist);

        final Object convO = ArrayUtil.rowMajorIteratorToMd(ArrayUtil.mdDoubleRowMajorIterator(o), expectedDim);
        assertTrue(ArrayUtil.deepEquals(o, convO));

        System.out.println(dlist);
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("allO")
    public void testRowMajorO(final Object o, final int[] expectedDim) {
        final List<Object> expectedList = new ArrayList<>();
        IntStream.range(0, ArrayUtil.size(expectedDim)).mapToObj(i -> HASH + (double) i).forEach(expectedList::add);

        final List<Object> ilist = new ArrayList<>();
        ArrayUtil.mdObjectRowMajorIterator(o).forEachRemaining(ilist::add);
        assertEquals(ArrayUtil.size(expectedDim), ilist.size());
        assertEquals(expectedList, ilist);

        final Object convO = ArrayUtil.rowMajorIteratorToMd(Object.class, ArrayUtil.mdObjectRowMajorIterator(o),
                expectedDim);
        assertTrue(ArrayUtil.deepEquals(o, convO));

        System.out.println(ilist);
    }

    @Test
    public void testRowMajorJagged() {
        final double[][][] dX = new double[3][][];
        for (int i = 0; i < dX.length; i++) {
            dX[i] = new double[Math.abs(i - 1) * 5][(i + 1) * 3];
        }
        int idx = 0;
        for (int i = 0; i < dX.length; i++) {
            for (int j = 0; j < dX[i].length; j++) {
                for (int k = 0; k < dX[i][j].length; k++) {
                    dX[i][j][k] = idx++;
                }
            }
        }
        final List<Double> expectedList = new ArrayList<>();
        IntStream.range(0, ArrayUtil.size(idx)).asDoubleStream().forEach(expectedList::add);

        System.out.println(Arrays.deepToString(dX));
        assertEquals(Arrays.deepToString(dX), ArrayUtil.deepToString(dX));

        final List<Double> ilist = new ArrayList<>();
        ArrayUtil.mdDoubleRowMajorIterator(dX).forEachRemaining((DoubleConsumer) ilist::add);
        System.out.println(ilist);
        assertEquals(idx, ilist.size());
        assertEquals(expectedList, ilist);
    }

    @Test
    public void testCopy1() {
        final double[] dSparse = new double[D1.length * 5];
        ArrayUtil.copyTo(dSparse, D1, D1.length / 2);
        ArrayUtil.copyTo(dSparse, D1, D1.length * 3);
        assertArrayEquals(ArrayUtil.data2MinDimensions(dSparse), ArrayUtil.data2MaxDimensions(dSparse));
        int i = 0;
        while (i < D1.length / 2) {
            assertEquals(0, dSparse[i++]);
        }
        for (final double d : D1) {
            assertEquals(d, dSparse[i++]);
        }
        while (i < D1.length * 3) {
            assertEquals(0, dSparse[i++]);
        }
        for (final double d : D1) {
            assertEquals(d, dSparse[i++]);
        }
        while (i < D1.length * 5) {
            assertEquals(0, dSparse[i++]);
        }
    }

    @Test
    public void testCopy2() {
        final double[][] dSparse = new double[D2.length * 5][D2[0].length * 5];
        ArrayUtil.copyTo(dSparse, D2, D2.length / 2, D2[0].length / 2);
        ArrayUtil.copyTo(dSparse, D2, D2.length * 3, D2[0].length * 3);

//        System.out.println(Arrays.deepToString(dSparse));
        assertArrayEquals(ArrayUtil.data2MinDimensions(dSparse), ArrayUtil.data2MaxDimensions(dSparse));
        // TODO: proper assert
    }

    public static Stream<Object> allD() {
        return Stream.of(Arguments.of(D0, new int[] {0}), Arguments.of(D1, new int[] {D1.length}),
                Arguments.of(D2, new int[] {D2.length, D2[0].length}),
                Arguments.of(D3, new int[] {D3.length, D3[0].length, D3[0][0].length}));
    }

    public static Stream<Object> allO() {
        return Stream.of(Arguments.of(O0, new int[] {0}), Arguments.of(O1, new int[] {O1.length}),
                Arguments.of(O2, new int[] {O2.length, O2[0].length}),
                Arguments.of(O3, new int[] {O3.length, O3[0].length, O3[0][0].length}));
    }
}
