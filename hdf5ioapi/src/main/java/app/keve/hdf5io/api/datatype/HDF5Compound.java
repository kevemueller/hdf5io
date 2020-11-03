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
package app.keve.hdf5io.api.datatype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * HDF5 Compound datatype.
 * 
 * @author keve
 * 
 * @see <a href=
 *      "https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#DatatypeMessage">HDF5
 *      Specification: Datatype Message</a>
 * 
 *
 */
public interface HDF5Compound extends HDF5CompositeDatatype {
    int getNumberOfMembers();

    Iterator<? extends Member> memberIterator();

    default List<Member> getMembers() {
        final List<Member> members = new ArrayList<>();
        memberIterator().forEachRemaining(members::add);
        return members;
    }

    /**
     * Definition of a compound datatype member datatype.
     * 
     * @author keve
     *
     */
    interface Member {
        String getName();

        int getByteOffset();

        HDF5Datatype getMemberType();
    }

    /**
     * Builder for the compound datatype.
     * 
     * @author keve
     *
     */
    interface CompoundBuilder extends Builder<HDF5Compound, CompoundBuilder> {
        default CompoundBuilder addMember(final Member member) {
            return addMember(member.getName(), member.getByteOffset(), member.getMemberType());
        }

        CompoundBuilder addMember(String name, int byteOffset, HDF5Datatype memberType);

        @Override
        default CompoundBuilder from(final HDF5Compound template) {
            template.getMembers().forEach(this::addMember);
            return this;
        }
    }
}
