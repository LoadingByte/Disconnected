/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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

package com.quartercode.disconnected.server.world.util;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * An {@link XmlAdapter} which can be put on interfaces in order to add JAXB support to them.<br>
 * <br>
 * For example, imagine an interface {@code Executor} and several implementations of that interface.
 * Then you reference an {@code Executor} implementation like this:
 *
 * <pre>
 * &commat;XmlElement
 * private Executor executor;
 * </pre>
 *
 * Of course, this code would immediately throw an exception because JAXB doesn't support interfaces.
 * You can avoid that problem by putting this XML adapter onto the {@code Executor} interface:
 *
 * <pre>
 * &commat;XmlJavaTypeAdapter (InterfaceAdapter.class)
 * public interface Executor {
 *     ...
 * }
 * </pre>
 *
 * From now on, JAXB will handle the interface like an abstract class.
 */
public class InterfaceAdapter extends XmlAdapter<Object, Object> {

    @Override
    public Object marshal(Object v) {

        return v;
    }

    @Override
    public Object unmarshal(Object v) {

        return v;
    }

}
