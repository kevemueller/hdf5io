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

public interface SizingContextKTreeDimension extends SizingContext {
    int dimensionality();

    static SizingContextKTreeDimension of(final SizingContext sc, final int dimensionality) {
        return new SizingContextKTreeDimension() {
            @Override
            public H5Factory h5Factory() {
                return sc.h5Factory();
            }

            @Override
            public int offsetSize() {
                return sc.offsetSize();
            }

            @Override
            public int lengthSize() {
                return sc.lengthSize();
            }

            @Override
            public int indexedStorageInternalNodeK() {
                return sc.indexedStorageInternalNodeK();
            }

            @Override
            public int groupLeafNodeK() {
                return sc.groupLeafNodeK();
            }

            @Override
            public int groupInternalNodeK() {
                return sc.groupInternalNodeK();
            }

            @Override
            public int dimensionality() {
                return dimensionality;
            }
        };
    }

}
