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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;
import com.quartercode.disconnected.server.world.comp.os.config.User;
import com.quartercode.disconnected.server.world.comp.os.mod.OSModule;
import com.quartercode.disconnected.server.world.util.WorldNode;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;
import com.quartercode.jtimber.api.node.wrapper.SubstituteWithWrapper;
import com.quartercode.jtimber.api.node.wrapper.collection.ListWrapper;

/**
 * This class represents an {@link OSModule operating system module} which is used to access the available {@link FileSystem file systems}.
 * It is an essential part of the operating system and is directly used by it.
 *
 * @see FileSystem
 * @see File
 * @see OSModule
 */
public class FileSystemModule extends WorldNode<OperatingSystem> implements OSModule {

    @XmlElementWrapper
    @XmlElement (name = "knownFileSystem")
    @SubstituteWithWrapper (ListWrapper.class)
    private final List<KnownFileSystem> knownFileSystems = new ArrayList<>();

    /**
     * Returns the {@link FileSystem}s which are associated with a known mountpoint (e.g. {@code system}) and can be mounted.
     * Actually, this method returns {@link KnownFileSystem} wrapper objects that wrap around real file systems and store the additional information.
     *
     * @return The known file systems registered in the module's registry.
     */
    public List<KnownFileSystem> getKnownFs() {

        return Collections.unmodifiableList(knownFileSystems);
    }

    /**
     * Registers the given {@link KnownFileSystem} to the module.
     * Such known file system wrapper objects wrap around real {@link FileSystem}s and store additional information like a mountpoint (e.g. {@code system}).
     *
     * @param knownFs The known file system to register to the module.
     * @throws IllegalArgumentException If the new known file system that should be registered is already mounted.
     */
    public void addKnownFs(KnownFileSystem knownFs) {

        Validate.isTrue(!knownFs.isMounted(), "Can't register known file system '%s' while it is mounted", knownFs.getMountpoint());
        knownFileSystems.add(knownFs);
    }

    /**
     * Unregisters the given {@link KnownFileSystem} from the module.
     * Such known file system wrapper objects wrap around real {@link FileSystem}s and store additional information like a mountpoint (e.g. {@code system}).
     * Note that known file systems must be {@link KnownFileSystem#setMounted(boolean) unmounted} before they can be removed using this method.
     *
     * @param knownFs The known file system to unregister from the module.
     * @throws IllegalArgumentException If the known file system that should be unregistered is still mounted.
     */
    public void removeKnownFs(KnownFileSystem knownFs) {

        Validate.isTrue(!knownFs.isMounted(), "Can't unregister known file system '%s' while it is still mounted", knownFs.getMountpoint());
        knownFileSystems.remove(knownFs);
    }

    /**
     * Returns the registered {@link KnownFileSystem} object which is representing the given {@link FileSystem}.
     *
     * @param fileSystem The file system the returned known file system is representing.
     * @return The known file system which is representing the given file system; {@code null} if it cannot be found.
     */
    public KnownFileSystem getKnownFsByFs(FileSystem fileSystem) {

        for (KnownFileSystem knownFs : knownFileSystems) {
            if (knownFs.getFileSystem().equals(fileSystem)) {
                return knownFs;
            }
        }

        return null;
    }

    /**
     * Returns a list containing all {@link KnownFileSystem}s which are currently {@link KnownFileSystem#isMounted() mounted}.
     * See the links above for more details on mounting file systems.
     * If you instead want to get all known file systems (also the ones which are not mounted), take a look at {@link #getKnownFs()}.
     *
     * @return The known file systems which are currently mounted.
     */
    public List<KnownFileSystem> getMountedKnownFs() {

        List<KnownFileSystem> mountedKnownFs = new ArrayList<>();

        for (KnownFileSystem knownFs : knownFileSystems) {
            if (knownFs.isMounted()) {
                mountedKnownFs.add(knownFs);
            }
        }

        return mountedKnownFs;
    }

    /**
     * Returns the {@link KnownFileSystem} which is currently {@link KnownFileSystem#isMounted() mounted} and associated with the given {@link KnownFileSystem#getMountpoint() mountpoint}.
     * See the links above for more details on mountpoints and mounting file systems.
     *
     * @param mountpoint The mountpoint of the returned mounted known file system.
     * @return The known file system which is currently mounted and associated with the given mountpoint; {@code null} if it cannot be found.
     */
    public KnownFileSystem getMountedKnownFsByMountpoint(String mountpoint) {

        for (KnownFileSystem knownFs : knownFileSystems) {
            if (knownFs.isMounted() && knownFs.getMountpoint().equals(mountpoint)) {
                return knownFs;
            }
        }

        return null;
    }

