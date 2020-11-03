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

import java.util.EnumMap;

public interface FileSpaceInfoMessageV1 extends FileSpaceInfoMessage {
    enum StrategyV1 {
        H5F_FSPACE_STRATEGY_FSM_AGGR, H5F_FSPACE_STRATEGY_PAGE, H5F_FSPACE_STRATEGY_AGGR, H5F_FSPACE_STRATEGY_NONE
    }

    StrategyV1 getStrategy();

    long getFileSpacePageSize();

    int getPageEndMetadataThreshold();

    long getEOA();

    EnumMap<Manager, Long> getLargeSizeFreeSpaceManagers();
}
