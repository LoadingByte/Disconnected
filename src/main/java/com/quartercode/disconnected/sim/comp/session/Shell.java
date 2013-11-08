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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import org.apache.commons.cli.HelpFormatter;
import com.quartercode.disconnected.graphics.session.ShellWidget;
import com.quartercode.disconnected.sim.comp.file.File;
import com.quartercode.disconnected.sim.comp.file.FileRights;
import com.quartercode.disconnected.sim.comp.file.FileRights.FileRight;
import com.quartercode.disconnected.sim.comp.file.NoFileRightException;
import com.quartercode.disconnected.sim.comp.os.Environment;
import com.quartercode.disconnected.sim.comp.program.ArgumentException;
import com.quartercode.disconnected.sim.comp.program.ArgumentException.MissingArgumentException;
import com.quartercode.disconnected.sim.comp.program.ArgumentException.MissingParameterException;
import com.quartercode.disconnected.sim.comp.program.ArgumentException.WrongArgumentTypeException;
import com.quartercode.disconnected.sim.comp.program.Parameter;
import com.quartercode.disconnected.sim.comp.program.Program;
import com.quartercode.disconnected.sim.comp.program.WrongSessionTypeException;
import com.quartercode.disconnected.sim.comp.session.ShellParser.CommandNotFoundException;
import com.quartercode.disconnected.sim.comp.session.ShellParser.ParsedCommand;
import com.quartercode.disconnected.sim.comp.session.ShellSessionProgram.ShellSession;
import com.quartercode.disconnected.util.ResourceBundles;

/**
 * A shell can run commands and holds the latest output messages.
 * 
 * @see ShellSession
 */
public class Shell {

    private ShellSession       host;

    @XmlElement
    private Environment        environment;
    private File               currentDirectory;
    @XmlElementWrapper (name = "output")
    @XmlElement (name = "line")
    private final List<String> output = new ArrayList<String>();

    /**
     * Creates a new empty shell.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected Shell() {

    }

    /**
     * Creates a new shell in the root directory.
     * 
     * @param host The hosting shell session which uses this shell.
     */
    public Shell(ShellSession host) {

        this.host = host;

        environment = ((Environment) host.getHost().getHost().getFileSystemManager().getFile("/system/config/environment.cfg").getContent()).clone();
    }

    /**
     * Creates a new shell in the given directory.
     * 
     * @param host The hosting shell session which uses this shell.
     * @param currentDirectory The directory the shell will start in.
     */
    public Shell(ShellSession host, File currentDirectory) {

        this.host = host;
        this.currentDirectory = currentDirectory;
    }

    /**
     * Returns the hosting shell session which uses this shell.
     * 
     * @return The hosting shell session which uses this shell.
     */
    public ShellSession getHost() {

        return host;
    }

    /**
     * Returns the environment the shell uses internally for parsing commands.
     * 
     * @return The environment the shell uses.
     */
    public Environment getEnvironment() {

        return environment;
    }

    /**
     * Returns the directory the shell is currently in.
     * This directory is used for all relative paths used in the shell.
     * The object is null if the shell is currently in the root directory.
     * 
     * @return The directory the shell is currently in.
     */
    @XmlIDREF
    public File getCurrentDirectory() {

        return currentDirectory;
    }

    /**
     * Changes the directory the shell is currently in.
     * This directory is used for all relative paths used in the shell.
     * The object is null if the shell is currently in the root directory.
     * 
     * @param currentDirectory The new directory the shell will be in after the change.
     */
    public void setCurrentDirectory(File currentDirectory) {

        this.currentDirectory = currentDirectory;

        for (ShellWidget widget : host.getWidgets()) {
            widget.update();
        }
    }

    /**
     * Returns the last messages the shell printed out.
     * Those messages wont get serialized.
     * 
     * @return The last messages the shell printed out.
     */
    public List<String> getOutput() {

        return Collections.unmodifiableList(output);
    }

    /**
     * Prints a new line onto the shell output.
     * This automatically updates all shell widgets so they display the current output.
     * 
     * @param line The new line to print onto the shell.
     */
    public void printLine(String line) {

        if (output.size() == 50) {
            output.remove(0);
        }
        output.add(line);

        for (ShellWidget widget : host.getWidgets()) {
            widget.update();
        }
    }

    /**
     * Runs the given command on the shell.
     * This will print the output of the command to the shell.
     * 
     * @param command The command to run on the shell.
     */
    public void run(String command) {

        try {
            ParsedCommand parsedCommand = ShellParser.parse(this, command);

            if (parsedCommand.getArguments().containsKey("help")) {
                printHelp((Program) parsedCommand.getFile().getContent(), command.split(" ")[0]);
            } else {
                try {
                    FileRights.checkRight(host.getHost(), parsedCommand.getFile(), FileRight.EXECUTE);
                    host.getHost().createChild(parsedCommand.getFile(), parsedCommand.getArguments());
                }
                catch (NoFileRightException e) {
                    printLine(MessageFormat.format(ResourceBundles.SHELL.getString("command.noRight"), parsedCommand.getFile().getGlobalHostPath()));
                }
                catch (WrongSessionTypeException e) {
                    printLine(MessageFormat.format(ResourceBundles.SHELL.getString("command.wrongSessionType"), parsedCommand.getFile().getGlobalHostPath()));
                }
                catch (MissingParameterException e) {
                    printLine(MessageFormat.format(ResourceBundles.SHELL.getString("command.parameter.missingParameter"), e.getParameter().getName()));
                }
                catch (MissingArgumentException e) {
                    printLine(MessageFormat.format(ResourceBundles.SHELL.getString("command.parameter.missingArgument"), e.getParameter().getName()));
                }
            }
        }
        catch (CommandNotFoundException e) {
            printLine(MessageFormat.format(ResourceBundles.SHELL.getString("command.notFound"), e.getCommand()));
        }
        catch (WrongArgumentTypeException e) {
            printLine(MessageFormat.format(ResourceBundles.SHELL.getString("command.parameter.wrongArgumentType"), e.getParameter().getName(), e.getArgument()));
        }
        catch (ArgumentException e) {
            throw new RuntimeException("Received unknown argument exception", e);
        }
    }

    private void printHelp(Program program, String call) {

        printLine(program.getResourceBundle().getString("description"));

        String usage = call;
        usage += " [" + ResourceBundles.SHELL.getString("options.help.options") + "]";
        for (Parameter parameter : program.getParameters()) {
            if (parameter.isRest()) {
                usage += " [" + parameter.getName() + "]";
                break;
            }
        }
        printLine(ResourceBundles.SHELL.getString("options.help.usage") + " " + usage);

        StringWriter stringWriter = new StringWriter();
        new HelpFormatter().printOptions(new PrintWriter(stringWriter), HelpFormatter.DEFAULT_WIDTH, ShellParser.generateOptions(program), 4, HelpFormatter.DEFAULT_DESC_PAD);
        for (String line : stringWriter.toString().split("\n")) {
            printLine(line);
        }
    }

    public void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {

        host = (ShellSession) parent;
    }

}
