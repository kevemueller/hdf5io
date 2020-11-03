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

import java.nio.ByteBuffer;

public interface H5Object<T extends H5Context> {

    int UINT16_MAX_VALUE = 0xFFFF;
    long UINT32_MAX_VALUE = 0xFFFF_FFFFL;

    // biggest unsigned value that can be stored on the number of bytes
    // the array does not contain a value for 8 bytes, as calculating with
    // Long.MAX_VALUE will give you unexpected results.
    long[] MAX_LENGTH = {0, (1 << 8) - 1, (1L << 2 * 8) - 1, (1L << 3 * 8) - 1, (1L << 4 * 8) - 1, (1L << 5 * 8) - 1,
            (1L << 6 * 8) - 1, (1L << 7 * 8) - 1};
    int MIN_DIMENSIONS = 1;
    int MAX_DIMENSIONS = 32;
    int MAX_MESSAGE_DATA = UINT16_MAX_VALUE;

    @Deprecated
    ByteBuffer getBuffer();

    T context();

    long size();

    default boolean getFlag(final int flags, final int mask) {
        return (flags & mask) > 0;
    }

    default int setFlag(final int flags, final int mask, final boolean value) {
        return flags & ~mask | (value ? mask : 0);
    }
}
