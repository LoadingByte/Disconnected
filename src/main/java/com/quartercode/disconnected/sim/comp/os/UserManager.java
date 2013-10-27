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

package com.quartercode.disconnected.sim.comp.os;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import com.quartercode.disconnected.util.InfoString;

/**
 * The user manager is a subclass the {@link OperatingSystem} uses for holding and modifing users and groups.
 * This class only gets used by the {@link OperatingSystem}.
 * 
 * @see User
 * @see Group
 * @see OperatingSystem
 */
public class UserManager implements InfoString {

    private OperatingSystem host;

    @XmlElement (name = "user")
    private List<User>      users;
    @XmlElement (name = "group")
    private List<Group>     groups;

    /**
     * Creates a new empty user manager.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected UserManager() {

    }

    /**
     * Creates a new user manager and sets the host the manager is running for.
     * 
     * @param host The {@link OperatingSystem} this user manager is running for.
     */
    public UserManager(OperatingSystem host) {

        this.host = host;

        users = new ArrayList<User>();
        groups = new ArrayList<Group>();
    }

    /**
     * Returns the {@link OperatingSystem} this file system manager is running for.
     * 
     * @return The {@link OperatingSystem} this file system manager is running for.
     */
    public OperatingSystem getHost() {

        return host;
    }

    /**
     * Returns the users which are registered on the system.
     * Users are used for managing rights for individual persons.
     * 
     * @return The users which are registered on the system.
     */
    public List<User> getUsers() {

        return Collections.unmodifiableList(users);
    }

    /**
     * Registers a new user to the system.
     * Users are used for managing rights for individual persons.
     * 
     * @param user The new user to register to the system.
     */
    public void addUser(User user) {

        users.add(user);
    }

    /**
     * Unregisters a user from the system.
     * Users are used for managing rights for individual persons.
     * 
     * @param user The user to unregister from the system.
     */
    public void removeUser(User user) {

        users.remove(user);
    }

    /**
     * Returns the groups which are registered on the system.
     * Groups are used for defining rights for multiple users.
     * 
     * @return The groups which are registered on the system.
     */
    public List<Group> getGroups() {

        return Collections.unmodifiableList(groups);
    }

    /**
     * Registers a new group to the system.
     * Groups are used for defining rights for multiple users.
     * 
     * @param group The new group to register to the system.
     */
    public void addGroup(Group group) {

        groups.add(group);
    }

    /**
     * Unregisters a group from the system.
     * Groups are used for defining rights for multiple users.
     * 
     * @param group The group to unregister from the system.
     */
    public void removeGroup(Group group) {

        groups.remove(group);
    }

    protected void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {

        host = (OperatingSystem) parent;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + (groups == null ? 0 : groups.hashCode());
        result = prime * result + (users == null ? 0 : users.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UserManager other = (UserManager) obj;
        if (groups == null) {
            if (other.groups != null) {
                return false;
            }
        } else if (!groups.equals(other.groups)) {
            return false;
        }
        if (users == null) {
            if (other.users != null) {
                return false;
            }
        } else if (!users.equals(other.users)) {
            return false;
        }
        return true;
    }

    @Override
    public String toInfoString() {

        return users.size() + " users, " + groups.size() + " groups";
    }

    @Override
    public String toString() {

        return getClass().getName() + " [" + toInfoString() + "]";
    }

}
