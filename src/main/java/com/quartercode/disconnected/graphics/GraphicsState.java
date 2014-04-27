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

package com.quartercode.disconnected.graphics;

import java.util.Map;
import javax.naming.OperationNotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.matthiasmann.twl.Container;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Widget;

/**
 * Graphics state widgets are like categories for deciding what should be drawn.
 * They contain {@link GraphicsModule}s which access the graphics state widgets for managing TWL {@link Widget}s.
 * The objects are used as top level child widgets for the root {@link Container} by the {@link GraphicsThread}.
 * 
 * @see GraphicsStateDescriptor
 * @see GraphicsModule
 */
public class GraphicsState extends Widget {

    private static final Logger               LOGGER = LoggerFactory.getLogger(GraphicsState.class);

    private final String                      name;
    private final Map<String, GraphicsModule> modules;

    /**
     * Creates a new graphics state that is using the given {@link GraphicsModule}s.
     * 
     * @param name The name of the new graphics state.
     * @param modules The {@link GraphicsModule}s the new state uses for managing {@link Widget}s.
     */
    public GraphicsState(String name, Map<String, GraphicsModule> modules) {

        this.name = name;
        this.modules = modules;

        setTheme("");
    }

    /**
     * Returns the name of the graphics state.
     * It can be used for debugging purposes.
     * 
     * @return The name of the graphics state.
     */
    public String getName() {

        return name;
    }

    /**
     * Returns the {@link GraphicsModule} that is assigned to the given name.
     * This method is usually used for accessing the value storage of other {@link GraphicsModule}s.
     * That way modules can share data without knowing each other's class name.
     * 
     * @param name The name the {@link GraphicsModule} for return is assigned to.
     * @return The {@link GraphicsModule} that is assigned to the given name.
     */
    public GraphicsModule getModule(String name) {

        return modules.get(name);
    }

    @Override
    protected void afterAddToGUI(GUI gui) {

        for (GraphicsModule module : modules.values()) {
            try {
                module.add(this);
            } catch (RuntimeException e) {
                LOGGER.error("Unexpected exception during graphics state add() (state '{}', module '{}'); aborting state", name, module.getClass().getName(), e);
                abort();
                return;
            }
        }
    }

    @Override
    protected void layout() {

        // Don't call super.layout(); the modules take care of that

        for (GraphicsModule module : modules.values()) {
            try {
                module.layout(this);
            } catch (RuntimeException e) {
                LOGGER.error("Unexpected exception during graphics state layout() (state '{}', module '{}'); aborting state", name, module.getClass().getName(), e);
                abort();
                return;
            }
        }
    }

    @Override
    protected void beforeRemoveFromGUI(GUI gui) {

        for (GraphicsModule module : modules.values()) {
            try {
                module.remove(this);
            } catch (RuntimeException e) {
                LOGGER.error("Unexpected exception during graphics state remove() (state '{}', module '{}'); aborting state", name, module.getClass().getName(), e);
                abort();
                return;
            }
        }
    }

    private void abort() {

        if (getParent() != null) {
            getParent().removeChild(this);
        } else {
            LOGGER.warn("Cannot abort graphics state '{}'; no parent widget set", name);
        }
    }

    @Override
    public void setVisible(boolean visible) {

        throw new RuntimeException(new OperationNotSupportedException("The visibility of a graphics state can't be changed"));
    }

}
