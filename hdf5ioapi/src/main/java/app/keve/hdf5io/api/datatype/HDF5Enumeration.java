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

import java.nio.ByteBuffer;

/**
 * HDF5 Enumeration datatype.
 * 
 * @author keve
 * 
 * @see <a href=
 *      "https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#DatatypeMessage">HDF5
 *      Specification: Datatype Message</a>
 * 
 *
 */
public interface HDF5Enumeration extends HDF5CompositeDatatype {
    HDF5Datatype getBaseType();

    String[] getNames();

    Object[] getValues();

    ByteBuffer getValueBuf();

    /**
     * Builder for enumeration types.
     * 
     * @author keve
     *
     */
    interface EnumerationBuilder extends Builder<HDF5Enumeration, EnumerationBuilder> {

        EnumerationBuilder withBaseType(HDF5Datatype baseType);

        EnumerationBuilder withNames(String[] names);

        EnumerationBuilder withValues(Object[] values);

        EnumerationBuilder withValueBuf(ByteBuffer values);

        @Override
        default EnumerationBuilder from(final HDF5Enumeration template) {
            withElementSize(template.getElementSize());
            withBaseType(template.getBaseType());
            withNames(template.getNames());
            withValueBuf(template.getValueBuf());
            return this;
        }
    }
}
