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

import java.util.ArrayList;
import java.util.List;

/**
 * This timer can schedule timer tasks which have to elapse a given delay (in ticks) and then can continue running in given periods.
 * 
 * @see TimerTask
 */
public class TickTimer implements TickAction {

    private final List<TimerTask> tasks = new ArrayList<TimerTask>();

    /**
     * Creates a new empty tick timer.
     */
    public TickTimer() {

    }

    /**
     * Schedules a given timer task. Every important parameter for scheduling (e.g. the delay) is set in the task object.
     * 
     * @param task The timer task to schedule.
     */
    public void schedule(TimerTask task) {

        tasks.add(task);
    }

    /**
     * Cancels a given timer task so it wont elapse more ticks.
     * 
     * @param task The timer task to cancel.
     */
    public void cancel(TimerTask task) {

        tasks.remove(task);
    }

    /**
     * Elapses one tick on every timer task and calls the tasks if the timing condition is true.
     */
    @Override
    public void update() {

        for (TimerTask task : new ArrayList<TimerTask>(tasks)) {
            if (task.getElapsed() < 0) {
                cancel(task);
            } else {
                task.elapse();

                if (task.getPeriod() <= 0 && task.getElapsed() == task.getDelay()) {
                    task.run();
                    cancel(task);
                } else if (task.getPeriod() > 0 && (task.getElapsed() - task.getDelay()) % task.getPeriod() == 0) {
                    task.run();
                }
            }
        }
    }

    /**
     * This class represents a timer task which elapses a given amount of ticks and then call the integrated runnable.
     */
    public static abstract class TimerTask implements Runnable {

        private final int delay;
        private final int period;

        private int       elapsed;

        /**
         * Creates a new abstract timer task and sets the delay after the task should be called.
         * 
         * @param delay The delay after the task should be called.
         */
        public TimerTask(int delay) {

            this.delay = delay;
            period = 0;
        }

        /**
         * Creates a new abstract timer task and sets the delay after the task should be called and the period.
         * 
         * @param delay The delay after the task should be called.
         * @param period Every time after those ticks elapsed, the task should be called.
         */
        public TimerTask(int delay, int period) {

            this.delay = delay;
            this.period = period;
        }

        /**
         * Returns the delay after the task should be called.
         * 
         * @return The delay after the task should be called.
         */
        public int getDelay() {

            return delay;
        }

        /**
         * Returns the period after which the task should be called.
         * 
         * @return Every time after those ticks elapsed, the task should be called.
         */
        public int getPeriod() {

            return period;
        }

        /**
         * Returns the amount of ticks that have elapsed.
         * 
         * @return The amount of ticks that have elapsed.
         */
        public int getElapsed() {

            return elapsed;
        }

        /**
         * Cancels the timer task so it wont elapse more ticks.
         */
        public void cancel() {

            elapsed = -1;
        }

        private void elapse() {

            elapsed++;
        }
    }

}
