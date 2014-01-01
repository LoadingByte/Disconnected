
package com.quartercode.disconnected.world.comp.os;

import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;

/**
 * Syscall invokers are classes which can invoke syscalls (like {@link UserSyscalls}).
 * Every invoker class must implement the {@link #GET_OPERATING_SYSTEM} function which provides the {@link OperatingSystem} object the invoker is related to.
 * 
 * @see OperatingSystem
 */
public interface SyscallInvoker extends FeatureHolder {

    /**
     * Returns the {@link OperatingSystem} the syscall invoker is related to.
     */
    public static final FunctionDefinition<OperatingSystem> GET_OPERATING_SYSTEM = FunctionDefinitionFactory.create("getOperatingSystem", OperatingSystem.class);

}
