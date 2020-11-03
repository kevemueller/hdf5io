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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.keve.hdf5io.api.HDF5File;
import app.keve.hdf5io.api.HDF5FormatException;
import app.keve.hdf5io.api.HDF5Group;
import app.keve.hdf5io.api.datatype.HDF5Datatype.DatatypeBuilder;
import app.keve.hdf5io.fileformat.AbstractBB;
import app.keve.hdf5io.fileformat.H5Context;
import app.keve.hdf5io.fileformat.H5MessageType;
import app.keve.hdf5io.fileformat.H5Registry;
import app.keve.hdf5io.fileformat.H5Resolver;
import app.keve.hdf5io.fileformat.LocalHeapManager;
import app.keve.hdf5io.fileformat.ResolutionListener;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextHeap;
import app.keve.hdf5io.fileformat.level0.FileDriverInfo;
import app.keve.hdf5io.fileformat.level0.FileDriverInfo.DriverInformation;
import app.keve.hdf5io.fileformat.level0.FileDriverInfo.FamilyDriverInformation;
import app.keve.hdf5io.fileformat.level0.FileDriverInfo.MultiDriverInformation;
import app.keve.hdf5io.fileformat.level0.FileDriverInfo.MultiDriverInformation.FileInfo;
import app.keve.hdf5io.fileformat.level0.Superblock;
import app.keve.hdf5io.fileformat.level0.SuperblockV0;
import app.keve.hdf5io.fileformat.level0.SuperblockV2;
import app.keve.hdf5io.fileformat.level0.SuperblockV3;
import app.keve.hdf5io.fileformat.level1.BTreeV1GroupLeaf;
import app.keve.hdf5io.fileformat.level1.SymbolTableEntry;
import app.keve.hdf5io.fileformat.level2.ObjectHeader;
import app.keve.hdf5io.fileformat.level2.ObjectHeaderV1;
import app.keve.hdf5io.fileformat.level2.ObjectHeaderV1.HeaderMessageEntryV1;
import app.keve.hdf5io.fileformat.level2datatype.AbstractDatatypeBB;
import app.keve.hdf5io.fileformat.level2message.SymbolTableMessage;
import app.keve.hdf5io.fileformat.level2message.SymbolTableMessageBB;
import app.keve.hdf5io.util.VMappedFile;

public final class LocalHDF5File extends AbstractH5File implements HDF5File {
    private final Logger logger = LoggerFactory.getLogger(LocalHDF5File.class);

    private final Path path;
    private final OpenOption[] openOptions;
    private final VMappedFile mappedFile;
    private Superblock superblock;
    private long preambleSize;

    private LocalHDF5File(final H5Registry h5Registry, final Path path, final OpenOption... openOptions)
            throws IOException {
        super(h5Registry);
        this.path = path;
        this.openOptions = openOptions;
        mappedFile = VMappedFile.of(path, openOptions);
        initialize();
    }

