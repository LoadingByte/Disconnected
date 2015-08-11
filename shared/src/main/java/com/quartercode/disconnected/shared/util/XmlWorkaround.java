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

package com.quartercode.disconnected.shared.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks JAXB properties which are involved in most workarounds for several JAXB bugs.
 * Such workarounds typically use fields or methods which are only visible to JAXB via JAXB annotations.
 * However, the property type of those fields or methods is specifically optimized for JAXB and doesn't reflect the real property.
 * For example, the following code shows an example workaround (note that the workaround is not necessary at all in this case, and an XML adapter could be used instead;
 * however, the code should only provide a <b>simple</b> example):
 *
 * <pre>
 * private Map&lt;String, List&lt;Integer&gt;&gt; map = new HashMap<>();
 *
 * ...
 *
 * &commat;XmlElement (name = "map")
 * private AdaptedMap getMapForJAXB() {
 *     ... // adapter magic for marshalling
 * }
 *
 *
 * private void setMapForJAXB(AdaptedMap map) {
 *     ... // adapter magic for unmarshalling
 * }
 * </pre>
 *
 * In this case, a script which searches for all persistent properties of a class wouldn't be able to locate the original {@code map} field as the correct property.
 * Instead, it would use the {@code AdaptedMap} supplied by {@code getMapForJAXB()}.
 * However, that class probably hasn't got any hashCode()/equals()/toString() methods or even applies some internal conversions which can not be understood from the outside.
 * Therefore, <code>&commat;XmlWorkaround</code> annotations can be used to tell such scripts which properties are real and which ones are just "fake".<br>
 * <br>
 * In the example case used above, the annotation would be applied like this:
 *
 * <pre>
 * <b>&commat;XmlWorkaround (WorkaroundPropertyType.REAL_PROPERTY)</b>
 * private Map&lt;String, List&lt;Integer&gt;&gt; map = new HashMap<>();
 *
 * ...
 *
 * &commat;XmlElement (name = "map")
 * <b>&commat;XmlWorkaround (WorkaroundPropertyType.WORKAROUND_PROPERTY)</b>
 * private AdaptedMap getMapForJAXB() {
 *     ... // adapter magic for marshalling
 * }
 *
 *
 * private void setMapForJAXB(AdaptedMap map) {
 *     ... // adapter magic for unmarshalling
 * }
 * </pre>
 */
@Target ({ ElementType.FIELD, ElementType.METHOD })
@Retention (RetentionPolicy.RUNTIME)
@Inherited
public @interface XmlWorkaround {

    WorkaroundPropertyType value ();

    public static enum WorkaroundPropertyType {

        REAL_PROPERTY, WORKAROUND_PROPERTY;

    }

}
