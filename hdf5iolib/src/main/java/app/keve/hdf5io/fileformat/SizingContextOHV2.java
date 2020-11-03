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

/**
 * sizing context to inherit object header v2 creation order information to
 * object header continuation blocks.
 */
public interface SizingContextOHV2 extends SizingContext {
    boolean isCreationOrderTracked();

    static SizingContextOHV2 of(final SizingContext sizingContext, final boolean creationOrderTracked) {
        return new SizingContextOHV2() {
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
            public int groupLeafNodeK() {
                return sizingContext.groupLeafNodeK();
            }

            @Override
            public int groupInternalNodeK() {
                return sizingContext.groupInternalNodeK();
            }

            @Override
            public boolean isCreationOrderTracked() {
                return creationOrderTracked;
            }
        };
    }
}
