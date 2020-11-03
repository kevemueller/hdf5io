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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.BiFunction;

import app.keve.hdf5io.api.HDF5DatatypeAdapter;
import app.keve.hdf5io.api.datatype.HDF5Datatype;
import app.keve.hdf5io.fileformat.level1.FractalHeap;
import app.keve.hdf5io.fileformat.level1.LocalHeap;

public interface H5Factory {
    // information about classes
    <T extends H5Object<S>, S extends H5Context> BiFunction<ByteBuffer, S, T> of(Class<T> tClass);

    <T extends H5Object<S>, S extends H5Context> Class<T> messageClass(int messageTypeNum);

    // allocate on the java heap
    /**
     * Allocate a buffer on the Java heap.
     * 
     * @param size the size of the buffer
     * @return the buffer's resolvable
     */
    Resolvable<ByteBuffer> allocate(int size);

    /**
     * Allocate an instance of provided class using provided sizingContext on the
     * Java heap.
     * 
     * @param <T>           the instance type
     * @param <S>           the H5Context type
     * @param tClass        the instance's class
     * @param sizingContext the context
     * @return the instance's resolvable
     * @throws IOException if an I/O error occurs
     */
    <T extends H5ObjectW<S>, S extends H5Context> Resolvable<T> allocate(Class<T> tClass, S sizingContext)
            throws IOException;

    <T extends H5ObjectW<S>, S extends H5Context> T allocateLocal(Class<T> tClass, S sizingContext) throws IOException;

    void markDirty(AbstractManager manager);

    // datatype creation support
    HDF5Datatype.DatatypeBuilder datatypeBuilder(H5Context context);

    HDF5DatatypeAdapter datatypeAdapter(HDF5Datatype datatype, SizingContext sizingContext);

    // resolvables from address

    <T extends H5Object<S>, S extends H5Context> Resolvable<T> resolvable(long address, long length, Class<T> tClass,
            S sizingContext);

    Resolvable<ByteBuffer> resolvable(long address, int size);

    // resolvables from other resolvables

    Resolvable<String> resolvable(Resolvable<? extends LocalHeap> heap, long stringOffset);

    <T extends H5Object<S>, S extends H5Context> Resolvable<T> resolvable(Resolvable<FractalHeap> fractalHeap,
            ByteBuffer embeddedData, Class<T> tClass, S sizingContext);

}
