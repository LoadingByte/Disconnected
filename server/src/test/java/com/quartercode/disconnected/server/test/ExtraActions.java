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

package com.quartercode.disconnected.server.test;

import java.util.Collection;
import org.apache.commons.lang3.mutable.Mutable;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;

public class ExtraActions {

    public static StoreArgumentActionBuilder storeArgument(int parameter) {

        return new StoreArgumentActionBuilder(parameter, null);
    }

    private ExtraActions() {

    }

    public static class StoreArgumentActionBuilder {

        private final int                parameter;
        private final StorageListener<?> listener;

        private StoreArgumentActionBuilder(int parameter, StorageListener<?> listener) {

            this.parameter = parameter;
            this.listener = listener;
        }

        public StoreArgumentActionBuilder withListener(StorageListener<?> listener) {

            return new StoreArgumentActionBuilder(parameter, listener);
        }

        public Action in(Mutable<?> reference) {

            return in(new MutableStorage<>(reference));
        }

        public Action in(Collection<?> collection) {

            return in(new CollectionStorage<>(collection));
        }

        public Action in(Storage<?> storage) {

            return new StoreArgumentAction<>(parameter, listener, storage);
        }

    }

    private static class StoreArgumentAction<L, T> extends CustomAction {

        private final int                parameter;
        private final StorageListener<L> listener;
        private final Storage<T>         storage;

        private StoreArgumentAction(int parameter, StorageListener<L> listener, Storage<T> storage) {

            super("stores objects in storage");

            this.parameter = parameter;
            this.listener = listener;
            this.storage = storage;
        }

        @SuppressWarnings ("unchecked")
        @Override
        public Object invoke(Invocation invocation) {

            Object argument = invocation.getParameter(parameter);

            try {
                if (listener == null || listener.accept((L) argument)) {
                    storage.store((T) argument);
                }
            } catch (ClassCastException e) {
                throw new RuntimeException("Method argument '" + argument + "' cannot be stored");
            }

            return null;
        }

    }

    public static interface Storage<T> {

        public void store(T object);

    }

    private static class MutableStorage<T> implements Storage<T> {

        private final Mutable<T> reference;

        private MutableStorage(Mutable<T> reference) {

            this.reference = reference;
        }

        @Override
        public void store(T object) {

            reference.setValue(object);
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

    public static interface StorageListener<T> {

        public boolean accept(T object);

    }

}
