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

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

/**
 * Utility functions for working with generic multi-dimensional Java arrays.
 * 
 * <p>
 * <b>Information</b>
 * <ul>
 * <li>{@link #size(int...)} size (number of objects)</li>
 * <li>{@link #rank(Object)} rank (number of dimensions)</li>
 * <li>{@link #data2Type(Object)} component type</li>
 * <li>{@link #copyTo(Object, int[], Object)} copy md array tile onto target md
 * array</li>
 * <li>{@link #data2DimensionsSimple(Object)} dimensions based on first
 * element</li>
 * <li>{@link #data2MinDimensions(Object)} minimal dimensions based on all
 * elements</li>
 * <li>{@link #data2MaxDimensions(Object)} maximal dimensions based on all
 * elements</li>
 * </ul>
 * 
 * <b>Utility</b>
 * <ul>
 * <li>{@link #deepEquals(Object, Object)} see
 * {@link java.util.Arrays#deepEquals(Object[], Object[])}</li>
 * <li>{@link #deepToString(Object)} see
 * {@link java.util.Arrays#deepToString(Object[])}</li>
 * </ul>
 * 
 * <b>Transformation</b>
 * 
 * <p>
 * <i>Iterator over elements in row major order</i>
 * <ul>
 * <li>{@link #mdIntRowMajorIterator(Object)}</li>
 * <li>{@link #mdLongRowMajorIterator(Object)}</li>
 * <li>{@link #mdDoubleRowMajorIterator(Object)}</li>
 * <li>{@link #mdObjectRowMajorIterator(Object)}</li>
 * </ul>
 * 
 * <p>
 * <i>Construct multi-dimensional array from elements in row major order</i>
 * <ul>
 * <li>{@link #rowMajorIteratorToMd(java.util.PrimitiveIterator.OfInt, int...)}</li>
 * <li>{@link #rowMajorIteratorToMd(java.util.PrimitiveIterator.OfLong, int...)}</li>
 * <li>{@link #rowMajorIteratorToMd(java.util.PrimitiveIterator.OfDouble, int...)}</li>
 * <li>{@link #rowMajorIteratorToMd(Class, Iterator, int...)}</li>
 * </ul>
 * 
 * <p>
 * <i>Write a multi-dimensional array in row major order to Buffer</i>
 * <ul>
 * <li>{@link #mdRowMajorBuffer(IntBuffer, Object)}</li>
 * <li>{@link #mdRowMajorBuffer(LongBuffer, Object)}</li>
 * <li>{@link #mdRowMajorBuffer(DoubleBuffer, Object)}</li>
 * </ul>
 * <i>Read a multi-dimensional array from row major ordered Buffer</i>
 * <ul>
 * <li>{@link #bufferToMd(IntBuffer, int...)}</li>
 * <li>{@link #bufferToMd(LongBuffer, int...)}</li>
 * <li>{@link #bufferToMd(DoubleBuffer, int...)}</li>
 * </ul>
 * 
 * @author keve
 *
 */
public final class ArrayUtil {
    private ArrayUtil() {

    }

    public static int[] data2DimensionsSimple(final Object data) {
        if (data.getClass().isArray()) {
            Object array = data;
            final ArrayList<Integer> dims = new ArrayList<>();
            while (array.getClass().isArray()) {
                final int length = Array.getLength(array);
                dims.add(length);
                if (0 == length) {
                    // set all lower ranks to zero
                    for (int i = 0; i < rank(array) - 1; i++) {
                        dims.add(0);
                    }
                    break;
                }
                array = Array.get(array, 0);
            }
            return dims.stream().mapToInt(Integer::intValue).toArray();
        } else {
            return new int[0];
        }
    }

    private static void data2MinDimensions(final int[] dim, final int d, final Object data) {
        if (data.getClass().isArray()) {
            final int length = Array.getLength(data);
            dim[d] = Integer.min(dim[d], length);
            if (0 == length) {
                // set all lower ranks to zero
                for (int i = d; i < dim.length; i++) {
                    dim[i] = 0;
                }
            } else {
                for (int i = 0; i < length; i++) {
                    data2MinDimensions(dim, d + 1, Array.get(data, i));
                }
            }

        }
    }

