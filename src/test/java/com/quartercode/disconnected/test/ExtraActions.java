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
/*
 * This file is part of EventBridge.
 * Copyright (c) 2014 QuarterCode <http://www.quartercode.com/>
 *
 * EventBridge is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * EventBridge is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EventBridge. If not, see <http://www.gnu.org/licenses/>.
 */

package com.quartercode.disconnected.test;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;

public class ExtraActions {

    public static StoreArgumentActionBuilder storeArgument(int parameter) {

        return new StoreArgumentActionBuilder(parameter);
    }

    private ExtraActions() {

    }

    public static class StoreArgumentActionBuilder {

        private final int parameter;

        private StoreArgumentActionBuilder(int parameter) {

            this.parameter = parameter;
        }

        public Action in(AtomicReference<?> reference) {

            return in(new AtomicReferenceStorage<>(reference));
        }

        public Action in(Collection<?> collection) {

            return in(new CollectionStorage<>(collection));
        }

        public Action in(Storage<?> storage) {

            return new StoreArgumentAction<>(parameter, storage);
        }

    }

    private static class StoreArgumentAction<T> extends CustomAction {

        private final int        parameter;
        private final Storage<T> storage;

        private StoreArgumentAction(int parameter, Storage<T> storage) {

            super("stores objects in storage");

            this.parameter = parameter;
            this.storage = storage;
        }

        @SuppressWarnings ("unchecked")
        @Override
        public Object invoke(Invocation invocation) {

            Object argument = invocation.getParameter(parameter);
            try {
                storage.store((T) argument);
            } catch (ClassCastException e) {
                throw new RuntimeException("Method argument '" + argument + "' cannot be stored");
            }

            return null;
        }

    }

    public static interface Storage<T> {

        public void store(T object);

    }

    private static class AtomicReferenceStorage<T> implements Storage<T> {

        private final AtomicReference<T> reference;

        private AtomicReferenceStorage(AtomicReference<T> reference) {

            this.reference = reference;
        }

        @Override
        public void store(T object) {

            reference.set(object);
        }

    }

    private static class CollectionStorage<T> implements Storage<T> {

        private final Collection<T> collection;

        private CollectionStorage(Collection<T> collection) {

            this.collection = collection;
        }

        @Override
        public void store(T object) {

            collection.add(object);
        }

    }

}
