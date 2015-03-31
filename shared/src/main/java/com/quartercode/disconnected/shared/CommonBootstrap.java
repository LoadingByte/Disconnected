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

package com.quartercode.disconnected.shared;

import java.io.IOException;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.shared.util.init.InitializationException;
import com.quartercode.disconnected.shared.util.init.Initializer;
import com.quartercode.disconnected.shared.util.init.InitializerIndexParser;
import com.quartercode.disconnected.shared.util.init.InitializerSettings;
import com.quartercode.disconnected.shared.util.init.InitializerSorter;

/**
 * A global bootstrap class which calls all indexed {@link Initializer}s and can only be executed once.
 * It should be used whenever the whole system needs to be initialized.
 */
public class CommonBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonBootstrap.class);

    private static boolean      started;

    /**
     * Reads all indexed {@link Initializer}s from {@code /init.index} files on the classpath roots and executes them.
     * Note that this method can only be called once.
     * All other calls result in no action at all (no exception as well).
     */
    public static void bootstrap() {

        if (started) {
            return;
        }
        started = true;

        try {
            Collection<Initializer> initializers = InitializerIndexParser.parseIndex("/init.index");
            initializers = InitializerSorter.sortByDependencies(initializers);

            for (Initializer initializer : initializers) {
                String[] groups = initializer.getClass().getAnnotation(InitializerSettings.class).groups();
                LOGGER.debug("Calling initializer '{}' {}", initializer.getClass().getName(), groups);
                initializer.initialize();
            }
        } catch (IOException e) {
            LOGGER.error("Unable to list 'init.index' files from classpath", e);
        } catch (InitializationException e) {
            LOGGER.error("Fatal error while executing common bootstrap", e);
        }
    }

    private CommonBootstrap() {

    }

}
