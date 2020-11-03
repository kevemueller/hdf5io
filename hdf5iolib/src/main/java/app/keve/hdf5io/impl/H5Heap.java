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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.keve.hdf5io.api.HDF5DatatypeAdapter;
import app.keve.hdf5io.api.HDF5FormatException;
import app.keve.hdf5io.api.datatype.HDF5Array;
import app.keve.hdf5io.api.datatype.HDF5Bitfield;
import app.keve.hdf5io.api.datatype.HDF5Compound;
import app.keve.hdf5io.api.datatype.HDF5Datatype;
import app.keve.hdf5io.api.datatype.HDF5Datatype.DatatypeBuilder;
import app.keve.hdf5io.api.datatype.HDF5Enumeration;
import app.keve.hdf5io.api.datatype.HDF5FixedPointNumber;
import app.keve.hdf5io.api.datatype.HDF5FloatingPointNumber;
import app.keve.hdf5io.api.datatype.HDF5Opaque;
import app.keve.hdf5io.api.datatype.HDF5Reference;
import app.keve.hdf5io.api.datatype.HDF5String;
import app.keve.hdf5io.api.datatype.HDF5VariableLength;
import app.keve.hdf5io.fileformat.AbstractManager;
import app.keve.hdf5io.fileformat.H5Context;
import app.keve.hdf5io.fileformat.H5Factory;
import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.H5ObjectW;
import app.keve.hdf5io.fileformat.H5Registry;
import app.keve.hdf5io.fileformat.H5Registry.H5MessageInfo;
import app.keve.hdf5io.fileformat.H5Registry.H5ObjectInfo;
import app.keve.hdf5io.fileformat.H5Resolver;
import app.keve.hdf5io.fileformat.ResolutionListener;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.level1.FractalHeap;
import app.keve.hdf5io.fileformat.level1.FractalHeap.DirectBlock;
import app.keve.hdf5io.fileformat.level1.FractalHeap.HeapBlock;
import app.keve.hdf5io.fileformat.level1.GlobalHeapCollection;
import app.keve.hdf5io.fileformat.level1.GlobalHeapObject;
import app.keve.hdf5io.fileformat.level1.LocalHeap;
import app.keve.hdf5io.fileformat.level1.LocalHeap.LocalHeapDataSegment;
import app.keve.hdf5io.fileformat.level2.ObjectHeader;
import app.keve.hdf5io.fileformat.level2datatype.AbstractDatatypeBB;
import app.keve.hdf5io.fileformat.level2datatypeadapter.AbstractBitfieldAdapter;
import app.keve.hdf5io.fileformat.level2datatypeadapter.AbstractNativeFixedPointNumberAdapter;
import app.keve.hdf5io.fileformat.level2datatypeadapter.AbstractNativeFloatingPointNumberAdapter;
import app.keve.hdf5io.fileformat.level2datatypeadapter.GenericArrayAdapter;
import app.keve.hdf5io.fileformat.level2datatypeadapter.GenericCompoundToMapAdapter;
import app.keve.hdf5io.fileformat.level2datatypeadapter.GenericEnumToLabelAdapter;
import app.keve.hdf5io.fileformat.level2datatypeadapter.GenericOpaqueAdapter;
import app.keve.hdf5io.fileformat.level2datatypeadapter.GenericReferenceAdapter;
import app.keve.hdf5io.fileformat.level2datatypeadapter.GenericStringAdapter;
import app.keve.hdf5io.fileformat.level2datatypeadapter.GenericVariableLengthAdapter;

public class H5Heap implements H5Resolver, H5Factory {

    private abstract static class AbstractResolvable<T> implements Resolvable<T> {
        protected final H5Heap parent;

        AbstractResolvable(final H5Heap parent) {
            this.parent = Objects.requireNonNull(parent);
        }

        @Override
        public final void addResolutionListener(final ResolutionListener listener, final Object param) {
            parent.addListener(getAddress(), listener, param);
        }

