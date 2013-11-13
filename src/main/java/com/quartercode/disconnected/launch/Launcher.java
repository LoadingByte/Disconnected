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

package com.quartercode.disconnected.launch;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.quartercode.disconnected.util.StreamGobbler;

/**
 * This class launches another main method inside the same jar using a second vm.
 * This allows to set vm arguments internally, e.g. for a natives path.
 */
public class Launcher {

    private static final Logger LOGGER = Logger.getLogger(Launcher.class.getName());

    private String              mainClass;
    private String[]            vmArguments;
    private String[]            programArguments;
    private File                directory;

    /**
     * Creates a new empty launcher.
     */
    public Launcher() {

    }

    /**
     * Creates a new launcher and initalizes the main class.
     * 
     * @param mainClass The main class which get called on launch.
     */
    public Launcher(String mainClass) {

        this.mainClass = mainClass;
        directory = new File(".");
    }

    /**
     * Creates a new launcher and initalizes the main class and vm and program arguments.
     * 
     * @param mainClass The main class which get called on launch.
     * @param vmArguments The vm arguments which are set on launch and read by the virtual machine.
     * @param programArguments The program arguments which are set on launch and read by the executed program.
     * @param directory The directory the new process will be launched in.
     */
    public Launcher(String mainClass, String[] vmArguments, String[] programArguments, File directory) {

        this.mainClass = mainClass;
        this.vmArguments = vmArguments;
        this.programArguments = programArguments;
        this.directory = directory;
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

        return vmArguments;
    }

    /**
     * Sets the vm arguments which are set on launch.
     * VM arguments are read by the virtual machine and set things like the max heap size etc.
     * 
     * @param vmArguments The vm arguments which are set on launch.
     */
    public void setVmArguments(String[] vmArguments) {

        this.vmArguments = vmArguments;
    }

    /**
     * Returns the program arguments which are set on launch.
     * Program arguments are read by the executed program. Every program handles them differently.
     * 
     * @return The program arguments which are set on launch.
     */
    public String[] getProgramArguments() {

        return programArguments;
    }

    /**
     * Sets the program arguments which are set on launch.
     * Program arguments are read by the executed program. Every program handles them differently.
     * 
     * @param vmArguments The program arguments which are set on launch.
     */
    public void setProgramArguments(String[] programArguments) {

        this.programArguments = programArguments;
    }

    /**
     * Returns the directory the new process will be launched in.
     * 
     * @return The working directory for the new process.
     */
    public File getDirectory() {

        return directory;
    }

    /**
     * Changes the directory the new process will be launched in.
     * 
     * @param directory The new working directory for the new process.
     */
    public void setDirectory(File directory) {

        this.directory = directory;
    }

    /**
     * Launches the given main class using a new vm with the given vm arguments.
     */
    public void launch() {

        LOGGER.info("Launching main class " + mainClass);

        try {
            File file = new File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            List<String> command = new ArrayList<String>();
            command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
            LOGGER.info("Java Binary: " + command.get(0));
            if (vmArguments != null && vmArguments.length > 0) {
                LOGGER.info("VM Arguments: " + Arrays.toString(vmArguments));
                for (String vmArgument : vmArguments) {
                    command.add(vmArgument);
                }
            }
            command.add("-cp");
            command.add(file.getAbsolutePath());
            command.add(mainClass);
            if (programArguments != null && programArguments.length > 0) {
                LOGGER.info("Program Arguments: " + Arrays.toString(programArguments));
                for (String programArgument : programArguments) {
                    command.add(programArgument);
                }
            }

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(directory);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            StreamGobbler outputGobbler = new StreamGobbler("> ", process.getInputStream(), System.out);
            outputGobbler.start();
            StreamGobbler errorGobbler = new StreamGobbler("> ", process.getErrorStream(), System.err);
            errorGobbler.start();

            process.waitFor();

            outputGobbler.interrupt();
            errorGobbler.interrupt();
        }
        catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Can't find jar file", e);
        }
        catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Can't build process/read process output", e);
        }
        catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Interrupted while waiting for launched process", e);
        }

        LOGGER.info("Launcher terminated");
    }

}
