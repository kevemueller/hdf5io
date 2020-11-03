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
package app.keve.hdf5io.fileformat.level1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import app.keve.hdf5io.fileformat.Resolvable;

public interface BTreeV1GroupInternal extends BTreeV1Group {
    Resolvable<BTreeV1Group> getChild(int index);

    Map<Resolvable<String>, Resolvable<BTreeV1Group>> getEntryNodes();

    default List<Resolvable<BTreeV1Group>> getChildren() {
        final List<Resolvable<BTreeV1Group>> children = new ArrayList<>(getEntriesUsed());
        for (int i = 0; i < getEntriesUsed(); i++) {
            children.add(getChild(i));
        }
        return children;
    }

    Iterator<Resolvable<BTreeV1Group>> valueIterator();
}
