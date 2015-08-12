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

package com.quartercode.disconnected.server.world.comp.prog;

import static com.quartercode.disconnected.server.world.comp.prog.ProgramUtils.getProgramFileFromPaths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.sim.TickService;
import com.quartercode.disconnected.server.sim.scheduler.DefaultScheduler;
import com.quartercode.disconnected.server.sim.scheduler.Scheduler;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTask;
import com.quartercode.disconnected.server.sim.scheduler.SchedulerTaskAdapter;
import com.quartercode.disconnected.server.world.comp.config.Config;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.file.FileException;
import com.quartercode.disconnected.server.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.server.world.comp.file.MissingFileRightsException;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.comp.os.config.EnvVariable;
import com.quartercode.disconnected.server.world.comp.os.config.User;
import com.quartercode.disconnected.server.world.comp.os.mod.OSModule;
import com.quartercode.disconnected.server.world.util.WorldNode;
import com.quartercode.disconnected.shared.util.registry.Registries;
import com.quartercode.disconnected.shared.util.registry.extra.NamedValueUtils;
import com.quartercode.disconnected.shared.world.comp.file.CommonFiles;

/**
 * This class represents an {@link OSModule operating system module} which is used to manage the {@link RootProcess}.
 * It is an essential part of the operating system and is directly used by it.
 *
 * @see RootProcess
 * @see OSModule
 */
public class ProcessModule extends WorldNode<OperatingSystem> implements OSModule {

    @XmlElement (type = DefaultScheduler.class)
    private final Scheduler<ProcessModule> scheduler = new DefaultScheduler<>();

    @XmlElement
    private RootProcess                    rootProcess;
    @XmlAttribute
    private int                            nextPid;

    /**
     * Returns the {@link RootProcess} which is the root of the entire {@link Process} tree.
     * It always has the {@link Process#getPid() PID} {@code 0}.
     * Every other process branches of this process somehow.
     * Note that the root processe's {@link Process#getUser()} is always {@code root}.
     *
     * @return The root process.
     *         It may be {@code null} if the process module is currently not running.
     */
    public RootProcess getRootProcess() {

        return rootProcess;
    }

    /**
     * Returns a list containing all currently running {@link Process}es, including the {@link #getRootProcess() root process}.
     *
     * @return All processes that are currently running.
     */
    public List<Process<?>> getAllProcesses() {

        List<Process<?>> processes = new ArrayList<>();
        processes.add(rootProcess);
        processes.addAll(rootProcess.getAllChildProcesses());
        return processes;
    }

    /**
     * Every time a new {@link Process} is created, its {@link Process#getPid() PID} should be set to the next PID returned by this function.
     * Internally, it just counts up integer numbers.
     * Note that that counter is reset to {@code 0} every time the process module shuts down.
     *
     * @return The next PID value for a {@link Process}.
     */
    protected int nextPid() {

        int pid = nextPid;
        nextPid++;
        return pid;
    }

    @Override
    public void setRunning(boolean running) {

        // Reset nextPid to 0
        resetNextPid();

        if (running) {
            launchRootProcess();
        } else {
            killRootProcess();
        }
    }

    /*
     * Resets the next PID to 0.
     */
    private void resetNextPid() {

        nextPid = 0;
    }

    /*
     * Launches a new root process with all necessary settings.
     */
    private void launchRootProcess() {

        FileSystemModule fsModule = getSingleParent().getFsModule();

        // Get BIN_PATHS
        List<String> binPaths = getBinPaths();

        // Get session program
        String sessionProgramFileName = NamedValueUtils.getByName(Registries.get(ServerRegistries.WORLD_PROGRAMS), "session").getCommonLocation().toString();
        ContentFile sessionProgramFile = getProgramFileFromPaths(fsModule, binPaths, sessionProgramFileName);
        Validate.validState(sessionProgramFile != null, "Cannot start process module: Session program not found");

        try {
            rootProcess = new RootProcess(nextPid(), sessionProgramFile);
        } catch (MissingFileRightsException e) {
            // Will never happen since this is the root process
        }

        // Get superuser
        User superuser = getSuperuser();

        // TODO: Set the superuser as the root session user
        // Session rootSession = (Session) rootProcess.getExecutor();
        // rootSession.setUser(superuser);
    }

    private List<String> getBinPaths() {

        FileSystemModule fsModule = getSingleParent().getFsModule();

        try {
            @SuppressWarnings ("unchecked")
            Config<EnvVariable> envConfig = ((ContentFile) fsModule.getFile(CommonFiles.ENVIRONMENT_CONFIG)).getContentAs(Config.class);
            EnvVariable binPathsEnvVar = envConfig.getEntryByColumn("name", "BIN_PATHS");
            if (binPathsEnvVar != null) {
                return binPathsEnvVar.getValueList();
            }
            // No BIN_PATHS variable -> code down below recovers
        } catch (FileException e) {
            // No environment config file -> code down below recovers
        }

        // If BIN_PATHS cannot be resolved, try to guess the directory which contains the session program
        return Arrays.asList(CommonFiles.SYS_BIN_DIR);
    }

    private User getSuperuser() {

        FileSystemModule fsModule = getSingleParent().getFsModule();

        try {
            @SuppressWarnings ("unchecked")
            Config<User> userConfig = ((ContentFile) fsModule.getFile(CommonFiles.USER_CONFIG)).getContentAs(Config.class);
            return userConfig.getEntryByColumn("name", User.SUPERUSER_NAME);
            // No superuser entry -> code down below recovers
        } catch (FileException e) {
            // No user config file -> code down below recovers
        }

        // If no superuser is set, use a temporary superuser with no password
        return new User(User.SUPERUSER_NAME);
    }

    /*
     * Kills the current root process and all child processes by firstly interrupting them and then stopping them by force after five seconds.
     */
    private void killRootProcess() {

        rootProcess.interrupt(true);
        scheduler.schedule("killRootProcess", "computer.programUpdate", TickService.DEFAULT_TICKS_PER_SECOND * 5, new KillRootProcessTask());
    }

    /**
     * This {@link SchedulerTask} finally kills the {@link ProcessModule#getRootProcess() root process} of a {@link ProcessModule} by setting it to {@code null}.
     *
     * @see ProcessModule
     */
    protected static class KillRootProcessTask extends SchedulerTaskAdapter<ProcessModule> {

        @Override
        public void execute(Scheduler<? extends ProcessModule> scheduler, ProcessModule processModule) {

            processModule.rootProcess = null;
        }

    }

}
