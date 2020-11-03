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
package app.keve.hdf5io.fileformat.level2message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.keve.hdf5io.fileformat.H5Object;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.level2.ObjectHeader;

public interface LinkMessage extends H5Object<SizingContext> {

    static long minSize(final SizingContext sc) {
        return LinkMessageV1BB.minSize(sc);
    }

    static long maxSize(final SizingContext sc) {
        return LinkMessageV1BB.maxSize(sc);
    }

    int getVersion();

    int getFlags();

    int getLinkType();

    OptionalLong getCreationOrder();

    OptionalInt getLinkNameCharacterSet();

    long getLinkNameLength();

    String getLinkName();

    LinkInformation getLinkInformation();

    interface LinkInformation extends H5Object<SizingContext> {

    }

    interface HardLinkInformation extends LinkInformation {
        Resolvable<ObjectHeader> getObjectHeader();
    }

    interface SoftLinkInformation extends LinkInformation {
        int getSoftLinkLength();

        String getSoftLink();
    }

    interface ExternalLinkInformation extends LinkInformation {
        int getExternalLinkLength();

        String getExternalLink();
    }

    interface UserLinkInformation extends LinkInformation {
        int getUserLinkLength();

        String getUserLink();
    }

    static LinkMessage of(final ByteBuffer buf, final SizingContext sizingContext) {
        assert 0 == buf.position();
        assert ByteOrder.LITTLE_ENDIAN == buf.order();
        switch (buf.get(0)) {
        case 1:
            return new LinkMessageV1BB(buf, sizingContext);
        default:
            throw new IllegalArgumentException("Implement version " + buf.get(0));
        }
    }
}
