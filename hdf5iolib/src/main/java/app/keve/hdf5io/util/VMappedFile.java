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
package app.keve.hdf5io.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

// TODO: for MapMode.READ_ONLY do heuristics to seek into the channel and read small chunk vs. map the file
public final class VMappedFile implements AutoCloseable {
    private final TreeMap<Long, Mapping> vMap;
    private Mapping appendMapping;

    private static class Mapping implements AutoCloseable {
        private final long virtualOffset; // could be put outside of Mapping class
        private final long maxSize;
        private final long offsetIntoFile;
        private final Path file;
        private final FileChannel fc;
        private final MapMode mapMode;

        private long currentSize; // TODO: int, ==buf.capacity if we remap aggressively
        private MappedByteBuffer buf;

        Mapping(final long virtualOffset, final long maxSize, final long offsetIntoFile, final Path file,
                final FileChannel fc, final MapMode mapMode) throws IOException {
            this.virtualOffset = virtualOffset;
            this.maxSize = maxSize;
            assert maxSize <= Integer.MAX_VALUE : "cannot map more than 2G";
            this.offsetIntoFile = offsetIntoFile;
            this.file = file;
            this.fc = fc;
            this.mapMode = mapMode;

            // offsetInfoFile might be bigger than the current file size, file will
            // be expanded at first write.
            this.currentSize = Long.max(0, fc.size() - offsetIntoFile);
        }

        ByteBuffer getBuffer(final int offset, final int size) throws IOException {
            if (null == buf || offset + Math.abs(size) > buf.capacity()) {
                // file size has changed!
                // TODO: multiple buffers;
                currentSize = Long.min(maxSize, fc.size() - offsetIntoFile);
                buf = fc.map(mapMode, offsetIntoFile, currentSize);
            }
            final ByteBuffer atBuf = buf.duplicate().position(offset).slice();
            if (size < 0) {
                assert atBuf.limit() >= -size : "Limit " + atBuf.limit() + " < " + -size;
            } else if (size > 0) {
                atBuf.limit(size);
            }
            return atBuf;
        }

        @Override
        public void close() throws IOException {
            if (null != buf) {
                buf.force();
                buf = null;
            }
            fc.close(); // it is allowed to close a channel multiple times
        }

        @Override
        public String toString() {
            return String.format("Mapping [virtual@%s:%s, offsetIntoFile=%s, file=%s, buf=%s]", virtualOffset, maxSize,
                    offsetIntoFile, file, fc, buf);
        }

    }

    private VMappedFile(final Mapping... mappings) throws IOException {
        vMap = new TreeMap<>();
        for (final Mapping mapping : mappings) {
            addMapping(mapping);
        }
    }

    private void addMapping(final Mapping mapping) {
        vMap.put(mapping.virtualOffset, mapping);
        if (MapMode.READ_WRITE == mapping.mapMode) {
            appendMapping = mapping;
        }
    }

    public void addMapping(final long virtualOffset, final long offsetInfoFile, final int size, final Path path,
            final OpenOption... openOptions) throws IOException {
        addMapping(virtualOffset, offsetInfoFile, size, path, mapModeFromOpenOptions(openOptions), openOptions);
    }

    public void addMapping(final long virtualOffset, final long offsetInfoFile, final int size, final Path path,
            final MapMode mapMode, final OpenOption... openOptions) throws IOException {
        for (final Mapping mapping : vMap.values()) {
            if (Files.isSameFile(mapping.file, path)) {
                final Mapping newMapping = new Mapping(virtualOffset, size, offsetInfoFile, mapping.file, mapping.fc,
                        mapMode);
                addMapping(newMapping);
                return;
            }
        }
        final FileChannel fc = FileChannel.open(path, openOptions);
        final Mapping newMapping = new Mapping(virtualOffset, size, offsetInfoFile, path, fc, mapMode);
        addMapping(newMapping);
    }

