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
package app.keve.hdf5io.util;

import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.function.Consumer;

/**
 * An Iterator specialized for {@code float} values.
 */
public interface OfFloat extends PrimitiveIterator<Float, FloatConsumer> {

    /**
     * Returns the next {@code float} element in the iteration.
     *
     * @return the next {@code float} element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    float nextFloat();

    /**
     * {@inheritDoc}
     * 
     * @implSpec The default implementation boxes the result of calling
     *           {@link #nextFloat()}, and returns that boxed result.
     */
    @Override
    default Float next() {
        return nextFloat();
    }

    /**
     * Performs the given action for each remaining element until all elements have
     * been processed or the action throws an exception. Actions are performed in
     * the order of iteration, if that order is specified. Exceptions thrown by the
     * action are relayed to the caller.
     *
     * @implSpec
     *           The default implementation behaves as if:
     * 
     *           <pre>
     * {@code
     *     while (hasNext())
     *         action.accept(nextFloat());
     * }
     * </pre>
     *
     * @param action The action to be performed for each element
     * @throws NullPointerException if the specified action is null
     */
    default void forEachRemaining(FloatConsumer action) {
        Objects.requireNonNull(action);
        while (hasNext()) {
            action.accept(nextFloat());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @implSpec If the action is an instance of {@code FloatConsumer} then it is
     *           cast to {@code FloatConsumer} and passed to
     *           {@link #forEachRemaining}; otherwise the action is adapted to an
     *           instance of {@code FloatConsumer}, by boxing the argument of
     *           {@code FloatConsumer}, and then passed to
     *           {@link #forEachRemaining}.
     */
    @Override
    default void forEachRemaining(Consumer<? super Float> action) {
        if (action instanceof FloatConsumer) {
            forEachRemaining((FloatConsumer) action);
        } else {
            // The method reference action::accept is never null
            Objects.requireNonNull(action);
            forEachRemaining((FloatConsumer) action::accept);
        }
    }
}
