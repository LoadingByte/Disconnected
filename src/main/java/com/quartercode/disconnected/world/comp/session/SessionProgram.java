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

package com.quartercode.disconnected.world.comp.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.extra.Delay;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.extra.FunctionExecutor;
import com.quartercode.disconnected.mocl.extra.Limit;
import com.quartercode.disconnected.mocl.extra.Prioritized;
import com.quartercode.disconnected.mocl.extra.StopExecutionException;
import com.quartercode.disconnected.mocl.extra.def.ReferenceProperty;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;
import com.quartercode.disconnected.mocl.util.PropertyAccessorFactory;
import com.quartercode.disconnected.util.ResourceBundles;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.os.User;
import com.quartercode.disconnected.world.comp.program.ArgumentException.WrongArgumentTypeException;
import com.quartercode.disconnected.world.comp.program.ChildProcess;
import com.quartercode.disconnected.world.comp.program.Parameter;
import com.quartercode.disconnected.world.comp.program.Parameter.ArgumentType;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.Program;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;

/**
 * This class represents a program which opens a session.
 * For opening an actual session, you just have to create a process out of this with the argument "user".
 */
public abstract class SessionProgram extends ProgramExecutor {

    // ----- Properties -----

    /**
     * The {@link User} the session is running under.
     * Every {@link ChildProcess} of the session can use the rights provided by the session.
     */
    protected static final FeatureDefinition<ReferenceProperty<User>> USER;

    static {

        USER = new AbstractFeatureDefinition<ReferenceProperty<User>>("user") {

            @Override
            public ReferenceProperty<User> create(FeatureHolder holder) {

                return new ReferenceProperty<User>(getName(), holder);
            }

        };

    }

    // ----- Properties End -----

    // ----- Functions -----

    /**
     * Returns the {@link User} the session is running under.
     * Every {@link ChildProcess} of the session can use the rights provided by the session.
     */
    public static final FunctionDefinition<User>                      GET_USER;

    static {

        GET_USER = FunctionDefinitionFactory.create("getUser", SessionProgram.class, PropertyAccessorFactory.createGet(USER));

        GET_PARAMETERS.addExecutor(SessionProgram.class, "sessionProgramDefault", new FunctionExecutor<List<Parameter>>() {

            @Override
            public List<Parameter> invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                List<Parameter> parameters = new ArrayList<Parameter>();
                parameters.add(Parameter.createArgument("user", "u", ArgumentType.STRING, false, true));
                return parameters;
            }

        });

        GET_RESOURCE_BUNDLE.addExecutor(SessionProgram.class, "default", new FunctionExecutor<ResourceBundle>() {

            @Override
            public ResourceBundle invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                return ResourceBundles.KERNEL;
            }
        });

        UPDATE.addExecutor(SessionProgram.class, "sessionProgramParseArgsDefault", new FunctionExecutor<Void>() {

            @Override
            @SuppressWarnings ("unchecked")
            @Limit (1)
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                Map<String, Object> programArguments = (Map<String, Object>) arguments[0];

                String username = (String) programArguments.get("user");
                User user = null;
                for (User testUser : ((SessionProgram) holder).getParent().get(Process.GET_ROOT).invoke().getUserManager().getUsers()) {
                    if (testUser.getName().equals(username)) {
                        user = testUser;
                        break;
                    }
                }

                if (user == null) {
                    throw new StopExecutionException(new WrongArgumentTypeException(holder.get(GET_PARAMETER_BY_NAME).invoke("user"), programArguments.get("user").toString()));
                } else {
                    holder.get(USER).set(user);
                }

                return null;
            }

        });
        UPDATE.addExecutor(SessionProgram.class, "manageDeamons", new FunctionExecutor<Void>() {

            @Override
            @Delay (delay = 10)
            @Prioritized (Prioritized.BEFORE_CHECK)
            public Void invoke(FeatureHolder holder, Object... arguments) throws StopExecutionException {

                // Gather classes of child processes
                Set<ContentFile> childSources = new HashSet<ContentFile>();
                for (Process<?> child : ((ProgramExecutor) holder).getParent().get(Process.GET_CHILDREN).invoke()) {
                    childSources.add(child.get(Process.GET_SOURCE).invoke());
                }

                // Accesss deamons configuration on current FileSystem
                FileSystem fileSystem = ((SessionProgram) holder).getParent().get(Process.GET_SOURCE).invoke().get(File.GET_FILE_SYSTEM).invoke();
                ContentFile deamonsConfiguration = (ContentFile) fileSystem.get(FileSystem.GET_FILE).invoke("etc/deamons.cfg");
                Set<ContentFile> deamons = new HashSet<ContentFile>();
                for (String line : String.valueOf(deamonsConfiguration.get(ContentFile.GET_CONTENT).invoke()).split("\n")) {
                    if (!line.isEmpty()) {
                        File<?> lineFile = fileSystem.get(FileSystem.GET_FILE).invoke(line);
                        if (lineFile != null && ((ContentFile) lineFile).get(ContentFile.GET_CONTENT) instanceof Program) {
                            deamons.add((ContentFile) lineFile);
                        }
                    }
                }

                for (ContentFile deamon : deamons) {
                    if (!childSources.contains(deamon)) {
                        ChildProcess child = ((SessionProgram) holder).getParent().get(Process.CREATE_CHILD).invoke();
                        child.setLocked(false);
                        child.get(Process.SET_SOURCE).invoke(deamon);
                        child.get(Process.LAUNCH).invoke(new HashMap<String, Object>());
                        child.setLocked(true);
                        // TODO: Launch
                    }
                }

                return null;
            }

        });

    }

    // ----- Functions End -----

    /**
     * Creates a new session program.
     */
    public SessionProgram() {

    }

}
