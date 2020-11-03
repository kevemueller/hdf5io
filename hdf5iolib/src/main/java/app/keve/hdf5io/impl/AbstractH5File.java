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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.keve.hdf5io.fileformat.H5Context;
import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.H5Registry;

public abstract class AbstractH5File extends H5Heap implements AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(AbstractH5File.class);

    // this is nice for performance, but unclean.
    // currently needed to avoid StackOverflow in traversing the HDF object tree
    // with cycles.
    @Deprecated
    private final Map<Long, H5Object<?>> resolvedMap;

    protected AbstractH5File(final H5Registry hdf5Registry) {
        super(hdf5Registry);
        resolvedMap = new LinkedHashMap<>();
    }

    protected abstract ByteBuffer at(long address, long length) throws IOException;

    @SuppressWarnings("unchecked")
    @Override
    public final <T extends H5Object<S>, S extends H5Context> T resolve(final long address, final long length,
            final Class<T> tClass, final S sc) {
//        logger.trace("resolve @{}:{} {}", address, length, tClass.getName());
        T t = super.resolve(address, length, tClass, sc);
        if (null != t) {
            return t;
        }

        t = (T) resolvedMap.get(address);
        if (null == t) {
            try {
                final long len = 0 == length ? -h5Registry.minSize(tClass, sc) : length;
                final BiFunction<ByteBuffer, S, T> of = of(tClass);
                final ByteBuffer buf = at(address, len).order(ByteOrder.LITTLE_ENDIAN);
                t = of.apply(buf, sc);
                assert t.size() > 0 : "Invalid size for instance of " + tClass;
                assert t.size() <= buf.capacity() : "Invalid size for instance of " + tClass + " resolved at " + address
                        + " requested" + t.size() + " has " + buf.capacity();
                buf.limit((int) t.size());
                resolvedMap.put(address, t);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return t;
    }

    @Override
    public final ByteBuffer resolve(final long address, final int size) {
        final ByteBuffer buf = super.resolve(address, size);
        if (null != buf) {
            return buf;
        }
        try {
            return at(address, size);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
