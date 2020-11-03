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
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.OptionalInt;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import app.keve.hdf5io.api.HDF5Dataset;
import app.keve.hdf5io.api.HDF5DatatypeAdapter;
import app.keve.hdf5io.api.HDF5FormatException;
import app.keve.hdf5io.api.datatype.HDF5Datatype;
import app.keve.hdf5io.api.datatype.HDF5Datatype.DatatypeBuilder;
import app.keve.hdf5io.api.util.ArrayUtil;
import app.keve.hdf5io.fileformat.BTreeV1DataManager;
import app.keve.hdf5io.fileformat.BTreeV1DataManager.TreeEntryData;
import app.keve.hdf5io.fileformat.H5Factory;
import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.H5Resolver;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.level1.ExtensibleArrayIndex;
import app.keve.hdf5io.fileformat.level1.ExtensibleArrayIndexBlock;
import app.keve.hdf5io.fileformat.level2.ObjectHeader;
import app.keve.hdf5io.fileformat.level2.ObjectHeaderV1;
import app.keve.hdf5io.fileformat.level2datatype.AbstractDatatypeBB;
import app.keve.hdf5io.fileformat.level2message.DataLayoutMessage;
import app.keve.hdf5io.fileformat.level2message.DataLayoutMessageCompact;
import app.keve.hdf5io.fileformat.level2message.DataLayoutMessageContiguous;
import app.keve.hdf5io.fileformat.level2message.DataLayoutMessageV1Contiguous;
import app.keve.hdf5io.fileformat.level2message.DataLayoutMessageV3Chunked;
import app.keve.hdf5io.fileformat.level2message.DataLayoutMessageV4Chunked;
import app.keve.hdf5io.fileformat.level2message.DataLayoutMessageV4Chunked.ChunkIndexingInformation;
import app.keve.hdf5io.fileformat.level2message.DataLayoutMessageV4Chunked.ExtensibleArrayIndexingInformation;
import app.keve.hdf5io.fileformat.level2message.DataLayoutMessageV4Chunked.Version2BTreeIndexingInformation;
import app.keve.hdf5io.fileformat.level2message.DataspaceMessage;
import app.keve.hdf5io.fileformat.level2message.DataspaceMessageV1;
import app.keve.hdf5io.fileformat.level2message.DatatypeMessage;
import app.keve.hdf5io.fileformat.level2message.FillValueMessageV1;
import app.keve.hdf5io.fileformat.level2message.FilterPipelineMessage;
import app.keve.hdf5io.fileformat.level2message.FilterPipelineMessage.FilterDescription;

public final class DatasetV extends AbstractNamedObjectV implements HDF5Dataset {
    private final HDF5Datatype datatype;
    private final DataLayoutMessage dataLayout;
    private final DataspaceMessage dataspace;
    private final FilterPipelineMessage filterPipeline;
    private final HDF5DatatypeAdapter adapter;

    public DatasetV(final long id, final H5Resolver hdf5Resolver, final ObjectHeader objectHeader)
            throws HDF5FormatException {
        super(id, hdf5Resolver, objectHeader);
        final HeaderMessageIterator it = new HeaderMessageIterator(hdf5Resolver, objectHeader.headerMessageIterator());
        HDF5Datatype datatypeL = null;
        DataLayoutMessage dataLayoutL = null;
        DataspaceMessage dataspaceL = null;
        FilterPipelineMessage filterPipelineL = null;
        while (it.hasNext()) {
            final ObjectHeader.HeaderMessageEntry<?> hme = it.next();
            switch (hme.getType()) {
            case DATATYPE:
                datatypeL = ((DatatypeMessage) hme.getMessage()).getDatatype();
                break;
            case DATA_LAYOUT:
                dataLayoutL = (DataLayoutMessage) (H5Object<?>) hme.getMessage();
                break;
            case DATASPACE:
                dataspaceL = (DataspaceMessage) (H5Object<?>) hme.getMessage();
                break;
            case FILTER_PIPELINE:
                filterPipelineL = (FilterPipelineMessage) hme.getMessage();
                break;
            default:
//                    System.err.println(Objects.toString(hme.getMessage()));
            }
        }
        if (null == datatypeL) {
            throw new HDF5FormatException("Must have datatype!");
        }
        if (null == dataLayoutL) {
            throw new HDF5FormatException("Must have layout!");
        }
        if (null == dataspaceL) {
            throw new HDF5FormatException("Must have dataspace!");
        }
        this.datatype = datatypeL;
        this.dataLayout = dataLayoutL;
        this.dataspace = dataspaceL;
        this.filterPipeline = filterPipelineL;
        this.adapter = objectHeader.context().h5Factory().datatypeAdapter(datatype, objectHeader.context());

    }

