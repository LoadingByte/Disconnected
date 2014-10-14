/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>
 *
 * Disconnected is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Disconnected is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Disconnected. If not, see <http://www.gnu.org/licenses/>.
 */

package com.quartercode.disconnected.client.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A value injector takes an object and injects some previously set values into its fields.
 * Such fields must be annotated with the {@link InjectValue} annotation.
 * Every object that has such fields must call the {@link #runOn(Object)} method on itself in order to get its required values injected.
 */
public class ValueInjector {

    private static final Logger       LOGGER = LoggerFactory.getLogger(ValueInjector.class);

    private final Map<String, Object> values = new HashMap<>();

    /**
     * Creates a new empty value injector.
     * The injector needs to be filled with values using {@link #put(String, Object)} after creation.
     */
    public ValueInjector() {

    }

    /**
     * Adds a new value to the value injector.
     * The value can then be injected into fields.
     * 
     * @param name The name of the value. It is used by {@link InjectValue#value()}.
     * @param value The actual object that is injected into requesting fields.
     */
    public void put(String name, Object value) {

        values.put(name, value);
    }

    /**
     * Runs the injector on the given object by injecting values into fields that have the {@link InjectValue} annotation.
     * Every object that somehow requests values must run this method on itself before the values can be used.
     * 
     * @param object The object whose injectable fields should be filled with values.
     */
    public void runOn(Object object) {

        Class<?> type = object.getClass();

        for (Field field : FieldUtils.getAllFields(type)) {
            InjectValue injectAnnotation = field.getAnnotation(InjectValue.class);
            if (injectAnnotation != null) {
                String valueName = injectAnnotation.value();
                boolean allowNull = injectAnnotation.allowNull();

                Object value = values.get(valueName);
                if (value != null) {
                    try {
                        FieldUtils.writeField(field, object, value, true);
                    } catch (IllegalArgumentException e) {
                        fail("Available value '" + valueName + "' for injection into '" + type.getName() + "." + field.getName() + "' is of wrong type", e, !allowNull);
                    } catch (IllegalAccessException e) {
                        fail("Cannot access field '" + type.getName() + "." + field.getName() + "' for value injection", e, !allowNull);
                    }
                } else if (!allowNull) {
                    throw new IllegalStateException("Field '" + type.getName() + "." + field.getName() + "' requires injection of unknown value '" + valueName + "'");
                }
            }
        }
    }

    private void fail(String message, Exception exception, boolean throwIllegalState) {

        if (throwIllegalState) {
            throw new IllegalStateException(message, exception);
        } else {
            LOGGER.warn(message);
        }
    }

    /**
     * The inject value annotation is put on fields that can receive a value from a {@link ValueInjector}.
     */
    @Target (ElementType.FIELD)
    @Retention (RetentionPolicy.RUNTIME)
    public static @interface InjectValue {

        /**
         * The name of the value that should be injected into annotated the field.
         * It is equivalent to the first argument of the {@link ValueInjector#put(String, Object)} method.
         */
        String value ();

        /**
         * Whether a non-null value is allowed to be injected into the annotated field.
         * If this value is set {@code false} (default), an exception is thrown by the {@link ValueInjector} if the value is null.
         * That guarantees that the annotated field always has a non-null value.
         */
        boolean allowNull () default false;

    }

}
