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

package com.quartercode.disconnected.world.comp.session;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.cli.HelpFormatter;
import com.quartercode.disconnected.util.ResourceBundles;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileRights;
import com.quartercode.disconnected.world.comp.file.FileRights.FileRight;
import com.quartercode.disconnected.world.comp.file.NoFileRightException;
import com.quartercode.disconnected.world.comp.program.ArgumentException;
import com.quartercode.disconnected.world.comp.program.ArgumentException.MissingArgumentException;
import com.quartercode.disconnected.world.comp.program.ArgumentException.MissingParameterException;
import com.quartercode.disconnected.world.comp.program.ArgumentException.WrongArgumentTypeException;
import com.quartercode.disconnected.world.comp.program.Parameter;
import com.quartercode.disconnected.world.comp.program.Program;
import com.quartercode.disconnected.world.comp.program.WrongSessionTypeException;
import com.quartercode.disconnected.world.comp.session.ShellMessage.ShellMessageSender;
import com.quartercode.disconnected.world.comp.session.ShellMessage.ShellMessageType;
import com.quartercode.disconnected.world.comp.session.ShellParser.CommandNotFoundException;
import com.quartercode.disconnected.world.comp.session.ShellParser.ParsedCommand;
import com.quartercode.disconnected.world.comp.session.ShellSessionProgram.ShellSession;

/**
 * A shell can run commands and holds the latest output messages.
 * 
 * @see ShellSession
 */
public class Shell implements ShellMessageSender {

    private ShellSession host;

    @XmlIDREF
    private File         currentDirectory;

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
     * Returns the directory the shell is currently in.
     * This directory is used for all relative paths used in the shell.
     * The object is null if the shell is currently in the root directory.
     * 
     * @return The directory the shell is currently in.
     */
    @XmlTransient
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

        for (ShellUserInterface userInterface : host.getUserInterfaces()) {
            userInterface.updateCurrentDirectory(currentDirectory);
        }
    }

    /**
     * Prints a new message onto the shell output.
     * This automatically updates all user interfaces so they can process the new message.
     * 
     * @param message The new message to print onto the shell.
     */
    public void printMessage(ShellMessage message) {

        for (ShellUserInterface userInterface : host.getUserInterfaces()) {
            userInterface.printMessage(message);
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
                    printMessage(new ShellMessage(this, ShellMessageType.ERROR, "command.noRight", parsedCommand.getFile().getGlobalHostPath()));
                }
                catch (WrongSessionTypeException e) {
                    printMessage(new ShellMessage(this, ShellMessageType.ERROR, "command.wrongSessionType", parsedCommand.getFile().getGlobalHostPath()));
                }
                catch (MissingParameterException e) {
                    printMessage(new ShellMessage(this, ShellMessageType.ERROR, "command.parameter.missingParameter", e.getParameter().getName()));
                }
                catch (MissingArgumentException e) {
                    printMessage(new ShellMessage(this, ShellMessageType.ERROR, "command.parameter.missingArgument", e.getParameter().getName()));
                }
            }
        }
        catch (CommandNotFoundException e) {
            printMessage(new ShellMessage(this, ShellMessageType.ERROR, "command.notFound", e.getCommand()));
        }
        catch (WrongArgumentTypeException e) {
            printMessage(new ShellMessage(this, ShellMessageType.ERROR, "command.parameter.wrongArgumentType", e.getParameter().getName(), e.getArgument()));
        }
        catch (ArgumentException e) {
            throw new RuntimeException("Received unknown argument exception", e);
        }
    }

    private void printHelp(Program program, String call) {

        String usage = call;
        usage += " [" + ResourceBundles.SHELL.getString("help.message.options") + "]";
        for (Parameter parameter : program.getParameters()) {
            if (parameter.isRest()) {
                usage += " [" + parameter.getName() + "]";
                break;
            }
        }
        usage = escape(usage);

        StringWriter stringWriter = new StringWriter();
        new HelpFormatter().printOptions(new PrintWriter(stringWriter), 55, ShellParser.generateOptions(program), 4, HelpFormatter.DEFAULT_DESC_PAD);
        String options = escape(stringWriter.toString());
        options = options.substring(0, options.length() - 1);

        printMessage(new ShellMessage(this, ShellMessageType.HELP, "message", escape(program.getResourceBundle().getString("description")), usage, options));
    }

    private String escape(String string) {

        return string.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }

    @Override
    public String translateShellMessage(ShellMessage message) {

        return MessageFormat.format(ResourceBundles.SHELL.getString(message.toKey()), message.getVariables());
    }

    public void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {

        host = (ShellSession) parent;
    }

}
