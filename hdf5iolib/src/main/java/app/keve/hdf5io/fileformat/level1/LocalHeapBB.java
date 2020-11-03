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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;

public final class LocalHeapBB extends AbstractSizedBB<SizingContext> implements LocalHeap {
    public LocalHeapBB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long size(final SizingContext sc) {
        return 8 + 2 * sc.lengthSize() + sc.offsetSize();
    }

    @Override
    public long size() {
        return 8 + 2 * context.lengthSize() + context.offsetSize();
    }

    @Override
    public boolean isValid() {
        return Arrays.equals(SIGNATURE, getSignature()) && 0 == getByte(5) && 0 == getShort(6);
    }

    @Override
    public byte[] getSignature() {
        return getBytes(0, 4);
    }

    @Override
    public void setSignature() {
        setBytes(0, SIGNATURE);
    }

    @Override
    public int getVersion() {
        return getUnsignedByte(4);
    }

    @Override
    public void setVersion(final int value) {
        setByte(4, value);
    }

    @Override
    public long getDataSegmentSize() {
        return getLength(8);
    }

    @Override
    public void setDataSegmentSize(final long value) {
        setLength(8, value);
    }

    @Override
    public long getOffsetHeadOfFreeList() {
        return getLength(8 + context.lengthSize());
    }

    @Override
    public void setOffsetHeadOfFreeList(final long value) {
        setLength(8 + context.lengthSize(), value);
    }

    @Override
    public Resolvable<? extends LocalHeapDataSegment> getDataSegment() {
        return getResolvable(8 + 2 * context.lengthSize(), (int) getDataSegmentSize(), LocalHeapDataSegment.class,
                context);
    }

    @Override
    public void setDataSegment(final Resolvable<? extends LocalHeapDataSegment> value) {
        setResolvable(8 + 2 * context.lengthSize(), value);
    }

    @Override
    public void initialize() {
        setSignature();
        setVersion(0);
        setByte(5, 0);
        setShort(6, 0);
    }

    public static final class LocalHeapDataSegmentBB extends AbstractSizedBB<SizingContext>
            implements LocalHeapDataSegment {
        private long size;

        public LocalHeapDataSegmentBB(final ByteBuffer buf, final SizingContext sizingContext) {
            super(buf, sizingContext);
            size = available(); // this object is externally sized
        }

        public static long minSize(final SizingContext sc) {
            return 2 * sc.lengthSize();
        }

        public static long maxSize(final SizingContext sc) {
            return 8 == sc.lengthSize() ? Long.MAX_VALUE : 2 * sc.lengthSize() + MAX_LENGTH[sc.lengthSize()];
        }

        @Override
        public long size() {
            return size;
        }

        @Override
        public void setSize(final long value) {
            size = value;
            resize();
        }

        @Override
        public long getNextFreeBlockOffset(final long offset) {
            return getLength((int) offset);
        }

        @Override
        public long getFreeBlockSize(final long offset) {
            return getLength((int) offset + context.lengthSize());
        }

        @Override
        public CharSequence getAsciiNulString(final long idx) {
            return super.getAsciiNulString((int) idx);
        }

        @Override
        public void setNextFreeBlockOffset(final long offset, final long value) {
            setLength((int) offset, value);
        }

        @Override
        public void setFreeBlockSize(final long offset, final long value) {
            setLength((int) offset + context.lengthSize(), value);
        }

        @Override
        public void setAsciiNulString(final long idx, final String value) {
            final byte[] stringBytes = value.getBytes(StandardCharsets.US_ASCII);
            int offset = (int) idx;
            for (final byte b : stringBytes) {
                setByte(offset++, b);
            }
            setByte(offset++, 0);
            final int end = offset + 7 & ~7;
            while (offset < end) {
                setByte(offset++, 0);
            }
        }

        @Override
        public void initialize() {
            setAsciiNulString(0, "");
            setNextFreeBlockOffset(8, 1);
            setFreeBlockSize(8, size() - 8);
        }

    }

}
