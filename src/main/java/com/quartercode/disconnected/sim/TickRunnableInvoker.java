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

package com.quartercode.disconnected.sim;

import java.util.LinkedList;
import java.util.Queue;
import com.quartercode.disconnected.util.RunnableInvocationProvider;

/**
 * The tick runnable invoker is used to invoke {@link Runnable}s in the tick update thread.
 */
public class TickRunnableInvoker implements TickAction, RunnableInvocationProvider {

    private final Queue<Runnable> toInvoke = new LinkedList<>();

    /**
     * Creates a new tick runnable invoker.
     */
    public TickRunnableInvoker() {

    }

    /**
     * Invokes the given {@link Runnable} in the tick update thread.
     * 
     * @param runnable The runnable to invoke in the tick update thread.
     */
    @Override
    public void invoke(Runnable runnable) {

        toInvoke.offer(runnable);
    }

    @Override
    public void update() {

        while (!toInvoke.isEmpty()) {
            toInvoke.poll().run();
        }
    }

}
