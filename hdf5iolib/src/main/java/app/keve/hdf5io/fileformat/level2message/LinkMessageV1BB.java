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
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.level2.ObjectHeader;

public final class LinkMessageV1BB extends AbstractSizedBB<SizingContext> implements LinkMessageV1 {
    public LinkMessageV1BB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long minSize(final SizingContext sc) {
        return 4 + sc.offsetSize();
    }

    public static long maxSize(final SizingContext sc) {
        return MAX_MESSAGE_DATA;
    }

    @Override
    public long size() {
        int offset = 2;
        offset += isLinkTypePresent() ? 1 : 0;
        offset += isCreationOrderPresent() ? 8 : 0;
        offset += isLinkNameCharsetPresent() ? 1 : 0;
        offset += 1 << (getFlags() & LINK_NAME_LENGTH_MASK);
        offset += getLinkNameLength();
        offset += getLinkInformation().size();
        return offset;
    }

    @Override
    public int getVersion() {
        return getByte(0);
    }

    @Override
    public int getFlags() {
        return getByte(1);
    }

    @Override
    public int getLinkType() {
        return isLinkTypePresent() ? getUnsignedByte(2) : 0;
    }

    @Override
    public OptionalLong getCreationOrder() {
        final int offset = 2 + (isLinkTypePresent() ? 1 : 0);
        return isCreationOrderPresent() ? OptionalLong.of(getLong(offset)) : OptionalLong.empty();
    }

    @Override
    public OptionalInt getLinkNameCharacterSet() {
        final int offset = 2 + (isLinkTypePresent() ? 1 : 0) + (isCreationOrderPresent() ? 8 : 0);
        return isLinkNameCharsetPresent() ? OptionalInt.of(getByte(offset)) : OptionalInt.empty();
    }

    @Override
    public long getLinkNameLength() {
        int offset = 2;
        offset += isLinkTypePresent() ? 1 : 0;
        offset += isCreationOrderPresent() ? 8 : 0;
        offset += isLinkNameCharsetPresent() ? 1 : 0;
        return getUnsignedNumber(offset, 1 << (getFlags() & LINK_NAME_LENGTH_MASK));
    }

    @Override
    public String getLinkName() {
        int offset = 2;
        offset += isLinkTypePresent() ? 1 : 0;
        offset += isCreationOrderPresent() ? 8 : 0;
        offset += isLinkNameCharsetPresent() ? 1 : 0;
        offset += 1 << (getFlags() & LINK_NAME_LENGTH_MASK);
        return getAsciiString(offset, (int) getLinkNameLength()).toString();
    }

    @Override
    public LinkInformation getLinkInformation() {
        int offset = 2;
        offset += isLinkTypePresent() ? 1 : 0;
        offset += isCreationOrderPresent() ? 8 : 0;
        offset += isLinkNameCharsetPresent() ? 1 : 0;
        offset += 1 << (getFlags() & LINK_NAME_LENGTH_MASK);
        offset += getLinkNameLength();
        switch (getLinkType()) {
        case 0:
            return getEmbedded(offset, HardLinkInformation.class);
        case 1:
            return getEmbedded(offset, SoftLinkInformation.class);
        case 64:
            return getEmbedded(offset, ExternalLinkInformation.class);
        default:
            if (getLinkType() >= 65 && getLinkType() < 255) {
                // user defined
                return getEmbedded(offset, UserLinkInformation.class);
            }
            throw new IllegalArgumentException("implement " + getLinkType());
        }
    }

    public abstract static class AbstractLinkInformationBB extends AbstractSizedBB<SizingContext>
            implements LinkInformation {
        protected AbstractLinkInformationBB(final ByteBuffer buf, final SizingContext sizingContext) {
            super(buf, sizingContext);
        }

        public static long minSize(final SizingContext sc) {
            return Long.min(ExternalLinkInformationBB.minSize(sc), Long.min(HardLinkInformationBB.size(sc),
                    Long.min(SoftLinkInformationBB.minSize(sc), UserLinkInformationBB.minSize(sc))));
        }

        public static long maxSize(final SizingContext sc) {
            return Long.max(ExternalLinkInformationBB.maxSize(sc), Long.max(HardLinkInformationBB.size(sc),
                    Long.max(SoftLinkInformationBB.maxSize(sc), UserLinkInformationBB.maxSize(sc))));
        }

        public static final LinkInformation of(final ByteBuffer buf, final SizingContext context) {
            throw new IllegalArgumentException();
        }

    }

    public static final class HardLinkInformationBB extends AbstractLinkInformationBB implements HardLinkInformation {
        public HardLinkInformationBB(final ByteBuffer buf, final SizingContext sizingContext) {
            super(buf, sizingContext);
        }

        public static long size(final SizingContext sc) {
            return sc.offsetSize();
        }

        @Override
        public long size() {
            return context.offsetSize();
        }

        @Override
        public Resolvable<ObjectHeader> getObjectHeader() {
            return getResolvable(0, ObjectHeader.class, context);
        }

    }

    public static final class SoftLinkInformationBB extends AbstractLinkInformationBB implements SoftLinkInformation {
        public SoftLinkInformationBB(final ByteBuffer buf, final SizingContext sizingContext) {
            super(buf, sizingContext);
        }

        public static long minSize(final SizingContext sc) {
            return 2;
        }

        public static long maxSize(final SizingContext sc) {
            return UINT16_MAX_VALUE;
        }

        @Override
        public long size() {
            return 2 + getSoftLinkLength();
        }

        @Override
        public int getSoftLinkLength() {
            return getUnsignedShort(0);
        }

        @Override
        public String getSoftLink() {
            return getAsciiString(2, getSoftLinkLength()).toString();
        }
    }

    public static final class ExternalLinkInformationBB extends AbstractLinkInformationBB
            implements ExternalLinkInformation {
        public ExternalLinkInformationBB(final ByteBuffer buf, final SizingContext sizingContext) {
            super(buf, sizingContext);
        }

        public static long minSize(final SizingContext sc) {
            return 2;
        }

        public static long maxSize(final SizingContext sc) {
            return UINT16_MAX_VALUE;
        }

        @Override
        public long size() {
            return 2 + getExternalLinkLength();
        }

        @Override
        public int getExternalLinkLength() {
            return getUnsignedShort(0);
        }

        @Override
        public String getExternalLink() {
            return getAsciiString(2, getExternalLinkLength()).toString();
        }
    }

    public static final class UserLinkInformationBB extends AbstractLinkInformationBB implements UserLinkInformation {
        public UserLinkInformationBB(final ByteBuffer buf, final SizingContext sizingContext) {
            super(buf, sizingContext);
        }

        public static long minSize(final SizingContext sc) {
            return 2;
        }

        public static long maxSize(final SizingContext sc) {
            return UINT16_MAX_VALUE;
        }

        @Override
        public long size() {
            return 2 + getUserLinkLength();
        }

        @Override
        public int getUserLinkLength() {
            return getUnsignedShort(0);
        }

        @Override
        public String getUserLink() {
            return getAsciiString(2, getUserLinkLength()).toString();
        }

    }

}
