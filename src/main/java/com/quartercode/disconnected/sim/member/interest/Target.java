
package com.quartercode.disconnected.sim.member.interest;

import com.quartercode.disconnected.sim.member.Member;

/**
 * This class defines methods for an interest which has a member as target.
 * This could also be used for defining computer targets (every computer is controlled by a member).
 * 
 * @see Interest
 */
public interface Target {

    /**
     * Returns the member the interest has as target.
     * 
     * @return The member the interest has as target.
     */
    public Member getTarget();

}
