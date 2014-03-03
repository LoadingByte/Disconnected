
package com.quartercode.disconnected.world;

import com.quartercode.disconnected.mocl.base.FeatureHolder;
import com.quartercode.disconnected.mocl.extra.FunctionDefinition;
import com.quartercode.disconnected.mocl.util.FunctionDefinitionFactory;

/**
 * <p>
 * The string representable interface defines methods for classes which can be converted to and created from strings. Such classes are called "representable by a string" and only they can be used as
 * program parameters.
 * </p>
 * 
 * <p>
 * The {@link #FROM_STRING} and {@link #TO_STRING} methods are required to function as follows:
 * 
 * <pre>
 * object1 -> TO_STRING -> string -> FROM_STRING -> object2
 * object1.equals(object2) == true
 * </pre>
 * 
 * An object converted into a string and then converted back into an object must be equal to the original object.
 * </p>
 */
public interface StringRepresentable extends FeatureHolder {

    /**
     * Returns a string which is representating the original object in its current state.
     * The returned string must be convertable back into an object using {@link #FROM_STRING} which must be equal to the original object.
     */
    public static final FunctionDefinition<String> TO_STRING   = FunctionDefinitionFactory.create("toString");

    /**
     * Changes the state of the object so its equal to the state described by the input string.
     * The new state of the object must be equal to the state of the object that created the string with {@link #TO_STRING}.
     * 
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Type</th>
     * <th>Parameter</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link String}</td>
     * <td>string</td>
     * <td>The input string which describes the new state of the object.</td>
     * </tr>
     * </table>
     */
    public static final FunctionDefinition<Void>   FROM_STRING = FunctionDefinitionFactory.create("fromString", String.class);

}
