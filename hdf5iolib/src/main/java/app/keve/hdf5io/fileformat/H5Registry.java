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
package app.keve.hdf5io.fileformat;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.ToLongFunction;

import app.keve.hdf5io.api.HDF5Constants.Profile;
import app.keve.hdf5io.fileformat.level0.FileDriverInfo;
import app.keve.hdf5io.fileformat.level0.FileDriverInfoV0;
import app.keve.hdf5io.fileformat.level0.FileDriverInfoV0BB;
import app.keve.hdf5io.fileformat.level0.Superblock;
import app.keve.hdf5io.fileformat.level0.SuperblockV0;
import app.keve.hdf5io.fileformat.level0.SuperblockV0BB;
import app.keve.hdf5io.fileformat.level0.SuperblockV1;
import app.keve.hdf5io.fileformat.level0.SuperblockV1BB;
import app.keve.hdf5io.fileformat.level0.SuperblockV2;
import app.keve.hdf5io.fileformat.level0.SuperblockV2BB;
import app.keve.hdf5io.fileformat.level0.SuperblockV3;
import app.keve.hdf5io.fileformat.level0.SuperblockV3BB;
import app.keve.hdf5io.fileformat.level1.AbstractBTreeV2NodeBB;
import app.keve.hdf5io.fileformat.level1.BTreeV1;
import app.keve.hdf5io.fileformat.level1.BTreeV1Data;
import app.keve.hdf5io.fileformat.level1.BTreeV1DataInternal;
import app.keve.hdf5io.fileformat.level1.BTreeV1DataInternalBB;
import app.keve.hdf5io.fileformat.level1.BTreeV1DataLeaf;
import app.keve.hdf5io.fileformat.level1.BTreeV1DataLeafBB;
import app.keve.hdf5io.fileformat.level1.BTreeV1Group;
import app.keve.hdf5io.fileformat.level1.BTreeV1GroupInternal;
import app.keve.hdf5io.fileformat.level1.BTreeV1GroupInternalBB;
import app.keve.hdf5io.fileformat.level1.BTreeV1GroupLeaf;
import app.keve.hdf5io.fileformat.level1.BTreeV1GroupLeafBB;
import app.keve.hdf5io.fileformat.level1.BTreeV2;
import app.keve.hdf5io.fileformat.level1.BTreeV2BB;
import app.keve.hdf5io.fileformat.level1.BTreeV2LeafNode;
import app.keve.hdf5io.fileformat.level1.BTreeV2LeafNodeBB;
import app.keve.hdf5io.fileformat.level1.BTreeV2Node;
import app.keve.hdf5io.fileformat.level1.ExtensibleArrayIndex;
import app.keve.hdf5io.fileformat.level1.ExtensibleArrayIndexBlock;
import app.keve.hdf5io.fileformat.level1.ExtensibleArrayIndexBlockV0;
import app.keve.hdf5io.fileformat.level1.ExtensibleArrayIndexBlockV0BB;
import app.keve.hdf5io.fileformat.level1.ExtensibleArrayIndexV0;
import app.keve.hdf5io.fileformat.level1.ExtensibleArrayIndexV0BB;
import app.keve.hdf5io.fileformat.level1.FixedArrayIndex;
import app.keve.hdf5io.fileformat.level1.FixedArrayIndexV0;
import app.keve.hdf5io.fileformat.level1.FixedArrayIndexV0BB;
import app.keve.hdf5io.fileformat.level1.FractalHeap;
import app.keve.hdf5io.fileformat.level1.FractalHeapBB;
import app.keve.hdf5io.fileformat.level1.GlobalHeapCollection;
import app.keve.hdf5io.fileformat.level1.GlobalHeapCollectionBB;
import app.keve.hdf5io.fileformat.level1.GlobalHeapId;
import app.keve.hdf5io.fileformat.level1.GlobalHeapIdBB;
import app.keve.hdf5io.fileformat.level1.GlobalHeapObject;
import app.keve.hdf5io.fileformat.level1.GlobalHeapObjectBB;
import app.keve.hdf5io.fileformat.level1.GroupSymbolTableNode;
import app.keve.hdf5io.fileformat.level1.GroupSymbolTableNodeBB;
import app.keve.hdf5io.fileformat.level1.LocalHeap;
import app.keve.hdf5io.fileformat.level1.LocalHeapBB;
import app.keve.hdf5io.fileformat.level1.SharedObjectHeaderMessageTable;
import app.keve.hdf5io.fileformat.level1.SharedObjectHeaderMessageTableBB;
import app.keve.hdf5io.fileformat.level1.SymbolTableEntry;
import app.keve.hdf5io.fileformat.level1.SymbolTableEntryBB;
import app.keve.hdf5io.fileformat.level2.AbstractObjectHeader;
import app.keve.hdf5io.fileformat.level2.ObjectHeader;
import app.keve.hdf5io.fileformat.level2.ObjectHeaderContinuationBlock;
import app.keve.hdf5io.fileformat.level2.ObjectHeaderContinuationBlockV1;
import app.keve.hdf5io.fileformat.level2.ObjectHeaderContinuationBlockV1BB;
import app.keve.hdf5io.fileformat.level2.ObjectHeaderContinuationBlockV2;
import app.keve.hdf5io.fileformat.level2.ObjectHeaderContinuationBlockV2BB;
import app.keve.hdf5io.fileformat.level2.ObjectHeaderV1;
import app.keve.hdf5io.fileformat.level2.ObjectHeaderV1BB;
import app.keve.hdf5io.fileformat.level2.ObjectHeaderV2;
import app.keve.hdf5io.fileformat.level2.ObjectHeaderV2BB;
import app.keve.hdf5io.fileformat.level2datatype.AbstractDatatypeBB;
import app.keve.hdf5io.fileformat.level2datatype.CompoundV1BB;
import app.keve.hdf5io.fileformat.level2datatype.CompoundV2BB;
import app.keve.hdf5io.fileformat.level2message.*;

public final class H5Registry {
    // InterfaceClass, or InterfaceVersionClass
    // -> minSize
    // -> maxSize
    //
    // InterfaceClass (+ Profile + R/W) -> instantiate

    private static final Map<Class<? extends H5Object<?>>, List<Class<? extends H5Object<?>>>> ALL_VERSIONS;
    private static final Map<Class<? extends H5Object<? extends H5Context>>, H5ObjectInfo> ALL_INFO;
    private static final LinkedHashMap<Integer, H5MessageInfo> DEFAULT_MESSAGE_MAP;

    private final Profile profile;
    private final Map<Integer, H5MessageInfo> messageTypeMap;
    private final LinkedHashMap<Class<? extends H5Object<? extends H5Context>>, H5ObjectInfo> sizedInfo;

