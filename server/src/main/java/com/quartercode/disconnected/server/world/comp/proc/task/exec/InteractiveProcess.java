/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

package com.quartercode.disconnected.server.world.comp.proc.task.exec;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.proc.Process;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskContainer;
import com.quartercode.disconnected.shared.event.comp.prog.control.SBPWorldProcessUserInterruptCommand;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;
import com.quartercode.disconnected.shared.world.comp.proc.SBPWorldProcessUserId;
import com.quartercode.disconnected.shared.world.comp.proc.WorldProcessState;
import com.quartercode.eventbridge.bridge.Bridge;
import com.quartercode.jtimber.api.node.Weak;

public class InteractiveProcess extends Process {

    @Weak
    @XmlAttribute
    @XmlIDREF
    private ContentFile           sourceFile;

    // Transient
    private SBPWorldProcessUserId worldProcessUser;

    // JAXB constructor
    protected InteractiveProcess() {

    }

    public InteractiveProcess(ContentFile sourceFile, SBPWorldProcessUserId worldProcessUser) {

        Validate.notNull(sourceFile, "Source file of interactive process cannot be null");
        Validate.notNull(worldProcessUser, "World process user of interactive process cannot be null");

        this.sourceFile = sourceFile;
        this.worldProcessUser = worldProcessUser;
    }

    /**
     * Returns the executed {@link ContentFile} which contains the {@link TaskContainer} that should be ran.
     * Such a container might be a program or a script. Therefore, this file might be a program or a script file.
     *
     * @return The executable file the interactive process executes.
     */
    public ContentFile getSourceFile() {

        return sourceFile;
    }

    /**
     * Returns the {@link TaskContainer} that should be ran.
     * Such a container might be a program or a script.
     * It is stored inside the interactive processe's {@link #getSource() executable source file}.
     *
     * @return The task container the interactive process executes.
     */
    public TaskContainer getTaskContainer() {

        return (TaskContainer) sourceFile.getContent();
    }

    /**
     * This method returns the {@link SBPWorldProcessUserId} that identifies the SBP and the world process user which launched the interactive process.
     * Such a remote launch is done using specific client-server communication events.
     *
     * @return An object that defines the SBP which is communicating with the interactive world process.
     *         Moreover, this object contains some information specifying the exact part of the SBP which launched the process (e.g. a client-side program GUI).
     */
    public SBPWorldProcessUserId getWorldProcessUser() {

        return worldProcessUser;
    }

    @Override
    protected void applyState(WorldProcessState state) {

        // If the process is being interrupted, ...
        if (state == WorldProcessState.INTERRUPTED) {
            // ... send SBPWorldProcessUserInterruptCommand in order to shut down any SBP-side user of the world process (e.g. a client-side program GUI)
            SBPWorldProcessUserId wpuId = worldProcessUser;
            Bridge bridge = getBridge();
            if (wpuId != null && bridge != null) {
                bridge.send(new SBPWorldProcessUserInterruptCommand(wpuId));
            }

            // ... also interrupt all task processes which have been launched by the interactive process
            for (Process childProcess : getChildProcesses()) {
                childProcess.interrupt();
            }
        }

        super.applyState(state);
    }

    @Override
    protected void initialize() throws Exception {

        super.initialize();

        // Check the read and execution rights on the source file
        sourceFile.checkRights("initialize interactive process using source file", getUser(), FileRights.READ, FileRights.EXECUTE);
    }

}
