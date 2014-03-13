
package com.quartercode.disconnected.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * The null preventer utilitly class contains methods to prevent that objects are null by returning "empty" objects.
 * Null objects are replaced with valid null values, like 0 for {@link Number}s, false for {@link Boolean}s or empty collections for {@link Collection}s.
 */
public class NullPreventer {

    /**
     * Returns 0 if the input {@link Byte} wrapper is null, otherwise the method returns the input again.
     * 
     * @param value The wrapped {@link Byte} object to prevent from null.
     * @return 0 or the input {@link Byte}, depending on the value.
     */
    public static Byte prevent(Byte value) {

        return value == null ? 0 : value;
    }

    /**
     * Returns 0 if the input {@link Short} wrapper is null, otherwise the method returns the input again.
     * 
     * @param value The wrapped {@link Short} object to prevent from null.
     * @return 0 or the input {@link Short}, depending on the value.
     */
    public static Short prevent(Short value) {

        return value == null ? 0 : value;
    }

    /**
     * Returns 0 if the input {@link Integer} wrapper is null, otherwise the method returns the input again.
     * 
     * @param value The wrapped {@link Integer} object to prevent from null.
     * @return 0 or the input {@link Integer}, depending on the value.
     */
    public static Integer prevent(Integer value) {

        return value == null ? 0 : value;
    }

    /**
     * Returns 0 if the input {@link Long} wrapper is null, otherwise the method returns the input again.
     * 
     * @param value The wrapped {@link Long} object to prevent from null.
     * @return 0 or the input {@link Long}, depending on the value.
     */
    public static Long prevent(Long value) {

        return value == null ? 0 : value;
    }

    /**
     * Returns false if the input {@link Boolean} wrapper is null, otherwise the method returns the input again.
     * 
     * @param value The wrapped {@link Boolean} object to prevent from null.
     * @return false or the input {@link Boolean}, depending on the value.
     */
    public static Boolean prevent(Boolean value) {

        return value == null ? false : value;
    }

    /**
     * Returns an empty {@link String} if the input {@link String} is null, otherwise the method returns the input again.
     * 
     * @param value The {@link String} to prevent from null.
     * @return An empty {@link String} or the input {@link String}, depending on the value.
     */
    public static String prevent(String value) {

        return value == null ? "" : value;
    }

    /**
     * Returns an empty {@link ArrayList} if the input {@link List} is null, otherwise the method returns the input again.
     * 
     * @param value The {@link List} to prevent from null.
     * @return An empty {@link ArrayList} or the input {@link List}, depending on the value.
     */
    public static <E> List<E> prevent(List<E> value) {

        return value == null ? new ArrayList<E>() : value;
    }

    /**
     * Returns an empty {@link HashSet} if the input {@link Set} is null, otherwise the method returns the input again.
     * 
     * @param value The {@link Set} to prevent from null.
     * @return An empty {@link HashSet} or the input {@link Set}, depending on the value.
     */
    public static <E> Set<E> prevent(Set<E> value) {

        return value == null ? new HashSet<E>() : value;
    }

    /**
     * Returns an empty {@link LinkedList} if the input {@link Queue} is null, otherwise the method returns the input again.
     * 
     * @param value The {@link Queue} to prevent from null.
     * @return An empty {@link LinkedList} or the input {@link Queue}, depending on the value.
     */
    public static <E> Queue<E> prevent(Queue<E> value) {

        return value == null ? new LinkedList<E>() : value;
    }

    /**
     * Returns an empty {@link HashMap} if the input {@link Map} is null, otherwise the method returns the input again.
     * 
     * @param value The {@link Map} to prevent from null.
     * @return An empty {@link HashMap} or the input {@link Map}, depending on the value.
     */
    public static <K, V> Map<K, V> prevent(Map<K, V> value) {

        return value == null ? new HashMap<K, V>() : value;
    }

    private NullPreventer() {

    }

}