    @Override
    public HDF5Datatype getDatatype() {
        return datatype;
    }

    @Override
    public long[] getDimensionSizes() {
        return dataspace.getDimensionSizes();
    }

    @Override
    public long[] getDimensionMaxSizes() {
        return dataspace.getDimensionMaxSizes();
    }

    @Override
    public ShortBuffer getAsShortBuffer(final long... dim) {
        final ByteBuffer dataBuf = getDataBuf(dim);
        return null == dataBuf ? null : adapter.asShortBuffer(dataBuf);
    }

    @Override
    public IntBuffer getAsIntBuffer(final long... dim) {
        final ByteBuffer dataBuf = getDataBuf(dim);
        return null == dataBuf ? null : adapter.asIntBuffer(dataBuf);
    }

    @Override
    public LongBuffer getAsLongBuffer(final long... dim) {
        final ByteBuffer dataBuf = getDataBuf(dim);
        return null == dataBuf ? null : adapter.asLongBuffer(dataBuf);
    }

    @Override
    public IntStream getAsIntStream(final long... dim) {
        final ByteBuffer dataBuf = getDataBuf(dim);
        return null == dataBuf ? null : adapter.asIntStream(dataBuf);
    }

    @Override
    public LongStream getAsLongStream(final long... dim) {
        final ByteBuffer dataBuf = getDataBuf(dim);
        return null == dataBuf ? null : adapter.asLongStream(dataBuf);
    }

    @Override
    public Stream<?> getAsStream(final long... dim) {
        final ByteBuffer dataBuf = getDataBuf(dim);
        if (null == dataBuf) {
            return null;
//            throw new IllegalArgumentException("Implement!");
        }
        return adapter.asStream(dataBuf);
    }

    @Override
    public Object getAsObject(final long... dim) {
        final long[] dataDim = getDimensionSizes();
        final int[] returnDim = new int[dataDim.length - dim.length];
        for (int i = 0; i < returnDim.length; i++) {
            returnDim[i] = (int) dataDim[i + dim.length];

        }
        final ByteBuffer dataBuf = getDataBuf(dim);
        if (null == dataBuf) {
            // not in a contiguous area
            // TODO: filter chunks
            // TODO: jagged arrays
            // TODO: fill value
            final Type componentType = adapter.getJavaType();
            final Object mdArray = Array.newInstance((Class<?>) componentType, returnDim);
            getChunks().peek(System.err::println).forEach(chunk -> {
                final long[] lofs = chunk.getOffset();
                final int[] ofs = new int[lofs.length];
                for (int i = 0; i < ofs.length; i++) {
                    ofs[i] = (int) lofs[i];
                }
                final Object mdChunkArray = chunk.getAsObject();
                ArrayUtil.copyTo(mdArray, mdChunkArray, ofs);
            });
            return mdArray;
        } else {
            return adapter.asObject(dataBuf, returnDim);
        }
    }

    public final class ChunkJ implements Chunk {
        public final long[] offset;
        public final long[] size;
        private final Supplier<ByteBuffer> bufferSupplier;

        public ChunkJ(final long[] offset, final long[] size, final Supplier<ByteBuffer> bufferSupplier) {
            assert offset.length == size.length;
            this.offset = offset;
            this.size = size;
            this.bufferSupplier = bufferSupplier;
        }

        @Override
        public long[] getOffset() {
            return offset;
        }

        @Override
        public long[] getSize() {
            return size;
        }

        @Override
        public Object getAsObject(final long... dim) {
            final ByteBuffer contiguousData = bufferSupplier.get();
            final ByteBuffer dataBuf = subset(contiguousData, size, dim);
            final int[] returnDim = new int[size.length - dim.length];
            for (int i = 0; i < returnDim.length; i++) {
                returnDim[i] = (int) size[i + dim.length];
            }
            return adapter.asObject(dataBuf, returnDim);
        }

        @Override
        public ShortBuffer getAsShortBuffer(final long... dim) {
            final ByteBuffer dataBuf = subset(bufferSupplier.get(), dim);
            return adapter.asShortBuffer(dataBuf);
        }

