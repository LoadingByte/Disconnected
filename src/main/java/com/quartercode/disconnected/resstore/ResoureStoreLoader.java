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

package com.quartercode.disconnected.resstore;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXB;
import com.quartercode.disconnected.sim.comp.ComputerPart;
import com.quartercode.disconnected.sim.comp.Vulnerability;

/**
 * This utility class is for loading stored resources, like computer parts, and converting them to real simulation object.
 */
public class ResoureStoreLoader {

    /**
     * Reads a simulation computer part from an input stream using a stored computer part.
     * The input stream must provide the xml structure.
     * 
     * @param inputStream The input stream to read the xml structure from.
     * @return The loaded simulation computer part.
     * @throws Exception Something goes wrong with the initalization of the new object.
     */
    public static ComputerPart loadComputerPart(InputStream inputStream) throws Exception {

        StoredComputerPart storedComputerPart = JAXB.unmarshal(inputStream, StoredComputerPart.class);

        Constructor<?> constructor = null;
        for (Constructor<?> testConstructor : storedComputerPart.getType().getConstructors()) {
            if (constructor != null) {
                break;
            }

            for (int arg = 2; arg < testConstructor.getParameterTypes().length; arg++) {
                if (!isAllowedAttributeType(testConstructor.getParameterTypes()[arg])) {
                    constructor = null;
                    break;
                } else {
                    constructor = testConstructor;
                }
            }
        }

        if (constructor == null) {
            throw new RuntimeException("Can't find allowed type attribute constructor in \"" + storedComputerPart.getType().getName() + "\"");
        }

        // This list will be filled with vulnerabilities after the creation of the part
        List<Vulnerability> vulnerabilities = new ArrayList<Vulnerability>();

        List<Object> initargs = new ArrayList<Object>();
        initargs.add(storedComputerPart.getName());
        initargs.add(vulnerabilities);

        for (int counter = 0; counter < storedComputerPart.getAttributes().size(); counter++) {
            String value = storedComputerPart.getAttributes().get(counter).getValue();
            Class<?> type = constructor.getParameterTypes()[counter + 2];
            if (isAllowedAttributeType(type)) {
                if (type == byte.class) {
                    initargs.add(Byte.parseByte(value));
                } else if (type == short.class) {
                    initargs.add(Short.parseShort(value));
                } else if (type == int.class) {
                    initargs.add(Integer.parseInt(value));
                } else if (type == long.class) {
                    initargs.add(Long.parseLong(value));
                } else if (type == float.class) {
                    initargs.add(Float.parseFloat(value));
                } else if (type == double.class) {
                    initargs.add(Double.parseDouble(value));
                } else if (type == boolean.class) {
                    initargs.add(Boolean.parseBoolean(value));
                } else if (type == char.class) {
                    initargs.add(value.charAt(0));
                } else if (type == String.class) {
                    initargs.add(value);
                }
            } else {
                throw new RuntimeException("Can't create computer part with non-primitive or non-string attributes");
            }
        }

        ComputerPart computerPart = (ComputerPart) constructor.newInstance(initargs.toArray(new Object[initargs.size()]));
        if (storedComputerPart.getVulnerabilities() != null) {
            for (StoredVulnerability vulnerability : storedComputerPart.getVulnerabilities()) {
                vulnerabilities.add(new Vulnerability(vulnerability.getName(), null, vulnerability.getScripts()));
            }
        }
        return computerPart;
    }

    private static boolean isAllowedAttributeType(Class<?> c) {

        return c.isPrimitive() || c == String.class;
    }

    private ResoureStoreLoader() {

    }

}
