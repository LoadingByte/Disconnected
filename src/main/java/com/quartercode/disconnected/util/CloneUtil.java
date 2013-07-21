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

package com.quartercode.disconnected.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This utility class clones creates deep clones of objects using serialization.
 * For using this, you need to make all your classes for cloning (and, of course, also the subclasses) serializable.
 */
public class CloneUtil {

    /**
     * Creates deep clones of objects using serialization.
     * For using this, you need to make all your classes for cloning (and, of course, also the subclasses) serializable.
     * 
     * @param object The input object to clone.
     * @return The created clone of the input object.
     */
    public static Object clone(Object object) {

        try {
            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(byteOutput);
            output.writeObject(object);
            ByteArrayInputStream byteInput = new ByteArrayInputStream(byteOutput.toByteArray());
            ObjectInputStream input = new ObjectInputStream(byteInput);
            return input.readObject();
        }
        catch (Exception e) {
            throw new RuntimeException("Exception while cloning deep using serialization", e);
        }
    }

    private CloneUtil() {

    }

}
