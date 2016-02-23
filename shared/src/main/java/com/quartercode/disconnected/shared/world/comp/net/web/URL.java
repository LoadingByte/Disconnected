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

package com.quartercode.disconnected.shared.world.comp.net.web;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class URL {

    public static final int    DEFAULT_PORT   = 80;

    public static final String PORT_SEPARATOR = ":";
    public static final String PATH_SEPARATOR = "/";

    private final String       domain;
    private final int          port;
    private final String[]     path;

    public URL(String url) {

        // Resolve the host part (e.g.: quartercode.com:8080/test1/test2 -> quartercode.com:8080)
        String host = StringUtils.substringBefore(url, PATH_SEPARATOR);

        // Resolve the domain (e.g.: quartercode.com:8080/test1/test2 -> quartercode.com)
        domain = StringUtils.substringBefore(host, PORT_SEPARATOR);
        Validate.notBlank(domain, "URL domain cannot be blank");

        // Resolve the port (if any) (e.g.: quartercode.com:8080/test1/test2 -> 8080); the default port is #DEFAULT_PORT
        if (host.contains(PORT_SEPARATOR)) {
            String portString = StringUtils.substringAfter(host, PORT_SEPARATOR);
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("URL port (" + portString + ") is not a valid integer");
            }

            Validate.isTrue(port >= 1 && port <= 65535, "URL port (%d) must be in range 1 <= port <= 65535 (e.g. 80)", port);
        } else {
            port = DEFAULT_PORT;
        }

        // Resolve the path (e.g.: quartercode.com:8080/test1/test2 -> test1/test2)
        path = StringUtils.substringAfter(url, PATH_SEPARATOR).split(PATH_SEPARATOR);
    }

    public URL(String domain, int port, String[] path) {

        Validate.notBlank(domain, "URL domain cannot be blank");
        Validate.isTrue(port >= 1 && port <= 65535, "URL port (%d) must be in range 1 <= port <= 65535 (e.g. 80)", port);
        Validate.notNull(path, "URL path cannot be null");

        this.domain = domain;
        this.port = port;
        this.path = path.clone();
    }

    public String getDomain() {

        return domain;
    }

    public int getPort() {

        return port;
    }

    public String getHost() {

        return new StringBuilder().append(domain).append(PORT_SEPARATOR).append(port).toString();
    }

    public String[] getPath() {

        return path.clone();
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

        return new StringBuilder().append(getHost()).append(PATH_SEPARATOR).append(StringUtils.join(path, PATH_SEPARATOR)).toString();
    }

}
