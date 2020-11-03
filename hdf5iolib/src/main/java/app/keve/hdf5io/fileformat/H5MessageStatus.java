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
package app.keve.hdf5io.fileformat;

public enum H5MessageStatus {
    OPTIONAL_REPEATABLE, OPTIONAL_NOT_REPEATABLE, REQUIRED_DATASET_NOT_REPEATABLE,
    REQUIRED_DATASET_COMMITED_DATATYPE_NOT_REPEATABLE, TEST_ONLY, REQUIRED_OLDGROUP_NOT_REPEATABLE
}
