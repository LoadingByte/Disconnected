
package com.quartercode.disconnected.world;

import com.quartercode.disconnected.sim.run.TickSimulator;

/**
 * {@link Property}s that implement this interface can be updated during a simulation tick.
 * Update properties are generally executed by the {@link TickSimulator#update()} method.
 */
public interface Updateable {

    /**
     * Executes a tick update in the {@link Property}.
     */
    public void update();

}