    public static int[] data2MinDimensions(final Object data) {
        final int[] dim = new int[rank(data)];
        Arrays.fill(dim, Integer.MAX_VALUE);
        data2MinDimensions(dim, 0, data);
        return dim;
    }

    private static void data2MaxDimensions(final int[] dim, final int d, final Object data) {
        if (data.getClass().isArray()) {
            final int length = Array.getLength(data);
            dim[d] = Integer.max(dim[d], length);
            for (int i = 0; i < length; i++) {
                data2MaxDimensions(dim, d + 1, Array.get(data, i));
            }
        }
    }

    public static int[] data2MaxDimensions(final Object data) {
        final int[] dim = new int[rank(data)];
        data2MaxDimensions(dim, 0, data);
        return dim;
    }

    public static long[] data2DimensionsLong(final Object data) {
        return IntStream.of(data2DimensionsSimple(data)).asLongStream().toArray();
    }

    public static int size(final int... dim) {
        int size = 1;
        for (final int d : dim) {
            size *= d;
        }
        return size;
    }

    public static int rank(final Object data) {
        int rank = 0;
        Class<?> dType = data.getClass();
        while (dType.isArray()) {
            dType = dType.getComponentType();
            rank++;
        }
        return rank;
    }

    public static String deepToString(final Object md) {
        if (md instanceof Object[]) {
            return Arrays.deepToString((Object[]) md);
        } else if (md instanceof byte[]) {
            return Arrays.toString((byte[]) md);
        } else if (md instanceof short[]) {
            return Arrays.toString((short[]) md);
        } else if (md instanceof int[]) {
            return Arrays.toString((int[]) md);
        } else if (md instanceof long[]) {
            return Arrays.toString((long[]) md);
        } else if (md instanceof float[]) {
            return Arrays.toString((float[]) md);
        } else if (md instanceof double[]) {
            return Arrays.toString((double[]) md);
        } else if (md instanceof boolean[]) {
            return Arrays.toString((boolean[]) md);
        } else if (md instanceof char[]) {
            return Arrays.toString((char[]) md);
        } else {
            return Objects.toString(md);
        }
    }

    public static boolean deepEquals(final Object md1, final Object md2) {
        if (md1 == md2) {
            return true;
        }
        if (null == md1) {
            return false;
        }
        final boolean eq;
        if (md1 instanceof Object[] && md2 instanceof Object[]) {
            eq = Arrays.deepEquals((Object[]) md1, (Object[]) md2);
        } else if (md1 instanceof byte[] && md2 instanceof byte[]) {
            eq = Arrays.equals((byte[]) md1, (byte[]) md2);
        } else if (md1 instanceof short[] && md2 instanceof short[]) {
            eq = Arrays.equals((short[]) md1, (short[]) md2);
        } else if (md1 instanceof int[] && md2 instanceof int[]) {
            eq = Arrays.equals((int[]) md1, (int[]) md2);
        } else if (md1 instanceof long[] && md2 instanceof long[]) {
            eq = Arrays.equals((long[]) md1, (long[]) md2);
        } else if (md1 instanceof char[] && md2 instanceof char[]) {
            eq = Arrays.equals((char[]) md1, (char[]) md2);
        } else if (md1 instanceof float[] && md2 instanceof float[]) {
            eq = Arrays.equals((float[]) md1, (float[]) md2);
        } else if (md1 instanceof double[] && md2 instanceof double[]) {
            eq = Arrays.equals((double[]) md1, (double[]) md2);
        } else if (md1 instanceof boolean[] && md2 instanceof boolean[]) {
            eq = Arrays.equals((boolean[]) md1, (boolean[]) md2);
        } else {
            eq = md1.equals(md2);
        }
        return eq;
    }

    public static Type data2Type(final Object data) {
        Class<?> dType = data.getClass();
        while (dType.isArray()) {
            dType = dType.getComponentType();
        }
        return dType;
    }

    private static void copy(final Object mdTarget, final int d, final int[] ofs, final Object mdTile) {
        if (d + 1 == ofs.length) {
            System.arraycopy(mdTile, 0, mdTarget, ofs[d], Array.getLength(mdTile));
        } else {
            final int tileLength = Array.getLength(mdTile);
            for (int j = 0; j < tileLength; j++) {
                copy(Array.get(mdTarget, ofs[d] + j), d + 1, ofs, Array.get(mdTile, j));
            }
        }
    }

