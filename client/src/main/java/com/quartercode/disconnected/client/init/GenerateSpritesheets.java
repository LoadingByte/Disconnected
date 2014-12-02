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

package com.quartercode.disconnected.client.init;

import java.io.IOException;
import java.nio.file.Path;
import com.quartercode.disconnected.client.registry.ClientRegistries;
import com.quartercode.disconnected.client.registry.Theme;
import com.quartercode.disconnected.client.util.TWLSpritesheetGenerator;
import com.quartercode.disconnected.shared.util.IOFileUtils;
import com.quartercode.disconnected.shared.util.ResourceLister;
import com.quartercode.disconnected.shared.util.TempFileManager;
import com.quartercode.disconnected.shared.util.init.Initializer;
import com.quartercode.disconnected.shared.util.init.InitializerSettings;
import com.quartercode.disconnected.shared.util.registry.Registries;

@InitializerSettings (groups = "initializeResources", dependencies = "loadConfigs")
public class GenerateSpritesheets implements Initializer {

    @Override
    public void initialize() {

        // Generate spritesheets
        try (ResourceLister resourceLister = new ResourceLister("/ui/sprites", false)) {
            // Assume that there is only one sprites directory on the classpath
            Path spritesDir = resourceLister.getResourcePaths().get(0);

            // Copy the sprites into a temporary directory to avoid jar problems
            Path tmpSpritesDir = TempFileManager.getTempDir().resolve("sprites");
            IOFileUtils.copyDirectory(spritesDir, tmpSpritesDir);

            // Generate the spritesheets and add the resulting twl config theme
            Path spriteTheme = TWLSpritesheetGenerator.generate(tmpSpritesDir, TempFileManager.getTempDir().resolve("spritesheets"));
            Registries.get(ClientRegistries.THEMES).addValue(new Theme("spritesheetTwlConfig", spriteTheme.toUri().toURL(), 1000000));
        } catch (IOException e) {
            throw new RuntimeException("Cannot generate sprite theme", e);
        }
    }

}
