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
package app.keve.hdf5io;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.time.temporal.TemporalAccessor;
import java.util.Iterator;
import java.util.Map;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.MethodProperty;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import app.keve.hdf5io.fileformat.H5Resolver;
import app.keve.hdf5io.fileformat.Resolvable;

public final class HDF5ObjectRepresenter extends Representer {
    private final H5Resolver hdf5Resolver;
    private final Represent javaBean;
    private Throwable lastThrowable;

    public HDF5ObjectRepresenter(final H5Resolver hdf5Resolver) {
        this.hdf5Resolver = hdf5Resolver;
        javaBean = representers.get(null);
        representers.put(null, new RepresentHDF5Object());
        setPropertyUtils(new PropertyUtilsX());
    }

    public Throwable getLastThrowable() {
        return lastThrowable;
    }

    // add interface default methods as they are not detected by the JavaBean
    // Introspector
    private static class PropertyUtilsX extends PropertyUtils {
        @Override
        protected Map<String, Property> getPropertiesMap(final Class<?> type, final BeanAccess bAccess) {
            final Map<String, Property> map = super.getPropertiesMap(type, bAccess);
            for (final Method method : type.getMethods()) {
                if (0 == method.getParameterCount()) {
                    String name = method.getName();
                    if (name.startsWith("get") && method.isDefault()) {
                        name = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                    } else if (name.startsWith("is") && method.isDefault()) {
                        name = Character.toLowerCase(name.charAt(2)) + name.substring(3);
                    } else if ("size".equals(name)) {
                        // also add the size method
                        name = "size";
                    } else {
                        continue;
                    }
                    try {
                        final PropertyDescriptor property = new PropertyDescriptor(name, method, null);
                        map.put(name, new MethodProperty(property));
                    } catch (final IntrospectionException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            }
            return map;
        }
    }

    private class RepresentHDF5Object implements Represent {
        @Override
        public Node representData(final Object data) {
            try {
                if (null == data) {
                    return representScalar(Tag.NULL, "null");
                } else if (data instanceof Resolvable<?>) {
                    final Object resolved = ((Resolvable<?>) data).resolve(hdf5Resolver);
                    if (null == resolved) {
                        return representScalar(Tag.STR, data.toString() + "-> null");
                    } else {
                        final Map<String, Object> mapping = Map.of("resolvable", data.toString(), "value", resolved);
                        return representMapping(getTag(data.getClass(), Tag.MAP), mapping, FlowStyle.AUTO);
                    }
                } else if (data instanceof OptionalInt) {
                    if (((OptionalInt) data).isEmpty()) {
                        return representScalar(Tag.NULL, "empty");
                    } else {
                        return representScalar(getTag(data.getClass(), Tag.INT),
                                Integer.toString(((OptionalInt) data).getAsInt()));
                    }
                } else if (data instanceof OptionalLong) {
                    if (((OptionalLong) data).isEmpty()) {
                        return representScalar(Tag.NULL, "empty");
                    } else {
                        return representScalar(getTag(data.getClass(), Tag.INT),
                                Long.toString(((OptionalLong) data).getAsLong()));
                    }
                } else if (data instanceof ByteBuffer) {
                    final ByteBuffer buf = ((ByteBuffer) data).duplicate();
                    buf.limit(buf.position() + Integer.min(buf.remaining(), 256));
                    return representSequence(Tag.SEQ, () -> new Iterator<>() {
                        @Override
                        public boolean hasNext() {
                            return buf.hasRemaining();
                        }

                        @Override
                        public Object next() {
                            return Byte.toUnsignedInt(buf.get());
                        }
                    }, FlowStyle.AUTO);
                } else if (data instanceof IntBuffer) {
                    final IntBuffer buf = ((IntBuffer) data).duplicate();
                    buf.limit(Integer.min(buf.remaining(), 256));
                    return representSequence(Tag.SEQ, () -> new Iterator<>() {

                        @Override
                        public boolean hasNext() {
                            return buf.hasRemaining();
                        }

                        @Override
                        public Object next() {
                            return buf.get();
                        }
                    }, FlowStyle.AUTO);
                } else if (data instanceof TemporalAccessor) {
                    return representScalar(getTag(data.getClass(), Tag.TIMESTAMP), data.toString());
                } else {
                    return javaBean.representData(data);
                }
            } catch (final YAMLException | IllegalArgumentException e) {
                // YAMLException does not link to the cause
                // try to find the cause ourselves
                final PropertyUtils propertyUtils = getPropertyUtils();
                final Set<Property> properties = propertyUtils.getProperties(data.getClass());
                for (final Property property : properties) {
                    try {
                        final Field pf = property.getClass().getDeclaredField("property");
                        pf.setAccessible(true);
                        final PropertyDescriptor pd = (PropertyDescriptor) pf.get(property);
                        pd.getReadMethod().invoke(data);
                    } catch (InvocationTargetException | NoSuchFieldException | SecurityException
                            | IllegalArgumentException | IllegalAccessException ite) {
                        final Throwable cause = ite.getCause();
                        if (null == cause) {
                            ite.printStackTrace();
                            lastThrowable = ite;
                        } else {
                            cause.printStackTrace();
                            lastThrowable = cause;
                        }
                        return representScalar(Tag.STR, lastThrowable.toString());
                    }
                }
                return representScalar(Tag.STR, e.toString());
            }
        }
    }

}
