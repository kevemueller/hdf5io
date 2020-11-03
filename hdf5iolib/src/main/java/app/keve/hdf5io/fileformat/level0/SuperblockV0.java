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
package app.keve.hdf5io.fileformat.level0;

import java.util.OptionalLong;

import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.level1.SymbolTableEntry;

public interface SuperblockV0 extends Superblock {
    int getVersionNumberFileFreeSpaceStorage();

    void setVersionNumberFileFreeSpaceStorage(int value);

    int getVersionNumberRootGroupSymbolTableEntry();

    void setVersionNumberRootGroupSymbolTableEntry(int value);

    int getVersionNumberSharedHeaderMessageFormat();

    void setVersionNumberSharedHeaderMessageFormat(int value);

    int getGroupLeafNodeK();

    void setGroupLeafNodeK(int value);

    int getGroupInternalNodeK();

    void setGroupInternalNodeK(int value);

    // TODO: H5Object
    OptionalLong getAddressOfFileFreeSpaceInfo();

    void setAddressOfFileFreeSpaceInfo(OptionalLong value);

    Resolvable<FileDriverInfo> getDriverInformationBlock();

    void setDriverInformationBlock(Resolvable<FileDriverInfo> value);

    SymbolTableEntry getRootGroupSymbolTableEntry();
}