    private H5Registry(final Profile profile, final Map<Integer, H5MessageInfo> messageTypeMap) {
        this.profile = profile;
        this.sizedInfo = new LinkedHashMap<>(ALL_INFO);
        this.messageTypeMap = messageTypeMap;
        if (null != profile && false) {
            for (final Entry<Class<? extends H5Object<? extends H5Context>>, H5ObjectInfo> sizedEntry : sizedInfo
                    .entrySet()) {
                final H5ObjectInfo info = sizedEntry.getValue();
                // todo: clean up
                if (null != info.wClass) {
                    final H5ObjectInfo wInfo = ALL_INFO.get(info.wClass);
                    sizedEntry.setValue(wInfo);
                } else {
                    // look for writeable version matching profile
                    final List<Class<? extends H5Object<?>>> avs = ALL_VERSIONS.get(sizedEntry.getKey());
                    if (null != avs) {
                        for (final Class<? extends H5Object<?>> av : avs) {
                            final H5ObjectInfo avInfo = sizedInfo.get(av);
                            if (null != avInfo) {
                                final H5ObjectInfo wInfo = new H5ObjectInfo(info.minSize, info.maxSize, avInfo.of,
                                        avInfo.wClass);
                                sizedEntry.setValue(wInfo);
                            }
                        }
                    }
                }
            }
        }
    }

    public static final class H5MessageInfo {
        public final int typeNum;
        public final String name;
        public final H5MessageStatus status;
        public final Class<? extends H5Object<?>> messageClass;

        private H5MessageInfo(final int typeNum, final String name, final H5MessageStatus status,
                final Class<? extends H5Object<?>> messageClass) {
            this.typeNum = typeNum;
            this.name = name;
            this.status = status;
            this.messageClass = messageClass;
        }

        public static H5MessageInfo of(final int typeNum, final String name, final H5MessageStatus status,
                final Class<? extends H5Object<?>> messageClass) {
            return new H5MessageInfo(typeNum, name, status, messageClass);
        }

        public static H5MessageInfo of(final H5MessageType messageType) {
            return new H5MessageInfo(messageType.typeNum, messageType.name, messageType.status,
                    messageType.messageClass);
        }
    }

    public static final class H5ObjectInfo {
        public final BiFunction<ByteBuffer, ? extends H5Context, ? extends H5Object<?>> of;
        public final Class<? extends H5ObjectW<?>> wClass;
        private final ToLongFunction<? extends H5Context> minSize;
        private final ToLongFunction<? extends H5Context> maxSize;

        private H5ObjectInfo(final ToLongFunction<? extends H5Context> minSize,
                final ToLongFunction<? extends H5Context> maxSize,
                final BiFunction<ByteBuffer, ? extends H5Context, ? extends H5Object<?>> of,
                final Class<? extends H5ObjectW<?>> wClass) {
            this.minSize = minSize;
            this.maxSize = maxSize;
            this.of = of;
            this.wClass = wClass;
        }

        public long minSize(final H5Context context) {
            final ToLongFunction<H5Context> m = (ToLongFunction<H5Context>) minSize;
            return m.applyAsLong(context);
        }

        public long maxSize(final H5Context context) {
            final ToLongFunction<H5Context> m = (ToLongFunction<H5Context>) maxSize;
            return m.applyAsLong(context);
        }

        public static <T extends H5Object<H5Context>> H5ObjectInfo of(final long minSize, final long maxSize,
                final BiFunction<ByteBuffer, H5Context, T> of) {
            return new H5ObjectInfo(sc -> minSize, sc -> maxSize, of, null);
        }

        public static <T extends H5Object<H5Context>> H5ObjectInfo of(final long minSize, final long maxSize,
                final BiFunction<ByteBuffer, H5Context, T> of, final Class<? extends H5ObjectW<?>> wClass) {
            return new H5ObjectInfo(sc -> minSize, sc -> maxSize, of, wClass);
        }

        public static <T extends H5Object<S>, S extends SizingContext> H5ObjectInfo of(
                final ToLongFunction<SizingContext> minSize, final ToLongFunction<SizingContext> maxSize,
                final BiFunction<ByteBuffer, S, T> of) {
            return new H5ObjectInfo(minSize, maxSize, of, null);
        }

        public static <T extends H5Object<S>, S extends SizingContext> H5ObjectInfo of(
                final ToLongFunction<SizingContext> minSize, final ToLongFunction<SizingContext> maxSize,
                final BiFunction<ByteBuffer, S, T> of, final Class<? extends H5ObjectW<?>> wClass) {
            return new H5ObjectInfo(minSize, maxSize, of, wClass);
        }

    }

    @SuppressWarnings("unchecked")
    private static <T extends H5Object<? extends H5Context>> void register(final Class<T> h5Class,
            final List<Class<? extends T>> versions) {
        final List<?> v = versions;
        ALL_VERSIONS.put(h5Class, (List<Class<? extends H5Object<?>>>) v);
    }

    private static <T extends H5Object<H5Context>> void register(final Class<T> h5Class, final long minSize,
            final long maxSize, final BiFunction<ByteBuffer, H5Context, T> ofR) {
        ALL_INFO.put(h5Class, H5ObjectInfo.of(minSize, maxSize, ofR));
    }

    @SuppressWarnings("unchecked")
    private static <T extends H5Object<H5Context>, W extends T> void register(final Class<T> h5Class,
            final long minSize, final long maxSize, final BiFunction<ByteBuffer, H5Context, T> ofR,
            final Class<W> h5WClass, final BiFunction<ByteBuffer, H5Context, W> ofW) {
        ALL_INFO.put(h5Class, H5ObjectInfo.of(minSize, maxSize, ofR, (Class<? extends H5ObjectW<?>>) h5WClass));
        ALL_INFO.put(h5WClass, H5ObjectInfo.of(minSize, maxSize, ofW));
    }

    private static <T extends H5Object<S>, S extends SizingContext, W extends T> void register(final Class<T> h5Class,
            final ToLongFunction<SizingContext> minSize, final ToLongFunction<SizingContext> maxSize,
            final BiFunction<ByteBuffer, S, T> ofR) {
        ALL_INFO.put(h5Class, H5ObjectInfo.of(minSize, maxSize, ofR));
    }

    @SuppressWarnings("unchecked")
    private static <T extends H5Object<S>, S extends SizingContext, W extends T> void register(final Class<T> h5Class,
            final ToLongFunction<SizingContext> minSize, final ToLongFunction<SizingContext> maxSize,
            final BiFunction<ByteBuffer, S, T> ofR, final Class<W> h5WClass, final BiFunction<ByteBuffer, S, W> ofW) {
        ALL_INFO.put(h5Class, H5ObjectInfo.of(minSize, maxSize, ofR, (Class<? extends H5ObjectW<?>>) h5WClass));
        ALL_INFO.put(h5WClass, H5ObjectInfo.of(minSize, maxSize, ofW));
    }

