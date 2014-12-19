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

package com.quartercode.disconnected.client.graphics.desktop;

import com.quartercode.disconnected.client.graphics.GraphicsState;
import com.quartercode.disconnected.client.util.ValueInjector.InjectValue;
import com.quartercode.disconnected.shared.event.comp.prog.SBPWorldProcessUserCommand;
import com.quartercode.disconnected.shared.event.comp.prog.SBPWorldProcessUserCommandPredicate;
import com.quartercode.disconnected.shared.event.comp.prog.control.SBPWorldProcessUserInterruptCommand;
import com.quartercode.disconnected.shared.event.comp.prog.control.WorldProcessInterruptCommand;
import com.quartercode.disconnected.shared.event.comp.prog.control.WorldProcessLaunchAcknowledgmentEvent;
import com.quartercode.disconnected.shared.event.comp.prog.control.WorldProcessLaunchCommand;
import com.quartercode.disconnected.shared.world.comp.prog.ClientProcessDetails;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessId;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.eventbridge.bridge.module.EventHandler;
import com.quartercode.eventbridge.bridge.module.StandardHandlerModule;
import com.quartercode.eventbridge.extra.predicate.MultiPredicates;
import com.quartercode.eventbridge.extra.predicate.TypePredicate;

/**
 * An abstract implementation of {@link ClientProgramWindow} that provides a skeletal structure for real client programs.
 * This class is used to remove a lot of boilerplate code from the actual client program implementations.<br>
 * <br>
 * The following preset fields are available:
 * 
 * <table>
 * <tr>
 * <td>{@link #bridge}</td>
 * <td>The {@link Bridge} to use for server communication. It is injected using the {@link ClientProgramContext}.</td>
 * </tr>
 * <tr>
 * <td>{@link #clientProcessDetails}</td>
 * <td>A {@link ClientProcessDetails} object that identifies the running client program. It is used for receiving server events.</td>
 * </tr>
 * <tr>
 * <td>{@link #worldProcessId}</td>
 * <td>A {@link WorldProcessId} that identifies the running process on the server. It should be used for sending server events.</td>
 * </tr>
 * </table>
 * 
 * The following preset methods are available:
 * 
 * <table>
 * <tr>
 * <td>{@link #initializeGraphics()}</td>
 * <td>Called first on construction; the graphical layout and components should be initialized here <b>without</b> logic being added to them.</td>
 * </tr>
 * <tr>
 * <td>{@link #initializeInteractions()}</td>
 * <td>Called second on construction; logic should be added to the graphical components here (e.g. a callback could be added to a button). By default, the method interrupts the world program when the
 * window is closed.</td>
 * </tr>
 * <tr>
 * <td>{@link #registerEventHandlers()}</td>
 * <td>Called third on construction; any {@link EventHandler}s that <b>do not</b> require the {@link #worldProcessId} field should be registered here.</td>
 * </tr>
 * <tr>
 * <td>{@link #launchWorldProcess()}</td>
 * <td>Called fourth on construction; by default, this method calls {@link #doLaunchWorldProcess()} and does some other things required for the program to work.</td>
 * </tr>
 * <tr>
 * <td>{@link #doLaunchWorldProcess()}</td>
 * <td>Called by {@link #launchWorldProcess()}; a {@link WorldProcessLaunchCommand} with the stored {@link #clientProcessDetails} to launch the actual world process should be sent here.</td>
 * </tr>
 * <tr>
 * <td>{@link #registerEventHandlersRequiringWorldProcessId()}</td>
 * <td>Called by {@link #launchWorldProcess()} once {@link #worldProcessId} is set; any {@link EventHandler}s that <b>do</b> require the {@link #worldProcessId} field should be registered here.</td>
 * </tr>
 * <tr>
 * <td>{@link #executeActionsRequiringWorldProcessId()}</td>
 * <td>Called by {@link #launchWorldProcess()} once {@link #worldProcessId} is set and {@link #registerEventHandlersRequiringWorldProcessId()} has been called; any other actions that <b>do</b> require
 * the {@link #worldProcessId} field should be executed here.</td>
 * </tr>
 * </table>
 * 
 * @see ClientProgramWindow
 */
public class ClientProgramWindowSkeleton extends ClientProgramWindow {

    /**
     * The {@link Bridge} that should be used for communicating with the server.
     * It is injected using the {@link ClientProgramContext}.
     */
    @InjectValue ("bridge")
    protected Bridge               bridge;

    /**
     * A {@link ClientProcessDetails} object that identifies the running client program.
     * It is used for receiving server events.
     */
    protected ClientProcessDetails clientProcessDetails;

    /**
     * A {@link WorldProcessId} that identifies the running process on the server.
     * It should be used for sending server events
     */
    protected WorldProcessId       worldProcessId;

