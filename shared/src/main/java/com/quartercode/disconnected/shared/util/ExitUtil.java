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

package com.quartercode.disconnected.shared.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The exit utility class stores an injected {@link ExitProcessor} that allows to exit the application.
 * The processor is typically injected by a highly depending class.
 */
public class ExitUtil {

    private static final Logger  LOGGER = LoggerFactory.getLogger(ExitUtil.class);

    private static ExitProcessor processor;
    private static boolean       exitUnderway;

    /**
     * Injects the given {@link ExitProcessor} into the exit utility.
     * This might override existing processors.
     * 
     * @param processor The exit processor to inject.
     */
    public static void injectProcessor(ExitProcessor processor) {

        ExitUtil.processor = processor;
    }

    /**
     * Exits the application using the injected {@link ExitProcessor}.
     * Please note that this method can only be executed once.
     */
    public static synchronized void exit() {

        if (!exitUnderway) {
            if (processor == null) {
                LOGGER.error("Cannot exit application because no exit processor is injected");
            } else {
                exitUnderway = true;
                processor.exit();
            }
        }
    }

    private ExitUtil() {

    }

    /**
     * An exit processor just provides one {@link #exit()} method that quits the application.
     * The processor is typically injected by a highly depending class into {@link ExitUtil}.
     */
    public static interface ExitProcessor {

        /**
         * Exits the application.
         */
        public void exit();

    }

}
