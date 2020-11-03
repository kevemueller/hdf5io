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

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Ancestor for accessible data (datasets, chunks, et. al.)
 * 
 * @author keve
 *
 */
public interface HDF5Data {
    /**
     * Get the dataset (sub-)dimensions in the most appropriate way as a whole.
     * Result can be a scalar or a multi-dimensional Java array on the heap.
     * 
     * @param dim the (sub-)dimensions
     * @return the data
     */
    Object getAsObject(long... dim);

    /**
     * Slice continuous (sub-)dimensions out of the dataset and return them
     * flattened as a ShortBuffer, provided the underlying storage and the datatype
     * supports this.
     * 
     * @param dim the dimensions to lock, if any.
     * @return the buffer or null.
     */
    ShortBuffer getAsShortBuffer(long... dim);

    IntBuffer getAsIntBuffer(long... dim);

    LongBuffer getAsLongBuffer(long... dim);

    /**
     * Slice continuous (sub-)dimensions out of the dataset and return them
     * flattened as an IntStream, provided the underlying storage and the datatype
     * supports this.
     * 
     * @param dim the dimensions to lock, if any.
     * @return the buffer or null.
     */
    IntStream getAsIntStream(long... dim);

    LongStream getAsLongStream(long... dim);

    /**
     * Stream the (sub-)dimensions from the dataset flattened.
     * 
     * @param dim the dimensions to lock, if any.
     * @return the flattened stream of values
     */
    Stream<?> getAsStream(long... dim);

    // TODO: setters
}
