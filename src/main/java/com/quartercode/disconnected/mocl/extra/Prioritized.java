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

package com.quartercode.disconnected.mocl.extra;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.quartercode.disconnected.mocl.base.FeatureHolder;

/**
 * {@link FunctionExecutor}s which have this annotation define their priority.
 * The priority is used for determinating the order in which the avaiable {@link FunctionExecutor}s are invoked.
 * This should be annotated at the actual {@link FunctionExecutor#invoke(FeatureHolder, Object...)} method.
 * 
 * @see FunctionExecutor
 */
@Target (ElementType.METHOD)
@Retention (RetentionPolicy.RUNTIME)
public @interface Prioritized {

    /**
     * {@link FunctionExecutor}s with the priority level 0 are invoked last.
     * The priority level sets the tier of the {@link FunctionExecutor}.
     */
    public static final int LEVEL_0    = 0;

    /**
     * {@link FunctionExecutor}s with the priority level 1 are invoked ninthly.
     * The priority level sets the tier of the {@link FunctionExecutor}.
     */
    public static final int LEVEL_1    = 10;

    /**
     * {@link FunctionExecutor}s with the priority level 2 are invoked eighthly.
     * The priority level sets the tier of the {@link FunctionExecutor}.
     */
    public static final int LEVEL_2    = 20;

    /**
     * {@link FunctionExecutor}s with the priority level 3 are invoked seventh.
     * The priority level sets the tier of the {@link FunctionExecutor}.
     */
    public static final int LEVEL_3    = 30;

    /**
     * {@link FunctionExecutor}s with the priority level 4 are invoked sixthly.
     * The priority level sets the tier of the {@link FunctionExecutor}.
     */
    public static final int LEVEL_4    = 40;

    /**
     * {@link FunctionExecutor}s with the priority level 5 are invoked fifthly.
     * The priority level sets the tier of the {@link FunctionExecutor}.
     * Level five is the default level.
     */
    public static final int LEVEL_5    = 50;

    /**
     * {@link FunctionExecutor}s with the priority level 6 are invoked fourthly.
     * The priority level sets the tier of the {@link FunctionExecutor}.
     */
    public static final int LEVEL_6    = 60;

    /**
     * {@link FunctionExecutor}s with the priority level 7 are invoked thirdly.
     * The priority level sets the tier of the {@link FunctionExecutor}.
     */
    public static final int LEVEL_7    = 70;

    /**
     * {@link FunctionExecutor}s with the priority level 8 are invoked secondly.
     * The priority level sets the tier of the {@link FunctionExecutor}.
     */
    public static final int LEVEL_8    = 80;

    /**
     * {@link FunctionExecutor}s with the priority level 9 are invoked first.
     * The priority level sets the tier of the {@link FunctionExecutor}.
     */
    public static final int LEVEL_9    = 90;

    /**
     * {@link FunctionExecutor}s with the sublevel 0 are invoked last inside their priority level or tier.
     * The sublevel sets the importance of the {@link FunctionExecutor} inside their tier.
     * For example, if {@link #LEVEL_5} is for changing some priorities, the changes could be placed on {@link #SUBLEVEL_0} while checks are placed on {@link #SUBLEVEL_1}.
     * You can combine a tier with a sublevel by simply writing <code>LEVEL_X + SUBLEVEL_X</code>.
     * Sublevel 0 is the default sublevel.
     */
    public static final int SUBLEVEL_0 = 0;

    /**
     * {@link FunctionExecutor}s with the sublevel 1 are invoked ninthly inside their priority level or tier.
     * The sublevel sets the importance of the {@link FunctionExecutor} inside their tier.
     * For example, if {@link #LEVEL_5} is for changing some priorities, the changes could be placed on {@link #SUBLEVEL_0} while checks are placed on {@link #SUBLEVEL_1}.
     * You can combine a tier with a sublevel by simply writing <code>LEVEL_X + SUBLEVEL_X</code>.
     */
    public static final int SUBLEVEL_1 = 1;

    /**
     * {@link FunctionExecutor}s with the sublevel 2 are invoked eighthly inside their priority level or tier.
     * The sublevel sets the importance of the {@link FunctionExecutor} inside their tier.
     * For example, if {@link #LEVEL_5} is for changing some priorities, the changes could be placed on {@link #SUBLEVEL_0} while checks are placed on {@link #SUBLEVEL_1}.
     * You can combine a tier with a sublevel by simply writing <code>LEVEL_X + SUBLEVEL_X</code>.
     */
    public static final int SUBLEVEL_2 = 2;

