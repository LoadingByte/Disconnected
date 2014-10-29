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

package com.quartercode.disconnected.shared.comp.program;

import java.io.Serializable;

/**
 * A marker interface for data objects which identify certain SBP modules/parts which use world processes.
 * Such a world process user details object is assigned to each world process.
 * All events sent by a world process carry that details object.
 * Using that object, the SBP can decide which module/part should receive the incoming event.<br>
 * <br>
 * For example, if an SBP would identify local world-process-using routines with sequential numbers, an implementation of this interface
 * would contain that sequential number in order to identify the routines.
 * Using that, the SBP can then pass incoming world process events to the correct routine.
 * 
 * @see SBPWorldProcessUserId
 */
public interface SBPWorldProcessUserDetails extends Serializable {

}
