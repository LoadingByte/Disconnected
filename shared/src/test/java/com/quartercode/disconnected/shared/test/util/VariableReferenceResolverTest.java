/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://quartercode.com/>
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
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import com.quartercode.disconnected.shared.util.VariableReferenceResolver;

public class VariableReferenceResolverTest {

    @Test
    public void testBasic() {

        assertEquals("No variables", "test", VariableReferenceResolver.process("test", null));
        assertEquals("No variable brackets", "10$", VariableReferenceResolver.process("10$", null));
        assertEquals("Escaped variable without brackets", "10$", VariableReferenceResolver.process("10\\$", null));
        assertEquals("Double escaped variable without brackets", "10\\$", VariableReferenceResolver.process("10\\\\$", null));
        assertEquals("Escaped variable with brackets", "10${test}", VariableReferenceResolver.process("10\\${test}", null));
        assertEquals("Double escaped variable with brackets", "10\\${test}", VariableReferenceResolver.process("10\\\\${test}", null));
        assertEquals("No variable dollar sign", "10{test}", VariableReferenceResolver.process("10{test}", null));

        assertEquals("No variable brackets (with variable specifier)", "10$V", VariableReferenceResolver.process("10$V", null));
        assertEquals("Escaped variable without brackets (with variable specifier)", "10$V", VariableReferenceResolver.process("10\\$V", null));
        assertEquals("Double escaped variable without brackets (with variable specifier)", "10\\$V", VariableReferenceResolver.process("10\\\\$V", null));
        assertEquals("Escaped variable with brackets (with variable specifier)", "10$V{test}", VariableReferenceResolver.process("10\\$V{test}", null));
        assertEquals("Double escaped variable with brackets (with variable specifier)", "10\\$V{test}", VariableReferenceResolver.process("10\\\\$V{test}", null));

        assertEquals("Multiple variables without brackets (with variable specifier)", "10$V10$V", VariableReferenceResolver.process("10$V10$V", null));
        assertEquals("Multiple escaped variables without brackets (with variable specifier)", "10$V10$V", VariableReferenceResolver.process("10\\$V10\\$V", null));
        assertEquals("Multiple double escaped variables without brackets (with variable specifier)", "10\\$V10\\$V", VariableReferenceResolver.process("10\\\\$V10\\\\$V", null));
        assertEquals("Multiple escaped variables with brackets (with variable specifier)", "10$V{test}10$V{test}", VariableReferenceResolver.process("10\\$V{test}10\\$V{test}", null));
        assertEquals("Multiple double escaped variable with brackets (with variable specifier)", "10\\$V{test}10\\$V{test}", VariableReferenceResolver.process("10\\\\$V{test}10\\\\$V{test}", null));

        assertEquals("Unknown variable specifier", "$A{test}", VariableReferenceResolver.process("$A{test}", null));
        assertEquals("Unknown long variable specifier", "$AB{test}", VariableReferenceResolver.process("$AB{test}", null));
        assertEquals("Unknown longer variable specifier", "$ABC{test}", VariableReferenceResolver.process("$ABC{test}", null));
    }

