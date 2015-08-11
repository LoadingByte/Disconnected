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

package com.quartercode.disconnected.server.test.sim.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.SortedSet;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import com.quartercode.disconnected.server.sim.gen.GenerationException;
import com.quartercode.disconnected.server.sim.gen.WorldGenerator;
import com.quartercode.disconnected.server.sim.profile.ProfileSerializationException;
import com.quartercode.disconnected.server.sim.profile.ProfileSerializationService;
import com.quartercode.disconnected.server.world.World;
import com.quartercode.disconnected.shared.CommonBootstrap;
import com.quartercode.disconnected.shared.util.ServiceRegistry;
import com.quartercode.disconnected.shared.util.XmlWorkaround;
import com.quartercode.disconnected.shared.util.XmlWorkaround.WorkaroundPropertyType;
import com.quartercode.jtimber.api.node.Node;

public class DefaultProfileSerializationServiceTest {

    @BeforeClass
    public static void setUpBeforeClass() {

        CommonBootstrap.bootstrap();
    }

    @Test
    public void testRoundtripWorld() throws GenerationException, ProfileSerializationException, IOException {

        ProfileSerializationService service = ServiceRegistry.lookup(ProfileSerializationService.class);

        // Roundtrip
        World world = WorldGenerator.generateWorld(new Random(1), 2);
        String serialized = serializeWorldToString(service, world);
        World copy = service.deserializeWorld(new ByteArrayInputStream(serialized.getBytes("UTF-8")));

        // Test 1: Remarshal the already "roundtripped" world copy and compare the result with the first XML document
        String serializedAgain = serializeWorldToString(service, world);
        assertEquals("Serialized-deserialized-serialized XML of world does not equal the original serialized XML", serialized, serializedAgain);

        // Test 1: Manually check that the content of all persistent fields and getter methods has stayed the same
        assertTrue("Serialized-deserialized copy of world does not equal original", nodesEqualPersistent(world, copy));

    }

    private String serializeWorldToString(ProfileSerializationService service, World world) throws ProfileSerializationException, IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        service.serializeWorld(outputStream, world);
        outputStream.flush();
        return new String(outputStream.toByteArray(), "UTF-8");
    }

    /*
     * Method for checking whether the persistent features of the given feature holders are equal to each other.
     */
    private boolean nodesEqualPersistent(Object node1, Object node2) {

        if (node1.getClass() != node2.getClass()) {
            return false;
        }

        try {
            Field[] fields = FieldUtils.getAllFields(node1.getClass());
            Method[] methods = getAllMethods(node1.getClass());
            AccessibleObject.setAccessible(fields, true);
            AccessibleObject.setAccessible(methods, true);

            for (Field field : fields) {
                if (isPersistentElement(field)) {
                    if (!objectsEqualPersistent(field.get(node1), field.get(node2))) {
                        return false;
                    }
                }
            }

            for (Method method : methods) {
                // Also ensure that the method is a getter (it doesn't take any parameters)
                if (method.getParameterTypes().length == 0 && isPersistentElement(method)) {
                    if (!objectsEqualPersistent(method.invoke(node1), method.invoke(node2))) {
                        return false;
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    private Method[] getAllMethods(Class<?> c) {

        List<Method> methods = new ArrayList<>();

        for (Class<?> currentClass : ClassUtils.hierarchy(c)) {
            for (Method method : currentClass.getDeclaredMethods()) {
                methods.add(method);
            }
        }

        return methods.toArray(new Method[methods.size()]);
    }

    private boolean isPersistentElement(AnnotatedElement element) {

        boolean result = false;

        for (Annotation annotation : element.getAnnotations()) {
            if (annotation.annotationType().getName().startsWith("javax.xml.bind.annotation.") && ! (annotation instanceof XmlTransient)) {
                result = true;
                // Don't break since an @XmlWorkaround annotation could change up the result
            } else if (annotation instanceof XmlWorkaround) {
                WorkaroundPropertyType propType = ((XmlWorkaround) annotation).value();
                if (propType == WorkaroundPropertyType.REAL_PROPERTY) {
                    return true;
                } else if (propType == WorkaroundPropertyType.WORKAROUND_PROPERTY) {
                    return false;
                }
            }
        }

        return result;
    }

    private boolean objectsEqualPersistent(Object object1, Object object2) {

        // System.out.println(object1 + " =? " + object2);

        if (object1 == null && object2 == null) {
            return true;
        } else if (object1 == null ^ object2 == null) {
            return false;
        } else if (object1.getClass() != object2.getClass()) {
            return false;
        } else if (object1 instanceof Object[]) {
            return orderedCollectionsEqualPersistent(Arrays.asList((Object[]) object1), Arrays.asList((Object[]) object2));
        } else if (object1 instanceof Collection) {
            Collection<?> collection1 = (Collection<?>) object1;
            Collection<?> collection2 = (Collection<?>) object2;

            if (isOrderImportant(collection1)) {
                return orderedCollectionsEqualPersistent(collection1, collection2);
            } else {
                return unorderedCollectionsEqualPersistent(collection1, collection2);
            }
        } else if (object1 instanceof Map) {
            return mapsEqualPersistent((Map<?, ?>) object1, (Map<?, ?>) object2);
        } else if (object1 instanceof Node) {
            return nodesEqualPersistent(object1, object2);
        } else {
            return object1 != null && object1.equals(object2);
        }
    }

    private boolean isOrderImportant(Collection<?> collection) {

        return collection instanceof List || collection instanceof Queue || collection instanceof SortedSet;
    }

    private boolean orderedCollectionsEqualPersistent(Collection<?> collection1, Collection<?> collection2) {

        if (collection1.size() != collection2.size()) {
            return false;
        } else {
            Iterator<?> iterator1 = collection1.iterator();
            Iterator<?> iterator2 = collection2.iterator();

            while (iterator1.hasNext()) {
                if (!objectsEqualPersistent(iterator1.next(), iterator2.next())) {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean unorderedCollectionsEqualPersistent(Collection<?> collection1, Collection<?> collection2) {

        if (collection1.size() != collection2.size()) {
            return false;
        } else {
            for (Object element1 : collection1) {
                boolean contains = false;

                for (Object element2 : collection2) {
                    if (objectsEqualPersistent(element1, element2)) {
                        contains = true;
                        break;
                    }
                }

                if (!contains) {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean mapsEqualPersistent(Map<?, ?> map1, Map<?, ?> map2) {

        if (map1.size() != map2.size()) {
            return false;
        } else {
            for (Entry<?, ?> entry1 : map1.entrySet()) {
                boolean contains = false;

                for (Entry<?, ?> entry2 : map2.entrySet()) {
                    if (objectsEqualPersistent(entry1.getKey(), entry2.getKey()) && objectsEqualPersistent(entry1.getValue(), entry2.getValue())) {
                        contains = true;
                        break;
                    }
                }

                if (!contains) {
                    return false;
                }
            }

            return true;
        }
    }

}
