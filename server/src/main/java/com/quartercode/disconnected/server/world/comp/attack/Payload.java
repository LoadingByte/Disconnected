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

package com.quartercode.disconnected.server.world.comp.attack;

import com.quartercode.disconnected.server.world.WorldFeatureHolder;
import com.quartercode.disconnected.server.world.comp.Vulnerability;
import com.quartercode.disconnected.server.world.comp.os.OperatingSystem;

/**
 * This class represents a payload which can be executed on an {@link OperatingSystem}.
 * In most of the cases, a payload gets executed after {@link Exploit}ing a {@link Vulnerability}.
 * 
 * @see Exploit
 * @see Vulnerability
 */
public class Payload extends WorldFeatureHolder {

}
