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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import app.keve.hdf5io.fileformat.SizingContext;

public final class AttributeMessageV3BB extends AttributeMessageV2BB implements AttributeMessageV3 {
    public AttributeMessageV3BB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext, 9);
    }

    public static long minSize(final SizingContext sc) {
        return 9;
    }

    public static long maxSize(final SizingContext sc) {
        return MAX_MESSAGE_DATA;
    }

    @Override
    public Charset getCharset() {
        switch (getByte(8)) {
        case 0:
            return StandardCharsets.US_ASCII;
        case 1:
            return StandardCharsets.UTF_8;
        default:
            throw new IllegalArgumentException("Charset " + getByte(8) + " not in spec!");
        }
    }
}
