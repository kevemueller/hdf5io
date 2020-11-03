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

import java.util.OptionalLong;

public interface Resolvable<T> {
    /**
     * Obtain the unique address of the resolvable's target.
     * 
     * @return the address.
     */
    long getAddress();

    /**
     * Obtain the size of the the resolvable's target.
     * 
     * @return the size
     */
    default OptionalLong getSize() {
        return OptionalLong.empty();
    }

    /**
     * Resolve the resolvable target using the resolver.
     * 
     * @param h5Resolver resolver the resolver.
     * @return the resolved instance.
     */
    T resolve(H5Resolver h5Resolver);

    /**
     * Subscribe to changes of the resolvable's target address.
     * 
     * @param listener the subscriber
     * @param param    the parameter to send back
     */
    void addResolutionListener(ResolutionListener listener, Object param);

    /**
     * Unlink the resolvable's target.
     */
    default void unlink() {

    }
}
