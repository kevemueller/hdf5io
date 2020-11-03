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

public interface SizingContextFractalHeap extends SizingContext {
    int maximumHeapSize();

    boolean blockChecksum();

    int directBlockSize();

    static SizingContextFractalHeap of(final SizingContext context, final int maximumHeapSize,
            final boolean blockChecksum, final int directBlockSize) {
        return new SizingContextFractalHeap() {

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
            public int maximumHeapSize() {
                return maximumHeapSize;
            }

            @Override
            public boolean blockChecksum() {
                return blockChecksum;
            }

            @Override
            public int directBlockSize() {
                return directBlockSize;
            }
        };
    }
}
