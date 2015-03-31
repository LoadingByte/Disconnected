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

/**
 * An initializer is a simple class which initializes a certain part of the application.
 * That initialization process is executed by the {@link #initialize()} method.
 * Each initializer must also be annotated with an {@link InitializerSettings} annotation.
 * That annotation provides more information about the initializer.
 * 
 * @see InitializerSettings
 */
public interface Initializer {

    /**
     * Initializes the part of the application the initializer is responsible for.
     * Note that this method should only be invoked once during the lifecycle of the application.
     * 
     * @throws InitializationException Thrown by the initializer if the whole initialization process should be interrupted.
     *         That causes the whole application to halt.
     *         Because of that, it should only be thrown when an unrecoverable error occurs.
     *         Minor errors should be ignored since the application might be able to work regardless of them.
     */
    public void initialize() throws InitializationException;

}
