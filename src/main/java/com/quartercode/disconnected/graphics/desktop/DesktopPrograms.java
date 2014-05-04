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

package com.quartercode.disconnected.graphics.desktop;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A storage class that holds all available {@link DesktopProgramDescriptor}s.
 * All descriptors that should be available must be registered here.
 * 
 * @see DesktopProgramDescriptor
 */
public class DesktopPrograms {

    private static Set<DesktopProgramDescriptor> descriptors = new HashSet<>();

    /**
     * Retunrs all the {@link DesktopProgramDescriptor}s that are registered so far.
     * The returned descriptors are available for usage in the launch menu.
     * 
     * @return The available {@link DesktopProgramDescriptor}s.
     */
    public static Set<DesktopProgramDescriptor> getDescriptors() {

        return Collections.unmodifiableSet(descriptors);
    }

    /**
     * Registers a {@link DesktopProgramDescriptor} to the list.
     * Such descriptors are available for usage in the launch menu.
     * 
     * @param descriptor The {@link DesktopProgramDescriptor} to make available.
     */
    public static void addDescriptor(DesktopProgramDescriptor descriptor) {

        descriptors.add(descriptor);
    }

    /**
     * Unregisters a {@link DesktopProgramDescriptor} from the list.
     * Such descriptors are available for usage in the launch menu.
     * 
     * @param descriptor The {@link DesktopProgramDescriptor} to make no longer available.
     */
    public static void removeDescriptor(DesktopProgramDescriptor descriptor) {

        descriptors.remove(descriptor);
    }

}