    static {
        ALL_VERSIONS = new LinkedHashMap<>();
        ALL_INFO = new LinkedHashMap<>();
        DEFAULT_MESSAGE_MAP = new LinkedHashMap<>();

        // II. Disk Format: Level 0 - File Metadata
        // (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#FileMetaData)
        /// II.A. Disk Format: Level 0A - Format Signature and Superblock
        // (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#Superblock)
        register(Superblock.class,
                List.of(SuperblockV0.class, SuperblockV1.class, SuperblockV2.class, SuperblockV3.class));
        register(Superblock.class, Superblock::minSize, Superblock::maxSize, Superblock::of);
        register(SuperblockV0.class, SuperblockV0BB::size, SuperblockV0BB::size, SuperblockV0BB::of, SuperblockV0.class,
                SuperblockV0BB::of);
        register(SuperblockV1.class, SuperblockV1BB::size, SuperblockV1BB::size, SuperblockV1BB::of);
        register(SuperblockV2.class, SuperblockV2BB::size, SuperblockV2BB::size, SuperblockV2BB::of);
        register(SuperblockV3.class, SuperblockV3BB::size, SuperblockV3BB::size, SuperblockV3BB::of);

        /// II.B. Disk Format: Level 0B - File Driver Info
        /// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#DriverInfo)
        register(FileDriverInfo.class, List.of(FileDriverInfoV0.class));
        register(FileDriverInfo.class, FileDriverInfo.MIN_SIZE, FileDriverInfo.MAX_SIZE, FileDriverInfo::of);
        register(FileDriverInfoV0.class, FileDriverInfoV0BB.MIN_SIZE, FileDriverInfoV0BB.MAX_SIZE,
                FileDriverInfoV0BB::new);

        register(FileDriverInfo.DriverInformation.class,
                List.of(FileDriverInfo.FamilyDriverInformation.class, FileDriverInfo.MultiDriverInformation.class));
        register(FileDriverInfo.DriverInformation.class, FileDriverInfoV0BB.AbstractDriverInformation.MIN_SIZE,
                FileDriverInfoV0BB.AbstractDriverInformation.MAX_SIZE,
                FileDriverInfoV0BB.AbstractDriverInformation::of);
        register(FileDriverInfo.FamilyDriverInformation.class, FileDriverInfoV0BB.FamilyDriverInformationBB.SIZE,
                FileDriverInfoV0BB.FamilyDriverInformationBB.SIZE, FileDriverInfoV0BB.FamilyDriverInformationBB::new);
        register(FileDriverInfo.MultiDriverInformation.class, FileDriverInfoV0BB.MultiDriverInformationBB.MIN_SIZE,
                FileDriverInfoV0BB.MultiDriverInformationBB.MAX_SIZE, FileDriverInfoV0BB.MultiDriverInformationBB::new);

        /// II.C. Disk Format: Level 0C - Superblock Extension
        /// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#SuperblockExt)
        //// .. is an ObjectHeader

        // III. Disk Format: Level 1 - File Infrastructure
        // (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#FileInfra)
        /// III.A.2. Disk Format: Level 1A2 - Version 2 B-trees
        /// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#V2Btrees)
        //// TODO:

        /// III.A. Disk Format: Level 1A - B-trees and B-tree Nodes
        // (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#Btrees)
        /// III.A.1. Disk Format: Level 1A1 - Version 1 B-trees
        // (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#V1Btrees)

        register(BTreeV1.class, List.of(BTreeV1Data.class, BTreeV1Group.class));
        register(BTreeV1.class, BTreeV1::minSize, BTreeV1::maxSize, BTreeV1::of);

        register(BTreeV1Data.class, List.of(BTreeV1DataLeaf.class, BTreeV1DataInternal.class));
        register(BTreeV1Data.class, BTreeV1Data::minSize, BTreeV1Data::maxSize, BTreeV1Data::of);
        register(BTreeV1DataLeaf.class, BTreeV1DataLeafBB::minSize, BTreeV1DataLeafBB::maxSize, BTreeV1DataLeafBB::new);
        register(BTreeV1DataInternal.class, BTreeV1DataInternalBB::minSize, BTreeV1DataInternalBB::maxSize,
                BTreeV1DataInternalBB::new);

        register(BTreeV1Group.class, List.of(BTreeV1GroupLeaf.class, BTreeV1GroupInternal.class));
        register(BTreeV1Group.class, BTreeV1Group::minSize, BTreeV1Group::maxSize, BTreeV1Group::of);
        register(BTreeV1GroupLeaf.class, BTreeV1GroupLeafBB::size, BTreeV1GroupLeafBB::size, BTreeV1GroupLeafBB::new,
                BTreeV1GroupLeaf.class, BTreeV1GroupLeafBB::new);
        register(BTreeV1GroupInternal.class, BTreeV1GroupInternalBB::size, BTreeV1GroupInternalBB::size,
                BTreeV1GroupInternalBB::new);

        /// III.A.2. Disk Format: Level 1A2 - Version 2 B-trees
        /// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#V2Btrees)
        register(BTreeV2.class, List.of());
        register(BTreeV2.class, BTreeV2BB::size, BTreeV2BB::size, BTreeV2BB::new);

        register(BTreeV2Node.class, List.of(BTreeV2LeafNode.class));
        register(BTreeV2Node.class, BTreeV2Node::minSize, BTreeV2Node::maxSize, BTreeV2Node::of);

        register(BTreeV2Node.Record.class, List.of(BTreeV2Node.LinkNameRecord.class));
        register(BTreeV2Node.Record.class, AbstractBTreeV2NodeBB.AbstractRecordBB::size,
                AbstractBTreeV2NodeBB.AbstractRecordBB::size, AbstractBTreeV2NodeBB.AbstractRecordBB::of);
        register(BTreeV2Node.LinkNameRecord.class, AbstractBTreeV2NodeBB.LinkNameRecordBB::size,
                AbstractBTreeV2NodeBB.LinkNameRecordBB::size, AbstractBTreeV2NodeBB.LinkNameRecordBB::new);

        register(BTreeV2LeafNode.class, List.of());
        register(BTreeV2LeafNode.class, BTreeV2LeafNodeBB::minSize, BTreeV2LeafNodeBB::maxSize, BTreeV2LeafNodeBB::new);

        /// III.B. Disk Format: Level 1B - Group Symbol Table Nodes
        /// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#SymbolTable)
        register(GroupSymbolTableNode.class, List.of());
        register(GroupSymbolTableNode.class, GroupSymbolTableNodeBB::size, GroupSymbolTableNodeBB::size,
                GroupSymbolTableNodeBB::new);

        /// III.C. Disk Format: Level 1C - Symbol Table Entry
        /// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#SymbolTableEntry)
        register(SymbolTableEntry.class, List.of());
        register(SymbolTableEntry.class, SymbolTableEntryBB::size, SymbolTableEntryBB::size, SymbolTableEntryBB::new,
                SymbolTableEntry.class, SymbolTableEntryBB::new);

        /// III.D. Disk Format: Level 1D - Local Heaps
        /// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#LocalHeap)
        register(LocalHeap.class, List.of());
        register(LocalHeap.class, LocalHeapBB::size, LocalHeapBB::size, LocalHeapBB::new, LocalHeap.class,
                LocalHeapBB::new);
        register(LocalHeap.LocalHeapDataSegment.class, LocalHeapBB.LocalHeapDataSegmentBB::minSize,
                LocalHeapBB.LocalHeapDataSegmentBB::maxSize, LocalHeapBB.LocalHeapDataSegmentBB::new,
                LocalHeapBB.LocalHeapDataSegment.class, LocalHeapBB.LocalHeapDataSegmentBB::new);

        /// III.E. Disk Format: Level 1E - Global Heap
        /// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#GlobalHeap)
        register(GlobalHeapCollection.class, List.of());
        register(GlobalHeapCollection.class, GlobalHeapCollectionBB::minSize, GlobalHeapCollectionBB::maxSize,
                GlobalHeapCollectionBB::new);
        register(GlobalHeapObject.class, List.of());
        register(GlobalHeapObject.class, GlobalHeapObjectBB::minSize, GlobalHeapObjectBB::maxSize,
                GlobalHeapObjectBB::new);
        register(GlobalHeapId.class, List.of());
        register(GlobalHeapId.class, GlobalHeapIdBB::minSize, GlobalHeapIdBB::maxSize, GlobalHeapIdBB::new);

        /// III.F. Disk Format: Level 1F - Global Heap Block for Virtual Datasets
        /// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#GlobalHeapVDS)
        //// TODO:
        /// III.G. Disk Format: Level 1G - Fractal Heap
        /// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#FractalHeap)
        register(FractalHeap.class, List.of());
        register(FractalHeap.class, FractalHeapBB::minSize, FractalHeapBB::minSize, FractalHeapBB::new);

        register(FractalHeap.HeapBlock.class, List.of(FractalHeap.DirectBlock.class));
        register(FractalHeap.HeapBlock.class, FractalHeapBB.AbstractHeapBlockBB::minSize,
                FractalHeapBB.AbstractHeapBlockBB::maxSize, FractalHeapBB.AbstractHeapBlockBB::of);
        register(FractalHeap.DirectBlock.class, FractalHeapBB.DirectBlockBB::minSize,
                FractalHeapBB.DirectBlockBB::maxSize, FractalHeapBB.DirectBlockBB::new);

        /// III.H. Disk Format: Level 1H - Free-space Manager
        /// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#FreeSpaceManager)
        //// TODO:
        /// III.I. Disk Format: Level 1I - Shared Object Header Message Table
        /// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#SOHMTable)
        register(SharedObjectHeaderMessageTable.class, SharedObjectHeaderMessageTableBB::minSize,
                SharedObjectHeaderMessageTableBB::maxSize, SharedObjectHeaderMessageTableBB::new);
        register(SharedObjectHeaderMessageTable.SharedObjectHeaderMessageIndex.class, List.of());
        register(SharedObjectHeaderMessageTable.SharedObjectHeaderMessageIndex.class,
                SharedObjectHeaderMessageTableBB.AbstractSharedObjectHeaderMessageIndexBB::size,
                SharedObjectHeaderMessageTableBB.AbstractSharedObjectHeaderMessageIndexBB::size,
                SharedObjectHeaderMessageTableBB.AbstractSharedObjectHeaderMessageIndexBB::of);
        register(SharedObjectHeaderMessageTable.SharedObjectHeaderMessageIndexRecordList.class,
                SharedObjectHeaderMessageTableBB.AbstractSharedObjectHeaderMessageIndexBB::size,
                SharedObjectHeaderMessageTableBB.AbstractSharedObjectHeaderMessageIndexBB::size,
                SharedObjectHeaderMessageTableBB.SharedObjectHeaderMessageIndexRecordListBB::new);
        register(SharedObjectHeaderMessageTable.SharedObjectHeaderMessageIndexBTree.class,
                SharedObjectHeaderMessageTableBB.AbstractSharedObjectHeaderMessageIndexBB::size,
                SharedObjectHeaderMessageTableBB.AbstractSharedObjectHeaderMessageIndexBB::size,
                SharedObjectHeaderMessageTableBB.SharedObjectHeaderMessageIndexBTreeBB::new);

        // IV. Disk Format: Level 2 - Data Objects
        // (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#DataObject)
        /// IV.A.1. Disk Format: Level 2A1 - Data Object Header Prefix
        // (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#ObjectHeaderPrefix)
        register(ObjectHeader.class, List.of(ObjectHeaderV1.class, ObjectHeaderV2.class));
        register(ObjectHeader.class, ObjectHeader::minSize, ObjectHeader::maxSize, ObjectHeader::of);
        register(ObjectHeader.HeaderMessageEntry.class,
                List.of(ObjectHeaderV1.HeaderMessageEntryV1.class, ObjectHeaderV2.HeaderMessageEntryV2.class));
        register(ObjectHeader.HeaderMessageEntry.class, AbstractObjectHeader.AbstractHeaderMessageEntry::minSize,
                AbstractObjectHeader.AbstractHeaderMessageEntry::maxSize,
                AbstractObjectHeader.AbstractHeaderMessageEntry::of);

        //// IV.A.1.a. Version 1 Data Object Header Prefix
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#V1ObjectHeaderPrefix)
        register(ObjectHeaderV1.class, ObjectHeaderV1BB::minSize, ObjectHeaderV1BB::maxSize, ObjectHeaderV1BB::new,
                ObjectHeaderV1.class, ObjectHeaderV1BB::new);
        register(ObjectHeaderV1.HeaderMessageEntryV1.class, ObjectHeaderV1BB.HeaderMessageEntryV1BB::minSize,
                ObjectHeaderV1BB.HeaderMessageEntryV1BB::maxSize, ObjectHeaderV1BB.HeaderMessageEntryV1BB::new,
                ObjectHeaderV1.HeaderMessageEntryV1.class, ObjectHeaderV1BB.HeaderMessageEntryV1BB::new);
        //// IV.A.1.b. Version 2 Data Object Header Prefix
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#V2ObjectHeaderPrefix)
        register(ObjectHeaderV2.class, ObjectHeaderV2BB::minSize, ObjectHeaderV2BB::maxSize, ObjectHeaderV2BB::new);
        register(ObjectHeaderV2.HeaderMessageEntryV2.class, ObjectHeaderV2BB.AbstractHeaderMessageEntryV2BB::size,
                ObjectHeaderV2BB.AbstractHeaderMessageEntryV2BB::size,
                ObjectHeaderV2BB.AbstractHeaderMessageEntryV2BB::of);
        register(ObjectHeaderV2BB.HeaderMessageEntryV2NoCreationOrderBB.class,
                ObjectHeaderV2BB.HeaderMessageEntryV2NoCreationOrderBB::size,
                ObjectHeaderV2BB.HeaderMessageEntryV2NoCreationOrderBB::size,
                ObjectHeaderV2BB.HeaderMessageEntryV2NoCreationOrderBB::new);
        register(ObjectHeaderV2BB.HeaderMessageEntryV2WithCreationOrderBB.class,
                ObjectHeaderV2BB.HeaderMessageEntryV2WithCreationOrderBB::size,
                ObjectHeaderV2BB.HeaderMessageEntryV2WithCreationOrderBB::size,
                ObjectHeaderV2BB.HeaderMessageEntryV2WithCreationOrderBB::new);

        /// IV.A.2. Disk Format: Level 2A2 - Data Object Header Messages
        /// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#ObjectHeaderMessages)
        register(SharedMessage.class, List.of(SharedMessageV1.class, SharedMessageV2.class, SharedMessageV3.class));
        register(SharedMessage.class, SharedMessage::minSize, SharedMessage::maxSize, SharedMessage::of);

        register(SharedMessageV1.class, SharedMessageV1BB::size, SharedMessageV1BB::size, SharedMessageV1BB::new);
        register(SharedMessageV2.class, SharedMessageV2BB::size, SharedMessageV2BB::size, SharedMessageV2BB::new);
        register(SharedMessageV3.class, SharedMessageV3BB::minSize, SharedMessageV3BB::maxSize, SharedMessageV3BB::new);

        //// IV.A.2.a. The NIL Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#NILMessage)
        register(NILMessage.class, List.of());
        register(NILMessage.class, NILMessage.MIN_SIZE, NILMessage.MAX_SIZE, NILMessageBB::new, NILMessage.class,
                NILMessageBB::new);

        //// IV.A.2.b. The Dataspace Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#DataspaceMessage)
        register(DataspaceMessage.class, List.of(DataspaceMessageV1.class, DataspaceMessageV2.class));
        register(DataspaceMessage.class, DataspaceMessage::minSize, DataspaceMessage::maxSize, DataspaceMessage::of);
        register(DataspaceMessageV1.class, DataspaceMessageV1BB::minSize, DataspaceMessageV1BB::maxSize,
                DataspaceMessageV1BB::new, DataspaceMessageV1BB.class, DataspaceMessageV1BB::new);
        register(DataspaceMessageV2.class, DataspaceMessageV2BB::minSize, DataspaceMessageV2BB::maxSize,
                DataspaceMessageV2BB::new);
        //// IV.A.2.c. The Link Info Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#LinkInfoMessage)
        register(LinkInfoMessage.class, List.of(LinkInfoMessageV0.class));
        register(LinkInfoMessage.class, LinkInfoMessage::minSize, LinkInfoMessage::maxSize, LinkInfoMessage::of);
        register(LinkInfoMessageV0.class, LinkInfoMessageV0BB::minSize, LinkInfoMessageV0BB::maxSize,
                LinkInfoMessageV0BB::new);
        //// IV.A.2.d. The Datatype Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#DatatypeMessage)
        register(DatatypeMessage.class, List.of());
        register(DatatypeMessage.class, DatatypeMessage.MIN_SIZE, DatatypeMessage.MAX_SIZE, DatatypeMessageBB::new,
                DatatypeMessage.class, DatatypeMessageBB::new);

        register(AbstractDatatypeBB.class, List.of());
        register(AbstractDatatypeBB.class, AbstractDatatypeBB.MIN_SIZE, AbstractDatatypeBB.MAX_SIZE,
                AbstractDatatypeBB::of);

        register(CompoundV1BB.MemberV1BB.class, CompoundV1BB.MemberV1BB.MIN_SIZE, CompoundV1BB.MemberV1BB.MAX_SIZE,
                CompoundV1BB.MemberV1BB::new);
        register(CompoundV2BB.MemberV2BB.class, CompoundV2BB.MemberV2BB.MIN_SIZE, CompoundV2BB.MemberV2BB.MAX_SIZE,
                CompoundV2BB.MemberV2BB::new);
        // CompoundV3BB.MemberV3BB depends on parent's element size :(

        //// IV.A.2.e. The Data Storage - Fill Value (Old) Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#OldFillValueMessage)
        register(FillValueOldMessage.class, List.of());
        register(FillValueOldMessage.class, FillValueOldMessage.MIN_SIZE, FillValueOldMessage.MAX_SIZE,
                FillValueOldMessageBB::new);
        //// IV.A.2.f. The Data Storage - Fill Value Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#FillValueMessage)
        register(FillValueMessage.class,
                List.of(FillValueMessageV1.class, FillValueMessageV2.class, FillValueMessageV3.class));
        register(FillValueMessage.class, FillValueMessage.MIN_SIZE_ALL, FillValueMessage.MAX_SIZE_ALL,
                FillValueMessage::of);
        register(FillValueMessageV1.class, FillValueMessageV1BB.MIN_SIZE, FillValueMessageV1BB.MAX_SIZE,
                FillValueMessageV1BB::new, FillValueMessageV1.class, FillValueMessageV1BB::new);
        register(FillValueMessageV2.class, FillValueMessageV1BB.MIN_SIZE, FillValueMessageV1BB.MAX_SIZE,
                FillValueMessageV2BB::new, FillValueMessageV2.class, FillValueMessageV2BB::new);
        register(FillValueMessageV3.class, FillValueMessageV3BB.MIN_SIZE, FillValueMessageV3BB.MAX_SIZE,
                FillValueMessageV3BB::new, FillValueMessageV3.class, FillValueMessageV3BB::new);
        //// IV.A.2.g. The Link Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#LinkMessage)
        register(LinkMessage.class, List.of(LinkMessageV1.class));
        register(LinkMessage.class, LinkMessage::minSize, LinkMessage::maxSize, LinkMessage::of);
        register(LinkMessageV1.class, LinkMessageV1BB::minSize, LinkMessageV1BB::maxSize, LinkMessageV1BB::new);

        register(LinkMessage.LinkInformation.class,
                List.of(LinkMessage.ExternalLinkInformation.class, LinkMessage.HardLinkInformation.class,
                        LinkMessage.SoftLinkInformation.class, LinkMessage.UserLinkInformation.class));

        register(LinkMessage.LinkInformation.class, LinkMessageV1BB.AbstractLinkInformationBB::minSize,
                LinkMessageV1BB.AbstractLinkInformationBB::maxSize, LinkMessageV1BB.AbstractLinkInformationBB::of);

        register(LinkMessage.HardLinkInformation.class, LinkMessageV1BB.HardLinkInformationBB::size,
                LinkMessageV1BB.HardLinkInformationBB::size, LinkMessageV1BB.HardLinkInformationBB::new);
        register(LinkMessage.SoftLinkInformation.class, LinkMessageV1BB.SoftLinkInformationBB::minSize,
                LinkMessageV1BB.SoftLinkInformationBB::maxSize, LinkMessageV1BB.SoftLinkInformationBB::new);
        register(LinkMessage.ExternalLinkInformation.class, LinkMessageV1BB.ExternalLinkInformationBB::minSize,
                LinkMessageV1BB.ExternalLinkInformationBB::maxSize, LinkMessageV1BB.ExternalLinkInformationBB::new);
        register(LinkMessage.UserLinkInformation.class, LinkMessageV1BB.UserLinkInformationBB::minSize,
                LinkMessageV1BB.UserLinkInformationBB::maxSize, LinkMessageV1BB.UserLinkInformationBB::new);

        //// IV.A.2.h. The Data Storage - External Data Files Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#ExternalFileListMessage)
        register(ExternalDataFilesMessage.class, List.of(ExternalDataFilesMessageV1.class));
        register(ExternalDataFilesMessage.class, ExternalDataFilesMessageV1BB::minSize,
                ExternalDataFilesMessageV1BB::maxSize, ExternalDataFilesMessageV1BB::new);
        register(ExternalDataFilesMessageV1.class, ExternalDataFilesMessageV1BB::minSize,
                ExternalDataFilesMessageV1BB::maxSize, ExternalDataFilesMessageV1BB::new);
        register(ExternalDataFilesMessage.SlotDefinition.class,
                List.of(ExternalDataFilesMessageV1.SlotDefinitionV1.class));
        register(ExternalDataFilesMessage.SlotDefinition.class, ExternalDataFilesMessageV1BB.SlotDefinitionV1BB::size,
                ExternalDataFilesMessageV1BB.SlotDefinitionV1BB::size,
                ExternalDataFilesMessageV1BB.SlotDefinitionV1BB::new);
        register(ExternalDataFilesMessageV1.SlotDefinitionV1.class,
                ExternalDataFilesMessageV1BB.SlotDefinitionV1BB::size,
                ExternalDataFilesMessageV1BB.SlotDefinitionV1BB::size,
                ExternalDataFilesMessageV1BB.SlotDefinitionV1BB::new);
        //// IV.A.2.i. The Data Layout Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#LayoutMessage)
        register(DataLayoutMessage.class, List.of(DataLayoutMessageCompact.class, DataLayoutMessageContiguous.class,
                DataLayoutMessageChunked.class, DataLayoutMessageVirtualStorage.class));
        register(DataLayoutMessage.class, DataLayoutMessage::minSize, DataLayoutMessage::maxSize,
                DataLayoutMessage::of);

        register(DataLayoutMessageCompact.class, List.of(DataLayoutMessageV1Compact.class,
                DataLayoutMessageV2Compact.class, DataLayoutMessageV3Compact.class, DataLayoutMessageV4Compact.class));
        register(DataLayoutMessageCompact.class, DataLayoutMessageCompact::minSize, DataLayoutMessageCompact::maxSize,
                DataLayoutMessageCompact::of);
        register(DataLayoutMessageV1Compact.class, DataLayoutMessageV1CompactBB::minSize,
                DataLayoutMessageV1CompactBB::maxSize, DataLayoutMessageV1CompactBB::new);
        register(DataLayoutMessageV2Compact.class, DataLayoutMessageV2CompactBB::minSize,
                DataLayoutMessageV2CompactBB::maxSize, DataLayoutMessageV2CompactBB::new);
        register(DataLayoutMessageV3Compact.class, DataLayoutMessageV3CompactBB::minSize,
                DataLayoutMessageV3CompactBB::maxSize, DataLayoutMessageV3CompactBB::new);
        register(DataLayoutMessageV4Compact.class, DataLayoutMessageV4CompactBB::minSize,
                DataLayoutMessageV4CompactBB::maxSize, DataLayoutMessageV4CompactBB::new);

        register(DataLayoutMessageContiguous.class,
                List.of(DataLayoutMessageV1Contiguous.class, DataLayoutMessageV2Contiguous.class,
                        DataLayoutMessageV3Contiguous.class, DataLayoutMessageV4Contiguous.class));
        register(DataLayoutMessageContiguous.class, DataLayoutMessageContiguous::minSize,
                DataLayoutMessageContiguous::maxSize, DataLayoutMessageContiguous::of);
        register(DataLayoutMessageV1Contiguous.class, DataLayoutMessageV1ContiguousBB::minSize,
                DataLayoutMessageV1ContiguousBB::maxSize, DataLayoutMessageV1ContiguousBB::new,
                DataLayoutMessageV1ContiguousBB.class, DataLayoutMessageV1ContiguousBB::new);
        register(DataLayoutMessageV2Contiguous.class, DataLayoutMessageV2ContiguousBB::minSize,
                DataLayoutMessageV2ContiguousBB::maxSize, DataLayoutMessageV2ContiguousBB::new);
        register(DataLayoutMessageV3Contiguous.class, DataLayoutMessageV3ContiguousBB::size,
                DataLayoutMessageV3ContiguousBB::size, DataLayoutMessageV3ContiguousBB::new);
        register(DataLayoutMessageV4Contiguous.class, DataLayoutMessageV4ContiguousBB::size,
                DataLayoutMessageV4ContiguousBB::size, DataLayoutMessageV4ContiguousBB::new);

        register(DataLayoutMessageChunked.class, List.of(DataLayoutMessageV1Chunked.class,
                DataLayoutMessageV2Chunked.class, DataLayoutMessageV3Chunked.class, DataLayoutMessageV4Chunked.class));
        register(DataLayoutMessageChunked.class, DataLayoutMessageChunked::minSize, DataLayoutMessageChunked::maxSize,
                DataLayoutMessageChunked::of);
        register(DataLayoutMessageV1Chunked.class, DataLayoutMessageV1ChunkedBB::minSize,
                DataLayoutMessageV1ChunkedBB::maxSize, DataLayoutMessageV1ChunkedBB::new);
        register(DataLayoutMessageV2Chunked.class, DataLayoutMessageV2ChunkedBB::minSize,
                DataLayoutMessageV2ChunkedBB::maxSize, DataLayoutMessageV2ChunkedBB::new);
        register(DataLayoutMessageV3Chunked.class, DataLayoutMessageV3ChunkedBB::minSize,
                DataLayoutMessageV3ChunkedBB::maxSize, DataLayoutMessageV3ChunkedBB::new);
        register(DataLayoutMessageV4Chunked.class, DataLayoutMessageV4ChunkedBB::minSize,
                DataLayoutMessageV4ChunkedBB::maxSize, DataLayoutMessageV4ChunkedBB::new);

        register(DataLayoutMessageV4Chunked.ChunkIndexingInformation.class,
                List.of(DataLayoutMessageV4Chunked.ExtensibleArrayIndexingInformation.class,
                        DataLayoutMessageV4Chunked.FixedArrayIndexingInformation.class,
                        DataLayoutMessageV4Chunked.ImplicitIndexingInformation.class,
                        DataLayoutMessageV4Chunked.SingleChunkIndexingInformation.class,
                        DataLayoutMessageV4Chunked.Version2BTreeIndexingInformation.class));

        register(DataLayoutMessageV4Chunked.ChunkIndexingInformation.class,
                DataLayoutMessageV4ChunkedBB.AbstractChunkIndexingInformationBB::minSize,
                DataLayoutMessageV4ChunkedBB.AbstractChunkIndexingInformationBB::maxSize,
                DataLayoutMessageV4ChunkedBB.AbstractChunkIndexingInformationBB::of);

        register(DataLayoutMessageV4Chunked.ExtensibleArrayIndexingInformation.class,
                DataLayoutMessageV4ChunkedBB.ChunkIndexExtensibleArrayBB::size,
                DataLayoutMessageV4ChunkedBB.ChunkIndexExtensibleArrayBB::size,
                DataLayoutMessageV4ChunkedBB.ChunkIndexExtensibleArrayBB::new);

        register(DataLayoutMessageV4Chunked.FixedArrayIndexingInformation.class,
                DataLayoutMessageV4ChunkedBB.ChunkIndexFixedArrayBB::size,
                DataLayoutMessageV4ChunkedBB.ChunkIndexFixedArrayBB::size,
                DataLayoutMessageV4ChunkedBB.ChunkIndexFixedArrayBB::new);
        register(DataLayoutMessageV4Chunked.ImplicitIndexingInformation.class,
                DataLayoutMessageV4ChunkedBB.ChunkIndexImplicitBB::size,
                DataLayoutMessageV4ChunkedBB.ChunkIndexImplicitBB::size,
                DataLayoutMessageV4ChunkedBB.ChunkIndexImplicitBB::new);
        register(DataLayoutMessageV4Chunked.SingleChunkIndexingInformation.class,
                DataLayoutMessageV4ChunkedBB.ChunkIndexSingleChunkBB::size,
                DataLayoutMessageV4ChunkedBB.ChunkIndexSingleChunkBB::size,
                DataLayoutMessageV4ChunkedBB.ChunkIndexSingleChunkBB::new);
        register(DataLayoutMessageV4Chunked.Version2BTreeIndexingInformation.class,
                DataLayoutMessageV4ChunkedBB.ChunkIndexBTreeV2BB::size,
                DataLayoutMessageV4ChunkedBB.ChunkIndexBTreeV2BB::size,
                DataLayoutMessageV4ChunkedBB.ChunkIndexBTreeV2BB::new);

        register(DataLayoutMessageVirtualStorage.class, List.of(DataLayoutMessageV4VirtualStorage.class));
        register(DataLayoutMessageVirtualStorage.class, DataLayoutMessageVirtualStorage::size,
                DataLayoutMessageVirtualStorage::size, DataLayoutMessageVirtualStorage::of);
        register(DataLayoutMessageV4VirtualStorage.class, DataLayoutMessageV4VirtualStorageBB::size,
                DataLayoutMessageV4VirtualStorageBB::size, DataLayoutMessageV4VirtualStorageBB::new);

        //// IV.A.2.j. The Bogus Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#BogusMessage)
        register(BogusMessage.class, List.of());
        register(BogusMessage.class, BogusMessage.SIZE, BogusMessage.SIZE, BogusMessageBB::new);
        //// IV.A.2.k. The Group Info Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#GroupInfoMessage)
        register(GroupInfoMessage.class, List.of(GroupInfoMessageV0.class));
        register(GroupInfoMessage.class, GroupInfoMessageV0.MIN_SIZE, GroupInfoMessageV0.MAX_SIZE,
                GroupInfoMessageV0BB::new);
        register(GroupInfoMessageV0.class, GroupInfoMessageV0.MIN_SIZE, GroupInfoMessageV0.MAX_SIZE,
                GroupInfoMessageV0BB::new);
        //// IV.A.2.l. The Data Storage - Filter Pipeline Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#FilterMessage)
        register(FilterPipelineMessage.class, List.of(FilterPipelineMessageV1.class, FilterPipelineMessageV2.class));
        register(FilterPipelineMessage.class, FilterPipelineMessage.MIN_SIZE, FilterPipelineMessage.MAX_SIZE,
                FilterPipelineMessage::of);

        register(FilterPipelineMessageV1.class, FilterPipelineMessageV1BB.MIN_SIZE, FilterPipelineMessageV1BB.MAX_SIZE,
                FilterPipelineMessageV1BB::new);
        register(FilterPipelineMessageV2.class, FilterPipelineMessageV2BB.MIN_SIZE, FilterPipelineMessageV2BB.MAX_SIZE,
                FilterPipelineMessageV2BB::new);
        register(FilterPipelineMessage.FilterDescription.class, List.of(
                FilterPipelineMessageV1.FilterDescriptionV1.class, FilterPipelineMessageV2.FilterDescriptionV2.class));
        register(FilterPipelineMessage.FilterDescription.class, FilterPipelineMessage.FilterDescription.MIN_SIZE,
                FilterPipelineMessage.FilterDescription.MAX_SIZE, FilterPipelineMessage.FilterDescription::of);

        register(FilterPipelineMessageV1.FilterDescriptionV1.class,
                FilterPipelineMessageV1BB.FilterDescriptionV1BB.MIN_SIZE,
                FilterPipelineMessageV1BB.FilterDescriptionV1BB.MAX_SIZE,
                FilterPipelineMessageV1BB.FilterDescriptionV1BB::new);
        register(FilterPipelineMessageV2.FilterDescriptionV2.class,
                FilterPipelineMessageV2BB.FilterDescriptionV2BB.MIN_SIZE,
                FilterPipelineMessageV2BB.FilterDescriptionV2BB.MAX_SIZE,
                FilterPipelineMessageV2BB.FilterDescriptionV2BB::new);

        //// IV.A.2.m. The Attribute Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#AttributeMessage)
        register(AttributeMessage.class,
                List.of(AttributeMessageV1.class, AttributeMessageV2.class, AttributeMessageV3.class));
        register(AttributeMessage.class, AttributeMessage::minSize, AttributeMessage::maxSize, AttributeMessage::of);

        register(AttributeMessageV1.class, AttributeMessageV1BB::minSize, AttributeMessageV1BB::maxSize,
                AttributeMessageV1BB::new);
        register(AttributeMessageV2.class, AttributeMessageV2BB::minSize, AttributeMessageV2BB::maxSize,
                AttributeMessageV2BB::new);
        register(AttributeMessageV3.class, AttributeMessageV3BB::minSize, AttributeMessageV3BB::maxSize,
                AttributeMessageV3BB::new);
        //// IV.A.2.n. The Object Comment Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#CommentMessage)
        register(ObjectCommentMessage.class, List.of());
        register(ObjectCommentMessage.class, ObjectCommentMessage.MIN_SIZE, ObjectCommentMessage.MAX_SIZE,
                ObjectCommentMessageBB::new);
        //// IV.A.2.o. The Object Modification Time (Old) Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#OldModificationTimeMessage)
        register(ObjectModificationTimeOldMessage.class, List.of());
        register(ObjectModificationTimeOldMessage.class, ObjectModificationTimeOldMessage.SIZE,
                ObjectModificationTimeOldMessage.SIZE, ObjectModificationTimeOldMessageBB::new);
        //// IV.A.2.p. The Shared Message Table Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#SOHMTableMessage)
        register(SharedMessageTableMessage.class, List.of());
        register(SharedMessageTableMessage.class, SharedMessageTableMessageBB::size, SharedMessageTableMessageBB::size,
                SharedMessageTableMessageBB::new);
        //// IV.A.2.q. The Object Header Continuation Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#ContinuationMessage)
        register(ObjectHeaderContinuationMessage.class, List.of());
        register(ObjectHeaderContinuationMessage.class, ObjectHeaderContinuationMessageBB::size,
                ObjectHeaderContinuationMessageBB::size, ObjectHeaderContinuationMessageBB::new);
        register(ObjectHeaderContinuationBlock.class,
                List.of(ObjectHeaderContinuationBlockV1.class, ObjectHeaderContinuationBlockV2.class));
        register(ObjectHeaderContinuationBlock.class, ObjectHeaderContinuationBlock::minSize,
                ObjectHeaderContinuationBlock::maxSize, ObjectHeaderContinuationBlock::of);

        register(ObjectHeaderContinuationBlockV1.class, ObjectHeaderContinuationBlockV1BB::minSize,
                ObjectHeaderContinuationBlockV1BB::maxSize, ObjectHeaderContinuationBlockV1BB::new);
        register(ObjectHeaderContinuationBlockV2.class, ObjectHeaderContinuationBlockV2BB::minSize,
                ObjectHeaderContinuationBlockV2BB::maxSize, ObjectHeaderContinuationBlockV2BB::new);
        //// IV.A.2.r. The Symbol Table Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#SymbolTableMessage)
        register(SymbolTableMessage.class, List.of());
        register(SymbolTableMessage.class, SymbolTableMessageBB::size, SymbolTableMessageBB::size,
                SymbolTableMessageBB::of, SymbolTableMessage.class, SymbolTableMessageBB::new);
        //// IV.A.2.s. The Object Modification Time Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#ModificationTimeMessage)
        register(ObjectModificationTimeMessageV1.class, List.of());
        register(ObjectModificationTimeMessageV1.class, ObjectModificationTimeMessageV1.SIZE,
                ObjectModificationTimeMessageV1.SIZE, ObjectModificationTimeMessageV1BB::new);
        //// IV.A.2.t. The B-tree ‘K’ Values Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#BtreeKValuesMessage)
        register(BTreeKValuesMessage.class, List.of(BTreeKValuesMessageV0.class));
        register(BTreeKValuesMessage.class, BTreeKValuesMessage.SIZE, BTreeKValuesMessage.SIZE,
                BTreeKValuesMessage::of);
        register(BTreeKValuesMessageV0.class, BTreeKValuesMessageV0.SIZE, BTreeKValuesMessageV0.SIZE,
                BTreeKValuesMessageV0BB::new);

        //// IV.A.2.u. The Driver Info Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#DrvInfoMessage)
        register(DriverInfoMessage.class, List.of());
        register(DriverInfoMessage.class, DriverInfoMessageV0BB.MIN_SIZE, DriverInfoMessageV0BB.MAX_SIZE,
                DriverInfoMessageV0BB::new);
        //// IV.A.2.v. The Attribute Info Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#AinfoMessage)
        register(AttributeInfoMessage.class, List.of());
        register(AttributeInfoMessage.class, AttributeInfoMessageBB::minSize, AttributeInfoMessageBB::maxSize,
                AttributeInfoMessageBB::new);
        //// IV.A.2.w. The Object Reference Count Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#RefCountMessage)
        register(ObjectReferenceCountMessage.class, List.of());
        register(ObjectReferenceCountMessage.class, ObjectReferenceCountMessage.SIZE, ObjectReferenceCountMessage.SIZE,
                ObjectReferenceCountMessageV0BB::new);
        //// IV.A.2.x. The File Space Info Message
        //// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#FsinfoMessage)
        register(FileSpaceInfoMessage.class, List.of(FileSpaceInfoMessageV0.class, FileSpaceInfoMessageV1.class));
        register(FileSpaceInfoMessage.class, FileSpaceInfoMessage::minSize, FileSpaceInfoMessage::maxSize,
                FileSpaceInfoMessage::of);
        register(FileSpaceInfoMessageV0.class, FileSpaceInfoMessageV0BB::minSize, FileSpaceInfoMessageV0BB::maxSize,
                FileSpaceInfoMessageV0BB::new);
        register(FileSpaceInfoMessageV1.class, FileSpaceInfoMessageV1BB::minSize, FileSpaceInfoMessageV1BB::maxSize,
                FileSpaceInfoMessageV1BB::new);

        // VII. Appendix C: Types of Indexes for Dataset Chunks
        // (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#AppendixC)
        /// VII.A. The Single Chunk Index
        /// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#SingleChunk)
        /// VII.B. The Implicit Index
        /// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#Implicit)
        /// VII.C. The Fixed Array Index
        /// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#FixedArray)
        register(FixedArrayIndex.class, List.of(FixedArrayIndexV0.class));
        register(FixedArrayIndex.class, FixedArrayIndex::size, FixedArrayIndex::size, FixedArrayIndex::of);
        register(FixedArrayIndexV0.class, FixedArrayIndexV0BB::size, FixedArrayIndexV0BB::size,
                FixedArrayIndexV0BB::new);

        /// VII.D. The Extensible Array Index
        /// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#ExtensibleArray)

        register(ExtensibleArrayIndex.class, List.of(ExtensibleArrayIndexV0.class));
        register(ExtensibleArrayIndex.class, ExtensibleArrayIndex::size, ExtensibleArrayIndex::size,
                ExtensibleArrayIndex::of);
        register(ExtensibleArrayIndexV0.class, ExtensibleArrayIndexV0BB::size, ExtensibleArrayIndexV0BB::size,
                ExtensibleArrayIndexV0BB::new);

        register(ExtensibleArrayIndexBlock.class, List.of(ExtensibleArrayIndexBlockV0.class));
        register(ExtensibleArrayIndexBlock.class, ExtensibleArrayIndexBlock::size, ExtensibleArrayIndexBlock::size,
                ExtensibleArrayIndexBlock::of);
        register(ExtensibleArrayIndexBlockV0.class, ExtensibleArrayIndexBlockV0BB::size,
                ExtensibleArrayIndexBlockV0BB::size, ExtensibleArrayIndexBlockV0BB::new);

        /// VII.E. The Version 2 B-trees Index
        /// (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#AppendV2Btrees)

        // register all known message types
        for (final H5MessageType messageType : H5MessageType.values()) {
            DEFAULT_MESSAGE_MAP.put(messageType.typeNum, H5MessageInfo.of(messageType));
        }
    }