    @Test
    public void testVariables() {

        Map<String, Object> vars = new HashMap<>();
        vars.put("var1", 1234.3);
        vars.put("var2", true);
        vars.put("var3", "test");

        assertEquals("Number variable", "1234.3", VariableReferenceResolver.process("$V{var1}", vars));
        assertEquals("Boolean variable", "true", VariableReferenceResolver.process("$V{var2}", vars));
        assertEquals("String/object variable", "test", VariableReferenceResolver.process("$V{var3}", vars));

        assertEquals("String variable surrounded by content", "fronttestback", VariableReferenceResolver.process("front$V{var3}back", vars));
        assertEquals("Two string variables", "testtest", VariableReferenceResolver.process("$V{var3}$V{var3}", vars));
        assertEquals("Two string variables surrounded by content", "fronttesttestback", VariableReferenceResolver.process("front$V{var3}$V{var3}back", vars));
        assertEquals("Two string variables with content between", "testmiddletest", VariableReferenceResolver.process("$V{var3}middle$V{var3}", vars));
        assertEquals("Two string variables surrounded by content with content between", "fronttestmiddletestback", VariableReferenceResolver.process("front$V{var3}middle$V{var3}back", vars));

        assertEquals("Different normal variables", "truetest", VariableReferenceResolver.process("$V{var2}$V{var3}", vars));
        assertEquals("Different normal variables surrounded by content", "fronttruetestback", VariableReferenceResolver.process("front$V{var2}$V{var3}back", vars));
        assertEquals("Different normal variables with content between", "truemiddletest", VariableReferenceResolver.process("$V{var2}middle$V{var3}", vars));
        assertEquals("Different normal variables surrounded by content with content between", "fronttruemiddletestback", VariableReferenceResolver.process("front$V{var2}middle$V{var3}back", vars));

        assertEquals("Unknown normal variable", "$V{unknown}", VariableReferenceResolver.process("$V{unknown}", null));
        assertEquals("Multiple unknown normal variables", "$V{unknown}$V{unknown}", VariableReferenceResolver.process("$V{unknown}$V{unknown}", null));
        assertEquals("Multiple unknown normal variables with other text", "front$V{unknown}middle$V{unknown}back", VariableReferenceResolver.process("front$V{unknown}middle$V{unknown}back", null));
    }

    @Test
    public void testConstantReferences() {

        String vClass = "com.quartercode.disconnected.shared.test.util.VariableReferenceResolverTest$TestVariables";

        assertEquals("Class variable", "var1", VariableReferenceResolver.process("$C{" + vClass + ".VAR_1}", null));
        assertEquals("Class variable surrounded by content", "testvar1test", VariableReferenceResolver.process("test$C{" + vClass + ".VAR_1}test", null));
        assertEquals("Two class variables", "var1var1", VariableReferenceResolver.process("$C{" + vClass + ".VAR_1}$C{" + vClass + ".VAR_1}", null));
        assertEquals("Two class variables surrounded by content", "testvar1var1test", VariableReferenceResolver.process("test$C{" + vClass + ".VAR_1}$C{" + vClass + ".VAR_1}test", null));
        assertEquals("Two class variables with content between", "var1testvar1", VariableReferenceResolver.process("$C{" + vClass + ".VAR_1}test$C{" + vClass + ".VAR_1}", null));
        assertEquals("Two class variables surrounded by content with content between", "testvar1testvar1test", VariableReferenceResolver.process("test$C{" + vClass + ".VAR_1}test$C{" + vClass + ".VAR_1}test", null));

        assertEquals("Different class variables", "var1var2", VariableReferenceResolver.process("$C{" + vClass + ".VAR_1}$C{" + vClass + ".VAR_2}", null));
        assertEquals("Different class variables surrounded by content", "testvar1var2test", VariableReferenceResolver.process("test$C{" + vClass + ".VAR_1}$C{" + vClass + ".VAR_2}test", null));
        assertEquals("Different class variables with content between", "var1testvar2", VariableReferenceResolver.process("$C{" + vClass + ".VAR_1}test$C{" + vClass + ".VAR_2}", null));
        assertEquals("Different class variables surrounded by content with content between", "testvar1testvar2test", VariableReferenceResolver.process("test$C{" + vClass + ".VAR_1}test$C{" + vClass + ".VAR_2}test", null));

        assertEquals("Unknown class variable", "$C{this.is.not.a.Class.VAR_1}", VariableReferenceResolver.process("$C{this.is.not.a.Class.VAR_1}", null));
        assertEquals("Multiple unknown class variables", "$C{no1.Class.VAR_1}$C{no2.Class.VAR_1}", VariableReferenceResolver.process("$C{no1.Class.VAR_1}$C{no2.Class.VAR_1}", null));
        assertEquals("Multiple unknown class variables with other text", "front$C{no1.Class.VAR_1}middle$C{no2.Class.VAR_1}back", VariableReferenceResolver.process("front$C{no1.Class.VAR_1}middle$C{no2.Class.VAR_1}back", null));
    }

    protected static class TestVariables {

        public static final String VAR_1 = "var1";
        public static final String VAR_2 = "var2";

    }

}
