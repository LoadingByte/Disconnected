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

package com.quartercode.disconnected.test.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.quartercode.disconnected.util.LogExceptionHandler;

@RunWith (Parameterized.class)
public class LogExceptionHandlerTest {

    @Parameters
    public static Collection<Object[]> getData() {

        List<Object[]> data = new ArrayList<Object[]>();
        data.add(new Object[] { new RuntimeException(), true });
        data.add(new Object[] { null, false });
        return data;
    }

    private final RuntimeException exception;
    private final boolean          log;

    public LogExceptionHandlerTest(RuntimeException exception, boolean log) {

        this.exception = exception;
        this.log = log;
    }

    @Test
    public void test() throws InterruptedException {

        Thread thread = new Thread() {

            @Override
            public void run() {

                if (exception != null) {
                    throw exception;
                }
            };
        };
        thread.setUncaughtExceptionHandler(new LogExceptionHandler());

        final AtomicBoolean catched = new AtomicBoolean();
        LogExceptionHandler.getLogger().setUseParentHandlers(false);
        LogExceptionHandler.getLogger().addHandler(new Handler() {

            @Override
            public void publish(LogRecord record) {

                if (record.getThrown().equals(exception)) {
                    catched.set(true);
                }
            }

            @Override
            public void flush() {

            }

            @Override
            public void close() {

            }
        });

        thread.start();
        thread.join();

        Assert.assertTrue("Logged uncaught throwable", catched.get() == log);
    }

}
