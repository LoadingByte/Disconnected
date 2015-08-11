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

package com.quartercode.disconnected.client.registry;

import java.net.URL;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.quartercode.disconnected.shared.util.registry.extra.NamedValue;

/**
 * A data object that represents a TWL theme file.
 * See {@link ClientRegistries#THEMES} for more details.
 */
public class Theme implements NamedValue {

    private final String name;
    private final URL    url;
    private final int    priority;

    /**
     * Creates a new theme data object.
     *
     * @param name The name of the theme.
     *        It only marks the theme and is not used to locate its file.
     * @param url The {@link URL} which locates the theme file that is represented by the new data object.
     * @param priority The loading priority of the theme.
     *        Themes with higher priorities are loaded before themes with lower ones.
     *        If a theme depends on another theme, it must have a lower loading priority.
     */
    public Theme(String name, URL url, int priority) {

        this.name = name;
        this.url = url;
        this.priority = priority;
    }

    /**
     * Returns the name of the theme.
     * It only marks the theme and is not used to locate its file.
     *
     * @return The theme's name.
     */
    @Override
    public String getName() {

        return name;
    }

    /**
     * Returns the {@link URL} which locates the theme file that is represented by the data object.
     *
     * @return The theme's location.
     */
    public URL getURL() {

        return url;
    }

    /**
     * Returns the loading priority of the theme.
     * Themes with higher priorities are loaded before themes with lower ones.
     * If a theme depends on another theme, it must have a lower loading priority.
     *
     * @return The theme's priority.
     */
    public int getPriority() {

        return priority;
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
