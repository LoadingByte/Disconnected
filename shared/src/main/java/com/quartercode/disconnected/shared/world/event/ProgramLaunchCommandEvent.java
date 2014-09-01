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

package com.quartercode.disconnected.shared.world.event;

import java.util.Collection;
import java.util.Map;
import com.quartercode.eventbridge.basic.EventBase;

/**
 * A program launch command event starts a program on the computer of the client that sends it.
 * Such an event must be sent to a server bridge which handles it.<br>
 * <br>
 * Before a program launch command is sent, the user probably retrieves some data using a {@link ProgramLaunchInfoRequestEvent}.
 * It is used to get a new pid for the new program instance.
 * Here's an example for the combination of both events:
 * 
 * <pre>
 * // Get launch information
 * ProgramLaunchInfoRequestEvent infoRequest = new ProgramLaunchInfoRequestEvent();
 * ReturnEventUtil.send(bridge, infoRequest, new AbstractEventHandler&lt;ProgramLaunchInfoResponseEvent&gt;(ProgramLaunchInfoResponseEvent.class) {
 * 
 *     public void handle(ProgramLaunchInfoResponseEvent event) {
 * 
 *         // Launch program
 *         Map&lt;String, Object&gt; executorProperties = new HashMap&lt;&gt;();
 *         executorProperties.put(&quot;PROP_NAME&quot;, &quot;propValue&quot;);
 *         bridge.send(new ProgramLaunchCommandEvent(event.getPid(), &quot;pathToProgram&quot;, executorProperties));
 *     }
 * 
 * });
 * </pre>
 * 
 * @see ProgramLaunchCommandEventHandler
 */
public class ProgramLaunchCommandEvent extends EventBase {

    private static final long                                serialVersionUID = 6395719275942293140L;

    private final int                                        pid;
    private final String                                     filePath;
    private final Map<FeatureDefinitionReference<?>, Object> executorProperties;

    /**
     * Creates a new program launch command event
     * 
     * @param pid The pid the newly launched program will have.
     * @param filePath The path under which the program file, which will be used for the new program instance, can be found.
     * @param executorProperties The properties which should be set on the program executor which'll run the new program instance.
     *        For more details, see {@link #getExecutorProperties()}.
     */
    public ProgramLaunchCommandEvent(int pid, String filePath, Map<FeatureDefinitionReference<?>, Object> executorProperties) {

        this.pid = pid;
        this.filePath = filePath;
        this.executorProperties = executorProperties;
    }

    /**
     * Returns the pid the newly launched program will have.
     * It is checked for uniqueness before it's actually used.
     * 
     * @return The pid for the new program.
     */
    public int getPid() {

        return pid;
    }

    /**
     * Returns the path under which the program file, which will be used for the new program instance, can be found.
     * If the path doesn't point to a valid program file, the launch process is stopped.
     * 
     * @return The source file path for the new program.
     */
    public String getFilePath() {

        return filePath;
    }

    /**
     * Returns the properties which should be set on the program executor which'll run the new program instance.
     * The key of an entry is the name of the definition constant in the executor class, the value is the value for the property.
     * If a collection property should be set, the value must be a {@link Collection}.
     * Example:
     * 
     * <pre>
     * <i>Program executor with the following property definition constants:</i>
     * 
     * public static final PropertyDefinition&lt;Integer&gt; VALUE_1;
     * public static final PropertyDefinition&lt;String&gt; VALUE_2;
     * 
     * <i>Executor properties map that sets both values:</i>
     * 
     * [
     *   { FeatureDefinitionReference(..., "VALUE_1"), 10 },
     *   { FeatureDefinitionReference(..., "VALUE_2"), "test" }
     * ]
     * </pre>
     * 
     * @return The properties map for the new program executor.
     */
    public Map<FeatureDefinitionReference<?>, Object> getExecutorProperties() {

        return executorProperties;
    }

}
