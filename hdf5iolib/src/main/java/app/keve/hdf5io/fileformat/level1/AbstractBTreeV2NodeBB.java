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

import app.keve.hdf5io.fileformat.AbstractBB;
import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextBTreeV2Node;
import app.keve.hdf5io.fileformat.level2message.LinkMessage;

public abstract class AbstractBTreeV2NodeBB extends AbstractSizedBB<SizingContextBTreeV2Node> implements BTreeV2Node {
    public AbstractBTreeV2NodeBB(final ByteBuffer buf, final SizingContextBTreeV2Node sizingContext) {
        super(buf, sizingContext);
    }

    public abstract static class AbstractRecordBB extends AbstractBB<SizingContextBTreeV2Node> implements Record {

        protected AbstractRecordBB(final ByteBuffer buf, final SizingContextBTreeV2Node context) {
            super(buf, context);
        }

        public static long size(final SizingContext sc) {
            return LinkNameRecordBB.size(sc);
        }

        public static Record of(final ByteBuffer buf, final SizingContext context) {
            throw new IllegalArgumentException();
        }
    }

    public static final class LinkNameRecordBB extends AbstractRecordBB implements LinkNameRecord {
        public LinkNameRecordBB(final ByteBuffer buf, final SizingContextBTreeV2Node context) {
            super(buf, context);
        }

        public static long size(final SizingContext sc) {
            return 11;
        }

        @Override
        public long size() {
            return 11;
        }

        @Override
        public int getHash() {
            return getInt(0);
        }

        @Override
        public Resolvable<LinkMessage> getHeapId() {
            return getResolvable(context.fractalHeap(), getEmbeddedData(4, 7), LinkMessage.class, context);
        }

    }
}
