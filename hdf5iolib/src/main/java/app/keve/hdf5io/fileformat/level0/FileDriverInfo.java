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

import java.nio.ByteBuffer;

import app.keve.hdf5io.api.HDF5Constants.MemberMapping;
import app.keve.hdf5io.fileformat.H5Context;
import app.keve.hdf5io.fileformat.H5Object;

public interface FileDriverInfo extends H5Object<H5Context> {
    long MIN_SIZE = FileDriverInfoV0BB.MIN_SIZE;
    long MAX_SIZE = FileDriverInfoV0BB.MAX_SIZE;

    int getVersion();

    String getDriverIdentification();

    ByteBuffer getDriverInformationBuffer();

    DriverInformation getDriverInformation();

    interface DriverInformation extends H5Object<H5Context> {

    }

    interface MultiDriverInformation extends DriverInformation {

        class FileInfo {
            private final long address;
            private final long endAddress;
            private final String name;

            public FileInfo(final long address, final long endAddress, final String name) {
                this.address = address;
                this.endAddress = endAddress;
                this.name = name;
            }

            public long getAddress() {
                return address;
            }

            public long getEndAddress() {
                return endAddress;
            }

            public String getName() {
                return name;
            }

        }

        MemberMapping[] getMemberMapping();

        int getNumberOfFiles();

        FileInfo[] getFileInfo();
    }

    interface FamilyDriverInformation extends DriverInformation {
        long getMemberFileSize();
    }

    static FileDriverInfo of(final ByteBuffer buf, final H5Context context) {
        switch (buf.get(0)) {
        case 0:
            return new FileDriverInfoV0BB(buf, context);
        default:
            throw new IllegalArgumentException("Implement version " + buf.get(0));
        }
    }
}
