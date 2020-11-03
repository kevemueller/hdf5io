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
package app.keve.hdf5io.impl;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import app.keve.hdf5io.fileformat.H5MessageType;
import app.keve.hdf5io.fileformat.H5Resolver;
import app.keve.hdf5io.fileformat.level2.ObjectHeader;
import app.keve.hdf5io.fileformat.level2.ObjectHeaderContinuationBlock;
import app.keve.hdf5io.fileformat.level2message.ObjectHeaderContinuationMessage;

/**
 * Continuous view of HeaderMessages across ObjectHeaderContinuations.
 */
public final class HeaderMessageIterator implements Iterator<ObjectHeader.HeaderMessageEntry<?>> {
    private final H5Resolver hdf5Resolver;
    private Iterator<? extends ObjectHeader.HeaderMessageEntry<?>> current;
    private final Deque<Iterator<? extends ObjectHeader.HeaderMessageEntry<?>>> next;
    private ObjectHeader.HeaderMessageEntry<?> nextMessage;

    public HeaderMessageIterator(final H5Resolver hdf5Resolver,
            final Iterator<? extends ObjectHeader.HeaderMessageEntry<?>> headerMessages) {
        this.hdf5Resolver = hdf5Resolver;
        this.current = headerMessages;
        this.next = new LinkedList<>();
        this.nextMessage = null;
    }

    private ObjectHeader.HeaderMessageEntry<?> advance() {
        while (current != null && !current.hasNext()) {
            current = next.poll();
        }
        if (null == current) {
            return null;
        }
        final ObjectHeader.HeaderMessageEntry<?> nm = current.next();
        if (H5MessageType.OBJECT_HEADER_CONTINUATION == nm.getType()) {
            final ObjectHeaderContinuationMessage md = nm.getMessage();
//                md.dump(System.out);
            final ObjectHeaderContinuationBlock<?> oh = md.getContinuationBlock().resolve(hdf5Resolver);
            next.push(oh.headerMessageIterator());
            return advance();
        } else {
            return nm;
        }
    }

    @Override
    public boolean hasNext() {
        if (null != nextMessage) {
            return true;
        }
        if (null == current) {
            return false;
        }
        nextMessage = advance();
        return null != nextMessage;
    }

    @Override
    public ObjectHeader.HeaderMessageEntry<?> next() {
        if (null != nextMessage) {
            final ObjectHeader.HeaderMessageEntry<?> ret = nextMessage;
            nextMessage = null;
            return ret;
        }
        nextMessage = advance();
        if (null == nextMessage) {
            throw new NoSuchElementException();
        }
        return nextMessage;
    }
}
