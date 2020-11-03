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

import java.nio.ByteBuffer;
import java.util.OptionalLong;

public abstract class AbstractSizedBB<S extends SizingContext> extends AbstractBB<S> implements ResolutionListener {
    public AbstractSizedBB(final ByteBuffer buf, final S sizingContext) {
        super(buf, sizingContext);
        assert 0 == buf.position();
    }

    protected final long getOffset(final int idx) {
        return getUnsignedNumber(idx, context.offsetSize());
    }

    protected final void setOffset(final int idx, final long value) {
        setUnsignedNumber(idx, context.offsetSize(), value);
    }

    protected final long getLength(final int idx) {
        return getUnsignedNumber(idx, context.lengthSize());
    }

    protected final void setLength(final int idx, final long value) {
        setUnsignedNumber(idx, context.lengthSize(), value);
    }

    protected final OptionalLong getOptionalOffset(final int idx) {
        switch (context.offsetSize()) {
        case 1:
            final byte b = getByte(idx);
            return -1 == b ? OptionalLong.empty() : OptionalLong.of(Byte.toUnsignedLong(b));
        case 2:
            final short s = getShort(idx);
            return -1 == s ? OptionalLong.empty() : OptionalLong.of(Short.toUnsignedLong(s));
        case 3:
            final int i3 = (int) getUnsignedNumber(idx, 3);
            return 0xFFFFFF == i3 ? OptionalLong.empty() : OptionalLong.of(i3);
        case 4:
            final int i = getInt(idx);
            return -1 == i ? OptionalLong.empty() : OptionalLong.of(Integer.toUnsignedLong(i));
        case 8:
            final long l = getLong(idx);
            return -1 == l ? OptionalLong.empty() : OptionalLong.of(l);
        default:
            throw new IllegalArgumentException("Unsupported size:" + context.offsetSize());
        }
    }

    protected final void setOptionalOffset(final int idx, final OptionalLong value) {
        switch (context.offsetSize()) {
        case 1:
            setByte(idx, (int) value.orElse(-1));
            break;
        case 2:
            setShort(idx, (int) value.orElse(-1));
            break;
        case 3:
            setUnsignedNumber(idx, 3, value.orElse(-1));
            break;
        case 4:
            setInt(idx, (int) value.orElse(-1));
            break;
        case 8:
            setLong(idx, value.orElse(-1));
            break;
        default:
            throw new IllegalArgumentException("Unsupported size:" + context.offsetSize());
        }
    }

    protected final <T extends H5Object<W>, W extends H5Context> Resolvable<T> getResolvable(final int idx,
            final int length, final Class<T> tclass, final W sc) {
        final OptionalLong at = getOptionalOffset(idx);
        return at.isEmpty() ? null : context.h5Factory().resolvable(at.getAsLong(), length, tclass, sc);
    }

    protected final <T extends H5Object<W>, W extends H5Context> Resolvable<T> getResolvable(final int idx,
            final Class<T> tclass, final W sizingContext) {
        return getResolvable(idx, 0, tclass, sizingContext);
    }

    protected final Resolvable<ByteBuffer> getResolvable(final int idx, final int length) {
        final OptionalLong at = getOptionalOffset(idx);
        return at.isEmpty() ? null : context.h5Factory().resolvable(at.getAsLong(), length);
    }

    protected final <T extends H5Object<W>, W extends H5Context> void setResolvable(final int idx,
            final Resolvable<T> value) {
        if (null == value) {
            setOffset(idx, -1);
        } else {
            value.addResolutionListener(this, idx);
            setOffset(idx, value.getAddress());
        }
    }

    protected final void setResolvableHeapString(final int idx, final Resolvable<String> value) {
        if (null == value) {
            setLength(idx, -1);
        } else {
            value.addResolutionListener(this, idx);
            setLength(idx, value.getAddress());
        }
    }

    protected final void setResolvableByteBuffer(final int idxOffset, final int idxLength,
            final Resolvable<ByteBuffer> value) {
        if (null == value) {
            setOffset(idxOffset, -1);
        } else {
            value.addResolutionListener(this, idxOffset);
            setOffset(idxOffset, value.getAddress());
            if (-1 != idxLength) {
                setLength(idxLength, value.getSize().getAsLong());
            }
        }
    }

    @Override
    public final void resolved(final Resolvable<?> resolved, final Object param) {
        setOffset((int) param, resolved.getAddress());
    }

}
