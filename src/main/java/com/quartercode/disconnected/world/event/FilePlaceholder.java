
package com.quartercode.disconnected.world.event;

import java.io.Serializable;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.quartercode.disconnected.world.comp.file.File;
import com.quartercode.disconnected.world.comp.file.FileRights;
import com.quartercode.disconnected.world.comp.file.FileSystem;
import com.quartercode.disconnected.world.comp.file.FileSystemModule.KnownFileSystem;
import com.quartercode.disconnected.world.comp.file.FileUtils;
import com.quartercode.disconnected.world.comp.file.RootFile;
import com.quartercode.disconnected.world.comp.os.Group;
import com.quartercode.disconnected.world.comp.os.User;

/**
 * A file placeholder represents a {@link File} object by storing commonly used data about it.
 * File systems are represented by their {@link RootFile}s.
 */
public class FilePlaceholder implements Serializable {

    private static final long              serialVersionUID = 2736746033872876416L;

    private final String[]                 path;
    private final Class<? extends File<?>> type;
    private final long                     size;
    private final String                   rights;
    private final String                   owner;
    private final String                   group;

    /**
     * Creates a new file placeholder that represents the given {@link KnownFileSystem}.
     * 
     * @param fileSystem The known file system that should be represented by the placeholder.
     */
    public FilePlaceholder(KnownFileSystem fileSystem) {

        FileSystem actualFs = fileSystem.get(KnownFileSystem.FILE_SYSTEM).get();
        RootFile root = actualFs.get(FileSystem.ROOT).get();

        path = new String[] { fileSystem.get(KnownFileSystem.MOUNTPOINT).get() };
        type = RootFile.class;
        size = actualFs.get(File.GET_SIZE).invoke();
        rights = root.get(File.RIGHTS).get().get(FileRights.TO_STRING).invoke();

        User ownerObject = root.get(File.OWNER).get();
        owner = ownerObject == null ? null : ownerObject.get(User.NAME).get();

        Group groupObject = root.get(File.GROUP).get();
        group = groupObject == null ? null : groupObject.get(Group.NAME).get();
    }

    /**
     * Creates a new file placeholder that represents the given {@link File} which is stored on a file system that is mounted under the given mountpoint.
     * 
     * @param fileSystemMountpoint The mountpoint of the file system the file is stored on.
     * @param file The file that should be represented by the placeholder.
     */
    // This unchecked cast for the file type does always work since Class<? extends File> is just casted to Class<? extends File<?>>
    @SuppressWarnings ("unchecked")
    public FilePlaceholder(String fileSystemMountpoint, File<?> file) {

        String stringPath = FileUtils.resolvePath(FileUtils.normalizePath(fileSystemMountpoint), file.get(File.GET_PATH).invoke());
        path = stringPath.substring(1).split(File.SEPARATOR);

        type = (Class<? extends File<?>>) file.getClass();
        size = file.get(File.GET_SIZE).invoke();
        rights = file.get(File.RIGHTS).get().get(FileRights.TO_STRING).invoke();

        User ownerObject = file.get(File.OWNER).get();
        owner = ownerObject == null ? null : ownerObject.get(User.NAME).get();

        Group groupObject = file.get(File.GROUP).get();
        group = groupObject == null ? null : groupObject.get(Group.NAME).get();
    }

    /**
     * Creates a new file placeholder using the provided data.
     * 
     * @param path The path of the represented file.
     *        The first entry should be the mountpoint of the file system the file is stored on.
     *        The last entry should be the actual name of the file.
     *        All other entries should be the names of the directories that lead to the file.
     *        Note that one entry is possible, in which case the placeholder represents a file system.
     * @param type The type (class object) of the represented file.
     * @param size The total size of the represented file.
     *        If the placeholder represents a file system, the size of that file system should be used.
     * @param rights A {@link FileRights}s string which stores the file rights of the represented file.
     * @param owner The name of the user that owns the represented file.
     * @param group The name of the group that is assigned to the represented file.
     */
    public FilePlaceholder(String[] path, Class<? extends File<?>> type, long size, String rights, String owner, String group) {

        Validate.notNull(path, "File placeholder path array cannot be null");
        Validate.isTrue(path.length > 0, "File placeholder path array length must be > 0");
        Validate.notNull(type, "File placeholder type cannot be null");
        Validate.isTrue(size >= 0, "File placeholder size must be >= 0");
        Validate.notBlank(rights, "File placeholder rights cannot be blank");

        this.path = path;
        this.type = type;
        this.size = size;
        this.rights = rights;
        this.owner = owner;
        this.group = group;
    }

    /**
     * Returns whether the placeholder represents a file system.
     * This returns true if {@link #getPath()} only has one entry.
     * 
     * @return Whether a file system is represented.
     */
    public boolean isFileSystem() {

        return path.length == 1;
    }

    /**
     * Returns the path of the represented file.
     * The first entry is the mountpoint of the file system the file is stored on.
     * The last entry is the actual name of the file.
     * All other entries are the names of the directories that lead to the file.
     * Note that one entry is possible, in which case the placeholder represents a file system.
     * 
     * @return The file path entries.
     * @see #getFileSystemMountpoint()
     * @see #getName()
     */
    public String[] getPath() {

        return path;
    }

    /**
     * Returns the mountpoint of the file system the file file is stored on.
     * It is derived from the first element of the path ({@link #getPath()}).
     * 
     * @return The file system mountpoint.
     */
    public String getFileSystemMountpoint() {

        return path[0];
    }

    /**
     * Returns the name of the represented file.
     * It is derived from the last element of the path ({@link #getPath()}).
     * 
     * @return The file name.
     */
    public String getName() {

        return path[path.length - 1];
    }

    /**
     * Returns the type (class object) of the represented file.
     * 
     * @return The file type.
     */
    public Class<? extends File<?>> getType() {

        return type;
    }

    /**
     * Returns the total size of the represented file.
     * If the placeholder represents a file system, the size of that file system is used.
     * 
     * @return The file/file system size.
     */
    public long getSize() {

        return size;
    }

    /**
     * Returns a {@link FileRights}s string which stores the file rights of the represented file.
     * 
     * @return The file rights as a string.
     * @see FileRights#TO_STRING
     */
    public String getRights() {

        return rights;
    }

    /**
     * Returns the name of the user which owns the represented file.
     * 
     * @return The name of the file owner.
     */
    public String getOwner() {

        return owner;
    }

    /**
     * Returns The name of the group which is assigned to the represented file.
     * 
     * @return The name of the file group.
     */
    public String getGroup() {

        return group;
    }

    @Override
    public int hashCode() {

        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {

        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this);
    }

}
