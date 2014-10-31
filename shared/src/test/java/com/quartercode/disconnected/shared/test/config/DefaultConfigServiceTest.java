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

package com.quartercode.disconnected.shared.test.config;

import java.io.IOException;
import java.io.StringReader;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import com.quartercode.disconnected.shared.config.ConfigCommandParser;
import com.quartercode.disconnected.shared.config.DefaultConfigService;

public class DefaultConfigServiceTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Test
    public void test() throws JDOMException, IOException {

        // Create a test xml document
        String xml = "<config>";
        xml += "<test1 attr=\"val\" />";
        xml += "<test2><elem>val2</elem></test2>";
        xml += "<test1 attr=\"val2\" />";
        xml += "</config>";
        final Document document = new SAXBuilder().build(new StringReader(xml));

        // Read the command elements from the document
        Element root = document.getRootElement();
        final Element test11 = root.getChildren("test1").get(0);
        final Element test2 = root.getChild("test2");
        final Element test12 = root.getChildren("test1").get(1);

        // Create two config command parsers for the test commands
        final ConfigCommandParser test1Parser = context.mock(ConfigCommandParser.class, "test1Parser");
        final ConfigCommandParser test2Parser = context.mock(ConfigCommandParser.class, "test2Parser");

        // @formatter:off
        context.checking(new Expectations() {{

            final Sequence commandParserCalls = context.sequence("commandParserCalls");

            oneOf(test1Parser).parse(document, test11); inSequence(commandParserCalls);
            oneOf(test2Parser).parse(document, test2); inSequence(commandParserCalls);
            oneOf(test1Parser).parse(document, test12); inSequence(commandParserCalls);

        }});
        // @formatter:on

        // Create a new config service and add the config command parsers
        DefaultConfigService service = new DefaultConfigService();
        service.addCommand("test1", test1Parser);
        service.addCommand("test2", test2Parser);

        // Let the config service parse the document
        service.parse(document);
    }

}
