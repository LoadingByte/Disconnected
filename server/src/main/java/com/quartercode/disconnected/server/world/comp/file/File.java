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
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.comp.os.config.User;
import com.quartercode.disconnected.server.world.util.DerivableSize;
import com.quartercode.disconnected.server.world.util.SizeUtils;
import com.quartercode.disconnected.server.world.util.WorldNode;
import com.quartercode.disconnected.shared.world.comp.file.FileRights;
import com.quartercode.disconnected.shared.world.comp.file.PathUtils;
import com.quartercode.jtimber.api.node.Node;
import com.quartercode.jtimber.api.node.Weak;

/**
 * This class represents a file on a {@link FileSystem}.
 * Every file knows its name and can resolve its path.
 * There are different variants of a file: A {@link ContentFile} holds content, a {@link ParentFile} holds other files.<br>
 * <br>
 * Note that any file expects to have only <b>one</b> parent.
 * Please use {@link Weak} references if you need to reference a file in an attribute.
 *
 * @param <P> The type of {@link Node}s that are able to be the parent of this file.
 *        Apart from the special case {@link RootFile}, this parameter should always reference another file class.
 * @see ContentFile
 * @see ParentFile
 * @see FileSystem
 */
public abstract class File<P extends Node<?>> extends WorldNode<P> implements DerivableSize {

    /**
     * The default {@link FileRights} string for every new file.
     * If you wish to obtain an instance of the default file rights, please use the {@link FileRights#FileRights(String)} constructor with this string.
     */
    // TODO: Make the default file rights dynamic
    public static final String DEFAULT_FILE_RIGHTS = "o:r,u:dw";

    @XmlAttribute
    private String             name;
    @Weak
    @XmlAttribute
    @XmlIDREF
    private User               owner;
    @XmlAttribute
    private String             group;
    @XmlAttribute
    private final FileRights   rights              = new FileRights(DEFAULT_FILE_RIGHTS);

    // JAXB constructor
    protected File() {

    }

    /**
     * Creates a new file.
     * Note that the file's {@link #getName() name} will be set as soon as the file is added to a {@link FileSystem}.
     *
     * @param owner The owning user of the file. Note that it may not be {@code null}.
     *        See {@link #getOwner()} for more details.
     */
    protected File(User owner) {

        setOwner(owner);
    }

    /**
     * Returns the name of the file.
     * It is the last entry of the file's {@link #getPath() path}.
     * Note that it probably ends with an extension (e.g. {@code .exe} or {@code .txt}).
     * An example file name might be {@code filemanager.exe}.
     *
     * @return The file name.
     *         It may be {@code null} if the file has not yet been added to a {@link FileSystem}.
     */
    public String getName() {

        return name;
    }

    /**
     * Changes the name of the file.
     * This operation represents renaming the file (or moving it while staying in the same directory).
     * See {@link #getName()} for more information on the file name.
     *
     * @param name The new file name.
     */
    public void setName(String name) {

        this.name = name.trim();
    }

    /**
     * Returns the {@link User} who "owns" the file.
     * By default, the owner of a certain file is the user who created that file.
     * However, the owner can be {@link #setOwner(User) changed}.<br>
     * <br>
     * This attribute is important for the {@link #getRights() file rights} system.
     * The user who owns a certain file has extra privileges on that file.
     * Also see {@link FileRights} for more details.
     *
     * @return The owning user of the file.
     *         Note that it may not be {@code null}.
     */
    public User getOwner() {

        return owner;
    }

    /**
     * Changes the {@link User} who "owns" the file.
     * See {@link #getOwner()} for more information on file owners.
     *
     * @param owner The new owning user of the file.
     */
    public void setOwner(User owner) {

        Validate.notNull(owner, "Cannot use null as file owner");
        this.owner = owner;
    }

    /**
     * Returns the name of the group which is assigned to the file.
     * By default, the group of a certain file is the {@link User#getPrimaryGroup() primary group} of the {@link User} who created that file.
     * However, the group can be {@link #setGroup(String) changed}.
     * It is even possible to set the file group to {@code null}, in which case the file has no group at all.<br>
     * <br>
     * This attribute is important for the {@link #getRights() file rights} system.
     * Every user in the group "partially owns" the file and has certain privileges on it (compared to everyone else).
     * However, the {@link #getOwner() owner} of the file often has even more rights.
     * Also see {@link FileRights} for more details.
     *
     * @return The group of the file.
     *         Note that it may be {@code null}.
     */
    public String getGroup() {

        return group;
    }

