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

package com.quartercode.disconnected.sim.comp.session;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import com.quartercode.disconnected.sim.comp.file.File;
import com.quartercode.disconnected.sim.comp.os.FileSystemManager;
import com.quartercode.disconnected.sim.comp.program.ArgumentException.WrongArgumentTypeException;
import com.quartercode.disconnected.sim.comp.program.Parameter;
import com.quartercode.disconnected.sim.comp.program.Program;
import com.quartercode.disconnected.util.ResourceBundles;

/**
 * The shell parser class contains utilities for parsing commands.
 * Example:
 * 
 * <pre>
 * Command: test -a 12345 -s therest
 * > File: "/system/bin/test.exe"
 * > Parameters:
 * >> Parameter "a" has argument "12345"
 * >> Switch "s" is set
 * >> Rest is set to "[therest]"
 * </pre>
 */
public class ShellParser {

    /**
     * Generates an apache cli options object out of the parameters which can be found in the program object.
     * The returned options object doesn't contain any requirements, they are checked before execution.
     * 
     * @param program The program object which contains the parameters for the new options object.
     * @return The generated apache cli options object.
     */
    public static Options generateOptions(Program program) {

        Options options = new Options();
        options.addOption(new Option("h", "help", false, ResourceBundles.SHELL.getString("options.help.description")));

        for (Parameter parameter : program.getParameters()) {
            if (!parameter.isRest()) {
                String description = program.getResourceBundle().getString("options." + parameter.getName() + ".description");
                Option option = new Option(parameter.getShortName(), parameter.getName(), parameter.isArgument(), description);
                if (option.hasArg()) {
                    option.setArgName(parameter.getName());
                    option.setOptionalArg(true);
                }
                options.addOption(option);
            }
        }

        return options;
    }

    /**
     * Parses a command and returns useful information the shell needs for executing that command.
     * Example:
     * 
     * <pre>
     * Command: test -a 12345 -s therest
     * > File: "/system/bin/test.exe"
     * > Parameters:
     * >> Parameter "a" has argument "12345"
     * >> Switch "s" is set
     * >> Rest is set to "[therest]"
     * </pre>
     * 
     * @param shell The shell which wants to parse the given command.
     * @param command The command to parse. It should got executed on the given shell.
     * @return A parsed command object with required information for executing the command. See {@link ParsedCommand} for more detail.
     * @throws CommandNotFoundException The program set in the given command can't be found.
     * @throws WrongArgumentTypeException Something goes wrong while parsing an argument.
     */
    public static ParsedCommand parse(Shell shell, String command) throws CommandNotFoundException, WrongArgumentTypeException {

        // Split whole command into program string (launch command) and argument string
        String launchCommand;
        String arguments = null;
        if (command.contains(" ")) {
            launchCommand = command.substring(0, command.indexOf(" "));
            arguments = command.substring(command.indexOf(" ") + 1, command.length());
        } else {
            launchCommand = command;
        }

        // Search the file the launch command specifies
        List<String> searchPaths = shell.getEnvironment().getVariable("PATH").getValueList();
        searchPaths.add(shell.getCurrentDirectory() == null ? File.SEPERATOR : shell.getCurrentDirectory().getGlobalHostPath());
        File file = searchFile(searchPaths, launchCommand, shell.getHost().getHost().getHost().getFileSystemManager());
        if (file == null) {
            file = searchFile(searchPaths, launchCommand + ".exe", shell.getHost().getHost().getHost().getFileSystemManager());
        }
        if (file == null) {
            throw new CommandNotFoundException(launchCommand);
        }

        // Resolve the program the file contains
        Program program = (Program) file.getContent();
        if (program instanceof SessionProgram) {
            throw new RuntimeException("Sessions are not supported yet");
        } else {
            // Parse arguments
            Map<String, Object> parsedArguments = new HashMap<String, Object>();
            if (arguments != null) {
                CommandLine commandLine = parseCommandLine(program, arguments.split(" "));
                for (Parameter parameter : program.getParameters()) {
                    if (parameter.isSwitch()) {
                        parsedArguments.put(parameter.getName(), commandLine.hasOption(parameter.getName()));
                    } else if (parameter.isArgument() && commandLine.hasOption(parameter.getName())) {
                        Object argument = null;
                        if (commandLine.getOptionValue(parameter.getName()) != null) {
                            argument = parameter.getType().parse(parameter, commandLine.getOptionValue(parameter.getName()));
                        }
                        parsedArguments.put(parameter.getName(), argument);
                    } else if (parameter.isRest()) {
                        parsedArguments.put(parameter.getName(), commandLine.getArgs());
                    }
                }
                if (commandLine.hasOption("help")) {
                    parsedArguments.put("help", true);
                }
            }

            return new ParsedCommand(file, parsedArguments);
        }
    }

    private static File searchFile(List<String> searchPaths, String name, FileSystemManager fileSystemManager) {

        for (String path : searchPaths) {
            File file = fileSystemManager.getFile(File.resolvePath(path, name));
            if (file != null && file.getContent() != null && file.getContent() instanceof Program) {
                return file;
            }
        }

        return null;
    }

    private static CommandLine parseCommandLine(Program program, String[] arguments) {

        try {
            return new PosixParser().parse(generateOptions(program), arguments, true);
        }
        catch (ParseException e) {
            throw new RuntimeException("Command line parser threw unknow exception", e);
        }
    }

    private ShellParser() {

    }

    /**
     * Parsed commands contains information a {@link Shell} needs for executing a command.
     * A parsed command tyically stores the program file and the parsed argument objects.
     */
    public static class ParsedCommand {

        private final File                file;
        private final Map<String, Object> arguments;

        /**
         * Creates a new parsed command.
         * 
         * @param file The program file a shell needs to execute.
         * @param arguments The parsed arguments of the command which got parsed.
         */
        public ParsedCommand(File file, Map<String, Object> arguments) {

            this.file = file;
            this.arguments = arguments;
        }

        /**
         * Returns the program file a shell needs to execute.
         * 
         * @return The program file a shell needs to execute.
         */
        public File getFile() {

            return file;
        }

        /**
         * Returns the parsed arguments of the command which got parsed.
         * 
         * @return The parsed arguments of the command which got parsed.
         */
        public Map<String, Object> getArguments() {

            return Collections.unmodifiableMap(arguments);
        }

    }

    /**
     * The command not found exception is thrown if a command file can't be found.
     */
    public static class CommandNotFoundException extends Exception {

        private static final long serialVersionUID = -1550131087803689771L;

        private final String      command;

        /**
         * Creates a new command not found exception.
         * 
         * @param command The command which can't be found.
         */
        public CommandNotFoundException(String command) {

            super("Can't find the command " + command);

            this.command = command;
        }

        /**
         * Returns the command which can't be found.
         * 
         * @return The command which can't be found.
         */
        public String getCommand() {

            return command;
        }

    }

}