    /**
     * Copy a md slice onto a target of the same rank at a specific offset.
     * 
     * @param mdTarget the target multi-dimensional array.
     * @param mdTile   the source multi-dimensional array (slice).
     * @param ofs      the offset into the target array at which to copy the source
     *                 slice to.
     */
    public static void copyTo(final Object mdTarget, final Object mdTile, final int... ofs) {
        final int targetRank = rank(mdTarget);
        final int tileRank = rank(mdTile);
        assert targetRank == tileRank : "ranks differ: " + targetRank + "<>" + tileRank;
        assert ofs.length == targetRank : "offset differs: " + targetRank + "<>" + ofs.length;
        assert ofs.length > 0 : "cannot copy scalar";

        copy(mdTarget, 0, ofs, mdTile);
    }

    /* md to row major */
    /**
     * Write the multi-dimensional array of double values in row major order to the
     * provided buffer.
     * 
     * @param buffer     the buffer to write to
     * @param mdIntArray the multi-dimensional array
     * @return the buffer
     */
    public static IntBuffer mdRowMajorBuffer(final IntBuffer buffer, final Object mdIntArray) {
        if (mdIntArray instanceof int[]) {
            buffer.put((int[]) mdIntArray);
        } else {
            for (int i = 0; i < Array.getLength(mdIntArray); i++) {
                mdRowMajorBuffer(buffer, Array.get(mdIntArray, i));
            }
        }
        return buffer;
    }

    /**
     * Write the multi-dimensional array of double values in row major order to the
     * provided buffer.
     * 
     * @param buffer      the buffer to write to
     * @param mdLongArray the multi-dimensional array
     * @return the buffer
     */
    public static LongBuffer mdRowMajorBuffer(final LongBuffer buffer, final Object mdLongArray) {
        if (mdLongArray instanceof long[]) {
            buffer.put((long[]) mdLongArray);
        } else {
            for (int i = 0; i < Array.getLength(mdLongArray); i++) {
                mdRowMajorBuffer(buffer, Array.get(mdLongArray, i));
            }
        }
        return buffer;
    }

    /**
     * Write the multi-dimensional array of double values in row major order to the
     * provided buffer.
     * 
     * @param buffer        the buffer to write to
     * @param mdDoubleArray the multi-dimensional array
     * @return the buffer
     */
    public static DoubleBuffer mdRowMajorBuffer(final DoubleBuffer buffer, final Object mdDoubleArray) {
        if (mdDoubleArray instanceof double[]) {
            buffer.put((double[]) mdDoubleArray);
        } else {
            for (int i = 0; i < Array.getLength(mdDoubleArray); i++) {
                mdRowMajorBuffer(buffer, Array.get(mdDoubleArray, i));
            }
        }
        return buffer;
    }

    /* buffer to md */
    private static void bufferToMd(final Object md, final IntBuffer buffer, final int d, final int... dim) {
        if (md instanceof int[]) {
            buffer.get((int[]) md);
        } else {
            for (int i = 0; i < dim[d]; i++) {
                bufferToMd(Array.get(md, i), buffer, d + 1, dim);
            }
        }
    }

    public static Object bufferToMd(final IntBuffer buffer, final int... dim) {
        final Object md = Array.newInstance(Integer.TYPE, dim);
        bufferToMd(md, buffer, 0, dim);
        return md;
    }

    private static void bufferToMd(final Object md, final LongBuffer buffer, final int d, final int... dim) {
        if (md instanceof long[]) {
            buffer.get((long[]) md);
        } else {
            for (int i = 0; i < dim[d]; i++) {
                bufferToMd(Array.get(md, i), buffer, d + 1, dim);
            }
        }
    }

    public static Object bufferToMd(final LongBuffer buffer, final int... dim) {
        final Object md = Array.newInstance(Long.TYPE, dim);
        bufferToMd(md, buffer, 0, dim);
        return md;
    }

    private static void bufferToMd(final Object md, final DoubleBuffer buffer, final int d, final int... dim) {
        if (md instanceof double[]) {
            buffer.get((double[]) md);
        } else {
            for (int i = 0; i < dim[d]; i++) {
                bufferToMd(Array.get(md, i), buffer, d + 1, dim);
            }
        }
    }

