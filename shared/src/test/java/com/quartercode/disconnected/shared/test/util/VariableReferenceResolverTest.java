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

package com.quartercode.disconnected.shared.test.util;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.quartercode.disconnected.shared.util.VariableReferenceResolver;

public class VariableReferenceResolverTest {

    @Test
    public void test() {

        String vClass = "com.quartercode.disconnected.shared.test.util.VariableReferenceResolverTest$TestVariables";

        assertEquals("No variables", "test", VariableReferenceResolver.process("test"));
        assertEquals("No variable brackets", "10$", VariableReferenceResolver.process("10$"));
        assertEquals("Escaped variable without brackets", "10$", VariableReferenceResolver.process("10\\$"));
        assertEquals("Double escaped variable without brackets", "10\\$", VariableReferenceResolver.process("10\\\\$"));
        assertEquals("Escaped variable with brackets", "10${test}", VariableReferenceResolver.process("10\\${test}"));
        assertEquals("Double escaped variable with brackets", "10\\${test}", VariableReferenceResolver.process("10\\\\${test}"));
        assertEquals("No variable dollar sign", "10{test}", VariableReferenceResolver.process("10{test}"));

        assertEquals("No variable brackets (with variable specifier)", "10$C", VariableReferenceResolver.process("10$C"));
        assertEquals("Escaped variable without brackets (with variable specifier)", "10$C", VariableReferenceResolver.process("10\\$C"));
        assertEquals("Double escaped variable without brackets (with variable specifier)", "10\\$C", VariableReferenceResolver.process("10\\\\$C"));
        assertEquals("Escaped variable with brackets (with variable specifier)", "10$C{test}", VariableReferenceResolver.process("10\\$C{test}"));
        assertEquals("Double escaped variable with brackets (with variable specifier)", "10\\$C{test}", VariableReferenceResolver.process("10\\\\$C{test}"));

        assertEquals("Class variable", "var1", VariableReferenceResolver.process("$C{" + vClass + ".VAR_1}"));
        assertEquals("Class variable surrounded by content", "testvar1test", VariableReferenceResolver.process("test$C{" + vClass + ".VAR_1}test"));
        assertEquals("Two class variables", "var1var1", VariableReferenceResolver.process("$C{" + vClass + ".VAR_1}$C{" + vClass + ".VAR_1}"));
        assertEquals("Two class variables surrounded by content", "testvar1var1test", VariableReferenceResolver.process("test$C{" + vClass + ".VAR_1}$C{" + vClass + ".VAR_1}test"));
        assertEquals("Two class variables with content between", "var1testvar1", VariableReferenceResolver.process("$C{" + vClass + ".VAR_1}test$C{" + vClass + ".VAR_1}"));
        assertEquals("Two class variables surrounded by content with content between", "testvar1testvar1test", VariableReferenceResolver.process("test$C{" + vClass + ".VAR_1}test$C{" + vClass + ".VAR_1}test"));

        assertEquals("Different class variables", "var1var2", VariableReferenceResolver.process("$C{" + vClass + ".VAR_1}$C{" + vClass + ".VAR_2}"));
        assertEquals("Different class variables surrounded by content", "testvar1var2test", VariableReferenceResolver.process("test$C{" + vClass + ".VAR_1}$C{" + vClass + ".VAR_2}test"));
        assertEquals("Different class variables with content between", "var1testvar2", VariableReferenceResolver.process("$C{" + vClass + ".VAR_1}test$C{" + vClass + ".VAR_2}"));
        assertEquals("Different class variables surrounded by content with content between", "testvar1testvar2test", VariableReferenceResolver.process("test$C{" + vClass + ".VAR_1}test$C{" + vClass + ".VAR_2}test"));
    }

    protected static class TestVariables {

        public static final String VAR_1 = "var1";
        public static final String VAR_2 = "var2";

    }

}
