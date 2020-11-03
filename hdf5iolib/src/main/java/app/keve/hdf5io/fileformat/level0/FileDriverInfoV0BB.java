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
import java.util.Arrays;
import java.util.Set;

import app.keve.hdf5io.api.HDF5Constants.MemberMapping;
import app.keve.hdf5io.fileformat.AbstractBB;
import app.keve.hdf5io.fileformat.H5Context;

public final class FileDriverInfoV0BB extends AbstractBB<H5Context> implements FileDriverInfoV0 {
    public static final long MIN_SIZE = 16;
    public static final long MAX_SIZE = 16 + UINT32_MAX_VALUE;

    public FileDriverInfoV0BB(final ByteBuffer buf, final H5Context context) {
        super(buf, context);
    }

    @Override
    public long size() {
        return 8 + 8 + getDriverInformationSize();
    }

    @Override
    public int getVersion() {
        return getByte(0);
    }

    @Override
    public String getDriverIdentification() {
        return getAsciiString(8, 8).toString();
    }

    private int getDriverInformationSize() {
        return getSmallUnsignedInt(4);
    }

    @Override
    public ByteBuffer getDriverInformationBuffer() {
        return getEmbeddedData(16, getDriverInformationSize());
    }

    @Override
    public DriverInformation getDriverInformation() {
        final String driverID = getDriverIdentification();
        if ("NCSAmult".equals(driverID)) {
            return getEmbedded(16, getDriverInformationSize(), MultiDriverInformation.class);
        } else if ("NCSAfami".equals(driverID)) {
            return getEmbedded(16, getDriverInformationSize(), FamilyDriverInformation.class);
        } else {
            return null;
        }
    }

    public abstract static class AbstractDriverInformation extends AbstractBB<H5Context> implements DriverInformation {
        public static final long MIN_SIZE = Long.min(MultiDriverInformationBB.MIN_SIZE,
                FamilyDriverInformationBB.SIZE);
        public static final long MAX_SIZE = Long.min(MultiDriverInformationBB.MAX_SIZE,
                FamilyDriverInformationBB.SIZE);

        protected AbstractDriverInformation(final ByteBuffer buf, final H5Context context) {
            super(buf, context);
        }

        public static DriverInformation of(final ByteBuffer buf, final H5Context context) {
            throw new IllegalArgumentException();
        }
    }

    public static final class MultiDriverInformationBB extends AbstractDriverInformation
            implements MultiDriverInformation {
        public static final long MIN_SIZE = 8 + 1 * 16;
        public static final long MAX_SIZE = UINT16_MAX_VALUE;

        public MultiDriverInformationBB(final ByteBuffer buf, final H5Context context) {
            super(buf, context);
        }

        @Override
        public long size() {
            int sofs = 8 + getNumberOfFiles() * 16;
            for (int i = 0; i < getNumberOfFiles(); i++) {
                final CharSequence name = getAsciiNulString(sofs);
                sofs += name.length() + 1;
                sofs = sofs + 7 & ~7;
            }
            return sofs;
        }

        @Override
        public MemberMapping[] getMemberMapping() {
            final MemberMapping[] memberMapping = new MemberMapping[6];
            for (int i = 0; i < 6; i++) {
                memberMapping[i] = MemberMapping.of(getUnsignedByte(i));
            }
            return memberMapping;
        }

        @Override
        public int getNumberOfFiles() {
            return Set.copyOf(Arrays.asList(getMemberMapping())).size();
        }

        @Override
        public FileInfo[] getFileInfo() {
            final FileInfo[] fileInfo = new FileInfo[getNumberOfFiles()];
            int sofs = 8 + getNumberOfFiles() * 16;
            for (int i = 0; i < fileInfo.length; i++) {
                final long address = getLong(8 + i * 16);
                final long endAddress = getLong(8 + i * 16 + 8);
                final CharSequence name = getAsciiNulString(sofs);
                fileInfo[i] = new FileInfo(address, endAddress, name.toString());
                sofs += name.length() + 1;
                sofs = sofs + 7 & ~7;
            }
            return fileInfo;
        }

    }

    public static final class FamilyDriverInformationBB extends AbstractDriverInformation
            implements FamilyDriverInformation {
        public static final long SIZE = 8;

        public FamilyDriverInformationBB(final ByteBuffer buf, final H5Context context) {
            super(buf, context);
        }

        @Override
        public long size() {
            return 8;
        }

        @Override
        public long getMemberFileSize() {
            return getLong(0);
        }
    }

}
