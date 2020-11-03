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
import java.nio.ByteOrder;

import app.keve.hdf5io.fileformat.AbstractSizedBB;
import app.keve.hdf5io.fileformat.Resolvable;
import app.keve.hdf5io.fileformat.SizingContext;
import app.keve.hdf5io.fileformat.SizingContextHeap;
import app.keve.hdf5io.fileformat.level1.BTreeV1Group;
import app.keve.hdf5io.fileformat.level1.LocalHeap;

public final class SymbolTableMessageBB extends AbstractSizedBB<SizingContext> implements SymbolTableMessage {
    public SymbolTableMessageBB(final ByteBuffer buf, final SizingContext sizingContext) {
        super(buf, sizingContext);
    }

    public static long size(final SizingContext sc) {
        return 2 * sc.offsetSize();
    }

    @Override
    public long size() {
        return 2 * context.offsetSize();
    }

    @Override
    public Resolvable<BTreeV1Group> getBTree() {
        final SizingContextHeap sc = SizingContextHeap.of(context, getLocalHeap());
        return getResolvable(0, BTreeV1Group.class, sc);
    }

    @Override
    public void setBTree(final Resolvable<? extends BTreeV1Group> value) {
        setResolvable(0, value);
    }

    @Override
    public Resolvable<? extends LocalHeap> getLocalHeap() {
        return getResolvable(context.offsetSize(), LocalHeap.class, context);
    }

    @Override
    public void setLocalHeap(final Resolvable<? extends LocalHeap> value) {
        setResolvable(context.offsetSize(), value);
    }

    @Override
    public void initialize() {
        setBTree(null);
        setLocalHeap(null);
    }

    public static SymbolTableMessage of(final ByteBuffer buf, final SizingContext sizingContext) {
        return new SymbolTableMessageBB(buf.order(ByteOrder.LITTLE_ENDIAN), sizingContext);
    }
}
