
package com.quartercode.disconnected.util;

/**
 * The primitive util class contains methods to prevent that wrapper objects for primitives are null.
 * Null objects are replaced with a valid null value, like 0 for {@link Number}s or false for {@link Boolean}s.
 * The class also provides such a method for the non-primitive type {@link String}.
 */
public class PrimitiveUtil {

    /**
     * Returns 0 if the input {@link Byte} wrapper is null, otherwise the method returns the input again.
     * 
     * @param value The wrapped {@link Byte} object to prevent from null.
     * @return 0 or the input {@link Byte}, depending on the value.
     */
    public static Byte preventNull(Byte value) {

        return value == null ? 0 : value;
    }

    /**
     * Returns 0 if the input {@link Short} wrapper is null, otherwise the method returns the input again.
     * 
     * @param value The wrapped {@link Short} object to prevent from null.
     * @return 0 or the input {@link Short}, depending on the value.
     */
    public static Short preventNull(Short value) {

        return value == null ? 0 : value;
    }

    /**
     * Returns 0 if the input {@link Integer} wrapper is null, otherwise the method returns the input again.
     * 
     * @param value The wrapped {@link Integer} object to prevent from null.
     * @return 0 or the input {@link Integer}, depending on the value.
     */
    public static Integer preventNull(Integer value) {

        return value == null ? 0 : value;
    }

    /**
     * Returns 0 if the input {@link Long} wrapper is null, otherwise the method returns the input again.
     * 
     * @param value The wrapped {@link Long} object to prevent from null.
     * @return 0 or the input {@link Long}, depending on the value.
     */
    public static Long preventNull(Long value) {

        return value == null ? 0 : value;
    }

    /**
     * Returns false if the input {@link Boolean} wrapper is null, otherwise the method returns the input again.
     * 
     * @param value The wrapped {@link Boolean} object to prevent from null.
     * @return false or the input {@link Boolean}, depending on the value.
     */
    public static Boolean preventNull(Boolean value) {

        return value == null ? false : value;
    }

    /**
     * Returns an empty {@link String} if the input {@link String} is null, otherwise the method returns the input again.
     * 
     * @param value The {@link String} to prevent from null.
     * @return An empty {@link String} or the input {@link String}, depending on the value.
     */
    public static String preventNull(String value) {

        return value == null ? "" : value;
    }

    private PrimitiveUtil() {

    }

}
