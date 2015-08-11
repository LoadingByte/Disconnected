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

package com.quartercode.disconnected.client.graphics.desktop.prog;

import com.quartercode.disconnected.client.graphics.GraphicsState;
import com.quartercode.disconnected.client.registry.ClientProgram;
import com.quartercode.disconnected.client.registry.ClientRegistries;
import com.quartercode.disconnected.client.util.LocalizationSupplier;
import com.quartercode.disconnected.shared.util.ValueInjector;
import com.quartercode.eventbridge.bridge.Bridge;

/**
 * A client program executor is able to execute a {@link ClientProgram}.
 * For example, that could be done starting a world process and displaying a GUI.<br>
 * <br>
 * Client programs are the counterpart to regular world programs that run on a server.
 * They only have a GUI and don't implement any logic.
 * That means that all logic is performed by the server-side world processes. The results are sent to the client programs.
 * In order to establish the client-server program connection, client programs create normal world processes on the server.<br>
 * <br>
 * Before the {@link #run()} method is called, a {@link ValueInjector} is used on the executor to provide the necessary context values.
 * The following values are available by default:
 *
 * <table>
 * <tr>
 * <td>{@code name} ({@link String})</td>
 * <td>The internal name of the program (equivalent to {@link ClientProgram#getName()}).</td>
 * </tr>
 * <tr>
 * <td>{@code category} ({@link String})</td>
 * <td>The internal category the program is assigned to (equivalent to {@link ClientProgram#getCategory()}).</td>
 * </tr>
 * <tr>
 * <td>{@code stateContext} ({@link ClientProgramStateContext})</td>
 * <td>A state context object that can be used for stopping the client program and adding listeners to react to such a stop.</td>
 * </tr>
 * <tr>
 * <td>{@code graphicsContext} ({@link GraphicsState})</td>
 * <td>The desktop state the program is running in. It should be used to open new windows.</td>
 * </tr>
 * <tr>
 * <td>{@code l10nContext} ({@link LocalizationSupplier})</td>
 * <td>The l10n supplier that can be used to retrieve localized strings.</td>
 * </tr>
 * <tr>
 * <td>{@code bridge} ({@link Bridge})</td>
 * <td>The bridge that should be used for sending and receiving world process events.</td>
 * </tr>
 * </table>
 *
 * @see ClientRegistries#CLIENT_PROGRAMS
 * @see ClientProgramWindow
 */
public interface ClientProgramExecutor {

    /**
     * This callback is executed once when the client program executor should start running.
     * This method typically launches a world process and opens a GUI window.<br>
     * <br>
     * Note that a {@link ValueInjector} has been run before this call.
     * See {@link ClientProgramExecutor} for more information on the available values.
     */
    public void run();

}
