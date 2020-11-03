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

import app.keve.hdf5io.fileformat.level1.FractalHeap;

public interface SizingContextBTreeV2 extends SizingContext {
    Resolvable<FractalHeap> fractalHeap();

    static SizingContextBTreeV2 of(final SizingContext context, final Resolvable<FractalHeap> fractalHeap) {
        // avoid keeping a reference to the previous context;
        final H5Factory h5Factory = context.h5Factory();
        final int offsetSize = context.offsetSize();
        final int lengthSize = context.lengthSize();
        final int indexedStorageInternalNodeK = context.indexedStorageInternalNodeK();
        final int groupInternalNodeK = context.groupInternalNodeK();
        final int groupLeafNodeK = context.groupLeafNodeK();
        return new SizingContextBTreeV2() {

            @Override
            public H5Factory h5Factory() {
                return h5Factory;
            }

            @Override
            public int offsetSize() {
                return offsetSize;
            }

            @Override
            public int lengthSize() {
                return lengthSize;
            }

            @Override
            public int indexedStorageInternalNodeK() {
                return indexedStorageInternalNodeK;
            }

            @Override
            public int groupInternalNodeK() {
                return groupInternalNodeK;
            }

            @Override
            public int groupLeafNodeK() {
                return groupLeafNodeK;
            }

            @Override
            public Resolvable<FractalHeap> fractalHeap() {
                return fractalHeap;
            }
        };
    }
}