    public static Object bufferToMd(final DoubleBuffer buffer, final int... dim) {
        final Object md = Array.newInstance(Double.TYPE, dim);
        bufferToMd(md, buffer, 0, dim);
        return md;
    }

    /* iterator to md */
    private static void rowMajorIteratorToMd(final Object md, final PrimitiveIterator.OfInt it, final int d,
            final int... dim) {
        if (d + 1 == dim.length) {
            for (int i = 0; i < dim[d]; i++) {
                Array.setInt(md, i, it.nextInt());
            }
        } else {
            for (int i = 0; i < dim[d]; i++) {
                rowMajorIteratorToMd(Array.get(md, i), it, d + 1, dim);
            }
        }
    }

    public static Object rowMajorIteratorToMd(final PrimitiveIterator.OfInt it, final int... dim) {
        final Object md = Array.newInstance(Integer.TYPE, dim);
        rowMajorIteratorToMd(md, it, 0, dim);
        return md;
    }

    private static void rowMajorIteratorToMd(final Object md, final PrimitiveIterator.OfLong it, final int d,
            final int... dim) {
        if (d + 1 == dim.length) {
            for (int i = 0; i < dim[d]; i++) {
                Array.setLong(md, i, it.nextLong());
            }
        } else {
            for (int i = 0; i < dim[d]; i++) {
                rowMajorIteratorToMd(Array.get(md, i), it, d + 1, dim);
            }
        }
    }

    public static Object rowMajorIteratorToMd(final PrimitiveIterator.OfLong it, final int... dim) {
        final Object md = Array.newInstance(Long.TYPE, dim);
        rowMajorIteratorToMd(md, it, 0, dim);
        return md;
    }

    private static void rowMajorIteratorToMd(final Object md, final PrimitiveIterator.OfDouble it, final int d,
            final int... dim) {
        if (d + 1 == dim.length) {
            for (int i = 0; i < dim[d]; i++) {
                Array.setDouble(md, i, it.nextDouble());
            }
        } else {
            for (int i = 0; i < dim[d]; i++) {
                rowMajorIteratorToMd(Array.get(md, i), it, d + 1, dim);
            }
        }
    }

    public static Object rowMajorIteratorToMd(final PrimitiveIterator.OfDouble it, final int... dim) {
        final Object md = Array.newInstance(Double.TYPE, dim);
        rowMajorIteratorToMd(md, it, 0, dim);
        return md;
    }

    private static <T> void rowMajorIteratorToMd(final Object md, final Iterator<T> it, final int d, final int... dim) {
        if (d + 1 == dim.length) {
            for (int i = 0; i < dim[d]; i++) {
                Array.set(md, i, it.next());
            }
        } else {
            for (int i = 0; i < dim[d]; i++) {
                rowMajorIteratorToMd(Array.get(md, i), it, d + 1, dim);
            }
        }
    }

    public static <T> Object rowMajorIteratorToMd(final Class<T> tClass, final Iterator<T> it, final int... dim) {
        final Object md = Array.newInstance(tClass, dim);
        rowMajorIteratorToMd(md, it, 0, dim);
        return md;
    }

    /* md to iterator */

    public static PrimitiveIterator.OfInt mdIntRowMajorIterator(final Object mdIntArray) {
        if (mdIntArray instanceof int[]) {
            // int[]
            final int[] intArray = (int[]) mdIntArray;
            return new PrimitiveIterator.OfInt() {
                private final int length = intArray.length;
                private int idx;

                @Override
                public boolean hasNext() {
                    return idx < length;
                }

                @Override
                public int nextInt() {
                    return intArray[idx++];
                }
            };
        } else {
            return new PrimitiveIterator.OfInt() {
                private final int length = null == mdIntArray ? 0 : Array.getLength(mdIntArray);
                private int idx;
                private PrimitiveIterator.OfInt currentIterator;

                @Override
                public boolean hasNext() {
                    while (true) {
                        if (null == currentIterator) {
                            if (idx >= length) {
                                return false;
                            }
                            currentIterator = mdIntRowMajorIterator(Array.get(mdIntArray, idx++));
                        }
                        if (currentIterator.hasNext()) {
                            return true;
                        }
                        currentIterator = null;
                    }
                }

                @Override
                public int nextInt() {
                    if (hasNext()) {
                        return currentIterator.nextInt();
                    } else {
                        throw new NoSuchElementException();
                    }
                }
            };
        }
    }

