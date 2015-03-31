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

package com.quartercode.disconnected.clientdist.launcher;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class launches another main method inside the same jar using a second vm.
 * This allows to set vm arguments internally, e.g. for a natives path.
 */
public class Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    private String              mainClass;
    private String[]            vmArguments;
    private String[]            programArguments;
    private Path                workingDirectory;

    /**
     * Creates a new empty launcher.
     */
    public Launcher() {

    }

    /**
     * Creates a new launcher and initializes the main class.
     * 
     * @param mainClass The main class which get called on launch.
     */
    public Launcher(String mainClass) {

        this.mainClass = mainClass;
        workingDirectory = Paths.get(".");
    }

    /**
     * Creates a new launcher and initializes the main class and vm and program arguments.
     * 
     * @param mainClass The main class which get called on launch.
     * @param vmArguments The vm arguments which are set on launch and read by the virtual machine.
     * @param programArguments The program arguments which are set on launch and read by the executed program.
     * @param workingDirectory The directory the new process will be launched in.
     */
    public Launcher(String mainClass, String[] vmArguments, String[] programArguments, Path workingDirectory) {

        this.mainClass = mainClass;
        this.vmArguments = vmArguments.clone();
        this.programArguments = programArguments.clone();
        this.workingDirectory = workingDirectory;
    }

    /**
     * Returns the main class which get called on launch.
     * 
     * @return The main class which get called on launch.
     */
    public String getMainClass() {

        return mainClass;
    }

    /**
     * Sets the main class which get called on launch.
     * 
     * @param mainClass The main class which get called on launch.
     */
    public void setMainClass(String mainClass) {

        this.mainClass = mainClass;
    }

    /**
     * Returns the vm arguments which are set on launch.
     * VM arguments are read by the virtual machine and set things like the max heap size etc.
     * 
     * @return The vm arguments which are set on launch.
     */
    public String[] getVmArguments() {

        return vmArguments.clone();
    }

    /**
     * Sets the vm arguments which are set on launch.
     * VM arguments are read by the virtual machine and set things like the max heap size etc.
     * 
     * @param vmArguments The vm arguments which are set on launch.
     */
    public void setVmArguments(String[] vmArguments) {

        this.vmArguments = vmArguments.clone();
    }

    /**
     * Returns the program arguments which are set on launch.
     * Program arguments are read by the executed program. Every program handles them differently.
     * 
     * @return The program arguments which are set on launch.
     */
    public String[] getProgramArguments() {

        return programArguments.clone();
    }

    /**
     * Sets the program arguments which are set on launch.
     * Program arguments are read by the executed program. Every program handles them differently.
     * 
     * @param programArguments The program arguments which are set on launch.
     */
    public void setProgramArguments(String[] programArguments) {

        this.programArguments = programArguments.clone();
    }

    /**
     * Returns the directory the new process will be launched in.
     * 
     * @return The working directory for the new process.
     */
    public Path getWorkingDirectory() {

        return workingDirectory;
    }

    /**
     * Changes the directory the new process will be launched in.
     * 
     * @param workingDirectory The new working directory for the new process.
     */
    public void setWorkingDirectory(Path workingDirectory) {

        this.workingDirectory = workingDirectory;
    }

    /**
     * Launches the given main class using a new vm with the given vm arguments.
     */
    public void launch() {

        LOGGER.info("Creating working directory '{}'", workingDirectory);
        try {
            Files.createDirectories(workingDirectory);
        } catch (IOException e) {
            LOGGER.error("Cannot create working directory '{}'; terminating launcher", workingDirectory);
            return;
        }

        LOGGER.info("Launching main class '{}'", mainClass);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(buildCommand());
            processBuilder.directory(workingDirectory.toFile());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            StreamGobbler outputGobbler = new StreamGobbler("> ", process.getInputStream(), System.out);
            outputGobbler.start();
            StreamGobbler errorGobbler = new StreamGobbler("> ", process.getErrorStream(), System.err);
            errorGobbler.start();

            process.waitFor();

            outputGobbler.interrupt();
            errorGobbler.interrupt();
        } catch (URISyntaxException e) {
            LOGGER.error("Can't find jar file", e);
        } catch (IOException e) {
            LOGGER.error("Can't build process/read process output", e);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted while waiting for launched process to terminate", e);
        }

        LOGGER.info("Launcher terminated");
    }

    private List<String> buildCommand() throws URISyntaxException {

        List<String> command = new ArrayList<>();

        // Append java vm binary
        String javaBinary = System.getProperty("java.home") + "/bin/java";
        LOGGER.info("Java Binary: '{}'", javaBinary);
        command.add(javaBinary);

        // Append java vm arguments
        LOGGER.info("VM Arguments: {}", Arrays.toString(vmArguments));
        if (vmArguments != null && vmArguments.length > 0) {
            for (String vmArgument : vmArguments) {
                command.add(vmArgument);
            }
        }

        // Append current application classpath (it contains the launched class)
        Path programClasspath = Paths.get(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        LOGGER.info("Program Classpath: '{}'", programClasspath);
        command.add("-cp");
        command.add(programClasspath.toString());
        command.add(mainClass);

        // Append application arguments
        LOGGER.info("Program Arguments: {}", Arrays.toString(programArguments));
        if (programArguments != null && programArguments.length > 0) {
            for (String programArgument : programArguments) {
                command.add(programArgument);
            }
        }

        return command;
    }

}