        @Override
        public IntBuffer getAsIntBuffer(final long... dim) {
            final ByteBuffer dataBuf = subset(bufferSupplier.get(), dim);
            return adapter.asIntBuffer(dataBuf);
        }

        @Override
        public LongBuffer getAsLongBuffer(final long... dim) {
            final ByteBuffer dataBuf = subset(bufferSupplier.get(), dim);
            return adapter.asLongBuffer(dataBuf);
        }

        @Override
        public IntStream getAsIntStream(final long... dim) {
            final ByteBuffer dataBuf = subset(bufferSupplier.get(), dim);
            return adapter.asIntStream(dataBuf);
        }

        @Override
        public LongStream getAsLongStream(final long... dim) {
            final ByteBuffer dataBuf = subset(bufferSupplier.get(), dim);
            return adapter.asLongStream(dataBuf);
        }

        @Override
        public Stream<?> getAsStream(final long... dim) {
            final ByteBuffer dataBuf = subset(bufferSupplier.get(), dim);
            return adapter.asStream(dataBuf);
        }

        @Override
        public String toString() {
            final int maxLen = 10;
            return String.format("Chunk[origin=%s, size=%s, data=%s]",
                    offset != null ? Arrays.toString(Arrays.copyOf(offset, Math.min(offset.length, maxLen))) : null,
                    size != null ? Arrays.toString(Arrays.copyOf(size, Math.min(size.length, maxLen))) : null,
                    ArrayUtil.deepToString(getAsObject()));
        }
    }

