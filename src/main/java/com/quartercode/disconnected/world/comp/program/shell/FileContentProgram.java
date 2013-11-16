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

package com.quartercode.disconnected.world.comp.program.shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import com.quartercode.disconnected.util.ResourceBundles;
import com.quartercode.disconnected.world.comp.ByteUnit;
import com.quartercode.disconnected.world.comp.Version;
import com.quartercode.disconnected.world.comp.Vulnerability;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.NoFileRightException;
import com.quartercode.disconnected.world.comp.file.OutOfSpaceException;
import com.quartercode.disconnected.world.comp.file.StringContent;
import com.quartercode.disconnected.world.comp.program.Parameter;
import com.quartercode.disconnected.world.comp.program.Parameter.ArgumentType;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.Program;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.world.comp.session.Shell;
import com.quartercode.disconnected.world.comp.session.ShellMessage;
import com.quartercode.disconnected.world.comp.session.ShellMessage.ShellMessageType;
import com.quartercode.disconnected.world.comp.session.ShellSessionProgram.ShellSession;

/**
 * The file content program displays the content of text files or changes it.
 * 
 * @see Shell
 */
@XmlSeeAlso ({ FileContentProgram.DisplayFileContentProgramExecutor.class })
public class FileContentProgram extends Program {

    /**
     * Creates a new empty display file content program.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected FileContentProgram() {

    }

    /**
     * Creates a new display file content program and sets the version and the vulnerabilities.
     * 
     * @param version The current version the program has.
     * @param vulnerabilities The vulnerabilities the program has.
     */
    public FileContentProgram(Version version, List<Vulnerability> vulnerabilities) {

        super(ResourceBundles.PROGRAM("fc").getString("name"), version, vulnerabilities);
    }

    @Override
    public long getSize() {

        return ByteUnit.BYTE.convert(20, ByteUnit.KILOBYTE);
    }

    @Override
    public ResourceBundle getResourceBundle() {

        return ResourceBundles.PROGRAM("fc");
    }

    @Override
    protected void addParameters() {

        addParameter(Parameter.createRest("path", true));
        addParameter(Parameter.createSwitch("line-numbers", "n"));
        addParameter(Parameter.createArgument("line", "l", ArgumentType.INTEGER, false, true));
        addParameter(Parameter.createArgument("change", "c", ArgumentType.STRING, false, true));
    }

    @Override
    protected ProgramExecutor createExecutorInstance(Process host, Map<String, Object> arguments) {

        return new DisplayFileContentProgramExecutor(host, arguments);
    }

    protected static class DisplayFileContentProgramExecutor extends ShellProgramExecutor {

        @XmlElement
        private String  path;
        @XmlElement
        private boolean lineNumbers;
        @XmlElement
        private int     line;
        @XmlElement
        private String  newContent;

        protected DisplayFileContentProgramExecutor() {

        }

        protected DisplayFileContentProgramExecutor(Process host, Map<String, Object> arguments) {

            super(host);

            path = ((String[]) arguments.get("path"))[0];
            lineNumbers = (Boolean) arguments.get("line-numbers");
            line = arguments.containsKey("line") ? (Integer) arguments.get("line") : -1;
            newContent = arguments.containsKey("change") ? (String) arguments.get("change") : null;
        }

        @Override
        public void update() {

            Shell shell = ((ShellSession) getHost().getSession()).getShell();

            String start = shell.getCurrentDirectory() == null ? File.SEPERATOR : shell.getCurrentDirectory().getGlobalHostPath();
            String path = File.resolvePath(start, this.path);
            File file = getHost().getHost().getFileSystemManager().getFile(path);
            if (file == null) {
                shell.printMessage(new ShellMessage(this, ShellMessageType.ERROR, "file.notFound", path));
            } else if (! (file.getContent() instanceof StringContent)) {
                shell.printMessage(new ShellMessage(this, ShellMessageType.ERROR, "file.noText", path));
            } else {
                try {
                    if (newContent == null) {
                        String content = ((StringContent) file.read(getHost())).toString();
                        if (lineNumbers) {
                            String[] lines = content.split("\n");
                            content = "";
                            for (int line = 0; line < lines.length; line++) {
                                content += String.format("%-4s%s\n", line + 1, lines[line]);
                            }
                            content = content.substring(0, content.length() - 1);
                        }

                        if (line >= 0) {
                            String[] lines = content.split("\n");
                            if (line < lines.length) {
                                content = lines[line - 1];
                            }
                        }
                        shell.printMessage(new ShellMessage(this, ShellMessageType.INFO, "display", content));
                    } else {
                        String content = ((StringContent) file.read(getHost())).toString();
                        if (line >= 0) {
                            List<String> lines = new ArrayList<String>(Arrays.asList(content.split("\n")));
                            if (line >= lines.size()) {
                                for (int counter = lines.size(); counter < line; counter++) {
                                    lines.add("");
                                }
                            }
                            lines.set(line - 1, newContent);

                            content = "";
                            for (String line : lines) {
                                content += line + "\n";
                            }
                            content = content.substring(0, content.length() - 1);
                        } else {
                            content = newContent;
                        }

                        file.write(getHost(), new StringContent(content));
                    }
                }
                catch (NoFileRightException e) {
                    String right = e.getRequiredRight().toString().substring(0, 1) + e.getRequiredRight().toString().substring(1).toLowerCase();
                    shell.printMessage(new ShellMessage(this, ShellMessageType.ERROR, "file.no" + right + "Rights", path));
                }
                catch (OutOfSpaceException e) {
                    shell.printMessage(new ShellMessage(this, ShellMessageType.ERROR, "fs.outOfSpace", path));
                }
            }

            getHost().stop(false);
        }
    }

}
