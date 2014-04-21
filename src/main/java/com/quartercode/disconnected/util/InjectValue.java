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

package com.quartercode.disconnected.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The inject value annotation is put on fields that can receive a value from a {@link ValueInjector}.
 */
@Target (ElementType.FIELD)
@Retention (RetentionPolicy.RUNTIME)
public @interface InjectValue {

    /**
     * The name of the value that should be injected into annotated the field.
     * It is equivalent to the first argument of the {@link ValueInjector#put(String, Object)} method.
     */
    String value ();

    /**
     * Whether a non-null value is allowed to be injected into the annotated field.
     * If this value is set <code>false</code> (default), an exception is thrown by the {@link ValueInjector} if the value is null.
     * That guarantees that the annotated field always has a non-null value.
     */
    boolean allowNull () default false;

}
