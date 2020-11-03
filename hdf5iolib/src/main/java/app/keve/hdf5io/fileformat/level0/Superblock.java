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
package app.keve.hdf5io.fileformat.level0;

import java.nio.ByteBuffer;
import java.util.Arrays;

import app.keve.hdf5io.fileformat.H5ObjectW;
import app.keve.hdf5io.fileformat.SizingContext;

public interface Superblock extends H5ObjectW<SizingContext> {
    byte[] SIGNATURE = {(byte) 137, 'H', 'D', 'F', '\r', '\n', 26, '\n'};

    static long minSize(final SizingContext sc) {
        return Long.min(SuperblockV0BB.size(sc),
                Long.min(SuperblockV1BB.size(sc), Long.min(SuperblockV2BB.size(sc), SuperblockV3BB.size(sc))));
    }

    static long maxSize(final SizingContext sc) {
        return Long.max(SuperblockV0BB.size(sc),
                Long.max(SuperblockV1BB.size(sc), Long.max(SuperblockV2BB.size(sc), SuperblockV3BB.size(sc))));
    }

    default boolean isValid() {
        return Arrays.equals(SIGNATURE, getFormatSignature());
    }

    byte[] getFormatSignature();

    void setFormatSignature();

    int getVersionNumber();

    void setVersionNumber(int value);

    int getSizeOfOffsets();

    void setSizeOfOffsets(int value);

    int getSizeOfLengths();

    void setSizeOfLengths(int value);

    int getFileConsistencyFlags();

    void setFileConsistencyFlags(int value);

    long getBaseAddress();

    void setBaseAddress(long value);

    long getEndOfFileAddress();

    void setEndOfFileAddress(long value);

    static Superblock of(final ByteBuffer buf, final SizingContext sizingContext) {
        final Superblock superblockV0 = SuperblockV0BB.of(buf, sizingContext);
        // v0 validity is too strict, so only check header
        if (Arrays.equals(SIGNATURE, superblockV0.getFormatSignature())) {
            switch (superblockV0.getVersionNumber()) {
            case 0:
                return superblockV0;
            case 1:
                return SuperblockV1BB.of(buf, sizingContext);
            case 2:
                return SuperblockV2BB.of(buf, sizingContext);
            case 3:
                return SuperblockV3BB.of(buf, sizingContext);
            default:
                throw new IllegalArgumentException("Implement superblock version " + superblockV0.getVersionNumber());
            }
        }
        return superblockV0;
    }

}
