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

import java.nio.ByteBuffer;

import app.keve.hdf5io.fileformat.H5Context;
import app.keve.hdf5io.fileformat.H5Object;

/**
 * IV.A.2.a. The NIL Message
 * (https://bitbucket.hdfgroup.org/pages/HDFFV/hdf5doc/master/browse/html/H5.format.html#NILMessage)
 *
 */
public interface ObjectCommentMessage extends H5Object<H5Context> {
    long MIN_SIZE = 0;
    long MAX_SIZE = MAX_MESSAGE_DATA;

    String getComment();

    static ObjectCommentMessage of(final ByteBuffer buf, final H5Context context) {
        return new ObjectCommentMessageBB(buf, context);
    }

}
