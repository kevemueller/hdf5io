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

public interface FileSpaceInfoMessageV0 extends FileSpaceInfoMessage {
    enum StrategyV0 {
        H5F_FILE_SPACE_ALL_PERSIST, H5F_FILE_SPACE_ALL, H5F_FILE_SPACE_AGGR_VFD, H5F_FILE_SPACE_VFD
    }

    StrategyV0 getStrategy();
}
