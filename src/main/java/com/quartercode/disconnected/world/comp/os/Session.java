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

package com.quartercode.disconnected.world.comp.os;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import org.apache.commons.codec.digest.DigestUtils;
import com.quartercode.disconnected.mocl.base.FeatureDefinition;
import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.base.def.AbstractFeatureDefinition;
import com.quartercode.disconnected.mocl.extra.ExecutorInvokationException;
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
import com.quartercode.disconnected.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.world.comp.os.Configuration.ConfigurationEntry;
import com.quartercode.disconnected.world.comp.program.ChildProcess;
import com.quartercode.disconnected.world.comp.program.Parameter;
import com.quartercode.disconnected.world.comp.program.Parameter.ArgumentType;
import com.quartercode.disconnected.world.comp.program.Process;
import com.quartercode.disconnected.world.comp.program.ProgramExecutor;

/**
 * This class represents a program which opens a session.
 * For opening an actual session, you just have to create a process using this with these arguments:
 * 
 * <ul>
 * <li>user: The name of the {@link User} the session should be running under.</li>
 * <li>password: The password of the given {@link User}. Can be null if the parent session is null or a root session.</li>
 * </ul>
 */
public class Session extends ProgramExecutor {

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

        GET_USER = FunctionDefinitionFactory.create("getUser", Session.class, PropertyAccessorFactory.createGet(USER));

        GET_PARAMETERS.addExecutor(Session.class, "default", new FunctionExecutor<List<Parameter>>() {

            @Override
            public List<Parameter> invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                List<Parameter> parameters = new ArrayList<Parameter>();
                parameters.add(Parameter.createArgument("user", "u", ArgumentType.STRING, false, true));
                return parameters;
            }

        });

        GET_RESOURCE_BUNDLE.addExecutor(Session.class, "default", new FunctionExecutor<ResourceBundle>() {

            @Override
            public ResourceBundle invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                return ResourceBundles.KERNEL;
            }
        });

        UPDATE.addExecutor(Session.class, "setUserFromArgument", new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_7 + Prioritized.SUBLEVEL_7)
            @Limit (1)
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                @SuppressWarnings ("unchecked")
                Map<String, Object> programArguments = (Map<String, Object>) arguments[0];
                String username = (String) programArguments.get("user");

                // This program is always allowed to read the user configuration file
                FileSystemModule fsModule = ((Session) holder).getParent().get(Process.GET_OPERATING_SYSTEM).invoke().get(OperatingSystem.GET_FS_MODULE).invoke();
                File<?> userConfigFile = fsModule.get(FileSystemModule.GET_FILE).invoke(CommonFiles.USER_CONFIG);
                if (! (userConfigFile instanceof ContentFile) || userConfigFile.get(ContentFile.GET_CONTENT).invoke() == null) {
                    throw new StopExecutionException(new IllegalStateException("User configuration file doesn't exist under '" + CommonFiles.USER_CONFIG + "'"));
                }
                Configuration userConfig = (Configuration) userConfigFile.get(ContentFile.GET_CONTENT).invoke();

                for (ConfigurationEntry entry : userConfig.get(Configuration.GET_ENTRIES).invoke()) {
                    if (entry instanceof User && ((User) entry).get(User.GET_NAME).equals(username)) {
                        holder.get(USER).set((User) entry);
                        return null;
                    }
                }

                throw new StopExecutionException(new IllegalArgumentException("Unknown user"));
            }

        });

        UPDATE.addExecutor(Session.class, "checkPassword", new FunctionExecutor<Void>() {

            @Override
            @Prioritized (Prioritized.LEVEL_7 + Prioritized.SUBLEVEL_5)
            @Limit (1)
            public Void invoke(FeatureHolder holder, Object... arguments) throws ExecutorInvokationException {

                // Check for no parent session or a parent root session
                Session parentSession = ((Session) holder).getParent().get(Process.GET_SESSION).invoke();
                if (parentSession != null && !parentSession.get(GET_USER).invoke().get(User.IS_SUPERUSER).invoke()) {
                    @SuppressWarnings ("unchecked")
                    Map<String, Object> programArguments = (Map<String, Object>) arguments[0];
                    String rawPassword = (String) programArguments.get("password");

                    String hashedPassword = DigestUtils.sha256Hex(rawPassword);
                    if (!holder.get(GET_USER).invoke().get(User.GET_PASSWORD).invoke().equals(hashedPassword)) {
                        throw new StopExecutionException(new IllegalArgumentException("Wrong password"));
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
    public Session() {

    }

}
