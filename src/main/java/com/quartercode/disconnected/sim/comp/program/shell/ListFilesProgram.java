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

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.Vulnerability;
import com.quartercode.disconnected.sim.comp.file.File;
import com.quartercode.disconnected.sim.comp.file.File.FileType;
import com.quartercode.disconnected.sim.comp.file.FileRights;
import com.quartercode.disconnected.sim.comp.file.FileRights.FileRight;
import com.quartercode.disconnected.sim.comp.file.FileSystem;
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
 * The change directory program just changes the current directory of a shell session.
 * 
 * @see Shell
 */
@XmlSeeAlso ({ ListFilesProgram.ListFilesProgramExecutor.class })
public class ListFilesProgram extends Program {

    /**
     * Creates a new empty change directory program.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected ListFilesProgram() {

    }

    /**
     * Creates a new change directory program and sets the version and the vulnerabilities.
     * 
     * @param version The current version the program has.
     * @param vulnerabilities The vulnerabilities the program has.
     */
    public ListFilesProgram(Version version, List<Vulnerability> vulnerabilities) {

        super(ResourceBundles.PROGRAM("ls").getString("name"), version, vulnerabilities);
    }

    @Override
    public long getSize() {

        return ByteUnit.BYTE.convert(20, ByteUnit.KILOBYTE);
    }

    @Override
    public ResourceBundle getResourceBundle() {

        return ResourceBundles.PROGRAM("ls");
    }

    @Override
    protected void addParameters() {

        addParameter(Parameter.createRest("path", false));
    }

    @Override
    protected ProgramExecutor createExecutorInstance(Process host, Map<String, Object> arguments) {

        return new ListFilesProgramExecutor(host, arguments.containsKey("path") ? ((String[]) arguments.get("path"))[0] : null);
    }

    protected static class ListFilesProgramExecutor extends ShellProgramExecutor {

        @XmlElement
        private String path;

        protected ListFilesProgramExecutor() {

        }

        protected ListFilesProgramExecutor(Process host, String path) {

            super(host);

            this.path = path;
        }

        @Override
        public void update() {

            Shell shell = ((ShellSession) getHost().getSession()).getShell();

            File directory = null;
            boolean found = false;
            if (path == null) {
                directory = shell.getCurrentDirectory();
                found = true;
            } else {
                String start = shell.getCurrentDirectory() == null ? File.SEPERATOR : shell.getCurrentDirectory().getGlobalHostPath();
                String path = File.resolvePath(start, this.path);
                // TEMP TODO: Replace fs root workaround; new virtual file system
                if (!path.equals(File.SEPERATOR)) {
                    if (path.indexOf(File.SEPERATOR, 1) < 0) {
                        FileSystem fileSystem = getHost().getHost().getFileSystemManager().getMounted(path);
                        if (fileSystem != null) {
                            directory = fileSystem.getRootFile();
                            found = true;
                        }
                    } else {
                        directory = getHost().getHost().getFileSystemManager().getFile(path);
                        if (directory != null) {
                            found = true;
                        }
                    }
                }
            }

            if (!found) {
                shell.printMessage(new ShellMessage(this, ShellMessageType.ERROR, "directory.notFound", path));
            } else {
                ShellList<String> list = new ShellList<String>();
                if (directory == null) {
                    for (FileSystem fileSystem : getHost().getHost().getFileSystemManager().getMounted()) {
                        list.add(getHost().getHost().getFileSystemManager().getMountpoint(fileSystem), new Color(0x6CECFF));
                    }
                } else {
                    for (File file : directory.getChildFiles()) {
                        if (file.getType() == FileType.DIRECTORY) {
                            list.add(file.getName(), new Color(0x6CE2FF));
                        } else if (FileRights.hasRight(getHost().getUser(), file, FileRight.EXECUTE)) {
                            list.add(file.getName(), new Color(0xfff600));
                        } else {
                            list.add(file.getName(), new Color(0x72FF00));
                        }
                    }
                }

                shell.printMessage(new ShellMessage(this, ShellMessageType.INFO, "list", list));
            }

            getHost().stop(false);
        }

    }

}
