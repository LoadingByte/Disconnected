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

package com.quartercode.disconnected.bridge;

import com.quartercode.disconnected.util.DataObjectBase;

/**
 * The add/remove predicate event is internally sent between two {@link Bridge}s for limiting the events that are sent
 * to the ones the other side is really interested in.
 * See the bridge javadoc for more details on the event limit system.<br>
 * <br>
 * <i>This class is internal and not intended to be used outside the bridge package.</i>
 * 
 * @see Bridge
 * @see EventPredicate
 */
public class AddRemovePredicateEvent extends DataObjectBase implements Event {

    private static final long       serialVersionUID = -5336795442194231716L;

    private final EventPredicate<?> predicate;
    private final boolean           add;

    public AddRemovePredicateEvent(EventPredicate<?> predicate, boolean add) {

        this.predicate = predicate;
        this.add = add;
    }

    public EventPredicate<?> getPredicate() {

        return predicate;
    }

    public boolean isAdd() {

        return add;
    }

}
