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

package com.quartercode.disconnected.sim.comp.program.shell;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.Vulnerability;
import com.quartercode.disconnected.sim.comp.file.File;
import com.quartercode.disconnected.sim.comp.file.NoFileRightException;
import com.quartercode.disconnected.sim.comp.file.TextContent;
import com.quartercode.disconnected.sim.comp.program.Parameter;
import com.quartercode.disconnected.sim.comp.program.Process;
import com.quartercode.disconnected.sim.comp.program.Program;
import com.quartercode.disconnected.sim.comp.program.ProgramExecutor;
import com.quartercode.disconnected.sim.comp.session.Shell;
import com.quartercode.disconnected.sim.comp.session.ShellMessage;
import com.quartercode.disconnected.sim.comp.session.ShellMessage.ShellMessageType;
import com.quartercode.disconnected.sim.comp.session.ShellSessionProgram.ShellSession;
import com.quartercode.disconnected.util.ResourceBundles;
import com.quartercode.disconnected.util.size.ByteUnit;

/**
 * The display file content program displays the content of text files on the shell.
 * 
 * @see Shell
 */
@XmlSeeAlso ({ DisplayFileContentProgram.DisplayFileContentProgramExecutor.class })
public class DisplayFileContentProgram extends Program {

    /**
     * Creates a new empty display file content program.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected DisplayFileContentProgram() {

    }

    /**
     * Creates a new display file content program and sets the version and the vulnerabilities.
     * 
     * @param version The current version the program has.
     * @param vulnerabilities The vulnerabilities the program has.
     */
    public DisplayFileContentProgram(Version version, List<Vulnerability> vulnerabilities) {

        super(ResourceBundles.PROGRAM("cat").getString("name"), version, vulnerabilities);
    }

    @Override
    public long getSize() {

        return ByteUnit.BYTE.convert(20, ByteUnit.KILOBYTE);
    }

    @Override
    public ResourceBundle getResourceBundle() {

        return ResourceBundles.PROGRAM("cat");
    }

    @Override
    protected void addParameters() {

        addParameter(Parameter.createRest("path", true));
    }

    @Override
    protected ProgramExecutor createExecutorInstance(Process host, Map<String, Object> arguments) {

        return new DisplayFileContentProgramExecutor(host, arguments);
    }

    protected static class DisplayFileContentProgramExecutor extends ShellProgramExecutor {

        @XmlElement
        private String path;

        protected DisplayFileContentProgramExecutor() {

        }

        protected DisplayFileContentProgramExecutor(Process host, Map<String, Object> arguments) {

            super(host);

            path = ((String[]) arguments.get("path"))[0];
        }

        @Override
        public void update() {

            Shell shell = ((ShellSession) getHost().getSession()).getShell();

            String start = shell.getCurrentDirectory() == null ? File.SEPERATOR : shell.getCurrentDirectory().getGlobalHostPath();
            String path = File.resolvePath(start, this.path);
            File file = getHost().getHost().getFileSystemManager().getFile(path);
            if (file == null) {
                shell.printMessage(new ShellMessage(this, ShellMessageType.ERROR, "file.notFound", path));
            } else if (! (file.getContent() instanceof TextContent)) {
                shell.printMessage(new ShellMessage(this, ShellMessageType.ERROR, "file.noText", path));
            } else {
                try {
                    shell.printMessage(new ShellMessage(this, ShellMessageType.INFO, "display", ((TextContent) file.read(getHost())).getTextContent()));
                }
                catch (NoFileRightException e) {
                    shell.printMessage(new ShellMessage(this, ShellMessageType.ERROR, "file.noRights", path));
                }
            }

            getHost().stop(false);
        }

    }

}
