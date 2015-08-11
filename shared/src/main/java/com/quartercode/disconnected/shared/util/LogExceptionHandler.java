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

import java.lang.Thread.UncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class catches all uncaught throwables in all threads and logs them.
 */
public class LogExceptionHandler implements UncaughtExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogExceptionHandler.class);

    /**
     * Returns the used logger for logging the exceptions.
     *
     * @return The used logger for logging the exceptions.
     */
    public static Logger getLogger() {

        return LOGGER;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable t) {

        LOGGER.error("Uncaught exception in thread '{}' (id {})", thread.getName(), thread.getId(), t);
    }

}
