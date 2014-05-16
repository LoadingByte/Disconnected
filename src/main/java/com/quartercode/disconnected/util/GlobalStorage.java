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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.Validate;

/**
 * The global storage class just stores all kinds of global values and makes them available through a filtering method.<br>
 * <br>
 * For example, you could put the following values in the category "test":
 * 
 * <pre>
 * GlobalStorage.put(&quot;test&quot;, 10);
 * GlobalStorage.put(&quot;test&quot;, 7.34);
 * GlobalStorage.put(&quot;test&quot;, &quot;something&quot;);
 * </pre>
 * 
 * Afterwards, you could request all stored objects that are an instance of a given type:
 * 
 * <pre>
 * GlobalStorage.get(&quot;test&quot;, Number.class);
 * =&gt; [10, 7.34]
 * </pre>
 */
public class GlobalStorage {

    private static Map<String, List<Object>> storage = new HashMap<>();

    /**
     * Returns all objects that are stored in the given category that are instances of the given type.
     * 
     * @param category The category the requested objects must be stored in.
     * @param type The type all requested objects must be an instance of.
     * @return All objects that are instances of the given type.
     */
    public static <T> List<T> get(String category, Class<T> type) {

        List<T> requestedObjects = new ArrayList<>();

        if (storage.containsKey(category)) {
            for (Object storedObject : storage.get(category)) {
                if (type.isInstance(storedObject)) {
                    requestedObjects.add(type.cast(storedObject));
                }
            }
        }

        return requestedObjects;
    }

    /**
     * Puts the given object in the given category.
     * It can be accessed later on with {@link #get(String, Class)}.<br>
     * Objects can be stored multiple times in the same category. Null objects are not allowed.
     * 
     * @param category The category the new object is stored in.
     * @param object The object to store.
     */
    public static void put(String category, Object object) {

        Validate.notNull(category, "Cannot use null category for storage");
        Validate.notNull(storage, "Cannot put null objects into storage");

        if (!storage.containsKey(category)) {
            storage.put(category, new ArrayList<>());
        }

        storage.get(category).add(object);
    }

    /**
     * Removes all occurrences of the given object from the given category.
     * The removed object will be no longer available through {@link #get(String, Class)}.
     * 
     * @param category The category to remove the object from.
     * @param object The object to remove from the category completely.
     */
    public static void remove(String category, Object object) {

        if (storage.containsKey(category)) {
            List<Object> categoryStorage = storage.get(category);

            while (categoryStorage.contains(object)) {
                categoryStorage.remove(object);
            }

            if (categoryStorage.isEmpty()) {
                storage.remove(category);
            }
        }
    }

    /**
     * Removes all objects that are stored in the given category.
     * 
     * @param category The category that should be cleared.
     */
    public static void clear(String category) {

        storage.remove(category);
    }

}