    /**
     * Returns the {@link File} which is stored on a mounted {@link FileSystem} under the given path.
     * Note that this will lookup the file using a global OS path <b>including the {@link KnownFileSystem#getMountpoint() mountpoint}</b>.
     * For example, a file with the {@link File#getPath() local file system path} {@code bin/prog.exe} could be located using the global OS
     * path {@code /system/bin/prog.exe} if it was lying on a {@link #getKnownFs() known file system} with the mountpoint {@code system}.
     *
     * @param path The path to search under.
     * @return The file which is located under the given global OS path.
     *         <b>Note that this file will never be {@code null}!</b> Instead, an {@link InvalidPathException} is thrown if the file doesn't exist.
     * @throws IllegalArgumentException If the given path is not absolute (it does not start with {@link PathUtils#SEPARATOR}).
     * @throws UnknownMountpointException If the file system the given path refers (first path component, e.g {@code /system}) cannot be found or is not mounted.
     * @throws InvalidPathException If the given path isn't valid (e.g. because the file does not exist or because a file along the path is not a parent file).
     */
    public File<?> getFile(String path) throws UnknownMountpointException, InvalidPathException {

        Validate.isTrue(path.startsWith(PathUtils.SEPARATOR), "Global path must start with a separator ('%s' is invalid)", path);

        String[] pathComponents = PathUtils.splitAfterMountpoint(PathUtils.normalize(path));
        Validate.isTrue(pathComponents[0] != null, "Global path must start with the mountpoint ('%s' is invalid)", path);

        KnownFileSystem knownFs = getMountedKnownFsByMountpoint(pathComponents[0]);
        if (knownFs == null) {
            throw new UnknownMountpointException(this, pathComponents[0]);
        }

        return knownFs.getFileSystem().getFile(pathComponents[1] == null ? "" : pathComponents[1]);
    }

    /**
     * Returns a {@link FileAddAction} for adding the given {@link File} under the given path.
     * Keep in mind that the file is added to the mounted {@link FileSystem} which is specified by the path's mountpoint.
     * If the path does not exist, this method creates directories to match it.
     * Those newly created directories have the same right settings as the file to add.
     * Note that the name of the file to add is changed to match the path.<br>
     * <br>
     * In order to actually add the file, the {@link FileAddAction#execute()} method must be invoked.
     * Note that that method might throw exceptions if the given file cannot be added.
     * Also note that no right checks or anything like that are done by that execution method.
     * If you need such permission checks, use {@link FileAddAction#isExecutableBy(User)} or {@link FileAddAction#getMissingRights(User)}.
     *
     * @param file The file to add under the given path.
     * @param path The global path for the new file (including a mountpoint).
     *        The name of the file will be changed to the last entry of this path.
     * @return A file add action for actually adding the file.
     * @throws IllegalArgumentException If the given path is not absolute (it does not start with {@link PathUtils#SEPARATOR}).
     * @throws UnknownMountpointException If the file system the given path refers (first path component, e.g {@code /system}) cannot be found or is not mounted.
     * @see FileAddAction#execute()
     */
    public FileAddAction prepareAddFile(File<ParentFile<?>> file, String path) throws UnknownMountpointException {

        String[] pathComponents = PathUtils.splitAfterMountpoint(PathUtils.normalize(path));
        Validate.isTrue(pathComponents[0] != null && pathComponents[1] != null, "Must provide an absolute path ('%s' is invalid)", path);

        KnownFileSystem knownFs = getMountedKnownFsByMountpoint(pathComponents[0]);
        if (knownFs == null) {
            throw new UnknownMountpointException(this, pathComponents[0]);
        }

        FileSystem fileSystem = knownFs.getFileSystem();
        return fileSystem.prepareAddFile(file, pathComponents[1]);
    }

    @Override
    public void setRunning(boolean running) {

        // Only invoke on bootstrap
        if (running) {
            mountSystemFs();
        }

        // Only invoke on shutdown
        if (!running) {
            unmountAllFs();
        }
    }

