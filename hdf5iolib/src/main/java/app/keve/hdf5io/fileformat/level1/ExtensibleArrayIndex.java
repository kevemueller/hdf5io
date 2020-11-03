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
package app.keve.hdf5io.fileformat.level1;

import java.nio.ByteBuffer;

import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;

public interface ExtensibleArrayIndex extends H5Object<SizingContext> {
    byte[] SIGNATURE = {'E', 'A', 'H', 'D'};

    static long size(final SizingContext sc) {
        return ExtensibleArrayIndexV0BB.size(sc);
    }

    boolean isValid();

    byte[] getSignature();

    int getVersion();

    int getClientID();

    int getElementSize();

    int getMaxNelmtsBits();

    int getIndexBlkElmts();

    int getDataBlkMinElmts();

    int getSecondaryBlkMinDataPtrs();

    int getMaxDataBlkPageNelmtsBits();

    long getNumSecondaryBlks();

    long getSecondaryBlkSize();

    long getNumDataBlks();

    long getDataBlkSize();

    long getMaxIndexSet();

    long getNumElements();

    Resolvable<ExtensibleArrayIndexBlock> getIndexBlock();

    int getChecksum();

    static ExtensibleArrayIndex of(final ByteBuffer buf, final SizingContext sizingContext) {
        switch (buf.get(4)) {
        case 0:
            return new ExtensibleArrayIndexV0BB(buf, sizingContext);
        default:
            throw new IllegalArgumentException();
        }
    }

}