    public long[] append(final ByteBuffer src) throws IOException {
        // TODO: check if we need to wrap the buffer (appendMapping.currentSize +
        // src.remaining > appendMapping.maxSize)
        if (null == appendMapping) {
            throw new IOException("No appendable mapping added.");
        }
        final int nBytes = appendMapping.fc.write(src, appendMapping.offsetIntoFile + appendMapping.currentSize);
        final long oldEOF = appendMapping.virtualOffset + appendMapping.currentSize;
//        appendMapping.eof += nBytes + 7 & ~7;
        appendMapping.currentSize += nBytes;
        return new long[] {oldEOF, nBytes};
    }

    /**
     * Cumulative size of all the mappings.
     * 
     * @return cumulative size of all the mappings.
     * @throws IOException if an I/O exception occurs
     */
    public long size() throws IOException {
        long size = 0;
        for (final Mapping me : vMap.values()) {
            size += me.currentSize;
        }
        return size;
    }

    /**
     * Maximum addressable vOffset +1.
     * 
     * @return Maximum addressable vOffset.
     * @throws IOException if an I/O exception occurs
     */
    public long maxVOffset() throws IOException {
        long maxVOffset = Long.MIN_VALUE;
        for (final Mapping me : vMap.values()) {
            maxVOffset = Long.max(maxVOffset, me.virtualOffset + me.currentSize);
        }
        return maxVOffset;
    }

    /**
     * Provide a sized buffer duplicate starting at offset.
     * 
     * @param vOffset the virtual offset
     * @param size    0 - best effort, negative - at least this number of available
     *                bytes, positive - exact number of bytes, max:
     *                Integer.MAX_VALUE
     * @return the buffer
     * @throws IOException if an I/O exception occurs
     */
    public ByteBuffer at(final long vOffset, final int size) throws IOException {
        final Entry<Long, Mapping> entry = vMap.floorEntry(vOffset);
        assert null != entry : "file@" + vOffset + ":" + size + " is before BOF";
        final Mapping mapping = entry.getValue();
        final int offset = (int) (vOffset - mapping.virtualOffset);
        return mapping.getBuffer(offset, size);
    }

    /**
     * Provide a best effort buffer duplicate starting at offset.
     * 
     * @param vOffset the virtual offset
     * @return the buffer
     * @throws IOException if an I/O exception occurs
     */
    public ByteBuffer at(final long vOffset) throws IOException {
        return at(vOffset, 0);
    }

    @Override
    public void close() throws IOException {
        for (final Mapping mapping : vMap.values()) {
            mapping.close();
        }
        appendMapping = null;
        vMap.clear();
    }

    @Override
    public String toString() {
        final int maxLen = 10;
        return String.format("MappedFile [vMap=%s]", vMap != null ? toString(vMap.entrySet(), maxLen) : null);
    }

    private String toString(final Collection<?> collection, final int maxLen) {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        int i = 0;
        for (final Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(iterator.next());
        }
        builder.append("]");
        return builder.toString();
    }

    private static MapMode mapModeFromOpenOptions(final OpenOption... openOptions) {
        for (final OpenOption option : openOptions) {
            if (StandardOpenOption.WRITE == option) {
                return MapMode.READ_WRITE;
            }
        }
        return MapMode.READ_ONLY;
    }

    public static VMappedFile of() throws IOException {
        return new VMappedFile();
    }

    public static VMappedFile of(final Path file, final OpenOption... openOptions) throws IOException {
        return of(file, mapModeFromOpenOptions(openOptions), openOptions);
    }

    public static VMappedFile of(final Path file, final MapMode mapMode, final OpenOption... openOptions)
            throws IOException {
        final FileChannel fc = FileChannel.open(file, openOptions);
        final long eof = fc.size();
        assert eof < Integer.MAX_VALUE; // TODO: multiple buffers
        return new VMappedFile(new Mapping(0, Integer.MAX_VALUE, 0, file, fc, mapMode));
    }

}
