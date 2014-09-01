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

package com.quartercode.disconnected.server.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A small utility class which provides some functions for hashing strings into other strings.
 * That way, users of the functions do not need to convert the strings to byte arrays.
 */
public class HashUtil {

    /**
     * Hashes the given string using the {@code SHA-256} algorithm and returns the result as a string.
     * 
     * @param value The string which should be hashed.
     * @return The resulting hash as a string.
     */
    public static String sha256(String value) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return new String(digest.digest(value.getBytes("UTF-8")), "UTF-8");
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException("Cannot hash with algorithm 'SHA-256': The platform doesn't support it", e);
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException("Cannot encode string in UTF-8: The platform doesn't support it", e);
        }
    }

    private HashUtil() {

    }

}
