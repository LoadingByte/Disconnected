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

package com.quartercode.disconnected.client.graphics;

import java.util.HashMap;
import java.util.Map;
import de.matthiasmann.twl.Widget;

/**
 * Abstract graphics modules implement common module functionalities of {@link GraphicsModule} and may be used as superclasses.
 * They implement the value storage system which is defined by the {@link GraphicsModule} interface.
 * They also provide empty implementations for all callback methods, as well as some useful utility methods.<br>
 * <br>
 * See {@link GraphicsModule} for further documentation on graphics modules.
 * 
 * @see GraphicsModule
 */
public abstract class AbstractGraphicsModule implements GraphicsModule {

    private final Map<String, Object> values = new HashMap<>();

    /**
     * Calculates the x value that is described by the given relative percent amount between {@code 0} and {@code 1}.
     * That means that the current width that is available to the given {@link GraphicsState} is just multiplied with the given percent value.
     * For example, calling the method on a {@link GraphicsState} that has {@code 200} available width pixels with {@code 0.5} percent would return {@code 100}.
     * 
     * @param state The {@link GraphicsState} whose available width should be used for the calculation.
     * @param percent The relative "amount of pixels" in percent. Must be between {@code 0} and {@code 1}.
     * @return The absolute amount of pixels calculated with the relative amount.
     */
    protected int getRelativeX(GraphicsState state, float percent) {

        return (int) (state.getWidth() * percent);
    }

    /**
     * Calculates the x location the given {@link Widget} must be positioned in order to make its position match with the given percent value.
     * For more details, see {@link #getRelativeX(GraphicsState, float)}.
     * 
     * @param state The {@link GraphicsState} whose available width should be used for the calculation.
     * @param component The {@link Widget} that can be positioned with the result of the calculation.
     * @param percent The relative "amount of pixels" in percent. Must be between {@code 0} and {@code 1}.
     * @return The x location where the given {@link Widget} must be positioned.
     */
    protected int getRelativeX(GraphicsState state, Widget component, float percent) {

        return (int) (getRelativeX(state, percent) - component.getWidth() * percent);
    }

    /**
     * Calculates the y value that is described by the given relative percent amount between {@code 0} and {@code 1}.
     * That means that the current height that is available to the given {@link GraphicsState} is just multiplied with the given percent value.
     * For example, calling the method on a {@link GraphicsState} that has {@code 200} available height pixels with {@code 0.5} percent would return {@code 100}.
     * 
     * @param state The {@link GraphicsState} whose available height should be used for the calculation.
     * @param percent The relative "amount of pixels" in percent. Must be between {@code 0} and {@code 1}.
     * @return The absolute amount of pixels calculated with the relative amount.
     */
    protected int getRelativeY(GraphicsState state, float percent) {

        return (int) (state.getHeight() * percent);
    }

    /**
     * Calculates the y location the given {@link Widget} must be positioned in order to make its position match with the given percent value.
     * For more details, see {@link #getRelativeY(GraphicsState, float)}.
     * 
     * @param state The {@link GraphicsState} whose available height should be used for the calculation.
     * @param component The {@link Widget} that can be positioned with the result of the calculation.
     * @param percent The relative "amount of pixels" in percent. Must be between {@code 0} and {@code 1}.
     * @return The y location where the given {@link Widget} must be positioned.
     */
    protected int getRelativeY(GraphicsState state, Widget component, float percent) {

        return (int) (getRelativeY(state, percent) - component.getHeight() * percent);
    }

    @Override
    public Object getValue(String key) {

        return values.get(key);
    }

    /**
     * Assigns a given value object to a key for making the object accessible to other {@link GraphicsModule}s.
     * The method overrides an existing value objects with the same key. <br>
     * <br>
     * See {@link #getValue(String)} for further documentation on the value system.
     * 
     * @param key The key to assign the given value object to.
     * @param value The value object to assign to the given key.
     */
    protected void setValue(String key, Object value) {

        values.put(key, value);
    }

    // Empty implementations (can be overriden)

    @Override
    public void add(GraphicsState state) {

    }

    @Override
    public void layout(GraphicsState state) {

    }

    @Override
    public void remove(GraphicsState state) {

    }

}
