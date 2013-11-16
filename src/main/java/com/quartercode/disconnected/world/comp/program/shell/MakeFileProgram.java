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
import com.quartercode.disconnected.world.comp.file.NoFileRightException;
import com.quartercode.disconnected.world.comp.file.OutOfSpaceException;
import com.quartercode.disconnected.world.comp.file.StringContent;
import com.quartercode.disconnected.world.comp.program.Parameter;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.Program;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;
import com.quartercode.disconnected.world.comp.session.Shell;
import com.quartercode.disconnected.world.comp.session.ShellMessage;
import com.quartercode.disconnected.world.comp.session.ShellMessage.ShellMessageType;
import com.quartercode.disconnected.world.comp.session.ShellSessionProgram.ShellSession;

/**
 * The make file program allows to create new files and directories.
 * 
 * @see Shell
 */
@XmlSeeAlso ({ MakeFileProgram.MakeFileProgramExecutor.class })
public class MakeFileProgram extends Program {

    /**
     * Creates a new empty make file program.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected MakeFileProgram() {

    }

    /**
     * Creates a new make file program and sets the version and the vulnerabilities.
     * 
     * @param version The current version the program has.
     * @param vulnerabilities The vulnerabilities the program has.
     */
    public MakeFileProgram(Version version, List<Vulnerability> vulnerabilities) {

        super(ResourceBundles.PROGRAM("fmk").getString("name"), version, vulnerabilities);
    }

    @Override
    public long getSize() {

        return ByteUnit.BYTE.convert(20, ByteUnit.KILOBYTE);
    }

    @Override
    public ResourceBundle getResourceBundle() {

        return ResourceBundles.PROGRAM("fmk");
    }

    @Override
    protected void addParameters() {

        addParameter(Parameter.createRest("path", true));
        addParameter(Parameter.createSwitch("directory", "d"));
    }

    @Override
    protected ProgramExecutor createExecutorInstance(Process host, Map<String, Object> arguments) {

        return new MakeFileProgramExecutor(host, arguments);
    }

    protected static class MakeFileProgramExecutor extends ShellProgramExecutor {

        @XmlElement
        private String   path;
        @XmlElement
        private FileType type;

        protected MakeFileProgramExecutor() {

        }

        protected MakeFileProgramExecutor(Process host, Map<String, Object> arguments) {

            super(host);

            path = ((String[]) arguments.get("path"))[0];
            type = (Boolean) arguments.get("directory") ? FileType.DIRECTORY : FileType.FILE;
        }

        @Override
        public void update() {

            Shell shell = ((ShellSession) getHost().getSession()).getShell();

            String start = shell.getCurrentDirectory() == null ? File.SEPERATOR : shell.getCurrentDirectory().getGlobalHostPath();
            String path = File.resolvePath(start, this.path);
            if (getHost().getHost().getFileSystemManager().getFile(path) != null) {
                shell.printMessage(new ShellMessage(this, ShellMessageType.ERROR, "file.exists", path));
            } else {
                String parent = path.substring(0, path.lastIndexOf(File.SEPERATOR));
                parent = parent.length() > 0 ? parent : File.SEPERATOR;
                try {
                    File file = getHost().getHost().getFileSystemManager().addFile(getHost(), path, type, getHost().getUser());
                    if (file != null) {
                        file.write(getHost(), new StringContent(""));
                    } else {
                        shell.printMessage(new ShellMessage(this, ShellMessageType.ERROR, "parent.notFound", parent));
                    }
                }
                catch (NoFileRightException e) {
                    shell.printMessage(new ShellMessage(this, ShellMessageType.ERROR, "parent.noRights", parent));
                }
                catch (OutOfSpaceException e) {
                    shell.printMessage(new ShellMessage(this, ShellMessageType.ERROR, "fs.outOfSpace", path));
                }
                catch (IllegalStateException e) {
                    int pathStart = e.getMessage().indexOf("valid: File '") + 13;
                    int pathEnd = e.getMessage().indexOf("' isn't a directory");
                    File errorFile = getHost().getHost().getFileSystemManager().getMounted(path).getFile(e.getMessage().substring(pathStart, pathEnd));
                    shell.printMessage(new ShellMessage(this, ShellMessageType.ERROR, "parent.noDirectory", errorFile.getGlobalPath(getHost().getHost())));
                }
            }

            getHost().stop(false);
        }

    }

}
