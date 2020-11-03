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
package app.keve.hdf5io.fileformat.level2;

import java.nio.ByteBuffer;

import app.keve.hdf5io.fileformat.H5Context;
import app.keve.hdf5io.fileformat.H5ObjectW;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.level2message.H5Message;

public interface ObjectHeaderV1 extends ObjectHeader, H5ObjectW<SizingContext> {
    int getTotalNumberOfHeaderMessages();

    void setTotalNumberOfHeaderMessages(int value);

    int getObjectReferenceCount();

    void setObjectReferenceCount(int value);

    int getObjectHeaderSize();

    void setObjectHeaderSize(int value);

    HeaderMessageEntry<SizingContext> addHeaderMessage(long messageDataSize);

    HeaderMessageEntry<SizingContext> addHeaderMessage(H5Message<? extends H5Context> message);

    interface HeaderMessageEntryV1 extends HeaderMessageEntry<SizingContext> {
        static HeaderMessageEntryV1 of(final ByteBuffer buf, final SizingContext sizingContext) {
            throw new IllegalArgumentException();
        }
    }

}