    private void mountSystemFs() {

        for (KnownFileSystem knownFs : knownFileSystems) {
            // TODO: Temp: Mount every available file system until a proper file system table is implemented
            // if (knownFs.getMountpoint().equals(CommonFiles.SYSTEM_MOUNTPOINT)) {
            knownFs.setMounted(true);
            // break;
            // }
        }
    }

    private void unmountAllFs() {

        for (KnownFileSystem knownFs : knownFileSystems) {
            knownFs.setMounted(false);
        }
    }

    /**
     * The known file system represents a {@link FileSystem} which can mounted into an operating system because it's mountpoint is known.
     * A mountpoint is a string like {@code system} and defines the start of all paths targeted at the file system.
     * For example, a file with the {@link File#getPath() local path} {@code bin/prog.exe} could be located using the global path {@code /system/bin/prog.exe} if
     * it was lying on a known file system with the mountpoint {@code system}.<br>
     * <br>
     * The usage of an extra object (this one) is required because the additional data cannot be directly stored in the {@link FileSystem} object.
     * If that was the case, a file system could only be mounted by one computer/OS at the same time.
     * This way, multiple computers which want to mount the file system hold the mountpoint data on their own.
     *
     * @see FileSystem
     */
    public static class KnownFileSystem extends WorldNode<FileSystemModule> {

        @XmlAttribute
        @XmlIDREF
        private FileSystem fileSystem;
        @XmlAttribute
        private String     mountpoint;
        @XmlAttribute
        private boolean    mounted;

        // JAXB constructor
        protected KnownFileSystem() {

        }

        /**
         * Creates a new known file system representation object.
         *
         * @param fileSystem The {@link FileSystem} which is represented by the new wrapper object.
         */
        public KnownFileSystem(FileSystem fileSystem) {

            this.fileSystem = fileSystem;
        }

        /**
         * Returns the {@link FileSystem} which is represented by the wrapper object.
         *
         * @return The represented file system.
         */
        public FileSystem getFileSystem() {

            return fileSystem;
        }

        /**
         * Returns the mountpoint the represented {@link FileSystem} is using.
         * A mountpoint is a string like {@code system} and defines the start of all paths targeted at the file system.
         * For example, a file with the {@link File#getPath() local path} {@code bin/prog.exe} could be located using the global path {@code /system/bin/prog.exe} if
         * it was lying on a known file system with the mountpoint {@code system}.
         *
         * @return The mountpoint of the file system.
         */
        public String getMountpoint() {

            return mountpoint;
        }

        /**
         * Changes the mountpoint the represented {@link FileSystem} is using.
         * See {@link #getMountpoint()} for more details.
         *
         * @return The mountpoint of the file system.
         * @throws IllegalStateException If the known file system is mounted.
         */
        public void setMountpoint(String mountpoint) {

            Validate.validState(!mounted, "Can't change mountpoint of known file system '%s' while it is mounted", this.mountpoint);
            this.mountpoint = mountpoint;
        }

        /**
         * If the represented {@link FileSystem} is actually mounted to the set {@link #MOUNTPOINT}.<br>
         * <br>
         * Exceptions that can occur when setting:
         *
         * <table>
         * <tr>
         * <th>Exception</th>
         * <th>When?</th>
         * </tr>
         * <tr>
         * <td>{@link IllegalStateException}</td>
         * <td>There is another known file system with the same mountpoint already mounted.</td>
         * </tr>
         * </table>
         */
        /**
         * Returns whether the represented {@link FileSystem} is actually mounted to the set {@link #getMountpoint() mountpoint}.
         * The OS can only access a file system when it is mounted.
         *
         * @return Whether the file system is mounted.
         */
        public boolean isMounted() {

            return mounted;
        }

        /**
         * Changes whether the represented {@link FileSystem} is currently mounted to the set {@link #getMountpoint() mountpoint}.
         * The OS can only access a file system when it is mounted.
         *
         * @param mounted Whether the file system should be mounted.
         * @throws IllegalStateException If there is another known file system with the same mountpoint already mounted.
         *         Of course, this restriction is limited to the file systems known to the {@link FileSystemModule} which manages this known file system.
         */
        public void setMounted(boolean mounted) {

            if (mounted) {
                boolean mountpointFree = getSingleParent().getMountedKnownFsByMountpoint(mountpoint) == null;
                Validate.validState(mountpointFree, "Cannot mount known file system because another FS with same mountpoint ('%s') is already mounted", mountpoint);
            }

            this.mounted = mounted;
        }

    }

}
