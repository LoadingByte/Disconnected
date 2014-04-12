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

package com.quartercode.disconnected.world.comp.program;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import com.quartercode.classmod.extra.CollectionPropertyDefinition;
import com.quartercode.classmod.extra.ExecutorInvocationException;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.extra.FunctionExecutor;
import com.quartercode.classmod.extra.FunctionInvocation;
import com.quartercode.classmod.extra.Prioritized;
import com.quartercode.classmod.extra.PropertyDefinition;
import com.quartercode.classmod.extra.def.ObjectProperty;
import com.quartercode.classmod.extra.def.ReferenceCollectionProperty;
import com.quartercode.classmod.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.sim.run.Scheduler;
import com.quartercode.disconnected.sim.run.SchedulerUser;
import com.quartercode.disconnected.world.WorldChildFeatureHolder;
import com.quartercode.disconnected.world.event.Event;
import com.quartercode.disconnected.world.event.EventListener;
import com.quartercode.disconnected.world.event.EventMatcher;
import com.quartercode.disconnected.world.event.QueueEventListener;

/**
 * This abstract class defines a program executor which takes care of acutally running a program.
 * The executor class is set in the {@link Program}.
 * 
 * @see Program
 * @see Process
 * @see PacketListener
 */
public abstract class ProgramExecutor extends WorldChildFeatureHolder<Process<?>> implements SchedulerUser, EventListener {

    // ----- Properties -----

    /**
     * The {@link EventListener} all incoming {@link Event}s are delegated to.
     * In the current implementation, this is just a {@link QueueEventListener} that stores incoming events for the next update call.
     */
    protected static final PropertyDefinition<QueueEventListener>                           IN_EVENT_LISTENER;

    /**
     * This list stores all {@link EventListener}s that are registered and want to receive any {@link Event}s sent by this executor.
     */
    protected static final CollectionPropertyDefinition<EventListener, List<EventListener>> OUT_EVENT_LISTENERS;

    static {

        IN_EVENT_LISTENER = ObjectProperty.createDefinition("inEventListener", new QueueEventListener());
        OUT_EVENT_LISTENERS = ReferenceCollectionProperty.createDefinition("outEventListeners", new ArrayList<EventListener>());

    }

    // ----- Functions -----

    /**
     * Returns the next received {@link Event} from the {@link Queue} which matches the criteria of the given {@link EventMatcher}.
     * Every returned {@link Event} is removed from the internal storage {@link Queue}.
     * Example:
     * 
     * <pre>
     * Format: EventType[Identifier]
     * Queue:  A[1], A[2], B[3], A[4], B[5], B[6]
     * 
     * NEXT_EVENT (matcher for A) returns A[1]
     * => A[2], B[3], A[4], B[5], B[6]
     * 
     * NEXT_EVENT (matcher for B) returns B[3]
     * => A[2], A[4], B[5], B[6]
     * 
     * NEXT_EVENT (matcher for A) returns A[2]
     * => A[4], B[5], B[6]
     * </pre>
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link EventMatcher}</td>
     * <td>matcher</td>
     * <td>The {@link EventMatcher} that checks for the {@link Event} which should be returned.</td>
     * </tr>
     * </table>
     * 
     * @see QueueEventListener#NEXT_EVENT
     */
    public static final FunctionDefinition<Event>                                           NEXT_EVENT;

    /**
     * This callback is executed once when the program executor should start running.
     * For example, this method could schedule tasks using the {@link Scheduler}.
     */
    public static final FunctionDefinition<Void>                                            RUN;

    static {

        NEXT_EVENT = FunctionDefinitionFactory.create("nextEvent", ProgramExecutor.class, new FunctionExecutor<Event>() {

            @Override
            public Event invoke(FunctionInvocation<Event> invocation, Object... arguments) throws ExecutorInvocationException {

                Event nextEvent = invocation.getHolder().get(IN_EVENT_LISTENER).get().get(QueueEventListener.NEXT_EVENT).invoke(arguments);
                invocation.next(arguments);
                return nextEvent;
            }

        }, EventMatcher.class);

        HANDLE_EVENT.addExecutor("delegate", ProgramExecutor.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                invocation.getHolder().get(IN_EVENT_LISTENER).get().get(EventListener.HANDLE_EVENT).invoke(arguments);
                return invocation.next(arguments);
            }

        });

        // Prevent the scheduler from updating if the current process state isn't an active one
        TICK_UPDATE.addExecutor("checkAllowTick", ProgramExecutor.class, new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_9)
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) throws ExecutorInvocationException {

                if ( ((ProgramExecutor) invocation.getHolder()).getParent().get(Process.STATE).get().isTickState()) {
                    invocation.next(arguments);
                }

                return null;
            }

        });

        RUN = FunctionDefinitionFactory.create("run");

    }

    /**
     * Creates a new program executor.
     */
    public ProgramExecutor() {

        setParentType(Process.class);
    }

}
