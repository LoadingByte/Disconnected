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

package com.quartercode.disconnected.server.world.comp.file;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.comp.os.config.User;
import com.quartercode.disconnected.server.world.util.DerivableSize;
import com.quartercode.disconnected.server.world.util.WorldNode;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;
import com.quartercode.jtimber.api.node.Node;

/**
 * This class represents a file system.
 * The system stores {@link File}s which can be accessed like regular file objects.
 * A file system can be virtual or physical.
 *
 * @see File
 */
public class FileSystem extends WorldNode<Node<?>> implements DerivableSize {

    @XmlAttribute
    private long           size;
    @XmlElement
    private final RootFile rootFile = new RootFile();

    // JAXB constructor
    protected FileSystem() {

    }

    /**
     * Creates a new file system.
     *
     * @param size The maximum size of the file system in bytes.
     *        See {@link #getSize()} for more details.
     */
    public FileSystem(long size) {

        Validate.isTrue(size > 0, "File system size must be > 0");

        this.size = size;
    }

    /**
     * Returns the maximum size of the file system in bytes.
     * The {@link #getFilledSpace() filled space size} must always be smaller or equal to the size of the file system.
     *
     * @return The size of the file system in bytes.
     */
    // Note that this file system size is the DerivableSize.getSize() value as well
    @Override
    public long getSize() {

        return size;
    }

    /**
     * Changes the size of the file system to the given one.
     * Since the {@link #getFilledSpace() filled space size} must always be smaller or equal to the size of the file system,
     * an {@link IllegalArgumentException} is thrown if the new size doesn't pass that criteria.
     *
     * @param size The new size of the file system in bytes.
     * @throws IllegalArgumentException If the new file system size is smaller that the size of the filled file system space.
     */
    public void setSize(long size) {

        Validate.isTrue(getFilledSpace() <= size);
        this.size = size;
    }

    /**
     * Returns the {@link RootFile} every other {@link File} branches off somehow.
     * It is like a virtual root directory.
     *
     * @return The root file of the file system.
     */
    public RootFile getRootFile() {

        return rootFile;
    }

    /**
     * Returns the total amount of bytes which are occupied by {@link File}s on the file system.
     *
     * @return The filled space on the file system in bytes.
     */
    public long getFilledSpace() {

        return rootFile.getSize();
    }

    /**
     * Returns the total amount of bytes which are not occupied by {@link File}s on the file system.
     *
     * @return The free space on the file system in bytes.
     */
    public long getFreeSpace() {

        return size - getFilledSpace();
    }

    /**
     * Returns the {@link File} which is stored under the given path.
     * This will lookup the file using a {@link File#getPath() local file system path}.
     * However, such a local path does <b>not</b> contain any kind of mountpoint information.<br>
     * <br>
     * If you invoke {@link File#getPath()} on the returned file, the original path which has been provided to this method will be the result.
     *
     * @param path The path to search under.
     * @return The file which is stored under the given path.
     *         <b>Note that this file will never be {@code null}!</b> Instead, an {@link InvalidPathException} is thrown if the file doesn't exist.
     * @throws InvalidPathException If the given path isn't valid (e.g. because the file does not exist or because a file along the path is not a parent file).
     */
    public File<?> getFile(String path) throws InvalidPathException {

        String[] pathParts = PathUtils.normalize(path).split(PathUtils.SEPARATOR);

        File<?> currentFile = rootFile;
        for (int pathPartIndex = 0; pathPartIndex < pathParts.length; pathPartIndex++) {
            String pathPart = pathParts[pathPartIndex];

            if (!pathPart.isEmpty()) {
                currentFile = ((ParentFile<?>) currentFile).getChildFileByName(pathPart);

                // Throw an InvalidPathException
                // - if the next file doesn't exist or
                // - if the next file is not a parent file and the last path part is not yet reached (invalid path)
                if (currentFile == null || ! (currentFile instanceof ParentFile) && pathPartIndex != pathParts.length - 1) {
                    throw new InvalidPathException(this, path);
                }
            }
        }

        return currentFile;
    }

    /**
     * Returns a {@link FileAddAction} for adding the given {@link File} to the given path on this file system.
     * If the path does not exist, this method creates directories to match it.
     * Those newly created directories have the same right settings as the file to add.
     * Note that the name of the file to add is changed to match the path.<br>
     * <br>
     * In order to actually add the file, the {@link FileAddAction#execute()} method must be invoked.
     * Note that that method might throw exceptions if the given file cannot be added.
     * Also note that no right checks or anything like that are done by that execution method.
     * If you need such permission checks, use {@link FileAddAction#isExecutableBy(User)} or {@link FileAddAction#getMissingRights(User)}.
     *
     * @param file The file to add to the file system.
     * @param path The local file system path for the new file.
     *        The name of the file will be changed to the last entry of this path.
     * @return A file add action for actually adding the file.
     * @see FileAddAction#execute()
     */
    public FileAddAction prepareAddFile(File<ParentFile<?>> file, String path) {

        return new FileAddAction(this, file, path);
    }

}
