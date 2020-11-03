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

public interface AttributeMessageV2 extends AttributeMessageV1 {
    int FLAG_DATATYPE_SHARED_MASK = 0x01;
    int FLAG_DATASPACE_SHARED_MASK = 0x02;

    int getFlags();

    SharedMessage getSharedDatatype();

    SharedMessage getSharedDataspace();

    default boolean isDatatypeShared() {
        return (getFlags() & FLAG_DATATYPE_SHARED_MASK) > 0;
    }

    default boolean isDataspaceShared() {
        return (getFlags() & FLAG_DATASPACE_SHARED_MASK) > 0;
    }
}
