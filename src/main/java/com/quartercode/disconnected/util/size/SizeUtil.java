
package com.quartercode.disconnected.util.size;

/**
 * This utility calculates the size of certain objects in bytes (of course, it's a fictitious size).
 * 
 * @see SizeObject
 */
public class SizeUtil {

    /**
     * Returns true if the size of the given object can be derived using this utility.
     * 
     * @param object The object to check.
     * @return True if the size of the given object can be derived using this utility.
     */
    public static boolean accept(Object object) {

        return object == null || object instanceof SizeObject || object instanceof String || object instanceof Boolean || object instanceof Number;
    }

    /**
     * Returns the size of an object in bytes (of course, it's a fictitious size).
     * If the object is a {@link SizeObject}. the size can be derived using {@link SizeObject#getSize()}.
     * In the case of a string, the size is equally to the length * 256. A boolean always has a size of 1, a number needs a byte for every digit.
     * 
     * @param object The object to calculate the size of.
     * @return The size of the object in bytes (of course, it's a fictitious size).
     * @throws IllegalArgumentException The given object isn't a {@link SizeObject}, string, boolean or number.
     */
    public static long getSize(Object object) {

        if (object == null) {
            return 0;
        } else if (object instanceof SizeObject) {
            return ((SizeObject) object).getSize();
        } else if (object instanceof Boolean) {
            return 1;
        } else if (object instanceof String) {
            return object.toString().length() * 256;
        } else if (object instanceof Number) {
            return object.toString().length();
        } else {
            throw new IllegalArgumentException("Type " + object.getClass().getName() + " isn't a SizeObject, string, boolean or number");
        }
    }

    private SizeUtil() {

    }

}
