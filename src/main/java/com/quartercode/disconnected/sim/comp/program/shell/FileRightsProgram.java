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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.Vulnerability;
import com.quartercode.disconnected.sim.comp.file.File;
import com.quartercode.disconnected.sim.comp.file.FileRights;
import com.quartercode.disconnected.sim.comp.file.FileRights.FileAccessor;
import com.quartercode.disconnected.sim.comp.file.FileRights.FileRight;
import com.quartercode.disconnected.sim.comp.program.Parameter;
import com.quartercode.disconnected.sim.comp.program.Parameter.ArgumentType;
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
 * The file rights program can display and change the right attributes of files.
 * 
 * @see Shell
 */
@XmlSeeAlso ({ FileRightsProgram.FileRightsProgramExecutor.class })
public class FileRightsProgram extends Program {

    /**
     * Creates a new empty file rights program.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected FileRightsProgram() {

    }

    /**
     * Creates a new file rights program and sets the version and the vulnerabilities.
     * 
     * @param version The current version the program has.
     * @param vulnerabilities The vulnerabilities the program has.
     */
    public FileRightsProgram(Version version, List<Vulnerability> vulnerabilities) {

        super(ResourceBundles.PROGRAM("rights").getString("name"), version, vulnerabilities);
    }

    @Override
    public long getSize() {

        return ByteUnit.BYTE.convert(20, ByteUnit.KILOBYTE);
    }

    @Override
    public ResourceBundle getResourceBundle() {

        return ResourceBundles.PROGRAM("rights");
    }

    @Override
    protected void addParameters() {

        addParameter(Parameter.createArgument("change", "c", ArgumentType.STRING, false, true));
        addParameter(Parameter.createRest("path", true));
    }

    @Override
    protected ProgramExecutor createExecutorInstance(Process host, Map<String, Object> arguments) {

        return new FileRightsProgramExecutor(host, arguments);
    }

    protected static class FileRightsProgramExecutor extends ShellProgramExecutor {

        @XmlElement
        private String path;
        @XmlElement
        private String changes;

        protected FileRightsProgramExecutor() {

        }

        protected FileRightsProgramExecutor(Process host, Map<String, Object> arguments) {

            super(host);

            path = ((String[]) arguments.get("path"))[0];
            changes = (String) arguments.get("change");
        }

        @Override
        public void update() {

            Shell shell = ((ShellSession) getHost().getSession()).getShell();

            String start = shell.getCurrentDirectory() == null ? File.SEPERATOR : shell.getCurrentDirectory().getGlobalHostPath();
            String path = File.resolvePath(start, this.path);
            // TEMP TODO: Replace fs root workaround; new virtual file system
            File file = getHost().getHost().getFileSystemManager().getFile(path);
            if (file == null) {
                shell.printMessage(new ShellMessage(this, ShellMessageType.ERROR, "file.notFound", path));
            } else {
                if (changes != null) {
                    if (FileRights.canChangeRights(getHost().getUser(), file)) {
                        if (changes.matches("[ugoa]+[+-=][rwdx]+")) {
                            String[] parts = changes.split("[+-=]");
                            Set<FileAccessor> accessors = new HashSet<FileAccessor>();
                            for (char accessor : parts[0].toCharArray()) {
                                accessors.add(FileAccessor.valueOf(accessor));
                            }
                            Set<FileRight> rights = new HashSet<FileRight>();
                            for (char right : parts[1].toCharArray()) {
                                rights.add(FileRight.valueOf(right));
                            }
                            char action = changes.charAt(parts[0].length());

                            for (FileAccessor accessor : accessors) {
                                for (FileRight right : FileRight.values()) {
                                    if ( (action == '+' || action == '-') && rights.contains(right)) {
                                        file.getRights().setRight(accessor, right, action == '+');
                                    } else if (action == '=') {
                                        file.getRights().setRight(accessor, right, rights.contains(right));
                                    }
                                }
                            }
                        } else {
                            shell.printMessage(new ShellMessage(this, ShellMessageType.ERROR, "syntax", path));
                        }
                    } else {
                        shell.printMessage(new ShellMessage(this, ShellMessageType.ERROR, "file.noRights", path));
                    }
                }

                shell.printMessage(new ShellMessage(this, ShellMessageType.INFO, "display", file.getRights()));
            }

            getHost().stop(false);
        }

    }

}
