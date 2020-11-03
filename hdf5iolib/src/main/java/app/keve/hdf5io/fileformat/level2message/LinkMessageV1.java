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

public interface LinkMessageV1 extends LinkMessage {
    int LINK_NAME_LENGTH_MASK = 0b0000_0011;
    int CREATION_ORDER_PRESENT_MASK = 0b0000_0100;
    int LINK_TYPE_PRESENT_MASK = 0b0000_1000;
    int LINK_NAME_CHARSET_PRESENT_MASK = 0b0001_0000;

    default boolean isCreationOrderPresent() {
        return (getFlags() & CREATION_ORDER_PRESENT_MASK) > 0;
    }

    default boolean isLinkTypePresent() {
        return (getFlags() & LINK_TYPE_PRESENT_MASK) > 0;
    }

    default boolean isLinkNameCharsetPresent() {
        return (getFlags() & LINK_NAME_CHARSET_PRESENT_MASK) > 0;
    }
}
