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

package com.quartercode.disconnected.bridge;

import java.util.concurrent.atomic.AtomicBoolean;
import com.quartercode.disconnected.util.DataObjectBase;

/**
 * The return event utility allows to send returnable {@link Event}s that are answered with return events.
 * Those return events are handled by a temporary local handler that redirects them to a provided handler.
 * The whole utility removes the need for boilerplate code and makes it easy to implement event conversations.
 * 
 * @see Event
 * @see Returnable
 * @see Return
 */
public class ReturnEventUtil {

    /**
     * Sends the given {@link Returnable} event over the given {@link Bridge} and redirects <b>one</b> {@link Return} event to the given handler.
     * Using the method could look like this:
     * 
     * <pre>
     * QuestionEvent question = new QuestionEvent();
     * ReturnEventUtil.send(bridge, question, new AbstractEventHandler&lt;AnswerEvent&gt;(AnswerEvent.class) {
     * 
     *     public void handle(AnswerEvent event) {
     * 
     *         // Do something with the return
     *     }
     * 
     * });
     * </pre>
     * 
     * In this case, an {@link EventHandler} that receives the {@code QuestionEvent} must also send <b>one</b> {@code AnswerEvent}.
     * 
     * @param bridge The local bridge that sends the question event and should receive the return event.
     * @param event The question event that requests an answer by the receiver of the question.
     * @param handler The event handler that will receive the return event.
     *        Please note that this hanlder is not directly added to the given bridge
     *        It is wrapped by another handler that is required for the technical stuff.
     */
    public static <R extends Return> void send(Bridge bridge, Returnable event, final EventHandler<R> handler) {

        send(bridge, event, handler, new BooleanCloseChecker(new AtomicBoolean(true)));
    }

    /**
     * Sends the given {@link Returnable} event over the given {@link Bridge} and redirects <b>all</b> {@link Return} events to the given handler.
     * The redirection of events will stop once the given {@link CloseChecker} returns {@code false} after the handler call.
     * Using the method could look like this:
     * 
     * <pre>
     * QuestionEvent question = new QuestionEvent();
     * AtomicBoolean closeFlag = new AtomicBoolean();
     * ReturnEventUtil.send(bridge, question, new AbstractEventHandler&lt;AnswerEvent&gt;(AnswerEvent.class) {
     * 
     *     public void handle(AnswerEvent event) {
     * 
     *         // Do something with the return and set &quot;closeFlag&quot; to
     *         // true when no more events should be received.
     *     }
     * 
     * }, new BooleanCloseChecker(closeFlag));
     * </pre>
     * 
     * In this case, an {@link EventHandler} that receives the {@code QuestionEvent} must also send <b>one</b> {@code AnswerEvent}.
     * 
     * @param bridge The local bridge that sends the question event and should receive the return events.
     * @param event The question event that requests an answer by the receiver of the question.
     * @param handler The event handler that will receive the return event.
     *        Please note that this hanlder is not directly added to the given bridge
     *        It is wrapped by another handler that is required for the technical stuff.
     * @param closeChecker A close checker that is called after each handler call (received event).
     *        Once the checker returns {@code false}, the redirection of events will stop.
     */
    public static <R extends Return> void send(final Bridge bridge, Returnable event, final EventHandler<R> handler, final CloseChecker closeChecker) {

        // Generate return id
        String returnId = bridge.nextId();

        // Create new event with return id
        event = event.withNextReturnId(returnId);

        // Add handler that delegates all return calls for the return id to the provided handler
        bridge.addHandler(new AbstractEventHandler<R>(new ReturnIdPredicate<R>(returnId)) {

            @Override
            public void handle(R event) {

                if (EventUtils.tryTest(handler.getPredicate(), event)) {
                    handler.handle(event);
                }

                if (closeChecker.check()) {
                    bridge.removeHandler(this);
                }
            }

        });

        // Send the event
        bridge.send(event);
    }

    private ReturnEventUtil() {

    }

    private static class ReturnIdPredicate<T extends Return> extends DataObjectBase implements EventPredicate<T> {

        private static final long serialVersionUID = 2959205586146211520L;

        private final String      returnId;

        private ReturnIdPredicate(String returnId) {

            this.returnId = returnId;
        }

        @Override
        public boolean test(T event) {

            return event.getReturnId().equals(returnId);
        }
    }

    /**
     * A returnable {@link Event} is like a question that can be answered with an {@link Return} event.
     * It stores a {@code nextReturnId} that is used to recognize return events.
     * 
     * @see ReturnEventUtil
     */
    public static interface Returnable extends Event {

        /**
         * Creates a new event with the same state and the given changed next return id.
         * The original event object should not be changed in any way.
         * 
         * @param nextReturnId The new next return id string.
         * @return A new event object with the same state and the given next return id.
         */
        public Returnable withNextReturnId(String nextReturnId);

        /**
         * Returns the next return id of the returnable event.
         * It is used as {@code returnId} in {@link Return} events.
         * 
         * @return The next return id.
         */
        public String getNextReturnId();

    }

    /**
     * A return {@link Event} is like an answer to a {@link Returnable} event.
     * It stores a {@code returnId} that is used to recognize it.
     * 
     * @see ReturnEventUtil
     */
    public static interface Return extends Event {

        /**
         * Returns the return id that is used to recognize the return event.
         * The value probably comes from a {@link Returnable} event's {@code nextReturnId}.
         * 
         * @return The event's return id.
         */
        public String getReturnId();

    }

    /**
     * A close checker is a simple class that returns whether a return listener may be closed.
     * Such a checker is used by the {@link ReturnEventUtil}.
     * It basically checks whether the return listener may be closed after each call of that return listener.
     * 
     * @see ReturnEventUtil#send(Bridge, Returnable, EventHandler, CloseChecker)
     */
    public static interface CloseChecker {

        /**
         * Returns whether the return listener of a question-return communication may be closed.
         * This method is invoked after each call of the return listener.
         * 
         * @return {@code True} if the return listener may be closed.
         */
        public boolean check();

    }

    /**
     * A boolean close checker takes an {@link AtomicBoolean} flag and returns its value in the {@link #check()} method.
     * That allows simple close checks like:
     * 
     * <pre>
     * <b>AtomicBoolean closeFlag = new AtomicBoolean();</b>
     * ReturnEventUtil.send(bridge, new QuestionEvent(), new AbstractEventHandler&lt;AnswerEvent&gt;(AnswerEvent.class) {
     * 
     *     public void handle(AnswerEvent event) {
     * 
     *         // Do something with the return <b>and set &quot;closeFlag&quot; to</b>
     *         // <b>true when no more events should be received.</b>
     *     }
     * 
     * }, <b>new BooleanCloseChecker(closeFlag)</b>);
     * </pre>
     * 
     * @see CloseChecker
     * @see ReturnEventUtil#send(Bridge, Returnable, EventHandler, CloseChecker)
     */
    public static class BooleanCloseChecker implements CloseChecker {

        private final AtomicBoolean flag;

        /**
         * Creates a new boolean close checker which uses the given flag.
         * 
         * @param flag The flag which should be used by the close checker.
         */
        public BooleanCloseChecker(AtomicBoolean flag) {

            this.flag = flag;
        }

        @Override
        public boolean check() {

            return flag.get();
        }

    }

}
