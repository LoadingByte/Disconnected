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

package com.quartercode.disconnected.sim.run;

import java.lang.reflect.Field;
import javax.xml.bind.annotation.XmlElement;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.extra.Function;
import com.quartercode.classmod.extra.FunctionDefinition;

/**
 * A function call schedule task is a {@link ScheduleTask} that "just" calls a {@link Function}.
 * It supports persistence and can restore scheduled function calls the were serialized before.
 * For achieving that, it stores the exact location of the {@link FunctionDefinition} that describes the function for invocation.
 * 
 * @see ScheduleTask
 */
public class FunctionCallScheduleTask implements ScheduleTask {

    private FunctionDefinition<?> functionDefinition;

    @XmlElement
    private Class<?>              functionDefinitionLocation;
    @XmlElement
    private String                functionDefinitionField;

    /**
     * Creates a new empty function call schedule task.
     * This is only recommended for direct field access (e.g. for serialization).
     */
    protected FunctionCallScheduleTask() {

    }

    /**
     * Creates a new function call schedule task with the given {@link FunctionDefinition}.<br>
     * <br>
     * Furthermore, the {@link Class} that contains the <code>public static</code> definition constant needs to be provided for persistence support.
     * Please note that the name of the definition constant needs to match the name of the function
     * Examples:
     * 
     * <pre>
     * Function: do
     * Constant: DO
     * 
     * Function: testFunction
     * Constant: TEST_FUNCTION
     * </pre>
     * 
     * @param functionDefinition The {@link FunctionDefinition} that defines the {@link Function} that should be called by the task.
     * @param functionDefinitionLocation The {@link Class} that contains the supplied {@link FunctionDefinition} constant.
     */
    public FunctionCallScheduleTask(FunctionDefinition<?> functionDefinition, Class<?> functionDefinitionLocation) {

        this.functionDefinition = functionDefinition;
        this.functionDefinitionLocation = functionDefinitionLocation;

        StringBuilder functionDefinitionField = new StringBuilder();
        for (char c : functionDefinition.getName().toCharArray()) {
            if (Character.isUpperCase(c)) {
                functionDefinitionField.append("_");
            }
            functionDefinitionField.append(Character.toUpperCase(c));
        }
        this.functionDefinitionField = functionDefinitionField.toString();

        // Test whether the field exists
        resolveDefinition();
    }

    /**
     * Creates a new function call schedule task with the given {@link FunctionDefinition}.<br>
     * <br>
     * Furthermore, the {@link Class} that contains the <code>public static</code> definition constant needs to be provided for persistence support,
     * as well as the name of the actual constant field.<br>
     * For example, if your feature holder <code>SomeFeatureHolder</code> contained the {@link FunctionDefinition} constant <code>public static final
     * FunctionDefinition&lt;...&gt; SOME_FUNCTION</code>, you would have to provide both values to this constructor.
     * 
     * @param functionDefinition The {@link FunctionDefinition} that defines the {@link Function} that should be called by the task.
     * @param functionDefinitionLocation The {@link Class} that contains the supplied {@link FunctionDefinition} constant.
     * @param functionDefinitionField The actual name of the supplied {@link FunctionDefinition} constant.
     */
    public FunctionCallScheduleTask(FunctionDefinition<?> functionDefinition, Class<?> functionDefinitionLocation, String functionDefinitionField) {

        this.functionDefinition = functionDefinition;
        this.functionDefinitionLocation = functionDefinitionLocation;
        this.functionDefinitionField = functionDefinitionField;

        // Test whether the field exists
        resolveDefinition();
    }

    @Override
    public void execute(FeatureHolder holder) {

        if (functionDefinition == null) {
            functionDefinition = resolveDefinition();
        }
        holder.get(functionDefinition).invoke();
    }

    private FunctionDefinition<?> resolveDefinition() {

        try {
            Field field = functionDefinitionLocation.getField(functionDefinitionField);
            field.setAccessible(true);
            return (FunctionDefinition<?>) field.get(null);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Function definition field '" + functionDefinitionField + "' doesn't exist", e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Function definition field '" + functionDefinitionField + "' isn't public", e);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Public static field '" + functionDefinitionField + "' doesn't contain a function definition", e);
        }
    }

}
