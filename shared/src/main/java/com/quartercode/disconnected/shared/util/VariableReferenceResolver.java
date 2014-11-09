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

package com.quartercode.disconnected.shared.util;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility that takes an input string and replaces certain "variables".
 * Those variables must have the format <code>$S{...}</code>, where {@code S} specifies the type of the variable and {@code ...} is the content.
 * Currently, the following variable types are available:
 * 
 * <table>
 * <tr>
 * <th>Specifier</th>
 * <th>Meaning of Content</th>
 * </tr>
 * <tr>
 * <td>{@code V}</td>
 * <td>Reference to a mapping from the <code>{String -&gt; Object}</code> map that was passed into the method.</td>
 * </tr>
 * <tr>
 * <td>{@code C}</td>
 * <td>Java constant reference with fully qualified class name; e.g. {@code com.quartercode.disconnected.shared.util.ApplicationInfo.TITLE}.<br>
 * Note that a $-sign should still be used for inner classes; e.g. (with short names) {@code com.qc.dc.SomeClass$SomeInnerClass.VERSION}.</td>
 * </tr>
 * </table>
 * 
 * Variable strings can be escaped by putting a backslash in front of the dollar sign (e.g. <code>\$S{...}</code>).
 * Each first backslash before any dollar sign is removed by the utility.<br>
 * <br>
 * Variables which cannot be resolved are not changed.
 * For example, the string <code>"$V{existing} - $V{unknown}"</code> results in <code>"YES - $V{unknown}"</code> if {@code existing} is mapped
 * to {@code YES} and {@code unknown} is not mapped at all.
 */
public class VariableReferenceResolver {

    private static final Logger  LOGGER  = LoggerFactory.getLogger(VariableReferenceResolver.class);

    /*
     * Pattern explained:
     * (?<!\\) -> No backslash (escape char) in front of the dollar sign
     * \$[VC] -> Dollar sign followed by a specifier
     * \{...\} -> Two curly brackets that contain the content (...)
     * Content: [^\{\}]* -> Any content that does not contain a curly bracket
     */
    private static final Pattern PATTERN = Pattern.compile("(?<!\\\\)\\$[VC]\\{[^\\{\\}]*\\}");

    /**
     * Replaces all variables inside the given string and returns the result.
     * See {@link VariableReferenceResolver} for more information on the format.
     * 
     * @param input The input string that should be processed.
     * @param variables The map used for resolving {@code $V} variables.
     * @return The processed output string.
     */
    public static String process(String input, Map<String, Object> variables) {

        // Make null checks in the more complex code unnecessary by ensuring that "variables" is not null
        if (variables == null) {
            variables = Collections.emptyMap();
        }

        // Process variables
        String output = processVariables(input, variables);

        // Unescape escaped variables
        output = output.replace("\\$", "$");

        return output;
    }

    private static String processVariables(String input, Map<String, Object> variables) {

        Matcher matcher = PATTERN.matcher(input);
        StringBuffer output = new StringBuffer(input.length());

        while (matcher.find()) {
            // Retrieve the variable type specifier (e.g. C) and the content between the two brackets
            char specifier = input.charAt(matcher.start() + 1);
            String content = input.substring(matcher.start() + 3, matcher.end() - 1);

            // Retrieve the value of the variable depending on the specifier
            String value = null;
            switch (specifier) {
                case 'V':
                    Object valueObject = variables.get(content);
                    value = valueObject == null ? null : valueObject.toString();
                    break;
                case 'C':
                    value = resolveStaticFieldReference(content);
                    break;
            }

            if (value != null) {
                // Replace the variable declaration with the retrieved value
                matcher.appendReplacement(output, Matcher.quoteReplacement(value));
            }
        }

        matcher.appendTail(output);
        return output.toString();
    }

    private static String resolveStaticFieldReference(String reference) {

        String className = StringUtils.substringBeforeLast(reference, ".");
        String fieldName = StringUtils.substringAfterLast(reference, ".");

        try {
            Class<?> c = Class.forName(className);
            Object fieldValue = c.getField(fieldName).get(null);
            return String.valueOf(fieldValue);
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("Cannot find static field '{}'; using 'null' for its value instead", reference, e);
            return null;
        }
    }

    private VariableReferenceResolver() {

    }

}
