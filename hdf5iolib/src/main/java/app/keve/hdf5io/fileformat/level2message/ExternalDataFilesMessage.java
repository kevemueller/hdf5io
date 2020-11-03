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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextHeap;
import app.keve.hdf5io.fileformat.level1.LocalHeap;

public interface ExternalDataFilesMessage extends H5Object<SizingContext> {
    boolean isValid();

    int getVersion();

    int getAllocatedSlots();

    int getUsedSlots();

    Resolvable<LocalHeap> getHeap();

    Iterator<? extends SlotDefinition> slotsIterator();

    default List<SlotDefinition> getSlots() {
        final ArrayList<SlotDefinition> slots = new ArrayList<>(getUsedSlots());
        slotsIterator().forEachRemaining(slots::add);
        return slots;
    }

    interface SlotDefinition extends H5Object<SizingContextHeap> {
        Resolvable<String> getName();

        long getExternalFileOffset();

        long getExternalFileDataSize();

        static SlotDefinition of(final ByteBuffer buf, final SizingContextHeap sizingContext) {
            throw new IllegalArgumentException();
        }

    }

    static ExternalDataFilesMessage of(final ByteBuffer buf, final SizingContext sizingContext) {
        switch (buf.get(0)) {
        case 1:
            return new ExternalDataFilesMessageV1BB(buf, sizingContext);
        default:
            throw new IllegalArgumentException("Implement version: " + buf.get(0));
        }
    }
}
