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

package com.quartercode.disconnected.client.util;

/**
 * A localization (l10n) supplier provides localized strings for l10n keys.
 * For example, the text of a certain button might be assigned to the key {@code testButton.text}.
 * By putting that key into the {@link #get(String)} method, the actual localized button text is returned for the currently set locale.
 */
public interface LocalizationSupplier {

    /**
     * Returns the localized string that is associated with the given key.
     * 
     * @param key The key the returned localized string is associated with.
     * @return The localized string that is associated with the given key.
     */
    public String get(String key);

}
