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

package com.quartercode.disconnected.server.world.comp.file;

import com.quartercode.classmod.extra.conv.CFeatureHolder;

/**
 * A marker interface for a {@link CFeatureHolder} that holds one or more {@link FileSystem}s.
 * Note that the {@link FileSystem} {@link FileSystem#getParent() parent} can only be such a holder.
 * Therefore, the main holder of a file system (e.g. a hard drive) must implement this interface.
 */
public interface FileSystemHolder extends CFeatureHolder {

}
