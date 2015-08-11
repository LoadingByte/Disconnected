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

package com.quartercode.disconnected.server.world.comp.prog.general;

import static com.quartercode.disconnected.server.world.comp.prog.util.ProgEventUtils.registerSBPAwareEventHandler;
import static com.quartercode.disconnected.server.world.comp.prog.util.ProgStateUtils.addInterruptionStopperRegisteringExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import com.quartercode.classmod.extra.conv.CFeatureHolder;
import com.quartercode.classmod.extra.func.FunctionExecutor;
import com.quartercode.classmod.extra.func.FunctionInvocation;
import com.quartercode.disconnected.server.bridge.SBPAwareEventHandler;
import com.quartercode.disconnected.server.sim.TickService;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTask;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTaskAdapter;
import com.quartercode.disconnected.server.world.comp.file.File;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.comp.prog.ProcessModule;
import com.quartercode.disconnected.server.world.comp.prog.Process;
import com.quartercode.disconnected.server.world.comp.prog.ProgramExecutor;
import com.quartercode.disconnected.server.world.comp.prog.ProgramUtils;
import com.quartercode.disconnected.server.world.comp.prog.SchedulerUsing;
import com.quartercode.disconnected.server.world.comp.user.User;
import com.quartercode.disconnected.shared.event.comp.prog.general.PMP_SBPWPU_UpdateViewCommand;
import com.quartercode.disconnected.shared.event.comp.prog.general.PMP_WP_InterruptProcessCommand;
import com.quartercode.disconnected.shared.event.comp.prog.generic.GP_SBPWPU_ErrorEvent;
import com.quartercode.disconnected.shared.identity.SBPIdentity;
import com.quartercode.disconnected.shared.world.comp.prog.SBPWorldProcessUserId;
import com.quartercode.disconnected.shared.world.comp.prog.WorldProcessPlaceholder;
import com.quartercode.eventbridge.bridge.Bridge;

/**
 * The process manager program is used to list and interrupt {@link Process}es.
 * 
 * @see ProgramExecutor
 */
public class ProcessManagerProgram extends ProgramExecutor implements SchedulerUsing {

    /**
     * The amount of ticks between two {@link PMP_SBPWPU_UpdateViewCommand}s are sent by this program.
     */
    public static final int UPDATE_VIEW_SENDING_PERIOD = TickService.DEFAULT_TICKS_PER_SECOND;

    // ----- Functions -----

    static {

        addInterruptionStopperRegisteringExecutor(ProcessManagerProgram.class);

        RUN.addExecutor("registerSendUpdateViewTask", ProcessManagerProgram.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                CFeatureHolder program = invocation.getCHolder();
                program.get(SCHEDULER).schedule("sendUpdateView", "computer.programUpdate", 1, UPDATE_VIEW_SENDING_PERIOD, new SendUpdateViewTask());

                return invocation.next(arguments);
            }

        });

        RUN.addExecutor("registerInterruptProcessCommandHandler", ProcessManagerProgram.class, new FunctionExecutor<Void>() {

            @Override
            public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                ProcessManagerProgram program = (ProcessManagerProgram) invocation.getCHolder();
                registerSBPAwareEventHandler(program, PMP_WP_InterruptProcessCommand.class, new InterruptProcessCommandHandler(program), true);

                return invocation.next(arguments);
            }

        });

    }

    /**
     * A {@link SchedulerTask} that sends {@link PMP_SBPWPU_UpdateViewCommand}s for a specific {@link ProcessManagerProgram} instance.
     * It is typically called periodically.
     * 
     * @see ProcessManagerProgram
     */
    public static class SendUpdateViewTask extends SchedulerTaskAdapter {

        static {

            EXECUTE.addExecutor("default", SendUpdateViewTask.class, new FunctionExecutor<Void>() {

                @Override
                public Void invoke(FunctionInvocation<Void> invocation, Object... arguments) {

                    ProcessManagerProgram program = (ProcessManagerProgram) arguments[0];
                    sendUpdateView(program.getParent());

                    return invocation.next(arguments);
                }

            });

        }

    }

    /**
     * An {@link SBPAwareEventHandler} that processes {@link PMP_WP_InterruptProcessCommand} for a specific {@link ProcessManagerProgram} instance.
     * 
     * @see ProcessManagerProgram
     */
    @RequiredArgsConstructor
    public static class InterruptProcessCommandHandler implements SBPAwareEventHandler<PMP_WP_InterruptProcessCommand> {

        private final ProcessManagerProgram program;

        @Override
        public void handle(PMP_WP_InterruptProcessCommand event, SBPIdentity sender) {

            int pid = event.getPid();
            Validate.isTrue(pid >= 0, "PID must be >= 0");

            SBPWorldProcessUserId wpuId = program.getParent().getObj(Process.WORLD_PROCESS_USER);
            Process<?> process = program.getParent();
            ProcessModule procModule = process.invoke(Process.GET_OS).getObj(OperatingSystem.PROC_MODULE);

            // Interrupting the root process is disallowed (in order to not mess up the OS)
            if (pid == 0) {
                String rootProcessName = getName(procModule.getObj(ProcessModule.ROOT_PROCESS));
                program.getBridge().send(new GP_SBPWPU_ErrorEvent(wpuId, "interruptProcess.missingRights", rootProcessName, String.valueOf(pid)));
                return;
            }

            // Retrieve the process that should be interrupted
            Process<?> targetProcess = null;
            for (Process<?> otherProcess : procModule.invoke(ProcessModule.GET_ALL)) {
                if (otherProcess.getObj(Process.PID) == pid) {
                    targetProcess = otherProcess;
                    break;
                }
            }

            // If this is null, the process might have terminated by itself
            if (targetProcess != null) {
                User sessionUser = process.invoke(Process.GET_USER);

                // Check for the rights to interrupt the process
                if (sessionUser.invoke(User.IS_SUPERUSER) || targetProcess.invoke(Process.GET_USER).equals(sessionUser)) {
                    // Do not interrupt recursively; the interrupted process might do that by itself
                    targetProcess.invoke(Process.INTERRUPT, false);
                } else {
                    String targetProcessName = getName(targetProcess);
                    program.getBridge().send(new GP_SBPWPU_ErrorEvent(wpuId, "interruptProcess.missingRights", targetProcessName, String.valueOf(pid)));
                }
            }
        }

        private String getName(Process<?> process) {

            return process.getObj(Process.SOURCE).getObj(File.NAME);
        }

    }

    /**
     * Sends an {@link PMP_SBPWPU_UpdateViewCommand} from the given {@link ProcessManagerProgram} {@link Process}.
     * Note that this method is completely independent from any state (it's a utility method).
     * 
     * @param programProcess The process manager program process which sends the command.
     */
    public static void sendUpdateView(Process<?> programProcess) {

        Bridge bridge = programProcess.getBridge();
        SBPWorldProcessUserId wpuId = programProcess.getObj(Process.WORLD_PROCESS_USER);

        Process<?> rootProcess = programProcess.invoke(Process.GET_ROOT);
        WorldProcessPlaceholder rootProcessPlaceholder = ProgramUtils.createProcessPlaceholder(rootProcess, true);

        bridge.send(new PMP_SBPWPU_UpdateViewCommand(wpuId, rootProcessPlaceholder));
    }

}
