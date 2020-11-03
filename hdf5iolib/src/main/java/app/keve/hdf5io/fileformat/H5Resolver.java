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

public interface H5Resolver {
    <T extends H5Object<S>, S extends H5Context> T resolve(long address, long length, Class<T> tClass, S sc);

    ByteBuffer resolve(long address, int size);

    <T> Resolvable<T> commit(Resolvable<T> resolvable) throws IOException;

    void commitAll() throws IOException;

    /**
     * Return the current end of file address.
     * @deprecated avoid
     * @return the end of file address
     * @throws IOException if an i/o error occurs.
     */
    @Deprecated
    long eof() throws IOException;

}
