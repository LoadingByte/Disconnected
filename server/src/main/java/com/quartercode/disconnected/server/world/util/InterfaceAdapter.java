
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