    /**
     * Changes the name of the group which is assigned to the file.
     * See {@link #getGroup()} for more information on file groups.
     *
     * @param group The new group of the file.
     *        Note that it may be {@code null}.
     */
    public void setGroup(String group) {

        Validate.isTrue(group == null || !group.isEmpty(), "Cannot use an empty string as file group; use null or an actual group instead");
        this.group = group;
    }

    /**
     * Returns the {@link FileRights} object that defines who (users) is allowed to execute which operations on the file.
     * See the {@link FileRights} class for more documentation on how it works in detail.<br>
     * <br>
     * Note that the file rights objects of a file cannot be replaced by another object.
     * Instead, you should use the {@link FileRights#setRight(char, char, boolean)} method to change file rights.
     *
     * @return The file rights object that defines who is allowed to do what with the file.
     */
    public FileRights getRights() {

        return rights;
    }

    /**
     * Returns whether the given {@link User} has access to the given file right on the file.
     * That means that he is allowed to execute operations related to the given file right on this file.
     * Note that the {@link User#isSuperuser() superuser} (or {@code null}) as user) has all rights; the method always returns {@code true}.
     * Otherwise, the set {@link #getRights() file rights} are checked.
     *
     * @param user The user whose access to the given file right should be checked.
     * @param right The file right character that describes the right which should be checked.
     * @return Whether the given user is allowed to execute operations related to the given file right.
     */
    public boolean hasRight(User user, char right) {

        if (user == null || user.isSuperuser()) {
            return true;
        } else if (rights.isRightSet(FileRights.OWNER, right) && owner.equals(user)) {
            return true;
        } else if (group != null && rights.isRightSet(FileRights.GROUP, right) && user.getGroups().contains(group)) {
            return true;
        } else if (rights.isRightSet(FileRights.OTHERS, right)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Throws a {@link MissingFileRightsException} if the given {@link User} hasn't got access to at least one of the given file rights on the file.
     * If an exception is thrown, it contains all missing rights and not just the first one.
     * If no rights are missing, no exception is thrown.
     * For more information on file rights, see {@link #hasRight(User, char)}.
     *
     * @param action A short string that describes what the missing rights are needed for. It is used in the exception message.
     *        The context is "Cannot &lt;action&gt; '&lt;file path&gt;' because the following rights are missing: ..."
     *        For example, this string could be {@code remove file}.
     * @param user The user whose access to the given file rights should be checked.
     * @param rights The file right character that describes the right which should be checked.
     * @throws MissingFileRightsException If the given user isn't granted at least one of the given file rights.
     */
    public void checkRights(String action, User user, char... rights) throws MissingFileRightsException {

        List<Character> missingRights = new ArrayList<>();
        for (char right : rights) {
            if (!hasRight(user, right)) {
                missingRights.add(right);
            }
        }

        if (!missingRights.isEmpty()) {
            throw new MissingFileRightsException(this, user, missingRights, action);
        }
    }

    /**
     * Returns whether the given {@link User} is allowed to change the {@link #getRights() file rights} attributes of the file.
     *
     * @param user The user whose ability to change the file rights should be checked.
     * @return Whether the given user is allowed to change the file rights of this file.
     */
    public boolean canChangeRights(User user) {

        return user == null || owner.equals(user) || user.isSuperuser();
    }

    /**
     * Returns the <b>local</b> the path of the file (e.g {@code bin/sys/filemanager.exe}.
     * The local path can be used to lookup the file a on its {@link FileSystem}.
     * However, it does <b>not</b> contain any kind of mountpoint information because that would be specific to a computer which mounted the file system.
     * Therefore, the local path cannot be used to lookup the file on a computer/OS level.
     *
     * @return The <b>local</b> path of the file.
     *         May be {@code null} if no path can be resolved because the file is no longer stored on any file system.
     */
    public String getPath() {

        // A file should only have one parent
        Node<?> parent = getSingleParent();

        // Check to avoid errors when this method is called on removed/unlinked files
        // Normally, the root file terminates the recursion by returning an empty string
        if (parent instanceof File) {
            String parentPath = ((File<?>) parent).getPath();

            if (parentPath != null) {
                return parentPath + (parentPath.isEmpty() ? "" : PathUtils.SEPARATOR) + name;
            }
        }

        return null;
    }

    /**
     * Returns the {@link FileSystem} which is hosting the file.
     * If you call {@link FileSystem#GET_FILE} using the path returned by {@link #getPath()}, this exact file must be returned.
     *
     * @return The file system which stores the file.
     */
    public FileSystem getFileSystem() {

        // A file should only have one parent
        Node<?> parent = getSingleParent();

        // Check to avoid errors when this method is called on removed/unlinked files
        // Normally, the root file terminates the recursion by returning its parent file system
        if (parent instanceof File) {
            return ((File<?>) parent).getFileSystem();
        } else {
            return null;
        }
    }

    @Override
    public long getSize() {

        return SizeUtils.getSize(name) + SizeUtils.getSize(owner.getName()) + SizeUtils.getSize(group) + SizeUtils.getSize(rights.exportRightsAsString());
    }

    /**
     * Returns a {@link FileMoveAction} for moving the file to a new local path on the {@link FileSystem} it's currently located on.
     * If the new file path does not exist, this method creates directories to match it.
     * Those newly created directories have the same right settings as the file to add.
     * Note that the name of the file to add is changed to match the path.<br>
     * <br>
     * In order to actually move the file, the {@link FileMoveAction#execute()} method must be invoked.
     * Note that that method might throw exceptions if the file cannot be moved.
     * Also note that no right checks or anything like that are done by that execution method.
     * If you need such permission checks, use {@link FileMoveAction#isExecutableBy(User)} or {@link FileMoveAction#getMissingRights(User)}.
     *
     * @param path The new local file system path the file should be moved to
     *        The name of the file will be changed to the last entry of this path.
     * @return A file move action for actually moving the file.
     * @see FileMoveAction#execute()
     */
    @SuppressWarnings ("unchecked")
    public FileMoveAction prepareMove(String path) {

        return new FileMoveAction((File<ParentFile<?>>) this, path);
    }

    /**
     * Returns a {@link FileMoveAction} for moving the file to a new local path on the given {@link FileSystem}.
     * If the new file path does not exist, this method creates directories to match it.
     * Those newly created directories have the same right settings as the file to add.
     * Note that the name of the file to add is changed to match the path.<br>
     * <br>
     * In order to actually move the file, the {@link FileMoveAction#execute()} method must be invoked.
     * Note that that method might throw exceptions if the file cannot be moved.
     * Also note that no right checks or anything like that are done by that execution method.
     * If you need such permission checks, use {@link FileMoveAction#isExecutableBy(User)} or {@link FileMoveAction#getMissingRights(User)}.
     *
     * @param path The new local file system path the file should be moved to
     *        The name of the file will be changed to the last entry of this path.
     * @param fileSystem The new file system the file will be moved to.
     *        The given local path relates to this file system.
     * @return A file move action for actually moving the file.
     * @see FileMoveAction#execute()
     */
    @SuppressWarnings ("unchecked")
    public FileMoveAction prepareMove(String path, FileSystem fileSystem) {

        return new FileMoveAction(fileSystem, (File<ParentFile<?>>) this, path);
    }

    /**
     * Returns a {@link FileRemoveAction} for removing the file from the file system it's currently stored on.
     * If the file is a {@link ParentFile}, all child files are also going to be removed.<br>
     * <br>
     * In order to actually remove the file, the {@link FileRemoveAction#execute()} method must be invoked.
     * Note that that method might throw exceptions if the file cannot be removed.
     * Also note that no right checks or anything like that are done by that execution method.
     * If you need such permission checks, use {@link FileRemoveAction#isExecutableBy(User)} or {@link FileRemoveAction#getMissingRights(User)}.
     *
     * @return A file remove action for actually removing the file.
     * @see FileRemoveAction#execute()
     */
    @SuppressWarnings ("unchecked")
    public FileRemoveAction prepareRemove() {

        return new FileRemoveAction((File<ParentFile<?>>) this);
    }

}
