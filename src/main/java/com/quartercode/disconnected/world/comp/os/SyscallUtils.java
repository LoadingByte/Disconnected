
package com.quartercode.disconnected.world.comp.os;

import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.extra.ExecutorInvokationException;
import com.quartercode.disconnected.mocl.extra.Function;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Prioritized;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;

/**
 * This utility class provides some useful methods for syscall classes and their wrappers.
 * 
 * @see SyscallInvoker
 */
public class SyscallUtils {

    /**
     * Adds a delegation for the given {@link FunctionDefinition}.
     * The method adds two {@link FunctionExecutor} to the method for delegation: The first one delegates the call and the second one cancels it.
     * The call is delegated to the same function on the {@link FeatureHolder} which is accessible through the wrapped accessor.
     * 
     * @param wrapper The class object of the wrapper.
     * @param wrappedAccessor The {@link FunctionDefinition} of a {@link Function} which returns the {@link FeatureHolder} to delegate the calls to.
     * @param delegated The {@link FunctionDefinition} the delegation to the same {@link FunctionDefinition} in the other object is added.
     */
    public static <R> void addDelegation(Class<? extends FeatureHolder> wrapper, final FunctionDefinition<? extends FeatureHolder> wrappedAccessor, final FunctionDefinition<R> delegated) {

        delegated.addExecutor(wrapper, "delegateCall", new FunctionExecutor<R>() {

            @Override
            @Prioritized (Prioritized.LEVEL_7 + Prioritized.SUBLEVEL_1)
            public R invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                return holder.get(wrappedAccessor).invoke().get(delegated).invoke(arguments);
            }

        });
        delegated.addExecutor(wrapper, "cancelCall", new FunctionExecutor<R>() {

            @Override
            @Prioritized (Prioritized.LEVEL_7)
            public R invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                throw new StopExecutionException("Cancel delegated call");
            }

        });
    }

    private SyscallUtils() {

    }

}