    @Override
    public Stream<? extends Chunk> getChunks() {
        final long[] size;
        final long[] origin;
        switch (dataLayout.getLayoutClass()) {
        case COMPACT:
            final ByteBuffer buf = ((DataLayoutMessageCompact) dataLayout).getCompactData().slice();
            size = dataspace.getDimensionSizes();
            origin = new long[size.length];
            return Stream.of(new ChunkJ(origin, size, () -> buf));
        case CONTIGUOUS:
            final Resolvable<ByteBuffer> data = ((DataLayoutMessageContiguous) dataLayout).getData();
            size = dataspace.getDimensionSizes();
            origin = new long[size.length];
            if (null == data) {
                // TODO:
                return Stream.of(new ChunkJ(origin, size, () -> null));
            }
            return Stream.of(new ChunkJ(origin, size, () -> {
                final ByteBuffer dataBuf = data.resolve(hdf5Resolver);
                long bufSize = datatype.getElementSize();
                for (final long d : getDimensionSizes()) {
                    bufSize *= d;
                }
                return dataBuf.limit((int) bufSize);
            }));
        case CHUNKED:
            switch (dataLayout.getVersion()) {
            case 3:
                final DataLayoutMessageV3Chunked dataLayoutChunked = (DataLayoutMessageV3Chunked) dataLayout;
                size = dataLayoutChunked.getDimensionSizes();
                final BTreeV1DataManager bTreeV1DataManager = new BTreeV1DataManager(hdf5Resolver,
                        dataLayoutChunked.getData());
                final Iterator<TreeEntryData> it = bTreeV1DataManager.chunkIterator();
                return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.IMMUTABLE), false)
                        .map(ted -> {
                            final long[] originC = Arrays.copyOf(ted.leftKey.getDimChunkOffset(), size.length);
                            return new ChunkJ(originC, size, () -> unfilter(ted.child.resolve(hdf5Resolver)));
                        });
            case 4:
            default:
                throw new IllegalArgumentException("implement chunked for version 4 layout");
            }
        default:
            throw new IllegalArgumentException("Implement api for " + dataLayout.getLayoutClass());
        }
    }

    private ByteBuffer subset(final ByteBuffer contiguousData, final long[] dimSizes, final long... dim) {
        assert 0 == contiguousData.position();
        int ofs = 0;
        int size = contiguousData.remaining();
        for (int i = 0; i < dim.length; i++) {
            size /= dimSizes[i];
            ofs += dim[i] * size;
        }
        return contiguousData.position(ofs).limit(ofs + size).slice();
    }

    private ByteBuffer getDataBuf(final long... dim) {
        switch (dataLayout.getLayoutClass()) {
        case COMPACT:
            return subset(((DataLayoutMessageCompact) dataLayout).getCompactData().slice(), dim);
        case CONTIGUOUS:
            final Resolvable<ByteBuffer> data = ((DataLayoutMessageContiguous) dataLayout).getData();
            if (null == data) {
                return null;
            }
            final ByteBuffer dataBuf = data.resolve(hdf5Resolver);
            long size = datatype.getElementSize();
            for (final long d : getDimensionSizes()) {
                size *= d;
            }
            dataBuf.limit((int) size);
            return subset(dataBuf, dim);
        case CHUNKED:
            switch (dataLayout.getVersion()) {
            case 3:
                final BTreeV1DataManager bTreeV1DataManager = new BTreeV1DataManager(hdf5Resolver,
                        ((DataLayoutMessageV3Chunked) dataLayout).getData());
                // TODO: implement searching for the (sub-) dimension
                if (bTreeV1DataManager.isSingleChunk()) {
                    final Iterator<TreeEntryData> it = bTreeV1DataManager.chunkIterator();
                    final ByteBuffer rawData = it.next().child.resolve(hdf5Resolver);
                    return unfilter(rawData);
                }
                return null; // not contiguous surface
            case 4:
                final ChunkIndexingInformation indexInformation = ((DataLayoutMessageV4Chunked) dataLayout)
                        .getChunkIndexingInformation();
                if (indexInformation instanceof Version2BTreeIndexingInformation) {
                    final long index = ((Version2BTreeIndexingInformation) indexInformation).getIndex();
                    System.err.println(index);
                } else if (indexInformation instanceof ExtensibleArrayIndexingInformation) {
                    final Resolvable<ExtensibleArrayIndex> rIndex = ((ExtensibleArrayIndexingInformation) indexInformation)
                            .getIndex();
                    final ExtensibleArrayIndex index = rIndex.resolve(hdf5Resolver);
                    final Resolvable<ExtensibleArrayIndexBlock> rIndexBlock = index.getIndexBlock();
                    final ExtensibleArrayIndexBlock indexBlock = rIndexBlock.resolve(hdf5Resolver);

                    System.err.println(index);
                }
                throw new IllegalArgumentException(
                        "Implement Version4 chunked layout index " + indexInformation.getClass());

            default:
                throw new IllegalArgumentException();
            }

        default:
            throw new IllegalArgumentException("Implement api for " + dataLayout.getLayoutClass());
        }

    }

    private ByteBuffer unfilter(final ByteBuffer rawData) {
        if (null != filterPipeline) {
            for (final Iterator<? extends FilterDescription> fit = filterPipeline.filterIterator(); fit.hasNext();) {
                final FilterDescription filter = fit.next();
                switch (filter.getFilterIdentification()) {
                case 1:
                    final Inflater inflater = new Inflater();
                    inflater.setInput(rawData);
                    long size = datatype.getElementSize();
                    for (final long d : getDimensionSizes()) {
                        size *= d;
                    }
                    final ByteBuffer output = ByteBuffer.allocate((int) size);
                    try {
                        inflater.inflate(output);
                    } catch (final DataFormatException e) {
                        throw new IllegalArgumentException(e);
                    } finally {
                        inflater.end();
                    }
                    return output.flip();
                default:
                    throw new IllegalArgumentException("Implement filter: " + filter);
                }
            }
        }
        return rawData;
    }

    @Override
    public String toString() {
        final int maxLen = 10;
        return String.format(
                "DatasetV [datatype=%s, dataLayout=%s, dataspace=%s, getDimensionSizes()=%s, getDimensionMaxSizes()=%s]",
                datatype, dataLayout, dataspace,
                getDimensionSizes() != null
                        ? Arrays.toString(
                                Arrays.copyOf(getDimensionSizes(), Math.min(getDimensionSizes().length, maxLen)))
                        : null,
                getDimensionMaxSizes() != null
                        ? Arrays.toString(
                                Arrays.copyOf(getDimensionMaxSizes(), Math.min(getDimensionMaxSizes().length, maxLen)))
                        : null);
    }

    @SuppressWarnings("checkstyle:hiddenfield")
    public static final class BuilderV implements Builder {
        private final long id;
        private final H5Resolver h5Resolver;
        private final ObjectHeader newObjectHeader;
        private long[] dimensions;
        private long[] maxDimensions;
        private HDF5Datatype datatype;
        private Object data;
        private Object fillValue;
        private ByteBuffer fillValueBuffer;

        public BuilderV(final long id, final H5Resolver h5Resolver, final ObjectHeader newObjectHeader) {
            this.id = id;
            this.h5Resolver = h5Resolver;
            this.newObjectHeader = newObjectHeader;
        }

        @Override
        public HDF5Dataset build() throws IOException {
            final SizingContext sizingContext = newObjectHeader.context();
            final H5Factory h5Factory = sizingContext.h5Factory();

            final DataspaceMessageV1 dataspaceMessage = h5Factory.allocateLocal(DataspaceMessageV1.class,
                    sizingContext);

            if (null == dimensions) {
                dimensions = ArrayUtil.data2DimensionsLong(data);
            }
            dataspaceMessage.setDimensionSizes(dimensions);
            if (null != maxDimensions) {
                dataspaceMessage.setDimensionMaxSizes(maxDimensions);
            }

            if (null == datatype) {
                final DatatypeBuilder datatypeBuilder = h5Factory.datatypeBuilder(sizingContext);
                datatype = data2Datatype(datatypeBuilder, data);
            }

            final DatatypeMessage datatypeMessage = h5Factory.allocateLocal(DatatypeMessage.class, sizingContext);
            final AbstractDatatypeBB datatypeBB = AbstractDatatypeBB.of(datatype, sizingContext);
            datatypeMessage.setDatatype(datatypeBB);

            final FillValueMessageV1 fillValueMessage = h5Factory.allocateLocal(FillValueMessageV1.class,
                    sizingContext);
            if (null != fillValue) {
                final HDF5DatatypeAdapter adapter = h5Factory.datatypeAdapter(datatypeBB, sizingContext);
                fillValueBuffer = adapter.allocate(1);
                adapter.fromScalarObject(fillValueBuffer, 0, fillValue);
                fillValueBuffer.flip();
            }
            if (null != fillValueBuffer) {
                fillValueMessage.setFillValue(fillValueBuffer);
            } else {
                fillValueMessage.setFillValueDefined(true);
                fillValueMessage.setFillValueSize(OptionalInt.of(0));
            }

            long size = datatypeBB.getElementSize();

            final DataLayoutMessageV1Contiguous dataLayoutMessageContiguous = h5Factory
                    .allocateLocal(DataLayoutMessageV1Contiguous.class, sizingContext);
            final long[] dim2 = Arrays.copyOf(dimensions, dimensions.length + 1);
            dim2[dimensions.length] = size;
            dataLayoutMessageContiguous.setDimensionSizes(dim2);

            for (final long d : dimensions) {
                size *= d;
            }
            Resolvable<ByteBuffer> rDataBuffer = h5Factory.allocate((int) size);
            dataLayoutMessageContiguous.setData(rDataBuffer);
            rDataBuffer = h5Resolver.commit(rDataBuffer);

            if (null != data) {
                final ByteBuffer dataBuffer = rDataBuffer.resolve(h5Resolver);
                final HDF5DatatypeAdapter adapter = h5Factory.datatypeAdapter(datatypeBB, sizingContext);
                adapter.fromObject(dataBuffer, data);
            }

            ((ObjectHeaderV1) newObjectHeader).addHeaderMessage(dataspaceMessage);
            ((ObjectHeaderV1) newObjectHeader).addHeaderMessage(datatypeMessage);
            ((ObjectHeaderV1) newObjectHeader).addHeaderMessage(fillValueMessage);
            ((ObjectHeaderV1) newObjectHeader).addHeaderMessage(dataLayoutMessageContiguous);

            return new DatasetV(id, h5Resolver, newObjectHeader);
        }

        private HDF5Datatype data2Datatype(final DatatypeBuilder datatypeBuilder, final Object data) {
            final Type dClass = ArrayUtil.data2Type(data);
            return datatypeBuilder.forType(dClass).build();
        }

        @Override
        public Builder forData(final Object data) {
            this.data = data;
            return this;
        }

        @Override
        public Builder withDatatype(final HDF5Datatype datatype) {
            this.datatype = datatype;
            return this;
        }

        @Override
        public Builder withFillValue(final Object value) {
            this.fillValue = value;
            return this;
        }

        @Override
        public Builder withFillValue(final ByteBuffer value) {
            this.fillValueBuffer = value;
            return this;
        }

        @Override
        public Builder withDimensions(final long... dim) {
            this.dimensions = dim;
            return this;
        }

        @Override
        public Builder withMaxDimensions(final long... dim) {
            this.maxDimensions = dim;
            return this;
        }

    }
}