        @Override
        public int hashCode() {
            return Objects.hash(parent);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AbstractResolvable)) {
                return false;
            }
            final AbstractResolvable<?> other = (AbstractResolvable<?>) obj;
            return parent == /* sameinstance */ other.parent;
        }

    }

    private static final class ResolvableByteBuffer extends AbstractResolvable<ByteBuffer> {
        private final long address;
        private final int size;

        private ResolvableByteBuffer(final H5Heap parent, final long address, final int size) {
            super(parent);
            this.address = address;
            this.size = size;
        }

        @Override
        public long getAddress() {
            return address;
        }

        @Override
        public OptionalLong getSize() {
            return OptionalLong.of(size);
        }

        @Override
        public ByteBuffer resolve(final H5Resolver resolver) {
            return resolver.resolve(address, size);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + Objects.hash(address);
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (!(obj instanceof ResolvableByteBuffer)) {
                return false;
            }
            final ResolvableByteBuffer other = (ResolvableByteBuffer) obj;
            return address == other.address;
        }

        @Override
        public String toString() {
            return String.format("ResolvableByteBuffer@%d:%d", address, size);
        }
    }

    protected static final class ResolvableH5Object<T extends H5Object<S>, S extends H5Context>
            extends AbstractResolvable<T> {
        private final long address;
        private final long size;
        private final Class<T> tClass;
        private final S context;

        private ResolvableH5Object(final H5Heap parent, final long address, final long size, final Class<T> tClass,
                final S context) {
            super(parent);
            this.address = address;
            this.size = size;
            this.tClass = Objects.requireNonNull(tClass);
            this.context = Objects.requireNonNull(context);
        }

        @Override
        public long getAddress() {
            return address;
        }

        @Override
        public OptionalLong getSize() {
            return OptionalLong.of(size);
        }

        @Override
        public T resolve(final H5Resolver resolver) {
            return resolver.resolve(address, size, tClass, context);
        }

        public Class<T> getTClass() {
            return tClass;
        }

        public S getContext() {
            return context;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + Objects.hash(address);
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (!(obj instanceof ResolvableH5Object)) {
                return false;
            }
            final ResolvableH5Object<?, ?> other = (ResolvableH5Object<?, ?>) obj;
            return address == other.address;
        }

        @Override
        public String toString() {
            return String.format("Resolvable@%d of %s", address, tClass);
        }
    }

    protected final Map<HDF5Datatype, HDF5DatatypeAdapter> datatypeMap;
    protected final H5Registry h5Registry;
    private final Logger logger = LoggerFactory.getLogger(H5Heap.class);
    private final AtomicLong heapAddress;
    private final TreeMap<Long, Object> heapResolvables;
    private final TreeMap<Long, List<Entry<ResolutionListener, Object>>> heapListeners;
    private final Deque<Object> dirty;

    protected H5Heap(final H5Registry hdf5Registry) {
        this.h5Registry = hdf5Registry;
        this.datatypeMap = new LinkedHashMap<>();
        this.heapResolvables = new TreeMap<>();
        this.heapListeners = new TreeMap<>();
        this.dirty = new LinkedList<>();
        this.heapAddress = new AtomicLong(0xFFFF_FFFF_FFFF_FFFEL); // -1 is already used as NULL pointer
    }

    public final <T> void addListener(final long address, final ResolutionListener listener, final Object param) {
        final List<Entry<ResolutionListener, Object>> listeners = heapListeners.get(address);
        if (null != listeners) {
            listeners.add(Map.entry(listener, param));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T extends H5Object<S>, S extends H5Context> BiFunction<ByteBuffer, S, T> of(final Class<T> tClass) {
        final H5ObjectInfo ci = h5Registry.info(tClass);
        assert null != ci : "No info for " + tClass;
        return (BiFunction<ByteBuffer, S, T>) ci.of;
    }

    private <T extends H5Object<S>, S extends H5Context> long minSize(final Class<T> tClass, final S sizingContext) {
        final H5ObjectInfo ci = h5Registry.info(tClass);
        assert null != ci : "No info for " + tClass;
        return ci.minSize(sizingContext);
    }

    private <T extends H5Object<S>, S extends H5Context> long maxSize(final Class<T> tClass, final S sizingContext) {
        final H5ObjectInfo ci = h5Registry.info(tClass);
        assert null != ci : "No info for " + tClass;
        return ci.maxSize(sizingContext);
    }

    @Override
    public final <T extends H5Object<S>, S extends H5Context> Class<T> messageClass(final int messageTypeNum) {
        final H5MessageInfo messageInfo = h5Registry.messageInfo(messageTypeNum);
        return null == messageInfo ? null : (Class<T>) messageInfo.messageClass;
    }

    @Override
    public final HDF5DatatypeAdapter datatypeAdapter(final HDF5Datatype datatype, final SizingContext sizingContext) {
        HDF5DatatypeAdapter datatypeAdapter = datatypeMap.get(datatype);
        // cannot use computeIfAbsent, as the datatypes are recursive.
        // will fail if a datatype contains itself transitively
        // TODO: check again!
        if (null == datatypeAdapter) {
            datatypeAdapter = datatypeAdapter2(datatype, sizingContext);
            datatypeMap.put(datatype, datatypeAdapter);
        }
        return datatypeAdapter;

    }

    private HDF5DatatypeAdapter datatypeAdapter2(final HDF5Datatype datatype, final SizingContext sizingContext) {
        if (datatype instanceof HDF5FixedPointNumber) {
            return AbstractNativeFixedPointNumberAdapter.forType((HDF5FixedPointNumber) datatype);
        } else if (datatype instanceof HDF5FloatingPointNumber) {
            return AbstractNativeFloatingPointNumberAdapter.forType((HDF5FloatingPointNumber) datatype);
        } else if (datatype instanceof HDF5Enumeration) {
            return new GenericEnumToLabelAdapter((HDF5Enumeration) datatype);
        } else if (datatype instanceof HDF5Compound) {
            return new GenericCompoundToMapAdapter((HDF5Compound) datatype);
        } else if (datatype instanceof HDF5Array) {
            return new GenericArrayAdapter((HDF5Array) datatype);
        } else if (datatype instanceof HDF5Reference) {
            return GenericReferenceAdapter.forType((HDF5Reference) datatype, l -> {
                try {
                    return new Link(this, "", resolvable(l, 0, ObjectHeader.class, sizingContext)).getTarget();
                } catch (final HDF5FormatException e) {
                    throw new IllegalArgumentException(e);
                }
            });
        } else if (datatype instanceof HDF5VariableLength) {
            // for VL datatypes we need a full resolver!
            // sizingContext to read the vl structure
            // resolveObject to get to the GlobalHeapCollection
            return new GenericVariableLengthAdapter((HDF5VariableLength) datatype, vlghr -> {
                final GlobalHeapCollection ghc = vlghr.getGlobalHeapCollection().resolve(this);
                final GlobalHeapObject gho = ghc.getHeapObject(vlghr.getIndex());
                return gho.getObjectData();
            }, sizingContext);
        } else if (datatype instanceof HDF5String) {
            return new GenericStringAdapter((HDF5String) datatype);
        } else if (datatype instanceof HDF5Bitfield) {
            return AbstractBitfieldAdapter.forType((HDF5Bitfield) datatype);
        } else if (datatype instanceof HDF5Opaque) {
            return new GenericOpaqueAdapter((HDF5Opaque) datatype);
        }
        throw new IllegalArgumentException("No adapter for " + datatype);
    }

    @Override
    public final DatatypeBuilder datatypeBuilder(final H5Context context) {
        return new AbstractDatatypeBB.DatatypeBuilderBB(context);
    }

    // create on heap

    @Override
    public final Resolvable<ByteBuffer> allocate(final int size) {
        final ByteBuffer buf = ByteBuffer.allocate(size);
        final long address = heapAddress.getAndDecrement();
        logger.trace("allocated buffer@{}:{}{}", address, size);
        heapResolvables.put(address, buf);
        heapListeners.put(address, new ArrayList<>());
        final Resolvable<ByteBuffer> rBuffer = new ResolvableByteBuffer(this, address, size);
        dirty.add(rBuffer);
        return rBuffer;
    }

    @Override
    public final <T extends H5ObjectW<S>, S extends H5Context> Resolvable<T> allocate(final Class<T> tClass,
            final S sizingContext) throws IOException {
        final T t = allocateLocal(tClass, sizingContext);
        final long address = heapAddress.getAndDecrement();
        logger.trace("allocated @{} instance of {}={}", address, tClass.getName(),
                Integer.toUnsignedString(System.identityHashCode(t), 16));
        Resolvable<T> rObject = new ResolvableH5Object<>(this, address, 0, tClass, sizingContext);
        final long maxSize = maxSize(tClass, sizingContext);
        final long minSize = minSize(tClass, sizingContext);
        if (minSize == maxSize) {
            // fixed size, we can commit immediately
            rObject = commit(rObject, t);
        } else {
            // variable size, defer commit
            heapResolvables.put(address, t);
            heapListeners.put(address, new ArrayList<>());
            dirty.add(rObject);
        }
        return rObject;
    }

    @Override
    public final <T extends H5ObjectW<S>, S extends H5Context> T allocateLocal(final Class<T> tClass,
            final S sizingContext) throws IOException {
        final BiFunction<ByteBuffer, S, T> ofOH = of(tClass);
        final long maxSize = maxSize(tClass, sizingContext);
        final ByteBuffer buf = ByteBuffer.allocate((int) Long.min(maxSize, 65535)).order(ByteOrder.LITTLE_ENDIAN);
        final T t = ofOH.apply(buf, sizingContext);
        t.initialize();
        return t;
    }

    // resolvables

    @Override
    public final <T extends H5Object<S>, S extends H5Context> Resolvable<T> resolvable(final long address,
            final long length, final Class<T> tClass, final S sizingContext) {
        return new ResolvableH5Object<>(this, address, length, tClass, sizingContext);
    }

    @Override
    public final Resolvable<ByteBuffer> resolvable(final long address, final int size) {
        assert size >= 0;
        return new ResolvableByteBuffer(this, address, size);
    }

    @Override
    public final Resolvable<String> resolvable(final Resolvable<? extends LocalHeap> heap, final long stringOffset) {
        return new AbstractResolvable<>(this) {
            @Override
            public String resolve(final H5Resolver resolver) {
                if (null == heap) {
                    assert 0 == stringOffset;
                    return null;
                }
                final LocalHeap localHeap = heap.resolve(resolver);
                final LocalHeapDataSegment dataSegment = localHeap.getDataSegment().resolve(resolver);
                return dataSegment.getAsciiNulString(stringOffset).toString();
            }

            @Override
            public long getAddress() {
                return /* heap.getAddress() + */stringOffset;
            }

            @Override
            public String toString() {
                return String.format("ResolvableString: %s[%d]", heap, stringOffset);
            }

        };
    }

    // TODO: This is simplified...
    @Override
    public final <T extends H5Object<S>, S extends H5Context> Resolvable<T> resolvable(
            final Resolvable<FractalHeap> rFractalHeap, final ByteBuffer heapId, final Class<T> tClass,
            final S sizingContext) {
        return new AbstractResolvable<>(this) {
            @Override
            public T resolve(final H5Resolver resolver) {
                final FractalHeap fractalHeap = rFractalHeap.resolve(resolver);
                final Resolvable<? extends HeapBlock> rRootBlock = fractalHeap.getRootBlock();
                final DirectBlock rootBlock = (DirectBlock) rRootBlock.resolve(resolver);

                final ByteBuffer hh = heapId.slice().order(ByteOrder.LITTLE_ENDIAN);
                final int ofs = hh.getInt(1);
                final int length = Short.toUnsignedInt(hh.getShort(5));
                final ByteBuffer buf = rootBlock.getObjectData(ofs, length);
                final BiFunction<ByteBuffer, S, T> of = of(tClass);
                return of.apply(buf.slice().order(ByteOrder.LITTLE_ENDIAN), sizingContext);
            }

            @Override
            public long getAddress() {
                return -1;
            }

            @Override
            public String toString() {
                final ArrayList<Integer> l = new ArrayList<>();
                final ByteBuffer bb = heapId.duplicate();
                while (bb.hasRemaining()) {
                    l.add(Byte.toUnsignedInt(bb.get()));
                }
                return String.format("ResolvableFractalHeapId(%s,%s, %s)", rFractalHeap, l, tClass);
            }

        };
    }

    // resolve from heap

    @Override
    public <T extends H5Object<S>, S extends H5Context> T resolve(final long address, final long length,
            final Class<T> tClass, final S sc) {
        @SuppressWarnings("unchecked")
        final T t = (T) heapResolvables.get(address);
        if (null == t) {
            return null;
        }
        assert tClass.isAssignableFrom(t.getClass());
        // asssert context?
        return t;
    }

    @Override
    public ByteBuffer resolve(final long address, final int size) {
        final ByteBuffer buf = (ByteBuffer) heapResolvables.get(address);
        if (null == buf) {
            return null;
        }
        if (size < 0) {
            assert buf.capacity() >= -size;
        } else if (size > 0) {
            assert buf.capacity() >= size;
            buf.limit(size);
        }
        return buf;
    }

    @Override
    public long eof() throws IOException {
        return -1;
    }

    /**
     * Commit the resolvable/instance pair and return an new resolvable.
     * 
     * @param <T>        the instance's type
     * @param resolvable the resolvable pointing to the instance
     * @param t          the instance
     * @return a possibly new resolvable
     * @throws IOException if an I/O error occurs.
     */
    protected <T> Resolvable<T> commit(final Resolvable<T> resolvable, final T t) throws IOException {
        if (t instanceof H5ObjectW) {
            ((H5ObjectW<?>) t).pack();
        }
        return resolvable;
    }

    @Override
    public final <T> Resolvable<T> commit(final Resolvable<T> resolvable) throws IOException {
        final long address = resolvable.getAddress();
        final Object heapObject = heapResolvables.get(address);
        if (null != heapObject) {
            final Resolvable<T> newResolvable = commit(resolvable, (T) heapObject);
            final List<Entry<ResolutionListener, Object>> listeners = heapListeners.get(resolvable.getAddress());
            fireResolved(listeners, newResolvable);
            heapResolvables.remove(address);
            heapListeners.remove(address);
            dirty.remove(resolvable);
            return newResolvable;
        }
        logger.trace("NOP-commit for {}", resolvable);
        return resolvable;
    }

    @Override
    public final void markDirty(final AbstractManager manager) {
        dirty.add(manager);
    }

    private <T> void fireResolved(final List<Entry<ResolutionListener, Object>> listeners,
            final Resolvable<T> resolved) {
        for (final Entry<ResolutionListener, Object> listener : listeners) {
            logger.trace("resolution {} to {}={}", resolved, listener.getKey().getClass().getName(),
                    Integer.toUnsignedString(System.identityHashCode(listener.getKey()), 16));
            listener.getKey().resolved(resolved, listener.getValue());
        }
    }

    @Override
    public final void commitAll() throws IOException {
        logger.trace("commitAll");
        for (final Object d : dirty) {
            logger.trace("c: {}", d);
        }
        while (!dirty.isEmpty()) {
            final Object d = dirty.pollLast();
            if (d instanceof Resolvable<?>) {
                commit((Resolvable<?>) d);
            } else if (d instanceof AbstractManager) {
                ((AbstractManager) d).commit(this);
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

}
