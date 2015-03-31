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

package com.quartercode.disconnected.server.sim.scheduler;

import static com.quartercode.classmod.factory.ClassmodFactory.factory;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.classmod.extra.func.Function;
import com.quartercode.classmod.extra.func.FunctionDefinition;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.classmod.extra.prop.PropertyDefinition;
import com.quartercode.classmod.extra.storage.StandardStorage;
import com.quartercode.classmod.factory.PropertyDefinitionFactory;
import com.quartercode.classmod.util.FeatureDefinitionReference;

/**
 * A function call scheduler task is a {@link SchedulerTask} that "just" calls a {@link Function} on execution.
 * It supports persistence and can restore scheduled function calls that were serialized before.
 * For achieving that, it uses the {@link FeatureDefinitionReference} utility.
 * 
 * @see SchedulerTask
 * @see FeatureDefinitionReference
 */
public class FunctionCallSchedulerTask extends SchedulerTaskAdapter {

    // ----- Properties -----

    /**
     * A {@link FeatureDefinitionReference} that references the {@link FunctionDefinition} which defines the {@link Function} that should be called by the task.
     */
    public static final PropertyDefinition<FeatureDefinitionReference<FunctionDefinition<?>>> FUNCTION_DEFINITION;

    static {

        FUNCTION_DEFINITION = factory(PropertyDefinitionFactory.class).create("functionDefinition", new StandardStorage<>());

    }

    // ----- Functions -----

    static {

        EXECUTE.addExecutor("default", FunctionCallSchedulerTask.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder schedulerHolder = (CFeatureHolder) arguments[0];
                FunctionDefinition<?> functionDefinition = invocation.getCHolder().getObj(FUNCTION_DEFINITION).getDefinition();

                schedulerHolder.invoke(functionDefinition);

                return invocation.next(arguments);
            }

        });

    }

}
