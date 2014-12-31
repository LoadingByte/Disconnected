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

package com.quartercode.disconnected.client.graphics.desktop.prog.util;

import static java.text.MessageFormat.format;
import com.quartercode.disconnected.client.graphics.desktop.popup.MessagePopup;
import com.quartercode.disconnected.client.graphics.desktop.prog.ClientProgramWindow;
import com.quartercode.disconnected.client.util.LocalizationSupplier;
import com.quartercode.disconnected.shared.event.comp.prog.generic.GP_SBPWPU_ErrorEvent;
import com.quartercode.eventbridge.bridge.module.EventHandler;

/**
 * A simple {@link EventHandler} for {@link GP_SBPWPU_ErrorEvent}s that just opens a {@link MessagePopup} window when an error event arrives.
 * 
 * @see GP_SBPWPU_ErrorEvent
 */
public class GP_SBPWPU_ErrorEventPopupHandler implements EventHandler<GP_SBPWPU_ErrorEvent> {

    private final ClientProgramWindow  programWindow;
    private final LocalizationSupplier l10nContext;
    private final String               keyPrefix;
    private final String               keySuffix;
    private final boolean              modal;

    /**
     * Creates a new {@link GP_SBPWPU_ErrorEvent} popup handler.
     * 
     * @param programWindow The {@link ClientProgramWindow} of the client program whose {@link GP_SBPWPU_ErrorEvent}s should be processed.
     * @param l10nContext The {@link LocalizationSupplier} which should be used to resolve the error messages.
     * @param keyPrefix A prefix that is put in front of the error type before it is used as a localization key.
     * @param keySuffix A suffix that is put behind the error type before it is used as a localization key.
     * @param modal Whether the popup window should be modal.
     */
    public GP_SBPWPU_ErrorEventPopupHandler(ClientProgramWindow programWindow, LocalizationSupplier l10nContext, String keyPrefix, String keySuffix, boolean modal) {

        this.programWindow = programWindow;
        this.l10nContext = l10nContext;
        this.keyPrefix = keyPrefix;
        this.keySuffix = keySuffix;
        this.modal = modal;
    }

    @Override
    public void handle(GP_SBPWPU_ErrorEvent event) {

        String key = keyPrefix + event.getType() + keySuffix;
        String message = format(l10nContext.get(key), (Object[]) event.getArguments());

        programWindow.openPopup(new MessagePopup(programWindow.getState(), message), modal);
    }

}