    public static PrimitiveIterator.OfLong mdLongRowMajorIterator(final Object mdLongArray) {
        if (mdLongArray instanceof long[]) {
            // long[]
            final long[] longArray = (long[]) mdLongArray;
            return new PrimitiveIterator.OfLong() {
                private final long length = longArray.length;
                private int idx;

                @Override
                public boolean hasNext() {
                    return idx < length;
                }

                @Override
                public long nextLong() {
                    return longArray[idx++];
                }
            };
        } else {
            return new PrimitiveIterator.OfLong() {
                private final long length = null == mdLongArray ? 0 : Array.getLength(mdLongArray);
                private int idx;
                private PrimitiveIterator.OfLong currentIterator;

                @Override
                public boolean hasNext() {
                    while (true) {
                        if (null == currentIterator) {
                            if (idx >= length) {
                                return false;
                            }
                            currentIterator = mdLongRowMajorIterator(Array.get(mdLongArray, idx++));
                        }
                        if (currentIterator.hasNext()) {
                            return true;
                        }
                        currentIterator = null;
                    }
                }

                @Override
                public long nextLong() {
                    if (hasNext()) {
                        return currentIterator.nextLong();
                    } else {
                        throw new NoSuchElementException();
                    }
                }
            };
        }
    }

    public static PrimitiveIterator.OfDouble mdDoubleRowMajorIterator(final Object mdDoubleArray) {
        if (mdDoubleArray instanceof double[]) {
            final double[] doubleArray = (double[]) mdDoubleArray;
            return new PrimitiveIterator.OfDouble() {
                private final int length = doubleArray.length;
                private int idx;

                @Override
                public boolean hasNext() {
                    return idx < length;
                }

                @Override
                public double nextDouble() {
                    return doubleArray[idx++];
                }
            };
        } else {
            return new PrimitiveIterator.OfDouble() {
                private final int length = null == mdDoubleArray ? 0 : Array.getLength(mdDoubleArray);
                private int idx;
                private PrimitiveIterator.OfDouble currentIterator;

                @Override
                public boolean hasNext() {
                    while (true) {
                        if (null == currentIterator) {
                            if (idx >= length) {
                                return false;
                            }
                            currentIterator = mdDoubleRowMajorIterator(Array.get(mdDoubleArray, idx++));
                        }
                        if (currentIterator.hasNext()) {
                            return true;
                        }
                        currentIterator = null;
                    }
                }

                @Override
                public double nextDouble() {
                    if (hasNext()) {
                        return currentIterator.nextDouble();
                    } else {
                        throw new NoSuchElementException();
                    }
                }
            };
        }
    }

    private static Iterator<Object> mdObjectRowMajorIterator(final int rank, final Object mdObjectArray) {
        if (1 == rank) {
            final Object[] objectArray = (Object[]) mdObjectArray;
            return new Iterator<>() {
                private final int length = objectArray.length;
                private int idx;

                @Override
                public boolean hasNext() {
                    return idx < length;
                }

                @Override
                public Object next() {
                    return objectArray[idx++];
                }
            };
        } else {
            return new Iterator<>() {
                private final int length = null == mdObjectArray ? 0 : Array.getLength(mdObjectArray);
                private int idx;
                private Iterator<Object> currentIterator;

                @Override
                public boolean hasNext() {
                    while (true) {
                        if (null == currentIterator) {
                            if (idx >= length) {
                                return false;
                            }
                            currentIterator = mdObjectRowMajorIterator(Array.get(mdObjectArray, idx++));
                        }
                        if (currentIterator.hasNext()) {
                            return true;
                        }
                        currentIterator = null;
                    }
                }

                @Override
                public Object next() {
                    if (hasNext()) {
                        return currentIterator.next();
                    } else {
                        throw new NoSuchElementException();
                    }
                }
            };
        }
    }

    public static Iterator<Object> mdObjectRowMajorIterator(final Object mdObjectArray) {
        return mdObjectRowMajorIterator(rank(mdObjectArray), mdObjectArray);
    }

}
