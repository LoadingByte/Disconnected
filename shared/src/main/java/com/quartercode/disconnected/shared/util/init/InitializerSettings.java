/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

package com.quartercode.disconnected.shared.util.init;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that provides information about an {@link Initializer} and specifies its requirements.
 * 
 * @see Initializer
 */
@Target (ElementType.TYPE)
@Retention (RetentionPolicy.RUNTIME)
public @interface InitializerSettings {

    /**
     * Returns the group(s) the annotated {@link Initializer} is part of.
     * In case of a single group, all initializers of that group must be executed before any initializer that depends on that group.
     * Note that it is possible for an initializer to be part of multiple groups.
     * In that case, the initializer must be executed before any initializer that depends on one of its groups.
     * 
     * @return The group(s) the initializer is part of.
     */
    String[] groups ();

    /**
     * Returns the group(s) the annotated {@link Initializer} depends on.
     * All initializers of all these groups must be executed before the annotated initializer is executed.
     * 
     * @return The group(s) the initializer depends on.
     */
    String[] dependencies () default {};

}
