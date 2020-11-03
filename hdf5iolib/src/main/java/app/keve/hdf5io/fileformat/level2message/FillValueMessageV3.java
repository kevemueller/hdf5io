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

public interface FillValueMessageV3 extends FillValueMessageV2 {

    int SPACE_ALLOCATION_TIME_MASK = 0b0000_0011;
    int FILL_VALUE_WRITE_TIME_MASK = 0b0000_1100;
    int FILL_VALUE_UNDEFINED_MASK = 0b0001_0000;
    int FILL_VALUE_DEFINED_MASK = 0b0010_0000;

    int getFlags();

    void setFlags(int value);

    boolean isFillValueUndefined();

    void setFillValueUndefined(boolean value);
}
