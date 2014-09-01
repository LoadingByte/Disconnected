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

package com.quartercode.disconnected.server.world.comp.program;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The common location annotation sets the file path the annotated {@link ProgramExecutor} can be commonly found under.
 * The annotation should be used on every program executor, so users of the programs do not need to search for it.
 * 
 * @see ProgramUtils#getCommonLocation(Class)
 */
@Target (ElementType.TYPE)
@Retention (RetentionPolicy.RUNTIME)
public @interface CommonLocation {

    /**
     * The directory that commonly contains the program (e.g. {@code /system/bin}).
     * When possible, a common files constant should be used here.
     */
    String dir ();

    /**
     * The commonly used name of the program file (e.g. {@code session.exe}).
     */
    String file ();

}