    private boolean isWriteable() {
        for (final OpenOption option : openOptions) {
            if (StandardOpenOption.WRITE == option) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long getPreambleSize() {
        return preambleSize;
    }

    @Override
    protected ByteBuffer at(final long address, final long length) throws IOException {
        assert length < Integer.MAX_VALUE;
        return mappedFile.at(address, (int) length);
    }

    public static long preambleSize(final long byteSize) {
        if (0 == byteSize) {
            return 0;
        }
        return 1L << Integer.max(9, 64 - Long.numberOfLeadingZeros(byteSize - 1));
    }

    private void initialize() throws IOException {
        if (0 == mappedFile.size()) {
            return;
        }
        // find Superblock
        ByteBuffer buf = mappedFile.at(0);
        int offset = 0;
        final SizingContext nullSC = SizingContext.ofUninitialized(this);
        Superblock superblockCandidate = Superblock.of(buf, nullSC);
        if (!superblockCandidate.isValid()) {
            offset = 256;
            do {
                offset *= 2;
                if (offset >= buf.remaining()) {
                    throw new HDF5FormatException("Could not find HDF5 header.");
                }
                superblockCandidate = Superblock.of(buf.duplicate().position(offset).slice(), nullSC);
            } while (!superblockCandidate.isValid());
        }
        if (offset > 0) {
            assert superblockCandidate.getBaseAddress() == 0 || offset == superblockCandidate.getBaseAddress();
            preambleSize = offset;
            // re-map the file with the current offset
            // adjust with baseaddress which might be non-zero??
            mappedFile.close();
            mappedFile.addMapping(0, offset, Integer.MAX_VALUE, path, openOptions);
            buf = mappedFile.at(0);
            superblockCandidate = Superblock.of(buf, nullSC);
        }
        this.superblock = superblockCandidate;
        if (superblockCandidate instanceof SuperblockV0) {
            final Resolvable<FileDriverInfo> rDriverInformationBlock = ((SuperblockV0) superblockCandidate)
                    .getDriverInformationBlock();
            if (null != rDriverInformationBlock) {
                final FileDriverInfo driverInformationBlock = rDriverInformationBlock.resolve(this);
                final DriverInformation driverInformation = driverInformationBlock.getDriverInformation();
                if (driverInformation instanceof MultiDriverInformation) {
                    final FileInfo[] filesInfo = ((MultiDriverInformation) driverInformation).getFileInfo();

                    String stem = null;

                    for (final FileInfo fileInfo : filesInfo) {
                        final Pattern filePattern = Pattern.compile(fileInfo.getName().replace("%s", "(.*)"));
                        final Matcher fileMatcher = filePattern.matcher(path.toString());
                        if (fileMatcher.matches()) {
                            // we found the entry that we started with
                            stem = fileMatcher.group(1);
                            break;
                        }
                    }
                    mappedFile.close();
                    for (final FileInfo fileInfo : filesInfo) {
                        final Path filePath = Path.of(fileInfo.getName().replace("%s", stem));
                        mappedFile.addMapping(fileInfo.getAddress(), 0, Integer.MAX_VALUE, filePath,
                                StandardOpenOption.READ);
                    }

                } else if (driverInformation instanceof FamilyDriverInformation) {
                    final long fileSize = ((FamilyDriverInformation) driverInformation).getMemberFileSize();
                    final long eof = superblockCandidate.getEndOfFileAddress();
                    final String pattern = path.toString().replace("00000", "%05d");
                    long vOffset = 0;
                    mappedFile.close();
                    for (int i = 0; i < (int) (eof / fileSize); i++) {
                        final Path p = Path.of(String.format(pattern, i));
                        mappedFile.addMapping(vOffset, 0, (int) fileSize, p, StandardOpenOption.READ);
                        vOffset += fileSize;
                    }
                } else {
                    throw new IllegalArgumentException("Implement driver information: " + driverInformation);
                }
                // replace Superblock to drop all references to the old MappedFile
                buf = mappedFile.at(0);
                this.superblock = Superblock.of(buf, nullSC);
                assert this.superblock.isValid();
            }
        }
    }

    Iterator<ObjectHeader.HeaderMessageEntry<?>> headerMessages(final ObjectHeader objectHeader) {
        return new HeaderMessageIterator(this, objectHeader.headerMessageIterator());
    }

    @Override
    protected <T> Resolvable<T> commit(final Resolvable<T> resolvable, final T t) throws IOException {
        final Resolvable<T> resolvable2 = super.commit(resolvable, t);
        if (t instanceof AbstractBB<?>) {
            final ResolvableH5Object<?, H5Context> rH5Object = (ResolvableH5Object<?, H5Context>) resolvable2;
            final ByteBuffer buf = ((AbstractBB<?>) t).getBuffer();
            final long[] offsetLength = mappedFile.append(buf);
            final Resolvable<T> newR = (Resolvable<T>) resolvable(offsetLength[0], offsetLength[1],
                    rH5Object.getTClass(), rH5Object.getContext());
            logger.trace("written: {}={} @{}:{} -> {}", resolvable2,
                    Integer.toUnsignedString(System.identityHashCode(t), 16), offsetLength[0], offsetLength[1], newR);
            return newR;
        } else if (t instanceof ByteBuffer) {
            final ByteBuffer buf = (ByteBuffer) t;
            final long[] offsetLength = mappedFile.append(buf);
            final Resolvable<T> newR = (Resolvable<T>) resolvable(offsetLength[0], (int) offsetLength[1]);
            logger.trace("written: {}={} @{}:{} -> {}", resolvable2,
                    Integer.toUnsignedString(System.identityHashCode(t), 16), offsetLength[0], offsetLength[1], newR);
            return newR;
        } else {
            throw new IllegalArgumentException(String.format("Cannot commit resolvable %s", resolvable2));
        }
    }

    @Override
    public long eof() throws IOException {
        return mappedFile.size();
    }

    @Override
    public void close() throws Exception {
        if (isWriteable()) {
            commitAll();
            superblock.setEndOfFileAddress(mappedFile.size());
        }
        mappedFile.close();
    }

    public Superblock getSuperblock() {
        return superblock;
    }

    @Override
    public HDF5Group getRootGroup() throws HDF5FormatException {
        switch (superblock.getVersionNumber()) {
        case 0:
        case 1:
            return new Link(this, "", ((SuperblockV0) superblock).getRootGroupSymbolTableEntry().getObjectHeader())
                    .getTarget().asGroup();
        case 2:
            return new Link(this, "", ((SuperblockV2) superblock).getRootGroupObjectHeader()).getTarget().asGroup();
        case 3:
            return new Link(this, "", ((SuperblockV3) superblock).getRootGroupObjectHeader()).getTarget().asGroup();
        default:
            throw new IllegalArgumentException("Implement Group V" + superblock.getVersionNumber());
        }
    }

    @Override
    public DatatypeBuilder getDatatypeBuilder() {
        return new AbstractDatatypeBB.DatatypeBuilderBB(superblock.context());
    }

    public static LocalHDF5File of(final H5Registry h5Registry, final Path p, final OpenOption... openOptions)
            throws IOException {
        return new LocalHDF5File(h5Registry, p, openOptions);
    }

    public static LocalHDF5File of(final Path p) throws IOException {
        return of(H5Registry.ofDefault(), p, StandardOpenOption.READ);
    }

    public static LocalHDF5File ofNew(final H5Registry h5Registry, final Path p, final OpenOption[] openOptions,
            final ByteBuffer preamble, final int offsetSize, final int lengthSize,
            final OptionalInt indexedStorageInternalNodeK, final OptionalInt groupInternalNodeK,
            final OptionalInt groupLeafNodeK) throws IOException {
        final LocalHDF5File newFile = of(h5Registry, p, openOptions);
        newFile.createNew(preamble, offsetSize, lengthSize, indexedStorageInternalNodeK, groupInternalNodeK,
                groupLeafNodeK);
        return newFile;
    }

    private void createNew(final ByteBuffer preamble, final int offsetSize, final int lengthSize,
            final OptionalInt indexedStorageInternalNodeK, final OptionalInt groupInternalNodeK,
            final OptionalInt groupLeafNodeK) throws IOException {
        SizingContext sizingContext = SizingContext.of(this, offsetSize, lengthSize, indexedStorageInternalNodeK,
                groupInternalNodeK, groupLeafNodeK);

        if (null != preamble) {
            writePreamble(preamble);
        }

        Resolvable<SuperblockV0> rSuperblockV0 = allocate(SuperblockV0.class, sizingContext);

        rSuperblockV0 = commit(rSuperblockV0);

        superblock = rSuperblockV0.resolve(this);
        final SuperblockV0 superblockV0 = (SuperblockV0) superblock;
        sizingContext = superblock.context();

        final Resolvable<ObjectHeaderV1> rObjectHeader = allocate(ObjectHeaderV1.class, sizingContext);
        final SymbolTableEntry rootGroupSymbolTableEntry = superblockV0.getRootGroupSymbolTableEntry();
        rootGroupSymbolTableEntry.setLinkName(new Resolvable<String>() {
            @Override
            public long getAddress() {
                return 0;
            }

            @Override
            public String resolve(final H5Resolver hdf5Resolver) {
                return null;
            }

            @Override
            public void addResolutionListener(final ResolutionListener listener, final Object param) {
            }
        });
        rootGroupSymbolTableEntry.setObjectHeader(rObjectHeader);

        final ObjectHeaderV1 objectHeader = rObjectHeader.resolve(this);

        final long messageDataSize = SymbolTableMessageBB.size(sizingContext);
        final HeaderMessageEntryV1 hme = (HeaderMessageEntryV1) objectHeader.addHeaderMessage(messageDataSize);
        hme.setType(H5MessageType.SYMBOL_TABLE);
        final SymbolTableMessage symbolTableMessage = hme.getMessage();
        symbolTableMessage.initialize();
        hme.setConstant(true);

        final LocalHeapManager localHeapManager = LocalHeapManager.of(this, sizingContext);
        symbolTableMessage.setLocalHeap(localHeapManager.getLocalHeap());

        final SizingContextHeap sizingContextHeap = SizingContextHeap.of(sizingContext,
                localHeapManager.getLocalHeap());
        final Resolvable<BTreeV1GroupLeaf> rBTreeV1Group = allocate(BTreeV1GroupLeaf.class, sizingContextHeap);
        symbolTableMessage.setBTree(rBTreeV1Group);
    }

    public void writePreamble(final ByteBuffer preamble) throws IOException {
        preambleSize = preambleSize(preamble.remaining());
        long padding = preambleSize - preamble.remaining();
//        if (preambleSize > getPreambleSize()) {
//            throw new HDF5FormatException("Cannot write longer preamble to existing HDF5 file.");
//        }
        mappedFile.append(preamble);
        if (padding > 0) {
            final ByteBuffer paddingBuf = ByteBuffer.allocate(4096);
            while (padding > 0) {
                if (padding < 4096) {
                    paddingBuf.limit((int) padding);
                }
                padding -= mappedFile.append(paddingBuf)[1];
                paddingBuf.rewind();
            }
        }
        mappedFile.close();
        mappedFile.addMapping(0, preambleSize, Integer.MAX_VALUE, path, StandardOpenOption.READ,
                StandardOpenOption.WRITE);
        System.out.println(mappedFile);
    }

}