    /**
     * {@link FunctionExecutor}s with the sublevel 3 are invoked seventh inside their priority level or tier.
     * The sublevel sets the importance of the {@link FunctionExecutor} inside their tier.
     * For example, if {@link #LEVEL_5} is for changing some priorities, the changes could be placed on {@link #SUBLEVEL_0} while checks are placed on {@link #SUBLEVEL_1}.
     * You can combine a tier with a sublevel by simply writing <code>LEVEL_X + SUBLEVEL_X</code>.
     */
    public static final int SUBLEVEL_3 = 3;

    /**
     * {@link FunctionExecutor}s with the sublevel 4 are invoked sixthly inside their priority level or tier.
     * The sublevel sets the importance of the {@link FunctionExecutor} inside their tier.
     * For example, if {@link #LEVEL_5} is for changing some priorities, the changes could be placed on {@link #SUBLEVEL_0} while checks are placed on {@link #SUBLEVEL_1}.
     * You can combine a tier with a sublevel by simply writing <code>LEVEL_X + SUBLEVEL_X</code>.
     */
    public static final int SUBLEVEL_4 = 4;

    /**
     * {@link FunctionExecutor}s with the sublevel 5 are invoked fifthly inside their priority level or tier.
     * The sublevel sets the importance of the {@link FunctionExecutor} inside their tier.
     * For example, if {@link #LEVEL_5} is for changing some priorities, the changes could be placed on {@link #SUBLEVEL_0} while checks are placed on {@link #SUBLEVEL_1}.
     * You can combine a tier with a sublevel by simply writing <code>LEVEL_X + SUBLEVEL_X</code>.
     */
    public static final int SUBLEVEL_5 = 5;

    /**
     * {@link FunctionExecutor}s with the sublevel 6 are invoked fourthly inside their priority level or tier.
     * The sublevel sets the importance of the {@link FunctionExecutor} inside their tier.
     * For example, if {@link #LEVEL_5} is for changing some priorities, the changes could be placed on {@link #SUBLEVEL_0} while checks are placed on {@link #SUBLEVEL_1}.
     * You can combine a tier with a sublevel by simply writing <code>LEVEL_X + SUBLEVEL_X</code>.
     */
    public static final int SUBLEVEL_6 = 6;

    /**
     * {@link FunctionExecutor}s with the sublevel 7 are invoked thirdly inside their priority level or tier.
     * The sublevel sets the importance of the {@link FunctionExecutor} inside their tier.
     * For example, if {@link #LEVEL_5} is for changing some priorities, the changes could be placed on {@link #SUBLEVEL_0} while checks are placed on {@link #SUBLEVEL_1}.
     * You can combine a tier with a sublevel by simply writing <code>LEVEL_X + SUBLEVEL_X</code>.
     */
    public static final int SUBLEVEL_7 = 7;

    /**
     * {@link FunctionExecutor}s with the sublevel 8 are invoked secondly inside their priority level or tier.
     * The sublevel sets the importance of the {@link FunctionExecutor} inside their tier.
     * For example, if {@link #LEVEL_5} is for changing some priorities, the changes could be placed on {@link #SUBLEVEL_0} while checks are placed on {@link #SUBLEVEL_1}.
     * You can combine a tier with a sublevel by simply writing <code>LEVEL_X + SUBLEVEL_X</code>.
     */
    public static final int SUBLEVEL_8 = 8;

    /**
     * {@link FunctionExecutor}s with the sublevel 9 are invoked first inside their priority level or tier.
     * The sublevel sets the importance of the {@link FunctionExecutor} inside their tier.
     * For example, if {@link #LEVEL_5} is for changing some priorities, the changes could be placed on {@link #SUBLEVEL_0} while checks are placed on {@link #SUBLEVEL_1}.
     * You can combine a tier with a sublevel by simply writing <code>LEVEL_X + SUBLEVEL_X</code>.
     */
    public static final int SUBLEVEL_9 = 9;

    /**
     * The default priority {@link FunctionExecutor}s without this annotation have.
     * This priority is using {@link #LEVEL_4}, so the total value is 40.
     */
    public static final int DEFAULT    = LEVEL_4;

    /**
     * The actual priority the {@link FunctionExecutor} has.
     */
    int value ();

}
