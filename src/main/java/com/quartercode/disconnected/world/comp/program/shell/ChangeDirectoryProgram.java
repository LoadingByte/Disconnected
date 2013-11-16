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
import com.quartercode.disconnected.world.comp.file.File.FileType;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.program.Parameter;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.Program;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.world.comp.session.Shell;
import com.quartercode.disconnected.world.comp.session.ShellMessage;
import com.quartercode.disconnected.world.comp.session.ShellMessage.ShellMessageType;
import com.quartercode.disconnected.world.comp.session.ShellSessionProgram.ShellSession;

/**
 * The change directory program just changes the current directory of a shell session.
 * 
 * @see Shell
 */
@XmlSeeAlso ({ ChangeDirectoryProgram.ChangeDirectoryProgramExecutor.class })
public class ChangeDirectoryProgram extends Program {

    /**
     * Creates a new empty change directory program.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected ChangeDirectoryProgram() {

    }

    /**
     * Creates a new change directory program and sets the version and the vulnerabilities.
     * 
     * @param version The current version the program has.
     * @param vulnerabilities The vulnerabilities the program has.
     */
    public ChangeDirectoryProgram(Version version, List<Vulnerability> vulnerabilities) {

        super(ResourceBundles.PROGRAM("cd").getString("name"), version, vulnerabilities);
    }

    @Override
    public long getSize() {

        return ByteUnit.BYTE.convert(20, ByteUnit.KILOBYTE);
    }

    @Override
    public ResourceBundle getResourceBundle() {

        return ResourceBundles.PROGRAM("cd");
    }

    @Override
    protected void addParameters() {

        addParameter(Parameter.createRest("path", true));
    }

    @Override
    protected ProgramExecutor createExecutorInstance(Process host, Map<String, Object> arguments) {

        return new ChangeDirectoryProgramExecutor(host, arguments);
    }

    protected static class ChangeDirectoryProgramExecutor extends ShellProgramExecutor {

        @XmlElement
        private String path;

        protected ChangeDirectoryProgramExecutor() {

        }

        protected ChangeDirectoryProgramExecutor(Process host, Map<String, Object> arguments) {

            super(host);

            path = ((String[]) arguments.get("path"))[0];
        }

        @Override
        public void update() {

            Shell shell = ((ShellSession) getHost().getSession()).getShell();

            String start = shell.getCurrentDirectory() == null ? File.SEPERATOR : shell.getCurrentDirectory().getGlobalHostPath();
            String path = File.resolvePath(start, this.path);
            // TEMP TODO: Replace fs root workaround; new virtual file system
            if (path.equals(File.SEPERATOR)) {
                shell.setCurrentDirectory(null);
            } else {
                File file = null;
                if (path.indexOf(File.SEPERATOR, 1) < 0) {
                    FileSystem fileSystem = getHost().getHost().getFileSystemManager().getMounted(path);
                    if (fileSystem != null) {
                        file = fileSystem.getRootFile();
                    }
                } else {
                    file = getHost().getHost().getFileSystemManager().getFile(path);
                }

                if (file == null) {
                    shell.printMessage(new ShellMessage(this, ShellMessageType.ERROR, "directory.notFound", path));
                } else if (! (file.getType() == FileType.DIRECTORY)) {
                    shell.printMessage(new ShellMessage(this, ShellMessageType.ERROR, "file.noDirectory", path));
                } else {
                    shell.setCurrentDirectory(file);
                }
            }

            getHost().stop(false);
        }

    }

}
