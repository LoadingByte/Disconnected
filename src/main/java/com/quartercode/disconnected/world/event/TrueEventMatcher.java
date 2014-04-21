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
 * The {@link #matches(Event)} method of every true event matcher just always returns {@code true}.
 * For example, it could be used to make the {@link QueueEventListener#NEXT_EVENT} method return the last received {@link Event}.
 * 
 * @see Event
 * @see QueueEventListener#NEXT_EVENT
 */
public class TrueEventMatcher implements EventMatcher {

    /**
     * The singleton instance of the true event matcher that just always returns {@code true} in the {@link #matches(Event)} method.
     */
    public static final TrueEventMatcher INSTANCE = new TrueEventMatcher();

    private TrueEventMatcher() {

    }

    @Override
    public boolean matches(Event event) {

        return true;
    }

}
