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

package com.quartercode.disconnected.server.world.comp.proc.prog;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.registry.WorldProgram;
import com.quartercode.disconnected.server.registry.WorldProgram.WorldProgramTask;
import com.quartercode.disconnected.server.world.comp.file.ContentFile;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskContainer;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskDefinition;
import com.quartercode.disconnected.server.world.comp.proc.task.def.TaskExecutor;
import com.quartercode.disconnected.server.world.comp.proc.task.exec.TaskProcess;
import com.quartercode.disconnected.server.world.comp.vuln.Vulnerability;
import com.quartercode.disconnected.server.world.comp.vuln.VulnerabilityContainer;
import com.quartercode.disconnected.server.world.util.DerivableSize;
import com.quartercode.disconnected.server.world.util.WorldNode;
import com.quartercode.disconnected.shared.util.registry.Registries;
import com.quartercode.disconnected.shared.util.registry.extra.NamedValueUtils;
import com.quartercode.disconnected.shared.world.comp.Version;

/**
 * This class stores minimal information about a program and is typically used as the content of a file.
 * In order to retrieve more information, you need to query the {@link WorldProgram} object with the same name as the program.
 * The execution of programs is done by different {@link TaskExecutor}s defined in the {@link WorldProgram} object
 * To run a task, you need to create a new {@link TaskProcess}.<br>
 * <br>
 * Since {@link WorldProgram} objects only define <i>global</i> properties of programs, this class specifies properties of a single {@link Version} of the program.
 * Currently, the only such property is a {@link VulnerabilityContainer} that manages the {@link Vulnerability}s of the specific program version.
 *
 * @see TaskContainer
 * @see VulnerabilityContainer
 */
public class Program extends WorldNode<ContentFile> implements TaskContainer, DerivableSize {

    @XmlAttribute
    private String                 name;
    @XmlAttribute
    private Version                version;
    @XmlElement
    private VulnerabilityContainer vulnContainer;

    // JAXB constructor
    protected Program() {

    }

    /**
     * Creates a new program.
     *
     * @param name The internal name of the program.
     *        There must exist a {@link WorldProgram} with the same name.
     * @param version The {@link Version} of the program.
     */
    public Program(String name, Version version) {

        Validate.notBlank(name, "Internal program name cannot be blank");
        Validate.notNull(version, "Program version cannot be null");

        this.name = name;
        this.version = version;
        vulnContainer = new VulnerabilityContainer();
    }

    /**
     * Returns the internal name of the program.
     * It is used to retrieve the {@link WorldProgram} object which defines the actual {@link TaskExecutor}s.
     *
     * @return The internal program name.
     */
    public String getName() {

        return name;
    }

    /**
     * Returns the {@link Version} of the program.
     *
     * @return The program version.
     */
    public Version getVersion() {

        return version;
    }

    /**
     * Returns the {@link VulnerabilityContainer} that manages the {@link Vulnerability}s of the program.
     *
     * @return The program's vulnerability container.
     */
    public VulnerabilityContainer getVulnContainer() {

        return vulnContainer;
    }

    @Override
    public Map<String, TaskDefinition> getTasks() {

        // Retrieve the program data object
        WorldProgram programData = NamedValueUtils.getByName(Registries.get(ServerRegistries.WORLD_PROGRAMS), name);
        Validate.validState(programData != null, "Cannot find world program with name '%s' for retrieving its tasks", name);

        // Convert the tasks defined by the program data object to a returnable task definition map
        Map<String, TaskDefinition> tasks = new HashMap<>();
        for (WorldProgramTask definedTask : programData.getTasks()) {
            @SuppressWarnings ("unchecked")
            Class<? extends TaskExecutor> executorClass = (Class<? extends TaskExecutor>) definedTask.getExecutorClass();
            tasks.put(definedTask.getName(), new ProgramTaskDefinition(executorClass));
        }
        return tasks;
    }

    @Override
    public long getSize() {

        return NamedValueUtils.getByName(Registries.get(ServerRegistries.WORLD_PROGRAMS), name).getSize();
    }

}
