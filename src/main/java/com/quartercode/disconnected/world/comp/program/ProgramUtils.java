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

package com.quartercode.disconnected.world.comp.program;

import java.util.List;
import com.quartercode.disconnected.world.comp.file.ContentFile;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileSystemModule;
import com.quartercode.disconnected.world.comp.file.FileUtils;
import com.quartercode.disconnected.world.comp.file.UnknownMountpointException;

/**
 * The program utils class provides some utility methods for programs and program executors.
 * 
 * @see Program
 * @see ProgramExecutor
 */
public class ProgramUtils {

    private static void checkCommonLocationAnnotation(Class<? extends ProgramExecutor> executor) {

        if (!executor.isAnnotationPresent(CommonLocation.class)) {
            throw new IllegalStateException("Program executor '" + executor.getName() + "' hasn't a common location annotation");
        }
    }

    /**
     * Returns the directory the program file for the given {@link ProgramExecutor} can be commonly found under.
     * Throws an {@link IllegalStateException} if the provided class doesn't have a {@link CommonLocation} annotation.<br>
     * This method is useful for quickly retrieving a program directory inside a oneliner.
     * 
     * @param executor The program executor type whose common directory should be resolved.
     * @return The common directory of the given program executor.
     * @throws IllegalStateException The given program executor hasn't a common location annotation.
     *         Note that every executor should have one.
     */
    public static String getCommonDirectory(Class<? extends ProgramExecutor> executor) {

        checkCommonLocationAnnotation(executor);
        return executor.getAnnotation(CommonLocation.class).dir();
    }

    /**
     * Returns the file name the program file for the given {@link ProgramExecutor} commonly has.
     * Throws an {@link IllegalStateException} if the provided class doesn't have a {@link CommonLocation} annotation.<br>
     * This method is useful for quickly retrieving a program file name inside a oneliner.
     * Afterwards, the actual location of the program could be resolved using the {@code PATH} environment variable.
     * 
     * @param executor The program executor type whose common file name should be resolved.
     * @return The common file name of the given program executor.
     * @throws IllegalStateException The given program executor hasn't a common location annotation.
     *         Note that every executor should have one.
     */
    public static String getCommonFileName(Class<? extends ProgramExecutor> executor) {

        checkCommonLocationAnnotation(executor);
        return executor.getAnnotation(CommonLocation.class).file();
    }

    /**
     * Returns the complete file path the given {@link ProgramExecutor} can be commonly found under.
     * Throws an {@link IllegalStateException} if the provided class doesn't have a {@link CommonLocation} annotation.<br>
     * This method is useful for quickly retrieving a program file path inside a oneliner.
     * 
     * @param executor The program executor type whose common file location should be resolved.
     * @return The common file path of the given program executor.
     * @throws IllegalStateException The given program executor hasn't a common location annotation.
     *         Note that every executor should have one.
     */
    public static String getCommonLocation(Class<? extends ProgramExecutor> executor) {

        return FileUtils.resolvePath(getCommonDirectory(executor), getCommonFileName(executor));
    }

    /**
     * Tries to resolve a {@link Program} file (a {@link ContentFile} which contains a program) with the given name from one of the provided directory paths.
     * That means that the algorithm iterates over all of the directories and uses the first one which contains a program file with the given name.
     * This method is used to resolve program files from the {@code PATH} environment variable.
     * Note that this method suppresses all exceptions that might occur.
     * 
     * @param fsModule The {@link FileSystemModule} which manages the file systems that should be searched.
     * @param directories A list of paths to the directories that should be searched.
     * @param fileName The name of the program file that should be found.
     * @return A program file with the given name in one of the given directories.
     */
    public static ContentFile getProgramFileFromPath(FileSystemModule fsModule, List<String> directories, String fileName) {

        for (String directoryPath : directories) {
            String programFilePath = FileUtils.resolvePath(directoryPath, fileName);

            try {
                File<?> programFile = fsModule.get(FileSystemModule.GET_FILE).invoke(programFilePath);

                if (programFile instanceof ContentFile && programFile.get(ContentFile.CONTENT).get() instanceof Program) {
                    return (ContentFile) programFile;
                }
            } catch (IllegalArgumentException | UnknownMountpointException e) {
                // Continue
            }
        }

        return null;
    }

    private ProgramUtils() {

    }

}
