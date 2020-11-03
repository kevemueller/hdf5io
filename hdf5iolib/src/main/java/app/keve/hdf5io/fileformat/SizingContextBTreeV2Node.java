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

public interface SizingContextBTreeV2Node extends SizingContext {
    Resolvable<FractalHeap> fractalHeap();

    int recordSize();

    int recordNum();

    static SizingContextBTreeV2Node of(final SizingContext context, final Resolvable<FractalHeap> fractalHeap,
            final int recordSize, final int recordNum) {
        return new SizingContextBTreeV2Node() {

            @Override
            public H5Factory h5Factory() {
                return context.h5Factory();
            }

            @Override
            public int offsetSize() {
                return context.offsetSize();
            }

            @Override
            public int lengthSize() {
                return context.lengthSize();
            }

            @Override
            public int indexedStorageInternalNodeK() {
                return context.indexedStorageInternalNodeK();
            }

            @Override
            public int groupInternalNodeK() {
                return context.groupInternalNodeK();
            }

            @Override
            public int groupLeafNodeK() {
                return context.groupLeafNodeK();
            }

            @Override
            public Resolvable<FractalHeap> fractalHeap() {
                return fractalHeap;
            }

            @Override
            public int recordSize() {
                return recordSize;
            }

            @Override
            public int recordNum() {
                return recordNum;
            }
        };
    }
}