    public Map<Class<? extends H5Object<? extends H5Context>>, H5ObjectInfo> allInfo() {
        return Collections.unmodifiableMap(ALL_INFO);
    }

    public <T extends H5Object<S>, S extends H5Context> H5ObjectInfo info(final Class<T> tClass) {
        return sizedInfo.get(tClass);
    }

    /**
     * Return specific information of an implementor of T, matching the current
     * profile, that is also writable.
     * 
     * @param <T>    the H5Object type
     * @param <S>    the H5Context type
     * @param tClass the interface to implement
     * @return the object information
     */
    public <T extends H5Object<S>, S extends H5Context> H5ObjectInfo infoW(final Class<T> tClass) {
        return sizedInfo.get(tClass);
    }

    public H5MessageInfo messageInfo(final int type) {
        return messageTypeMap.get(type);
    }

    public <T extends H5Object<V>, V extends H5Context> long maxSize(final Class<T> interfaceClass,
            final V sizingContext) {
        final H5ObjectInfo sinfo = sizedInfo.get(interfaceClass);
        if (null != sinfo) {
            final ToLongFunction<V> f = (ToLongFunction<V>) sinfo.maxSize;
            return f.applyAsLong(sizingContext);
        }
        throw new IllegalArgumentException("No mapping for " + interfaceClass);
    }

    public <T extends H5Object<V>, V extends H5Context> long minSize(final Class<T> interfaceClass,
            final V sizingContext) {
        final H5ObjectInfo sinfo = sizedInfo.get(interfaceClass);
        if (null != sinfo) {
            final ToLongFunction<V> f = (ToLongFunction<V>) sinfo.minSize;
            return f.applyAsLong(sizingContext);
        }
        throw new IllegalArgumentException("No mapping for " + interfaceClass);
    }

    @SuppressWarnings("unchecked")
    public <T extends H5Object<S>, S extends H5Context> T instance(final Class<T> tClass, final ByteBuffer buf,
            final S sizingContext) {
        final H5ObjectInfo info = sizedInfo.get(tClass);
        if (null == info) {
            throw new IllegalArgumentException("No mapping for " + tClass);
        }
        return ((BiFunction<ByteBuffer, S, T>) info.of).apply(buf, sizingContext);
    }

    public static H5Registry ofDefault() {
        return of(Profile.HDFv1_4);
    }

    public static H5Registry of(final Profile hdf5Profile) {
        return new H5Registry(hdf5Profile, new LinkedHashMap<>(DEFAULT_MESSAGE_MAP));
    }
}
