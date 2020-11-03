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

import app.keve.hdf5io.fileformat.level2message.AttributeInfoMessage;
import app.keve.hdf5io.fileformat.level2message.AttributeMessage;
import app.keve.hdf5io.fileformat.level2message.BTreeKValuesMessage;
import app.keve.hdf5io.fileformat.level2message.BogusMessage;
import app.keve.hdf5io.fileformat.level2message.DataLayoutMessage;
import app.keve.hdf5io.fileformat.level2message.DataspaceMessage;
import app.keve.hdf5io.fileformat.level2message.DatatypeMessage;
import app.keve.hdf5io.fileformat.level2message.DriverInfoMessage;
import app.keve.hdf5io.fileformat.level2message.ExternalDataFilesMessage;
import app.keve.hdf5io.fileformat.level2message.FileSpaceInfoMessage;
import app.keve.hdf5io.fileformat.level2message.FillValueMessage;
import app.keve.hdf5io.fileformat.level2message.FillValueOldMessage;
import app.keve.hdf5io.fileformat.level2message.FilterPipelineMessage;
import app.keve.hdf5io.fileformat.level2message.GroupInfoMessage;
import app.keve.hdf5io.fileformat.level2message.LinkInfoMessage;
import app.keve.hdf5io.fileformat.level2message.LinkMessage;
import app.keve.hdf5io.fileformat.level2message.NILMessage;
import app.keve.hdf5io.fileformat.level2message.ObjectCommentMessage;
import app.keve.hdf5io.fileformat.level2message.ObjectHeaderContinuationMessage;
import app.keve.hdf5io.fileformat.level2message.ObjectModificationTimeMessageV1;
import app.keve.hdf5io.fileformat.level2message.ObjectModificationTimeOldMessage;
import app.keve.hdf5io.fileformat.level2message.ObjectReferenceCountMessage;
import app.keve.hdf5io.fileformat.level2message.SharedMessageTableMessage;
import app.keve.hdf5io.fileformat.level2message.SymbolTableMessage;

/**
 * Enumeration of all known message types from the specification with additional
 * key information: ordinal number, name, status, interface class.
 * 
 * @author keve
 *
 */
public enum H5MessageType {
    NIL(0x0000, "NIL", H5MessageStatus.OPTIONAL_REPEATABLE, NILMessage.class),
    DATASPACE(0x0001, "Dataspace", H5MessageStatus.REQUIRED_DATASET_NOT_REPEATABLE, DataspaceMessage.class),
    LINK_INFO(0x0002, "Link Info", H5MessageStatus.OPTIONAL_NOT_REPEATABLE, LinkInfoMessage.class),
    DATATYPE(0x0003, "Datatype", H5MessageStatus.REQUIRED_DATASET_COMMITED_DATATYPE_NOT_REPEATABLE,
            DatatypeMessage.class),
    FILL_VALUE_OLD(0x0004, "Fill Value (old)", H5MessageStatus.OPTIONAL_NOT_REPEATABLE, FillValueOldMessage.class),
    FILL_VALUE(0x0005, "Fill Value", H5MessageStatus.REQUIRED_DATASET_NOT_REPEATABLE, FillValueMessage.class),
    LINK(0x0006, "Link", H5MessageStatus.OPTIONAL_REPEATABLE, LinkMessage.class),
    EXTERNAL_DATA_FILES(0x0007, "External Data Files", H5MessageStatus.OPTIONAL_NOT_REPEATABLE,
            ExternalDataFilesMessage.class),
    DATA_LAYOUT(0x0008, "Data Layout", H5MessageStatus.REQUIRED_DATASET_NOT_REPEATABLE, DataLayoutMessage.class),
    BOGUS(0x0009, "Bogus", H5MessageStatus.TEST_ONLY, BogusMessage.class),
    GROUP_INFO(0x000A, "Group Info", H5MessageStatus.OPTIONAL_NOT_REPEATABLE, GroupInfoMessage.class),
    FILTER_PIPELINE(0x000B, "Data Storage - Filter Pipeline", H5MessageStatus.OPTIONAL_NOT_REPEATABLE,
            FilterPipelineMessage.class),
    ATTRIBUTE(0x000C, "Attribute", H5MessageStatus.OPTIONAL_REPEATABLE, AttributeMessage.class),
    OBJECT_COMMENT(0x000D, "Object Comment", H5MessageStatus.OPTIONAL_NOT_REPEATABLE, ObjectCommentMessage.class),
    OBJECT_MODIFICATION_TIME_OLD(0x000E, "Object Modification Time (Old)", H5MessageStatus.OPTIONAL_NOT_REPEATABLE,
            ObjectModificationTimeOldMessage.class),
    SHARED_MESSAGE_TABLE(0x000F, "Shared Message Table", H5MessageStatus.OPTIONAL_NOT_REPEATABLE,
            SharedMessageTableMessage.class),
    OBJECT_HEADER_CONTINUATION(0x0010, "Object Header Continuation", H5MessageStatus.OPTIONAL_REPEATABLE,
            ObjectHeaderContinuationMessage.class),
    SYMBOL_TABLE(0x0011, "Symbol Table Message", H5MessageStatus.REQUIRED_OLDGROUP_NOT_REPEATABLE,
            SymbolTableMessage.class),
    OBJECT_MODIFICATION_TIME(0x0012, "Object Modification Time", H5MessageStatus.OPTIONAL_NOT_REPEATABLE,
            ObjectModificationTimeMessageV1.class),
    BTREE_K_VALUES(0x0013, "B-tree 'K' Values", H5MessageStatus.OPTIONAL_NOT_REPEATABLE, BTreeKValuesMessage.class),
    DRIVER_INFO(0x0014, "Driver Info", H5MessageStatus.OPTIONAL_NOT_REPEATABLE, DriverInfoMessage.class),
    ATTRIBUTE_INFO(0x0015, "Attribute Info", H5MessageStatus.OPTIONAL_NOT_REPEATABLE, AttributeInfoMessage.class),
    OBJECT_REFERENCE_COUNT(0x0016, "Object Reference Count", H5MessageStatus.OPTIONAL_NOT_REPEATABLE,
            ObjectReferenceCountMessage.class),
    FILE_SPACE_INFO(0x0017, "File Space Info", H5MessageStatus.OPTIONAL_NOT_REPEATABLE, FileSpaceInfoMessage.class);

    public final int typeNum;
    public final String name;
    public final H5MessageStatus status;
    public final Class<? extends H5Object<?>> messageClass;

    H5MessageType(final int typeNum, final String name, final H5MessageStatus status,
            final Class<? extends H5Object<?>> messageClass) {
        this.typeNum = typeNum;
        this.name = name;
        this.status = status;
        this.messageClass = messageClass;
    }

    public static H5MessageType of(final int typeNum) {
        if (typeNum >= values().length) {
//            throw new NoSuchElementException("No Message info for " + typeNum);
            return null;
        }
        return values()[typeNum];
    }

}
