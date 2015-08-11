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

package com.quartercode.disconnected.client.graphics;

/**
 * A graphics module is a small class which takes care of <b>one</b> rendering responsibility. <br>
 * <br>
 * The {@link Class} objects of graphics modules are stored in {@link GraphicsStateDescriptor}s.
 * These {@link GraphicsStateDescriptor}s create instances of the modules when a new {@link GraphicsState} is created.
 * That means that modules can store data as member variables. <br>
 * <br>
 * {@link GraphicsStateDescriptor}s usually store massive amounts of modules.
 * Every module only has <b>one</b> responsibility, like creating a layout or adding one button.
 * That way modifications can be done by simply adding or removing modules without changing their code. <br>
 * <br>
 * Every graphics module has several methods that are called when an instance of the module is used.
 * For example, the {@link #add(GraphicsState)} method is called when a new module is instantiated and added to a {@link GraphicsState}.
 * Furthermore, graphics modules provide and integrated value storage.
 * That way modules can depend on other modules and retrieve their values, like layouts etc.
 *
 * @see AbstractGraphicsModule
 * @see GraphicsStateDescriptor
 */
public interface GraphicsModule {

    /**
     * Callback that is invoked when a new object of the module is instantiated and added to the given {@link GraphicsState}.
     * This method usually adds the gui components the modules requires to work.
     *
     * @param state The {@link GraphicsState} that uses the module.
     */
    public void add(GraphicsState state);

    /**
     * Callback that is invoked when the layout() method is called on the given {@link GraphicsState}.
     * This method usually layouts the components added in {@link #add(GraphicsState)} if that's not done by a layout manager.
     *
     * @param state The {@link GraphicsState} that uses the module. The original layout() call was done there.
     */
    public void layout(GraphicsState state);

    /**
     * Callback that is invoked when the given {@link GraphicsState} is disabled.
     * Actually, the callback is called when the given {@link GraphicsState} is removed from the root container.
     *
     * @param state The {@link GraphicsState} that uses the module.
     */
    public void remove(GraphicsState state);

    /**
     * Returns the value that is stored and associated with the given key.
     * The value is probably set by the implementing module.
     * This method should be used by other modules which want to access the module's data.
     *
     * @param key The key the value for return is associated with.
     * @return The value that is associated with the given key.
     */
    public Object getValue(String key);

}
