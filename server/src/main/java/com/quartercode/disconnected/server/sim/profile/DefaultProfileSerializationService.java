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

package com.quartercode.disconnected.server.sim.profile;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.quartercode.classmod.base.FeatureDefinition;
import com.quartercode.classmod.base.FeatureHolder;
import com.quartercode.classmod.util.TreeInitializer;
import com.quartercode.disconnected.server.registry.PersistentClassScanDirective;
import com.quartercode.disconnected.server.registry.PersistentClassScanDirective.ScanMethod;
import com.quartercode.disconnected.server.registry.ServerRegistries;
import com.quartercode.disconnected.server.world.World;
import com.quartercode.disconnected.shared.util.ClasspathScanningUtils;
import com.quartercode.disconnected.shared.util.XmlPersistent;
import com.quartercode.disconnected.shared.util.registry.Registries;
import com.quartercode.disconnected.shared.util.registry.extra.MappedValueRegistry.Mapping;

/**
 * This is the default implementation of the {@link ProfileSerializationService}.
 * 
 * @see ProfileSerializationService
 */
public class DefaultProfileSerializationService implements ProfileSerializationService {

    private static final Logger         LOGGER            = LoggerFactory.getLogger(DefaultProfileSerializationService.class);

    private static Collection<Class<?>> persistentClasses = new HashSet<>();

    /**
     * Scans the classpath for persistent {@link Class}es as defined by the given {@link PersistentClassScanDirective}.
     * The resulting classes are then added to the internal storage, whose contents are used by {@link #createWorldContext()} as bound classes.<br>
     * <br>
     * Note that all exceptions, which occur during the scanning process, are only logged as errors.
     * That way, small exceptions like inaccessible <i>unimportant</i> directories, do not cause the whole game to halt.
     * If an exception doesn't cause any damage, it is just "ignored" and only logged.
     * 
     * @param scanDirective The scanning directive which defines the exact scanning process.
     */
    public void scanForPersistentClasses(PersistentClassScanDirective scanDirective) {

        String packageName = scanDirective.getPackageName();
        ScanMethod method = scanDirective.getMethod();

        LOGGER.debug("Scanning package '{}' using method '{}'", packageName, method.getName());

        // Recursive annotation search
        if (method == ScanMethod.RECURSIVE_ANNOTATION_SEARCH) {
            recursiveAnnotationSearch(packageName);
        }
        // JAXB index lookup
        else if (method == ScanMethod.JAXB_INDEX_LOOKUP) {
            jaxbIndexLookup(packageName);
        }
    }

    private void recursiveAnnotationSearch(String rootPackage) {

        try {
            // Iterate over the package and all subpackages
            for (String childPackage : ClasspathScanningUtils.getSubpackages(rootPackage, true, false)) {
                try {
                    // Iterate over all classes in those packages
                    for (Class<?> c : ClasspathScanningUtils.getPackageClasses(childPackage, false)) {
                        // Check whether the class is a valid persistent class
                        if (c.isAnnotationPresent(XmlPersistent.class) && !c.isInterface()) {
                            // Add the found class to the list
                            persistentClasses.add(c);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("Cannot read any classes from package '{}'", childPackage, e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Cannot list subpackages of package '{}'", rootPackage, e);
        }
    }

    private void jaxbIndexLookup(String packageName) {

        try {
            // Add the all found classes to the list
            persistentClasses.addAll(ClasspathScanningUtils.getIndexedPackageClasses(packageName, "jaxb.index", false, false));
        } catch (IOException e) {
            LOGGER.error("Cannot read any classes from package '{}'", packageName, e);
        }
    }

    // ----- Profile -----

    @Override
    public void serializeProfile(OutputStream outputStream, Profile profile) throws ProfileSerializationException {

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            zipOutputStream.putNextEntry(new ZipEntry("world.xml"));
            serializeWorld(zipOutputStream, profile.getWorld());
            zipOutputStream.closeEntry();

            zipOutputStream.putNextEntry(new ZipEntry("random.ser"));
            serializeRandom(zipOutputStream, profile.getRandom());
            zipOutputStream.closeEntry();
        } catch (IOException e) {
            throw new ProfileSerializationException("Cannot serialize profile '" + profile.getName() + "' to output stream", e);
        }
    }

    @Override
    public void deserializeProfile(InputStream inputStream, Profile target) throws ProfileSerializationException {

        World world = null;
        Random random = null;

        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry zipEntry = null;
            while ( (zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.getName().equals("world.xml")) {
                    world = deserializeWorld(zipInputStream);
                } else if (zipEntry.getName().equals("random.ser")) {
                    random = deserializeRandom(zipInputStream);
                }
            }
        } catch (IOException e) {
            throw new ProfileSerializationException("Cannot deserialize profile '" + target.getName() + "' from input stream", e);
        }

        if (world == null) {
            throw new ProfileSerializationException("No valid world object found while deserializing profile '" + target.getName() + "'");
        } else if (random == null) {
            throw new ProfileSerializationException("No valid Random object found while deserializing profile '" + target.getName() + "'");
        }

        target.setWorld(world);
        target.setRandom(random);
    }

    // ----- World -----

    @Override
    public JAXBContext createWorldContext() throws ProfileSerializationException {

        try {
            Class<?>[] classArray = persistentClasses.toArray(new Class[persistentClasses.size()]);
            return JAXBContext.newInstance(classArray);
        } catch (JAXBException e) {
            throw new ProfileSerializationException("Cannot create new JAXB context with dynamically resolved bound classes", e);
        }
    }

    @Override
    public void serializeWorld(OutputStream outputStream, World world) throws ProfileSerializationException {

        try {
            Marshaller marshaller = createWorldContext().createMarshaller();
            // Turn this option on for debugging:
            // marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(world, outputStream);
        } catch (JAXBException e) {
            throw new ProfileSerializationException("Cannot marshal input world object as XML", e);
        }
    }

    @Override
    public World deserializeWorld(InputStream inputStream) throws ProfileSerializationException {

        // Unmarshal world
        World world;
        try {
            world = (World) createWorldContext().createUnmarshaller().unmarshal(new NoCloseInputStream(inputStream));
        } catch (JAXBException e) {
            throw new ProfileSerializationException("Cannot unmarshal world object from input XML", e);
        }

        // Initialize world
        TreeInitializer worldInitializer = new TreeInitializer();
        for (Mapping<Class<? extends FeatureHolder>, FeatureDefinition<?>> mapping : Registries.get(ServerRegistries.WORLD_INITIALIZER_MAPPINGS)) {
            worldInitializer.addInitializationDefinition(mapping.getLeft(), mapping.getRight());
        }
        worldInitializer.apply(world);

        return world;
    }

    // ----- Random -----

    @Override
    public void serializeRandom(OutputStream outputStream, Random random) throws ProfileSerializationException {

        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(random);
            objectOutputStream.flush();
        } catch (IOException e) {
            throw new ProfileSerializationException("Cannot serialize input Random object to output stream", e);
        }
    }

    @Override
    public Random deserializeRandom(InputStream inputStream) throws ProfileSerializationException {

        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            return (Random) objectInputStream.readObject();
        } catch (IOException e) {
            throw new ProfileSerializationException("Cannot deserialize Random object from input stream", e);
        } catch (ClassNotFoundException e) {
            throw new ProfileSerializationException("Cannot find a certain class which is used by the serialized Random object", e);
        }
    }

    private static class NoCloseInputStream extends FilterInputStream {

        private NoCloseInputStream(InputStream inputStream) {

            super(inputStream);
        }

        @Override
        public void close() {

            // Do nothing
        }

    }

}
