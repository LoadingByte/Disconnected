
package com.quartercode.disconnected.sim.run;

import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.Function;
import com.quartercode.classmod.extra.FunctionDefinition;
import com.quartercode.classmod.util.FunctionDefinitionFactory;

/**
 * {@link FeatureHolder}s which implement this interface inherit the {@link #TICK_UPDATE} {@link Function} that is automatically invoked by the tick simulator.
 * The simulator goes over all {@link FeatureHolder}s of a world and invoked that {@link #TICK_UPDATE} {@link Function} on all tick updatables.
 */
public interface TickUpdatable extends FeatureHolder {

    /**
     * The tick update {@link Function} is automatically invoked by the tick simulator on every tick.
     * It should execute some activities related to the simulation of the world tree.
     */
    public static final FunctionDefinition<Void> TICK_UPDATE = FunctionDefinitionFactory.create("tickUpdate");

}
