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

package com.quartercode.disconnected.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class can listen on an input stream and write the results to an output stream (must be a print stream).
 */
public class StreamGobbler extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamGobbler.class);

    private final String        prefix;
    private final InputStream   inputStream;
    private final PrintStream   outputStream;

    /**
     * Creates a new stream gobbler and setsthe prefix and the input and output streams.
     * 
     * @param prefix The prefix to use on each line.
     * @param inputStream The input stream to listen on.
     * @param outputStream The output stream (must be a print stream) to write on.
     */
    public StreamGobbler(String prefix, InputStream inputStream, PrintStream outputStream) {

        this.prefix = prefix;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ( (line = reader.readLine()) != null) {
                outputStream.println( (prefix == null ? "" : prefix) + line);
            }
        } catch (IOException e) {
            LOGGER.error("Error while listening on process stream", e);
        }
    }
}
