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

package com.quartercode.disconnected.client.graphics.desktop.prog.util;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.mutable.Mutable;
import com.quartercode.disconnected.client.graphics.desktop.DesktopWindow;
import com.quartercode.disconnected.client.graphics.desktop.prog.ClientProgramExecutor;
import com.quartercode.disconnected.client.graphics.desktop.prog.ClientProgramStateContext;
import com.quartercode.disconnected.shared.event.comp.prog.control.SBPWorldProcessUserInterruptCommand;
import com.quartercode.disconnected.shared.event.comp.prog.control.WorldProcessInterruptCommand;
import com.quartercode.disconnected.shared.world.comp.prog.SBPWorldProcessUserDetails;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessId;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.module.EventHandler;

/**
 * This utility class provides some utility methods and classes regarding the {@link ClientProgramStateContext} of {@link ClientProgramExecutor}s.
 * They should be used to remove the need for boilerplate code.
 *
 * @see ClientProgramExecutor
 */
public class ProgStateUtils {

    /**
     * Calls {@link ClientProgramStateContext#stop()} as soon as the world process, which addresses its events at the given {@link SBPWorldProcessUserDetails world process user id}, is interrupted.
     *
     * @param bridge The {@link Bridge} through which the server running the world process can be reached.
     * @param stateContext The state context that should be stopped.
     *        It is also used to remove the added event handlers when it stops for another reason.
     * @param wpu The {@link SBPWorldProcessUserDetails world process user id} the world processes addresses its events at.
     */
    public static void stopOnWorldProcessInterrupt(Bridge bridge, final ClientProgramStateContext stateContext, SBPWorldProcessUserDetails wpu) {

        Validate.notNull(bridge, "Bridge cannot be null");
        Validate.notNull(stateContext, "Client process state context cannot be null");
        Validate.notNull(wpu, "World process user details cannot be null");

        ProgEventUtils.addEventHandler(bridge, stateContext, new EventHandler<SBPWorldProcessUserInterruptCommand>() {

            @Override
            public void handle(SBPWorldProcessUserInterruptCommand event) {

                stateContext.stop();
            }

        }, SBPWorldProcessUserInterruptCommand.class, wpu);
    }

    /**
     * Sets the given {@link Mutable} to {@code null} as soon as the world process, which addresses its events at the given {@link SBPWorldProcessUserDetails world process user id}, is interrupted.
     *
     * @param bridge The {@link Bridge} through which the server running the world process can be reached.
     * @param stateContext The state context of the {@link ClientProgramExecutor} that called the method.
     *        It is used to remove the added event handlers when the program stops.
     * @param wpu The {@link SBPWorldProcessUserDetails world process user id} the world processes addresses its events at.
     * @param field The {@link Mutable} field that should be {@link Mutable#setValue(Object) set} to {@code null}.
     */
    public static void nullFieldOnWorldProcessInterrupt(Bridge bridge, final ClientProgramStateContext stateContext, SBPWorldProcessUserDetails wpu, final Mutable<?> field) {

        Validate.notNull(bridge, "Bridge cannot be null");
        Validate.notNull(stateContext, "Client process state context cannot be null");
        Validate.notNull(wpu, "World process user details cannot be null");
        Validate.notNull(field, "Mutable field cannot be null");

        ProgEventUtils.addEventHandler(bridge, stateContext, new EventHandler<SBPWorldProcessUserInterruptCommand>() {

            @Override
            public void handle(SBPWorldProcessUserInterruptCommand event) {

                field.setValue(null);
            }

        }, SBPWorldProcessUserInterruptCommand.class, wpu);
    }

    /**
     * Calls {@link ClientProgramStateContext#stop()} as soon as the given {@link DesktopWindow} is closed.
     * For doing that, a closing listener is added using {@link DesktopWindow#addCloseListener(Runnable)}.
     *
     * @param stateContext The state context that should be stopped.
     * @param window The desktop window that should be observed for closing.
     */
    public static void stopOnWindowClose(final ClientProgramStateContext stateContext, DesktopWindow window) {

        Validate.notNull(stateContext, "Client process state context cannot be null");
        Validate.notNull(window, "Desktop window cannot be null");

        window.addCloseListener(new Runnable() {

            @Override
            public void run() {

                stateContext.stop();
            }

        });
    }

    /**
     * Interrupts a certain world process as soon as the given {@link ClientProgramStateContext} is stopped.
     * That is done by sending a {@link WorldProcessInterruptCommand}.
     *
     * @param bridge The {@link Bridge} through which the server running the world process can be reached.
     * @param stateContext The state context that should be observed for stopping.
     * @param worldProcessIdField A {@link Mutable} {@link WorldProcessId} field the world process for interruption is taken from.
     *        The field is mutable in order to allow changes to the world process id after the return of this method.
     *        Note that a non-exception-throwing {@code null} check is executed before the actual interruption command is finally sent.
     */
    public static void interruptWorldProcessOnStop(final Bridge bridge, ClientProgramStateContext stateContext, final Mutable<WorldProcessId> worldProcessIdField) {

        Validate.notNull(bridge, "Bridge cannot be null");
        Validate.notNull(stateContext, "Client process state context cannot be null");
        Validate.notNull(worldProcessIdField, "Mutable world process id field cannot be null");

        stateContext.addStoppingListener(new Runnable() {

            @Override
            public void run() {

                if (worldProcessIdField.getValue() != null) {
                    bridge.send(new WorldProcessInterruptCommand(worldProcessIdField.getValue().getPid(), false));
                }
            }

        });
    }

    private ProgStateUtils() {

    }

}
