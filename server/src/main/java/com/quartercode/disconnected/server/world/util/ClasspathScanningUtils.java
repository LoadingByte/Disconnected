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

package com.quartercode.disconnected.server.world.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashSet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.disconnected.shared.util.ResourceLister;

/**
 * A utility class for scanning the classpath for packages and {@link Class}es.
 * That means that this class is able to resolve things which are not accessible through normal reflection.
 */
public class ClasspathScanningUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathScanningUtils.class);

    /**
     * Returns all subpackages of the given package.
     * For example, imagine the following packages exist:
     * 
     * <pre>
     * a.b
     * a.b.c
     * a.b.d
     * a.b.d.e
     * a.f
     * </pre>
     * 
     * A call of this method with the root package {@code a.b} and {@code returnItself} on {@code true} would yield the following packages:
     * 
     * <pre>
     * a.b
     * a.b.c
     * a.b.d
     * a.b.d.e
     * </pre>
     * 
     * @param packageName The root package whose subpackages should be returned.
     * @param returnItself Whether this method should also return the initial root package ({@code packageName} parameter).
     * @param throwAll Whether {@link IOException}s, which are thrown during the search process and would therefore interrupt it, should be thrown.
     *        If this is {@code false}, the regarding exceptions are printed into the log as errors.
     * @return The subpackages of the given package.
     * @throws IOException Thrown if the available package directories cannot be listed.
     *         If {@code throwAll} is {@code true}, this exception is also thrown if the contents of one (sub)package directory cannot be listed.
     */
    public static Collection<String> getSubpackages(final String packageName, boolean returnItself, boolean throwAll) throws IOException {

        final Collection<String> packages = new HashSet<>();

        // Translate the package name into a resource path
        String packageDirName = convertPackageToPath(packageName);

        // Iterate over all directories which represent the package
        try (ResourceLister resourceLister = new ResourceLister(packageDirName)) {
            for (final Path packageDir : resourceLister.getResourcePaths()) {
                try {
                    Files.walkFileTree(packageDir, new SimpleFileVisitor<Path>() {

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {

                            String relativePath = StringUtils.strip(packageDir.relativize(dir).toString(), "/");
                            if (!relativePath.isEmpty()) {
                                packages.add(packageName + "." + relativePath.replace('/', '.'));
                            }
                            return FileVisitResult.CONTINUE;
                        }

                    });
                } catch (IOException e) {
                    if (throwAll) {
                        throw e;
                    } else {
                        LOGGER.error("Cannot read contents from package directory '{}' (subpackage of '{}')", packageDir, packageName, e);
                    }
                }
            }
        }

        if (returnItself) {
            packages.add(packageName);
        }

        return packages;
    }

    /**
     * Returns all {@link Class}es which are located inside the given package.
     * Note that all classes from a package are loaded when this method is called on that package.
     * That is required because all classes from the package are returned by this method.
     * 
     * @param packageName The name of the package whose classes should be returned.
     * @param throwAll Whether {@link IOException}s, which are thrown during the search process and would therefore interrupt it, should be thrown.
     *        If this is {@code false}, the regarding exceptions are printed into the log as errors.
     * @return The classes which are located inside the given package.
     * @throws IOException Thrown if the available package directories cannot be listed.
     *         If {@code throwAll} is {@code true}, this exception is also thrown if the contents of one package directory cannot be listed.
     */
    public static Collection<Class<?>> getPackageClasses(String packageName, boolean throwAll) throws IOException {

        Collection<Class<?>> classes = new HashSet<>();

        // Translate the package name into a resource path
        String packageDirName = convertPackageToPath(packageName);

        // Iterate over all directories which provide classes for the package
        try (ResourceLister resourceLister = new ResourceLister(packageDirName)) {
            for (Path packageDir : resourceLister.getResourcePaths()) {
                // Iterate over all the files in the directory
                try (DirectoryStream<Path> packageDirStream = Files.newDirectoryStream(packageDir)) {
                    for (Path file : packageDirStream) {
                        // Try to add the current file as class
                        // The called method checks whether the file actually is a class file
                        tryAddClass(packageName, file, classes);
                    }
                } catch (IOException e) {
                    if (throwAll) {
                        throw e;
                    } else {
                        LOGGER.error("Cannot read contents from class directory '{}' (package '{}')", packageDir, packageName, e);
                    }
                }
            }
        }

        return classes;
    }

    /**
     * Reads the {@code jaxb.index} context path file from the given package and returns all {@link Class}es it mentions.
     * Note that only the classes listed in the index file are loaded.
     * 
     * @param packageName The name of the package whose {@code jaxb.index} context path file should be evaluated.
     * @param throwAll Whether {@link IOException}s, which are thrown during the evaluation process and would therefore interrupt, it should be thrown.
     *        If this is {@code false}, the regarding exceptions are printed into the log as errors.
     * @return The classes which are mentioned inside the {@code jaxb.index} context path file from the given package.
     * @throws IOException Thrown if the available package directories cannot be listed.
     *         If {@code throwAll} is {@code true}, this exception is also thrown if the contents of one {@code jaxb.index} file cannot be read.
     */
    public static Collection<Class<?>> getJAXBIndexedPackageClasses(String packageName, boolean throwAll) throws IOException {

        Collection<Class<?>> classes = new HashSet<>();

        // Translate the package name into a resource path
        String packageDirName = convertPackageToPath(packageName);

        // Iterate over all directories which provide classes for the package
        try (ResourceLister resourceLister = new ResourceLister(packageDirName)) {
            for (Path packageDir : resourceLister.getResourcePaths()) {
                // Retrieve the index file
                Path indexFile = packageDir.resolve("jaxb.index");

                if (Files.exists(indexFile)) {
                    try {
                        // Iterate over the index and try to add each class which is mentioned there
                        for (String line : Files.readAllLines(indexFile, Charset.forName("UTF-8"))) {
                            tryAddClass(packageName, packageDir.resolve(line + ".class"), classes);
                        }
                    } catch (IOException e) {
                        if (throwAll) {
                            throw e;
                        } else {
                            LOGGER.error("Cannot read contents from JAXB index file '{}' (package '{}')", indexFile, packageName, e);
                        }
                    }
                }
            }
        }

        return classes;
    }

    private static String convertPackageToPath(String packageName) {

        return ( (packageName.startsWith("/") ? "" : "/") + packageName).replace('.', '/');
    }

    /*
     * Checks whether the given file, which is located inside the given package, is a class file.
     * If that is the case, the associated Class object is added to the given target collection.
     */
    private static void tryAddClass(String packageName, Path file, Collection<Class<?>> target) {

        // Check whether the file has the ".class" file extension
        if (file.toString().endsWith(".class")) {
            // Remove the ".class" file extension
            String simpleClassName = StringUtils.substringBeforeLast(file.getFileName().toString(), ".");
            // Construct the fully qualified class name of the class file
            String fqClassName = packageName + "." + simpleClassName;
            try {
                // Retrieve the class object and add it to the class list
                target.add(Class.forName(fqClassName));
            } catch (ClassNotFoundException e) {
                // File is not a class -> ignore
            }
        }
    }

    private ClasspathScanningUtils() {

    }

}
