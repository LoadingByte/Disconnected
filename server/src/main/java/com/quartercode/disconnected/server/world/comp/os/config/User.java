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

package com.quartercode.disconnected.server.world.comp.os.config;

import java.security.acl.Group;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.comp.config.ConfigEntry;
import com.quartercode.disconnected.server.world.comp.config.UnknownColumnException;
import com.quartercode.jtimber.api.node.wrapper.SubstituteWithWrapper;
import com.quartercode.jtimber.api.node.wrapper.collection.CollectionWrapper;

/**
 * A user represents a system user (basically someone who can use a system).
 * The user object represents a user and his name, his (hashed) password and the groups he is part of.
 * However, everything related to rights (apart from the {@link #isSuperuser() superuser} property) is done through the file system.<br>
 * <br>
 * The user object can be used as a {@link ConfigEntry}.
 *
 * @see Group
 */
// "primaryGroup" must be unmarshalled after "groups"
@XmlType (propOrder = { "name", "password", "groups", "primaryGroup" })
public class User extends ConfigEntry<User> {

    /**
     * This is the name of the superuser on a system.
     * The superuser of a system can do everything without having the rights applied for doing it.
     * You can check whether a user is the superuser by using {@link #isSuperuser()}.
     */
    public static final String       SUPERUSER_NAME = "root";

    @XmlAttribute
    private String                   name;
    @XmlAttribute
    private String                   password;
    @XmlAttribute
    @XmlList
    @SubstituteWithWrapper (CollectionWrapper.class)
    private final Collection<String> groups         = new LinkedHashSet<>();
    @XmlAttribute
    private String                   primaryGroup;

    // JAXB constructor
    protected User() {

        super(Arrays.asList("name", "password", "groups", "primaryGroup"));
    }

    /**
     * Creates a new user.
     *
     * @param name The {@link #getName() name} of the new user.
     *        It cannot be {@code null}.
     */
    public User(String name) {

        this();

        setName(name);
    }

    /**
     * Returns the name of the user.
     * The name is used to recognize a user on the OS level.
     *
     * @return The name of the user.
     */
    public String getName() {

        return name;
    }

    /**
     * Sets the name of the user.
     * The name is used to recognize a user on the OS level.
     * Note that the name of the {@link #isSuperuser() superuser} cannot be changed; instead, an exception will be thrown if such a change is attempted.
     *
     * @param name The new name of the user.
     *        It cannot be {@code null}.
     */
    public void setName(String name) {

        Validate.notNull(name, "Cannot use null as user name");
        Validate.validState(!isSuperuser(), "Cannot change the name of the superuser");

        this.name = name;
    }

    /**
     * Returns the hashed password of the user.
     * It is hashed using the SHA-256 algorithm and can be used to login/authenticate as the user.
     *
     * @return The hashed password of the user.
     */
    public String getPassword() {

        return password;
    }

    /**
     * Sets the hashed password of the user.
     * It <b>must be hashed using the SHA-256 algorithm before it is provided to this method</b> and can be used to login/authenticate as the user.
     *
     * @param password The new already hashed password of the user.
     */
    public void setPassword(String password) {

        Validate.notNull(name, "Cannot use null as hashed user password");
        this.password = password;
    }

    /**
     * Returns all {@link Group}s the user is a member of.
     * Such groups are used to set rights for multiple users.
     *
     * @return The groups the user is a member of.
     */
    public Collection<String> getGroups() {

        return Collections.unmodifiableCollection(groups);
    }

    /**
     * Makes the user member of the given {@link Group}.
     * Such groups are used to set rights for multiple users.
     * Note that the {@link #isSuperuser() superuser} cannot be a member of any group.
     *
     * @param group The group the user should be a member of.
     */
    public void addGroup(String group) {

        Validate.notNull(name, "Cannot make a user member of a null group");
        Validate.validState(!isSuperuser(), "Cannot make the superuser member of a group");

        groups.add(group);
    }

    /**
     * Removes the membership of the user from the given {@link Group}.
     * Such groups are used to set rights for multiple users.
     * Note that the user cannot be directly removed from his {@link #getPrimaryGroup() primary group} using this method.
     * Instead, the {@link #setPrimaryGroup(String)} method must be called with another group (or {@code null)} before this method can be invoked.
     *
     * @param group The group the user should no longer be a member of.
     */
    public void removeGroup(String group) {

        Validate.validState(!isSuperuser(), "Cannot remove the membership of the superuser from any group (the superuser cannot be member of any group)");
        Validate.validState(!group.equals(getPrimaryGroup()), "Cannot remove a user from his primary group");

        groups.remove(group);
    }

    /**
     * Returns the primary {@link Group} of the user.
     * The primary group is the group which is automatically applied to new files created by the user.
     * Note that it must be part of the {@link #getGroups() regular group list}.
     * Also note that it may be {@code null}, in which case no group is applied to new files.
     *
     * @return The primary group of the user. May be {@code null}.
     */
    public String getPrimaryGroup() {

        return primaryGroup;
    }

    /**
     * Sets the primary {@link Group} of the user.
     * The primary group is the group which is automatically applied to new files created by the user.
     * Note that it must be part of the {@link #getGroups() regular group list}.
     * Also note that it may be {@code null}, in which case no group is applied to new files.
     *
     * @param primaryGroup The new primary group of the user.
     *        The user must already be a member of this group. May be {@code null}.
     */
    public void setPrimaryGroup(String primaryGroup) {

        Validate.notNull(name, "Cannot use null as the user primary group");
        Validate.isTrue(groups.contains(primaryGroup), "Cannot set user primary group to a group the user isn't a member of");

        this.primaryGroup = primaryGroup;
    }

    /**
     * Returns {@code true} if the user is the superuser.
     * The superuser of a system can do everything without having the rights applied for doing it.
     * Note that the superuser must have the {@link #SUPERUSER_NAME} as name.
     *
     * @return Whether the user is the superuser.
     */
    public boolean isSuperuser() {

        return name != null && name.equals(SUPERUSER_NAME);
    }

    @Override
    public String getColumnValue(String columnName) {

        switch (columnName) {
            case "name":
                return getName();
            case "password":
                return getPassword();
            case "groups":
                return StringUtils.join(groups, ',');
            case "primaryGroup":
                return primaryGroup;
            default:
                throw new UnknownColumnException(columnName);
        }
    }

    @Override
    public void setColumnValue(String columnName, String columnValue) {

        switch (columnName) {
            case "name":
                setName(columnValue);
                break;
            case "password":
                setPassword(columnValue);
                break;
            case "groups":
                groups.clear();
                for (String group : StringUtils.split(columnValue, ',')) {
                    addGroup(group);
                }
                break;
            case "primaryGroup":
                setPrimaryGroup(columnValue);
                break;
            default:
                throw new UnknownColumnException(columnName);
        }
    }

}
