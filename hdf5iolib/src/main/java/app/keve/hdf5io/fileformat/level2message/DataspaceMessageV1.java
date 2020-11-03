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
package app.keve.hdf5io.fileformat.level2message;

import java.nio.ByteBuffer;

import app.keve.hdf5io.fileformat.H5ObjectW;
import app.keve.hdf5io.fileformat.SizingContext;

public interface DataspaceMessageV1 extends DataspaceMessage, H5ObjectW<SizingContext> {
    int PERMUTATION_INDEX_PRESENT_MASK = 0x02;

    default boolean isPermutationIndexPresent() {
        return getFlag(getFlags(), PERMUTATION_INDEX_PRESENT_MASK);
    }

    default void setPermutationIndexPresent(final boolean value) {
        setFlags(setFlag(getFlags(), PERMUTATION_INDEX_PRESENT_MASK, value));
    }

    long[] getPermutationIndex();

    void setPermutationIndex(long... value);

    static DataspaceMessageV1 of(final ByteBuffer buf, final SizingContext sizingContext) {
        switch (buf.get(0)) {
        case 1:
            return new DataspaceMessageV1BB(buf, sizingContext);
        default:
            throw new IllegalArgumentException("Implement DataspaceMessage version " + buf.get(0));
        }
    }

}
