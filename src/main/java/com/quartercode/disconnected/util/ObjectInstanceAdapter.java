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

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * The object instance adapter is used for "storing an object".
 * Of course, this doesn't store a whole object, but it stores the class of an object and can create a new instance if required.
 * 
 * @param <C> The type of object you want to store.
 */
public class ObjectInstanceAdapter<C> extends XmlAdapter<Class<? extends C>, C> {

    @Override
    public C unmarshal(Class<? extends C> v) throws InstantiationException, IllegalAccessException {

        return v.newInstance();
    }

    @SuppressWarnings ("unchecked")
    @Override
    public Class<? extends C> marshal(C v) {

        return (Class<? extends C>) v.getClass();
    }

}
