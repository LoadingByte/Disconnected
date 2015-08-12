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

package com.quartercode.disconnected.server.world.comp.os.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import com.quartercode.disconnected.server.world.comp.config.ConfigEntry;
import com.quartercode.disconnected.server.world.comp.config.UnknownColumnException;

/**
 * Environment variables are used by different programs.
 * They are like global variables on the level of the os.
 * Every environment variable has a name and an associated value string.
 */
public class EnvVariable extends ConfigEntry<EnvVariable> {

    /**
     * The separator which is used to separate the different subvalues of a {@link #getValueList() value list}.
     */
    public static final String LIST_SEPARATOR = ";";

    @XmlAttribute
    private String             name;
    @XmlAttribute
    private String             value;

    // JAXB constructor
    protected EnvVariable() {

        super(Arrays.asList("name", "value"));
    }

    /**
     * Creates a new environment variable.
     *
     * @param name The {@link #getName() name} of the new environment variable.
     *        It cannot be {@code null}.
     */
    public EnvVariable(String name) {

        this();

        setName(name);
    }

    /**
     * Creates a new environment variable and directly sets its value {@link String}.
     *
     * @param name The {@link #getName() name} of the new environment variable.
     *        It cannot be {@code null}.
     * @param value The initial {@link #getValue() value string} of the new environment variable.
     *        It can be {@code null}.
     */
    public EnvVariable(String name, String value) {

        this(name);

        setValue(name);
    }

    /**
     * Creates a new environment variable and directly sets the given {@link #getValueList() value list} as its value.
     *
     * @param name The {@link #getName() name} of the new environment variable.
     *        It cannot be {@code null}.
     * @param value The initial value list of the new environment variable.
     *        See {@link #setValueList(List)} for more information.
     */
    public EnvVariable(String name, List<String> valueList) {

        this(name);

        setValue(name);
    }

    /**
     * Returns the name of the environment variable (you could also call it the "key").
     * It is used to identify environment variable and retrieve its value.
     *
     * @return The name of the environment variable.
     */
    public String getName() {

        return name;
    }

    /**
     * Sets the name of the environment variable (you could also call it the "key").
     * It is used to identify environment variable and retrieve its value.
     *
     * @param name The new name of the environment variable.
     *        It cannot be {@code null}.
     */
    public void setName(String name) {

        Validate.notNull(name, "Cannot use null as user name");
        this.name = name;
    }

    /**
     * Returns the value {@link String} which is assigned to the environment variable.
     * This value can be retrieved by anyone who knows the environment variable's {@link #getName() name}.
     *
     * @return The value of the environment variable.
     */
    public String getValue() {

        return value;
    }

    /**
     * Returns the environment variable's {@link #getValue() value} {@link String} as a {@link List}.
     * In the value string, such a list is represented as a collection of subvalues separated by the character {@value #LIST_SEPARATOR}.
     * Example:
     *
     * <pre>
     * Value String: "subvalue1;subvalue2;subvalue3"
     * Value List:   ["subvalue1", "subvalue2", "subvalue3"]
     * </pre>
     *
     * If the value string is {@code null} or empty ({@code ""}), the returned list is {@link List#isEmpty() empty} as well.
     * If the value string does not contain any list seperators ({@value #LIST_SEPARATOR}), the returned list contains the entire value string as its only element.<br>
     * <br>
     * Modifications to the returned list object do not change this environment variable in any way.
     *
     * @return The list containing the subvalues of this environment variable.
     */
    public List<String> getValueList() {

        List<String> valueList = new ArrayList<>();

        if (value != null && !value.isEmpty()) {
            // Split at separator and return
            // If there's no separator, the whole value is handled as one entry
            valueList.addAll(Arrays.asList(value.split(LIST_SEPARATOR)));
        }

        return valueList;
    }

    /**
     * Changes the value {@link String} which is assigned to the environment variable.
     *
     * @param value The new value which should be assigned to the environment variable.
     */
    public void setValue(String value) {

        this.value = value;
    }

    /**
     * Changes the environment variable's {@link #getValue() value} {@link String} to the given {@link List}.
     * In the value string, such a list is represented as a collection of subvalues separated by the character {@value #LIST_SEPARATOR}.
     * Example:
     *
     * <pre>
     * Value List:   ["subvalue1", "subvalue2", "subvalue3"]
     * Value String: "subvalue1;subvalue2;subvalue3"
     * </pre>
     *
     * If the value list is {@code null}, the new value string is {@code null} as well.
     * If the value list is empty, the new value string is empty ({@code ""}) as well.
     * If the value list only contains one element, the new value string contains that entire single element without any list seperators ({@value #LIST_SEPARATOR}).<br>
     * <br>
     * Later modifications to the input {@link List} object do not apply to the variable.
     * Note that you can get the currently set value list using {@link #getValueList()}.
     *
     * @param valueList The value list containing the new subvalues.
     */
    public void setValueList(List<String> valueList) {

        value = StringUtils.join(valueList, LIST_SEPARATOR);
    }

    @Override
    public String getColumnValue(String columnName) {

        switch (columnName) {
            case "name":
                return getName();
            case "value":
                return getValue();
            default:
                throw new UnknownColumnException(columnName);
        }
    }

    @Override
    public void setColumnValue(String columnName, String columnValue) {

        switch (columnName) {
            case "name":
                setName(columnValue);
                break;
            case "value":
                setValue(columnValue);
                break;
            default:
                throw new UnknownColumnException(columnName);
        }
    }

}
