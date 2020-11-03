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

import app.keve.hdf5io.fileformat.level1.LocalHeap;

/**
 * sizing context for offsets and local heap reference.
 */
public interface SizingContextHeap extends SizingContext {
    Resolvable<? extends LocalHeap> heap();

    static SizingContextHeap of(final SizingContext sizingContext, final Resolvable<? extends LocalHeap> heap) {
        return new SizingContextHeap() {
            @Override
            public H5Factory h5Factory() {
                return sizingContext.h5Factory();
            }

            @Override
            public int offsetSize() {
                return sizingContext.offsetSize();
            }

            @Override
            public int lengthSize() {
                return sizingContext.lengthSize();
            }

            @Override
            public int indexedStorageInternalNodeK() {
                return sizingContext.indexedStorageInternalNodeK();
            }

            @Override
            public int groupInternalNodeK() {
                return sizingContext.groupInternalNodeK();
            }

            @Override
            public int groupLeafNodeK() {
                return sizingContext.groupLeafNodeK();
            }

            @Override
            public Resolvable<? extends LocalHeap> heap() {
                return heap;
            }
        };
    }
}
