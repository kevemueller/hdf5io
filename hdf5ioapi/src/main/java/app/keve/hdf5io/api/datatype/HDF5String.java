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

import java.nio.charset.Charset;

/**
 * HDF5 String datatype.
 * 
 * @author keve
 * 
 * @see <a href=
 *      "https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#DatatypeMessage">HDF5
 *      Specification: Datatype Message</a>
 * 
 *
 */
public interface HDF5String extends HDF5AtomicDatatype {
    PaddingType getPaddingType();

    Charset getCharset();

    /**
     * Builder for String datatypes.
     * 
     * @author keve
     *
     */
    interface StringBuilder extends Builder<HDF5String, StringBuilder> {
        StringBuilder withPaddingType(PaddingType paddingType);

        StringBuilder withCharset(Charset charset);
    }

}
