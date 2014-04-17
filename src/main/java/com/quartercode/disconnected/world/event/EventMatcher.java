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

package com.quartercode.disconnected.world.event;

/**
 * Event matchers are used to search for {@link Event}s of a specific type.
 * For example, matchers are used to retrieve the next {@link Event} a {@link QueueEventListener} wants to handle in {@link QueueEventListener#NEXT_EVENT}.
 * 
 * @see Event
 */
public interface EventMatcher {

    /**
     * Checks if the given {@link Event} matches the criteria that are defined by the event matcher.
     * 
     * @param event The {@link Event} to check.
     * @return True if the given {@link Event} matches the defined criteria, false if not.
     */
    public boolean matches(Event event);

}
