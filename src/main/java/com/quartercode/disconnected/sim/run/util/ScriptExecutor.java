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

package com.quartercode.disconnected.sim.run.util;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import com.quartercode.disconnected.sim.Simulation;
import com.quartercode.disconnected.world.member.Member;

/**
 * This utility class executes scripts which change certain parameters of a computer or deliver data
 */
public class ScriptExecutor {

    private static final Logger              LOGGER                = Logger.getLogger(ScriptExecutor.class.getName());
    private static final ScriptEngineManager SCRIPT_ENGINE_MANAGER = new ScriptEngineManager();

    /**
     * Executes the given script on the given member in the given simulation.
     * This may change some parameters of his computer, his reputation or even transmit data to the causer of the script execution.
     * 
     * @param script The script to execute on the given member.
     * @param simulation The simulation to execute the script in.
     * @param member The member to execute the given script on.
     * @param causer The member who caused the execution of the given script.
     */
    public static void execute(String script, Simulation simulation, Member member, Member causer) {

        ScriptEngine engine = SCRIPT_ENGINE_MANAGER.getEngineByName("JavaScript");

        try {
            engine.put("simulation", simulation);
            engine.put("member", member);
            engine.put("computer", member.getComputer());
            engine.put("causer", causer);
            engine.put("causerComputer", causer.getComputer());

            engine.eval(script);
        }
        catch (ScriptException e) {
            LOGGER.log(Level.SEVERE, "Can't execute the following script on member \"" + member.getName() + "\", caused by member \"" + causer.getName() + "\":\n" + script, e);
        }
    }

    /**
     * Executes the given scripts on the given member in the given simulation.
     * This may change some parameters of his computer, his reputation or even transmit data to the causer of the script executions.
     * 
     * @param scripts The scripts to execute on the given member.
     * @param simulation The simulation to execute the script in.
     * @param member The member to execute the given script on.
     * @param causer The member who caused the execution of the given script.
     */
    public static void execute(Collection<String> scripts, Simulation simulation, Member member, Member causer) {

        for (String script : scripts) {
            execute(script, simulation, member, causer);
        }
    }

    private ScriptExecutor() {

    }

}
