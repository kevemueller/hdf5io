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
import java.nio.ByteOrder;

import app.keve.hdf5io.fileformat.H5ObjectW;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;

public interface LocalHeap extends H5ObjectW<SizingContext> {
    byte[] SIGNATURE = {'H', 'E', 'A', 'P'};

    boolean isValid();

    byte[] getSignature();

    void setSignature();

    int getVersion();

    void setVersion(int value);

    long getDataSegmentSize();

    void setDataSegmentSize(long value);

    Resolvable<? extends LocalHeapDataSegment> getDataSegment();

    void setDataSegment(Resolvable<? extends LocalHeapDataSegment> value);

    long getOffsetHeadOfFreeList();

    void setOffsetHeadOfFreeList(long value);

    interface LocalHeapDataSegment extends H5ObjectW<SizingContext> {
        void setSize(long value);

        long getNextFreeBlockOffset(long offset);

        void setNextFreeBlockOffset(long offset, long value);

        long getFreeBlockSize(long offset);

        void setFreeBlockSize(long offset, long value);

        CharSequence getAsciiNulString(long idx);

        void setAsciiNulString(long idx, String value);

        static LocalHeapDataSegment of(final ByteBuffer buf, final SizingContext sizingContext) {
            return new LocalHeapBB.LocalHeapDataSegmentBB(buf.order(ByteOrder.LITTLE_ENDIAN), sizingContext);
        }

    }

    static LocalHeap of(final ByteBuffer buf, final SizingContext sizingContext) {
        return new LocalHeapBB(buf, sizingContext);
    }

}
