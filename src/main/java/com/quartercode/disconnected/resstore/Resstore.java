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

package com.quartercode.disconnected.resstore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.quartercode.disconnected.sim.comp.ComputerPart;

/**
 * This static class stores different resource store resources which can be loaded using the resstore loader.
 * 
 * @see ResstoreLoader
 * @see ComputerPart
 */
public class Resstore {

    private static List<ComputerPart> computerParts = new ArrayList<ComputerPart>();

    /**
     * Returns a list of all stored computer parts.
     * 
     * @return A list of all stored computer parts.
     */
    public static List<ComputerPart> getComputerParts() {

        return Collections.unmodifiableList(computerParts);
    }

    /**
     * Returns a list of all stored computer parts which are assignable from the given type.
     * 
     * @param type Check if a computer part is assignable from this type.
     * @return A list of all stored computer parts which are assignable from the given type.
     */
    public static List<ComputerPart> getComputerParts(Class<? extends ComputerPart> type) {

        List<ComputerPart> computerParts = new ArrayList<ComputerPart>();

        for (ComputerPart computerPart : Resstore.computerParts) {
            if (type.isAssignableFrom(computerPart.getClass())) {
                computerParts.add(computerPart);
            }
        }

        return computerParts;
    }

    /**
     * Stores a computer part into the resstore.
     * 
     * @param computerPart The computer part to store.
     */
    public static void addComputerPart(ComputerPart computerPart) {

        computerParts.add(computerPart);
    }

    private Resstore() {

    }

}
