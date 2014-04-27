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

package com.quartercode.disconnected.graphics.desktop;

import com.quartercode.disconnected.util.ValueInjector;

/**
 * A desktop program world context stores information about the environment of a running desktop program.
 * For example, the context might store the computer the program is running on or the parent session that controls it.<br>
 * <br>
 * Every program should store a world context that defines its environment.
 * Programs can access the stored values by using the internal {@link ValueInjector} with {@link #injectValues(Object)} on themselves.
 * 
 * @see DesktopProgramWindow
 * @see ValueInjector
 */
public class DesktopProgramWorldContext {

    private final ValueInjector valueInjector;

    /**
     * Creates a new desktop program world context that uses the values that are stored in the given {@link ValueInjector}.
     * Modifications to the injector object automatically transfer into the world context-
     * 
     * @param valueInjector The value injector whose stored values should be used in the context.
     */
    public DesktopProgramWorldContext(ValueInjector valueInjector) {

        this.valueInjector = valueInjector;
    }

    /**
     * Injects the stored values into the given object.
     * See {@link ValueInjector#runOn(Object)} for more details.
     * 
     * @param object The object to inject values into.
     */
    public void injectValues(Object object) {

        valueInjector.runOn(object);
    }

}
