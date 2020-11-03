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

public interface GroupInfoMessageV0 extends GroupInfoMessage {
    long MIN_SIZE = 2;
    long MAX_SIZE = 10;

    int FLAG_LINK_PHASE_CHANGE_MASK = 0b00;
    int FLAG_ESTIMATED_ENTRY_MASK = 0b10;

    default boolean isLinkPhaseChange() {
        return (getFlags() & FLAG_LINK_PHASE_CHANGE_MASK) > 0;
    }

    default boolean isEstimatedEntryStored() {
        return (getFlags() & FLAG_ESTIMATED_ENTRY_MASK) > 0;
    }
}
