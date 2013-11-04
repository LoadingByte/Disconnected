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

package com.quartercode.disconnected.sim.comp.program.desktop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.quartercode.disconnected.graphics.session.Frame;
import com.quartercode.disconnected.graphics.session.ShellWidget;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.Vulnerability;
import com.quartercode.disconnected.sim.comp.file.NoFileRightException;
import com.quartercode.disconnected.sim.comp.program.Process;
import com.quartercode.disconnected.sim.comp.program.Process.ProcessState;
import com.quartercode.disconnected.sim.comp.program.Program;
import com.quartercode.disconnected.sim.comp.program.ProgramExecutor;
import com.quartercode.disconnected.sim.comp.program.WrongSessionTypeException;
import com.quartercode.disconnected.sim.comp.session.Desktop.Window;
import com.quartercode.disconnected.sim.comp.session.Shell;
import com.quartercode.disconnected.sim.comp.session.ShellSessionProgram.ShellSession;
import com.quartercode.disconnected.util.ResourceBundles;
import com.quartercode.disconnected.util.size.ByteUnit;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ScrollPane.Fixed;

/**
 * The terminal emulates a {@link Shell} on a desktop using a shell widget.
 * 
 * @see Shell
 */
public class TerminalProgram extends Program {

    /**
     * Creates a new empty terminal program.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected TerminalProgram() {

    }

    /**
     * Creates a new terminal program and sets the version and the vulnerabilities.
     * 
     * @param version The current version the program has.
     * @param vulnerabilities The vulnerabilities the program has.
     */
    public TerminalProgram(Version version, List<Vulnerability> vulnerabilities) {

        super(ResourceBundles.PROGRAM("terminal").getString("name"), version, vulnerabilities);
    }

    @Override
    public long getSize() {

        return ByteUnit.BYTE.convert(250, ByteUnit.KILOBYTE);
    }

    @Override
    protected ProgramExecutor createExecutorInstance(Process host, Map<String, Object> arguments) {

        return new DesktopProgramExecutor(host) {

            private Shell                 shell;
            private Window<TerminalFrame> mainWindow;

            @Override
            public void update() {

                if (shell == null) {
                    try {
                        Map<String, Object> arguments = new HashMap<String, Object>();
                        arguments.put("user", getHost().getSession().getUser());
                        Process shellProcess = createProcess(getHost().getHost().getFileSystemManager().getFile("/system/bin/lash.exe"), arguments);
                        shell = ((ShellSession) shellProcess.getExecutor()).getShell();
                    }
                    catch (NoFileRightException e) {
                        getHost().interrupt(true);
                    }
                    catch (WrongSessionTypeException e) {
                        getHost().interrupt(true);
                    }
                }

                if (mainWindow == null && shell != null) {
                    mainWindow = new Window<TerminalFrame>(new TerminalFrame(shell), getName(), getName());
                    openWindow(mainWindow);
                } else if (mainWindow.isClosed()) {
                    getHost().interrupt(true);
                }

                if (getHost().getState() == ProcessState.INTERRUPTED) {
                    if (!mainWindow.isClosed()) {
                        mainWindow.close();
                    }
                    if (shell != null) {
                        shell.getHost().getHost().interrupt(true);
                    }
                    getHost().stop(false);
                }
            }
        };
    }

    private class TerminalFrame extends Frame {

        private final ShellWidget shellWidget;

        private TerminalFrame(Shell shell) {

            shellWidget = shell.getHost().createWidget();

            ScrollPane scrollPane = new ScrollPane(shellWidget);
            scrollPane.setTheme("/scrollpane");
            scrollPane.setFixed(Fixed.HORIZONTAL);

            add(scrollPane);
        }

        @Override
        protected void layout() {

            super.layout();

            setMinSize(500, 300);
            shellWidget.setSize(getInnerWidth(), getInnerHeight());
        }

    }

}
