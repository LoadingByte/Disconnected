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

package com.quartercode.disconnected.sim.comp.programs;

import java.util.List;
import java.util.Map;
import com.quartercode.disconnected.graphics.desktop.Frame;
import com.quartercode.disconnected.sim.comp.Desktop.Window;
import com.quartercode.disconnected.sim.comp.OperatingSystem;
import com.quartercode.disconnected.sim.comp.OperatingSystem.RightLevel;
import com.quartercode.disconnected.sim.comp.Version;
import com.quartercode.disconnected.sim.comp.Vulnerability;
import com.quartercode.disconnected.sim.comp.program.Process;
import com.quartercode.disconnected.sim.comp.program.Program;
import com.quartercode.disconnected.sim.comp.program.ProgramExecutor;
import de.matthiasmann.twl.Label;

/**
 * The system viewer displays information about different system components, like the process tree.
 * 
 * @see OperatingSystem
 * @see Process
 */
public class SystemViewerProgram extends Program {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new empty system viewer program.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    public SystemViewerProgram() {

    }

    /**
     * Creates a new system viewer program and sets the name, the version, the vulnerabilities and the required right level.
     * 
     * @param name The name the program has.
     * @param version The current version the program has.
     * @param vulnerabilities The vulnerabilities the program has.
     * @param rightLevel The required right level a user need for executing the program.
     */
    public SystemViewerProgram(String name, Version version, List<Vulnerability> vulnerabilities, RightLevel rightLevel) {

        super(name, version, vulnerabilities, rightLevel);
    }

    @Override
    public long getSize() {

        return 0;
    }

    @Override
    protected ProgramExecutor createExecutorInstance(Process host, Map<String, Object> arguments) {

        return new ProgramExecutor(host) {

            private Window mainWindow;

            @Override
            public void update() {

                if (mainWindow == null) {
                    mainWindow = openWindow(new Window(new SystemViewerFrame(), getName(), getName()));
                } else if (mainWindow.isClosed()) {
                    interrupt();
                }

                if (getOsState() == OSProgramState.INTERRUPTED) {
                    stop();
                }
            }
        };
    }

    class SystemViewerFrame extends Frame {

        private final Label label;

        public SystemViewerFrame() {

            label = new Label("Test-Label");
            label.setTheme("/label");
            add(label);
        }

        @Override
        protected void layout() {

            super.layout();

            label.adjustSize();
            label.setPosition(getInnerRight() - label.getWidth(), getInnerBottom() - label.getHeight());
        }

    }

}