    /**
     * Creates a new client program window skeleton with the given parameters.
     * 
     * @param state The desktop state the new program will be running in.
     * @param descriptor The program descriptor that created the object.
     * @param context The {@link ClientProgramContext} that contains information about the environment of the program.
     * @see ClientProgramWindow#ClientProgramWindow(GraphicsState, ClientProgramDescriptor, ClientProgramContext)
     */
    public ClientProgramWindowSkeleton(GraphicsState state, ClientProgramDescriptor descriptor, ClientProgramContext context) {

        super(state, descriptor, context);

        context.injectValues(this);

        // Use a dummy client process id
        // TODO: Use propert client process ids; this system will eventually run into collisions
        clientProcessDetails = new ClientProcessDetails((int) (Math.random() * 10000D));

        initializeGraphics();
        initializeInteractions();
        registerEventHandlers();
        launchWorldProcess();
    }

    // ----- Overridable methods -----

    /**
     * This method should initialize the graphical layout and components <b>without</b> logic being added to them.
     * It is called first on construction.
     */
    protected void initializeGraphics() {

    }

    /**
     * This method should add logic to the graphical components.
     * For example, a callback could be added to a button.
     * By default, the method interrupts the assigned world process when the window is closed.
     * It is called second on construction.
     */
    protected void initializeInteractions() {

        // Register a callback that interrupts the world process
        addCloseListener(new Runnable() {

            @Override
            public void run() {

                bridge.send(new WorldProcessInterruptCommand(worldProcessId.getPid(), false));
            }

        });
    }

    /**
     * This method should register any {@link EventHandler}s that <b>do not</b> require the {@link #worldProcessId} field.
     * By default, the method closes the program window when a {@link SBPWorldProcessUserInterruptCommand} is received.
     * It is called third on construction.
     */
    protected void registerEventHandlers() {

        registerEventHandler(SBPWorldProcessUserInterruptCommand.class, new EventHandler<SBPWorldProcessUserInterruptCommand>() {

            @Override
            public void handle(SBPWorldProcessUserInterruptCommand event) {

                close();
            }

        });
    }

    /**
     * By default, this method calls {@link #doLaunchWorldProcess()} and does some other things required for the program to work.
     * It is called fourth on construction and should not be overridden if {@link #doLaunchWorldProcess()}, {@link #registerEventHandlersRequiringWorldProcessId()} or
     * {@link #executeActionsRequiringWorldProcessId()} is used.
     */
    protected void launchWorldProcess() {

        // Register a handler for catching the acknowledgment event and store the world process identity
        registerEventHandler(WorldProcessLaunchAcknowledgmentEvent.class, new EventHandler<WorldProcessLaunchAcknowledgmentEvent>() {

            @Override
            public void handle(WorldProcessLaunchAcknowledgmentEvent event) {

                worldProcessId = event.getWorldProcessId();

                registerEventHandlersRequiringWorldProcessId();
                executeActionsRequiringWorldProcessId();

                // Remove the handler after the acknowledgment arrived
                bridge.getModule(StandardHandlerModule.class).removeHandler(this);
            }

        });

        doLaunchWorldProcess();
    }

    /**
     * This method should send a {@link WorldProcessLaunchCommand} with the stored {@link #clientProcessDetails} to launch the actual world process.
     * It is called by {@link #launchWorldProcess()}.
     */
    protected void doLaunchWorldProcess() {

    }

    /**
     * This method should register any {@link EventHandler}s that <b>do</b> require the {@link #worldProcessId} field.
     * It is called by {@link #launchWorldProcess()} immediately after {@link #worldProcessId} has been set.
     */
    protected void registerEventHandlersRequiringWorldProcessId() {

    }

    /**
     * This method should execute any actions that <b>do</b> require the {@link #worldProcessId} field (apart from registering {@link EventHandler}s).
     * It is called by {@link #launchWorldProcess()} immediately after {@link #worldProcessId} has been set and {@link #registerEventHandlersRequiringWorldProcessId()} has been called.
     */
    protected void executeActionsRequiringWorldProcessId() {

    }

    // ----- Utility methods -----

    /**
     * Registers the given {@link EventHandler} for the client program and makes it listen for the given type of {@link SBPWorldProcessUserCommand} events that are sent to it.
     * The handler will be automatically removed once the client program is stopped.
     * 
     * @param eventType The event type all handled events must have.
     * @param handler The event handler that should handle the incoming events.
     */
    protected void registerEventHandler(Class<? extends SBPWorldProcessUserCommand> eventType, final EventHandler<?> handler) {

        bridge.getModule(StandardHandlerModule.class).addHandler(handler,
                MultiPredicates.and(new TypePredicate<>(eventType), new SBPWorldProcessUserCommandPredicate<>(clientProcessDetails)));

        addCloseListener(new Runnable() {

            @Override
            public void run() {

                bridge.getModule(StandardHandlerModule.class).removeHandler(handler);
            }

        });
    }

}
