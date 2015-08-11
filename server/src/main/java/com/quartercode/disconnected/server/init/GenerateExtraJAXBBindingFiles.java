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

package com.quartercode.disconnected.server.init;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import com.quartercode.disconnected.server.sim.profile.DefaultProfileSerializationService;
import com.quartercode.disconnected.shared.util.init.Initializer;
import com.quartercode.disconnected.shared.util.init.InitializerSettings;

/*
 * At the moment, this initializer creates OXM binding files which mark all @XmlPersistent java types as possible root elements.
 * That way, @XmlElementRef can be used for producing elegant world XML trees.
 */
@InitializerSettings (groups = "initializeResources", dependencies = { "initializeServices" })
public class GenerateExtraJAXBBindingFiles implements Initializer {

    @Override
    public void initialize() {

        Map<String, Writer> packageWriters = new HashMap<>();

        try {
            // Create the binding file directory
            Files.createDirectories(DefaultProfileSerializationService.EXTRA_JAXB_BINDING_DIR);

            for (Class<?> persistentClass : DefaultProfileSerializationService.getPersistentClasses()) {
                String packageName = persistentClass.getPackage().getName();

                Writer packageWriter = packageWriters.get(packageName);
                if (packageWriter == null) {
                    Path packageBindingFile = DefaultProfileSerializationService.EXTRA_JAXB_BINDING_DIR.resolve(packageName.replace('.', '_') + ".xml");
                    packageWriter = Files.newBufferedWriter(packageBindingFile, Charset.defaultCharset());
                    packageWriters.put(packageName, packageWriter);

                    packageWriter.write("<?xml version=\"1.0\"?>");
                    packageWriter.write("<xml-bindings xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence/oxm\" package-name=\"" + packageName + "\">");
                    packageWriter.write("<java-types>");
                }

                String packageRelativeClassName = persistentClass.getName().substring(packageName.length() + 1);
                packageWriter.write("<java-type name=\"" + packageRelativeClassName + "\">");
                packageWriter.write("<xml-root-element />");
                packageWriter.write("</java-type>");
            }

            for (Writer packageWriter : packageWriters.values()) {
                packageWriter.write("</java-types></xml-bindings>");
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot generate extra JAXB binding files", e);
        } finally {
            for (Writer packageWriter : packageWriters.values()) {
                try {
                    packageWriter.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

}
