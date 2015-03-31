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
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * @param rootPackage The root package whose subpackages should be returned.
     * @param returnRoot Whether this method should also return the initial root package ({@code rootPackage} parameter).
     * @param throwAll Whether {@link IOException}s, which are thrown during the search process and would therefore interrupt it, should be thrown.
     *        If this is {@code false}, the regarding exceptions are logged as errors.
     * @return The subpackages of the given package.
     * @throws IOException Thrown if the available package directories cannot be listed.
     *         If {@code throwAll} is {@code true}, this exception is also thrown if the contents of one (sub)package directory cannot be listed.
     */
    public static Collection<String> getSubpackages(final String rootPackage, boolean returnRoot, boolean throwAll) throws IOException {

        final Collection<String> packages = new HashSet<>();

        // Translate the package name into a resource path
        String packageDirName = convertPackageToPath(rootPackage);

        // Iterate over all directories which represent the package
        try (ResourceLister resourceLister = new ResourceLister(packageDirName, throwAll)) {
            for (final Path packageDir : resourceLister.getResourcePaths()) {
                try {
                    Files.walkFileTree(packageDir, new SimpleFileVisitor<Path>() {

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {

                            String relativePath = StringUtils.strip(packageDir.relativize(dir).toString(), "/");
                            if (!relativePath.isEmpty()) {
                                packages.add(rootPackage + "." + relativePath.replace('/', '.'));
                            }
                            return FileVisitResult.CONTINUE;
                        }

                    });
                } catch (IOException e) {
                    if (throwAll) {
                        throw e;
                    } else {
                        LOGGER.error("Cannot read contents from package directory '{}' (subpackage of '{}')", packageDir, rootPackage, e);
                    }
                }
            }
        }

        if (returnRoot) {
            packages.add(rootPackage);
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
     *        If this is {@code false}, the regarding exceptions are logged as errors.
     * @return The classes which are located inside the given package.
     * @throws IOException Thrown if the available package directories cannot be listed.
     *         If {@code throwAll} is {@code true}, this exception is also thrown if the contents of one package directory cannot be listed.
     */
    public static Collection<Class<?>> getPackageClasses(String packageName, boolean throwAll) throws IOException {

        Collection<Class<?>> classes = new HashSet<>();

        // Translate the package name into a resource path
        String packageDirName = convertPackageToPath(packageName);

        // Iterate over all directories which provide classes for the package
        try (ResourceLister resourceLister = new ResourceLister(packageDirName, throwAll)) {
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
     * Reads the index file, which is located under the given resource, from all occurrences and returns all {@link Class}es it mentions.
     * Each (non-empty) line from the file should reference one class from the package, which contains the index file, by its name.
     * Note that it is also possible to reference classes from subpackages by prepending the relative subpackage path.
     * Also note that only the classes listed in the index file are loaded.
     * 
     * @param indexResource The classpath resource path to the index file, which should be evaluated.
     *        All mentioned classes must also lie in the directory of this resource.
     * @param allowRemoval If {@code true}, lines prefixed with a {@code -} mark classes which should not be added to the resulting class list.
     * @param throwAll Whether {@link IOException}s, which are thrown during the evaluation process and would therefore interrupt it, should be thrown.
     *        If this is {@code false}, the regarding exceptions are logged as errors.
     * @return The classes which are mentioned inside the given index file.
     * @throws IOException Thrown if the available index resources directories cannot be listed.
     *         If {@code throwAll} is {@code true}, this exception is also thrown if the contents of one index file cannot be read.
     */
    public static Collection<Class<?>> getIndexedPackageClasses(String indexResource, boolean allowRemoval, boolean throwAll) throws IOException {

        Validate.isTrue(indexResource.startsWith("/"), "Cannot use relative class index resources, make sure your path starts with '/'");

        // Translate the index resource path into into a package name
        String packageName = StringUtils.substringBeforeLast("/" + StringUtils.strip(indexResource, "/"), "/").replace('/', '.');

        return getIndexedPackageClassesInternal(indexResource, packageName, allowRemoval, throwAll);
    }

    /**
     * Reads an index file with the given name from the given package and returns all {@link Class}es it mentions.
     * Each (non-empty) line from the file should reference one class from the package by its name.
     * Note that it is also possible to reference classes from subpackages by prepending the relative subpackage path.
     * Also note that only the classes listed in the index file are loaded.
     * 
     * @param packageName The name of the package whose index file should be evaluated.
     * @param indexFileName The name of the index file, which should be evaluated.
     * @param allowRemoval If {@code true}, lines prefixed with a {@code -} mark classes which should not be added to the resulting class list.
     * @param throwAll Whether {@link IOException}s, which are thrown during the evaluation process and would therefore interrupt it, should be thrown.
     *        If this is {@code false}, the regarding exceptions are logged as errors.
     * @return The classes which are mentioned inside the index file from the given package.
     * @throws IOException Thrown if the available package directories cannot be listed.
     *         If {@code throwAll} is {@code true}, this exception is also thrown if the contents of one index file cannot be read.
     */
    public static Collection<Class<?>> getIndexedPackageClasses(String packageName, String indexFileName, boolean allowRemoval, boolean throwAll) throws IOException {

        // Translate the package name into a resource path
        String indexResource = convertPackageToPath(packageName) + "/" + indexFileName;

        return getIndexedPackageClassesInternal(indexResource, packageName, allowRemoval, throwAll);
    }

    private static String convertPackageToPath(String packageName) {

        return StringUtils.prependIfMissing(packageName.replace('.', '/'), "/");
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

    private static Collection<Class<?>> getIndexedPackageClassesInternal(String indexResource, String packageName, boolean allowRemoval, boolean throwAll) throws IOException {

        Collection<Class<?>> addedClasses = new HashSet<>();
        Collection<Class<?>> removedClasses = allowRemoval ? new HashSet<Class<?>>() : null;

        // Iterate over all index files provided on the classpath
        try (ResourceLister resourceLister = new ResourceLister(indexResource, throwAll)) {
            for (Path indexFile : resourceLister.getResourcePaths()) {
                try {
                    // Iterate over the index and try to add each class which is mentioned there
                    for (String line : Files.readAllLines(indexFile, Charset.forName("UTF-8"))) {
                        // Remove comment part
                        line = StringUtils.substringBefore(line, "#");

                        // Trim whitespace
                        line = line.trim();

                        // Check whether the line removes the class it references
                        boolean remove = line.startsWith("-");

                        // Ignore the line if it removes the class although removal is disabled
                        if (remove && !allowRemoval) {
                            LOGGER.warn("Index file '{}' (package '{}') defines a class removal although that is disabled", indexFile, packageName);
                            continue;
                        }

                        // Retrieve the referenced class name by removing any leading removal character ("-") and trimming the resulting string
                        String className = StringUtils.stripStart(line, "-").trim();

                        // Dispose the line if it doesn't contain any class reference
                        if (className.isEmpty()) {
                            continue;
                        }

                        // Determine the collection the class should be put into (add or remove)
                        Collection<Class<?>> target = remove ? removedClasses : addedClasses;

                        String fqClassName = packageName + (packageName.isEmpty() ? "" : ".") + className;

                        try {
                            // Retrieve the class object and add it to the determined class list
                            target.add(Class.forName(fqClassName));
                        } catch (ClassNotFoundException e) {
                            LOGGER.warn("Invalid indexed class reference to '{}' (index file '{}')", fqClassName, indexFile);
                        }
                    }
                } catch (IOException e) {
                    if (throwAll) {
                        throw e;
                    } else {
                        LOGGER.error("Cannot read contents from index file '{}' (package '{}')", indexFile, packageName, e);
                    }
                }
            }
        }

        if (allowRemoval) {
            // Apply the remove class orders
            addedClasses.removeAll(removedClasses);
        }

        return addedClasses;
    }

    private ClasspathScanningUtils() {

    }

}
