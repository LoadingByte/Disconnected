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

package com.quartercode.disconnected.clientdist;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import com.quartercode.disconnected.clientdist.launcher.Launcher;

/**
 * The launcher main class creates a new {@link Launcher} which starts the main process.
 */
public class LauncherMain {

    /**
     * The main method which creates and calls a new launcher.
     * This is not part of the launcher utility.
     * 
     * @param args The command line arguments.
     */
    public static void main(String[] args) {

        System.setProperty("logName", "launcher");

        String mainClass = Main.class.getName();
        String[] vmArguments = { "-DlogName=disconnected", "-Djava.library.path=" + Paths.get("lib/natives").toAbsolutePath() };
        Path workingDirectory = Paths.get(System.getProperty("user.home"), ".disconnected");

        // If the first parameter is an existing file, use that one as running dir
        if (args.length > 0) {
            Path argumentWorkingDirectory = Paths.get(args[0]);
            if (Files.exists(argumentWorkingDirectory)) {
                workingDirectory = argumentWorkingDirectory;
                args = new ArrayList<>(Arrays.asList(args)).subList(1, args.length).toArray(new String[args.length - 1]);
            }
        }

        Launcher launcher = new Launcher(mainClass, vmArguments, args, workingDirectory);
        launcher.launch();
    }

    private LauncherMain() {

    }

}
