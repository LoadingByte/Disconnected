
package com.quartercode.disconnected.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * The map adapter can marshal a {@link Map} using the style:
 * 
 * <pre>
 * &lt;mapname&gt;
 *     &lt;item key="key"&gt;
 *         &lt;value xmlns:xs="http://www.w3.org/2001/XMLSchema" xsi:type="xs:..."&gt;value&lt;/value&gt
 *     &lt;/item&gt;
 * &lt;/mapname&gt;
 * </pre>
 * 
 * The key of the map must be a string.
 */
public class MapAdapter extends XmlAdapter<MapAdapter.MapEntry[], Map<String, Object>> {

    /**
     * Creates a new map adapter.
     */
    public MapAdapter() {

    }

    @Override
    public MapEntry[] marshal(Map<String, Object> v) {

        List<MapEntry> entries = new ArrayList<MapEntry>();
        for (Entry<String, Object> entry : v.entrySet()) {
            entries.add(new MapEntry(entry.getKey(), entry.getValue()));
        }
        return entries.toArray(new MapEntry[entries.size()]);
    }

    @Override
    public Map<String, Object> unmarshal(MapEntry[] v) {

        Map<String, Object> map = new HashMap<String, Object>();
        for (MapEntry entry : v) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    /**
     * A map entry basically represents an entry of a map.
     * In the xml output, an entry looks like (in the exact form):
     * 
     * <pre>
     * &lt;item key="key"&gt;
     *     &lt;value xmlns:xs="http://www.w3.org/2001/XMLSchema" xsi:type="xs:..."&gt;value&lt;/value&gt
     * &lt;/item&gt;
     * </pre>
     * 
     * The key of the entry must be a string.
     */
    public static class MapEntry {

        @XmlAttribute
        private String key;
        @XmlElement (name = "value")
        private Object value;

        /**
         * Creates a new empty map entry.
         * This is only recommended for direct field access (e.g. for serialization).
         */
        protected MapEntry() {

        }

        /**
         * Creates a new map entry and sets the key and the assigned value.
         * 
         * @param key The key the entry has.
         * @param value The value which is assigned to the key.
         */
        public MapEntry(String key, Object value) {

            this.key = key;
            this.value = value;
        }

        /**
         * Returns the key string the entry has.
         * 
         * @return The key string the entry has.
         */
        public String getKey() {

            return key;
        }

        /**
         * Returns the value which is assigned to the key.
         * 
         * @return The value which is assigned to the key.
         */
        public Object getValue() {

            return value;
        }

    }

}
