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

package com.quartercode.disconnected.client.util;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;

/**
 * The twl spritesheet generator generates spritesheets from single sprites and a twl theme file that describes those spritesheets.
 * The resulting twl config contains references to the locations of the sprites on the spritesheets.
 * By including the twl config into another theme file, all sprites become available.
 * Internally, this class uses the libgdx {@link TexturePacker} for creating the spritesheets.
 */
public class TWLSpritesheetGenerator {

    private static final Logger                 LOGGER                = LoggerFactory.getLogger(TWLSpritesheetGenerator.class);

    private static final String                 SPRITESHEET_FILE_NAME = "spritesheet";
    private static final TexturePacker.Settings PACKER_SETTINGS       = new TexturePacker.Settings();

    static {

        PACKER_SETTINGS.fast = true;
        PACKER_SETTINGS.combineSubdirectories = true;
        PACKER_SETTINGS.useIndexes = false;

    }

    /**
     * Packs the sprites which are located inside the given sprites directory and creates spritesheets and a twl config inside the given output directory.
     * The resulting twl config contains references to the locations of the sprites on the spritesheets.
     * Note that large sprites are not put onto spritesheets. Instead, they are referenced directly.<br>
     * <br>
     * If the sprites directory contains subdirectories, the sprites from all subdirectories are packed as well.
     * Such sprites get a special prefix to mark which sprite was located inside which directory.
     * For example, the sprite in {@code default/button/background.png} would get the name {@code default_button_background}.<br>
     * <br>
     * A config file with the name {@code sprites.xml} can be used inside each (sub)directory to configure the sprites.
     * Here are some example for such a config file:
     * 
     * <pre>
     * &lt;sprites&gt;
     *     &lt;!-- Sets the attributes <i>splitx</i> and <i>splity</i> for the sprite "button.background" --&gt;
     *     &lt;!-- They will be added to the area definition --&gt;
     *     <b>&lt;sprite name="button.background" splitx="L1,R1" splity="T1,B1" /&gt;</b>
     * 
     *     &lt;!-- Creates 32-pixel-wide sprites by iterating over the set image horizontally --&gt;
     *     &lt;!-- Each times 32 pixels have passed on the x-axis, a new sprite is started --&gt;
     *     &lt;!-- The resulting sprites have the names button2.background$0, button2.background$1, ... --&gt;
     *     <b>&lt;spriteArray name="button2.background" itemLength="32" /&gt;</b>
     * 
     *     &lt;!-- Creates a new animation with the given name --&gt;
     *     &lt;!-- It adds all sprites from the array <i>button2.background</i> to the animation --&gt;
     *     <b>&lt;animation name="button2.background.anim" timeSource="hover"&gt;
     *         &lt;frameArray spriteArray="button2.background" duration="100" /&gt;
     *     &lt;/animation&gt;</b>
     * 
     *     &lt;!-- Creates a new animation and adds all array sprites from index 3 to 12 (indices start at 0) --&gt;
     *     &lt;!-- Note that a non-existing start or end attribute will default the values to the start/end of the sprite array --&gt;
     *     &lt;animation name="button2.background.anim" timeSource="hover"&gt;
     *         &lt;frameArray spriteArray="button2.background" <b>start="3" end="12</b> duration="100" /&gt;
     *     &lt;/animation&gt;
     * 
     *     &lt;!-- Animation that first displays <i>button.background</i> for 1 second --&gt;
     *     &lt;!-- and then plays the whole sprite array <i>button2.background</i> --&gt;
     *     &lt;animation name="button2.background.anim" timeSource="hover"&gt;
     *         <b>&lt;frame sprite="button.background" duration="1000" /&gt;</b>
     *         &lt;frameArray spriteArray="button2.background" duration="100" /&gt;
     *     &lt;/animation&gt;
     * 
     *     &lt;!-- Animation that first displays <i>button.background</i> for 1 second --&gt;
     *     &lt;!-- and then plays the whole sprite array <i>button2.background</i> 3 times --&gt;
     *     &lt;animation name="button2.background.anim" timeSource="hover"&gt;
     *         &lt;frame sprite="button.background" duration="1000" /&gt;
     *         &lt;frameArray spriteArray="button2.background" <b>reps="3"</b> duration="100" /&gt;
     *     &lt;/animation&gt;
     * 
     *     &lt;!-- Animation that first plays the whole sprite array <i>button2.background</i> --&gt;
     *     &lt;!-- and then displays a <i>button.background</i> which flashes red 5 times --&gt;
     *     &lt;animation name="button2.background.anim" timeSource="hover"&gt;
     *         &lt;frameArray spriteArray="button2.background" duration="100" /&gt;
     *         <b>&lt;repeat reps="5"&gt;</b>
     *             &lt;frame sprite="button.background" duration="1000" tint="#000" /&gt;
     *             &lt;frame sprite="button.background" duration="1000" tint="#F00" /&gt;
     *         <b>&lt;/repeat&gt;</b>
     *     &lt;/animation&gt;
     * &lt;/sprites&gt;
     * </pre>
     * 
     * Note that not all normal sprites have to be defined in the sprites config.
     * The sprites which are not mentioned are plain twl areas that have no special attributes.
     * 
     * @param spritesDir The directory which contains the sprites for packing.
     *        Note that this {@link Path} should point to a plain directory and not to a zip/jar file.
     * @param outputDir The directory where the method writes the resulting spritesheets and the twl config.
     * @return The generated twl config.
     *         It can be included into other twl theme files for using the sprites.
     * @throws IOException An error occurs while packing the sprites or doing some other file operation.
     */
    public static Path generate(Path spritesDir, Path outputDir) throws IOException {

        // Index all sprites by checking whether they are "small" or "large"
        // That is required because only small sprites should be packed into spritesheets
        // Moreover, all available twl writers are parsed
        Triple<List<Path>, Map<Path, Sprite>, List<TWLWriter>> spriteIndex = indexSprites(spritesDir, PACKER_SETTINGS.maxWidth / 2, PACKER_SETTINGS.maxHeight / 2);
        List<Path> smallSpriteFiles = spriteIndex.getLeft();
        Map<Path, Sprite> largeSprites = spriteIndex.getMiddle();
        List<TWLWriter> twlWriters = spriteIndex.getRight();

        // Pack the small sprites
        LOGGER.debug("Packing small sprites from '{}' to '{}'", spritesDir, outputDir);
        TexturePacker texturePacker = new TexturePacker(spritesDir.toFile(), PACKER_SETTINGS);
        for (Path smallSpriteFile : smallSpriteFiles) {
            texturePacker.addImage(smallSpriteFile.toFile());
        }
        texturePacker.pack(outputDir.toFile(), SPRITESHEET_FILE_NAME);

        // Read the resulting texture atlas file for locating the packed sprites and convert the atlas to a sprite list
        Path smallSpriteAtlasFile = outputDir.resolve(SPRITESHEET_FILE_NAME + ".atlas");
        LOGGER.debug("Reading texture atlas from '{}'", smallSpriteAtlasFile);
        TextureAtlasData smallSpriteAtlas = new TextureAtlasData(new FileHandle(smallSpriteAtlasFile.toFile()), new FileHandle(outputDir.toFile()), false);
        Map<Path, List<Sprite>> smallSprites = mapPagesToSprites(smallSpriteAtlas);

        // Write a twl theme config file that defines the sprites using the writers
        Path twlConfigFile = outputDir.resolve("twlConfig.xml");
        LOGGER.debug("Writing twl config to '{}'", twlConfigFile);
        writeTWLConfig(smallSprites, largeSprites, twlWriters, Files.newBufferedWriter(twlConfigFile, Charset.forName("UTF-8")));

        return twlConfigFile;
    }

    /*
     * This method walks over the sprites dir tree and determines which sprites are small and which ones are large.
     * Large sprites are directly converted into Sprite objects while only the path of small sprites is returned.
     * The method also reads all sprites.xml configuration files and creates TWLWriter objects.
     * 
     * Return triple: [smallSpriteFiles, largeSprites, twlWriters]
     */
    private static Triple<List<Path>, Map<Path, Sprite>, List<TWLWriter>> indexSprites(final Path spritesDir, final int thresholdWidth, final int thresholdHeight) throws IOException {

        LOGGER.debug("Indexing sprites in '{}'", spritesDir);

        final List<Path> smallSpriteFiles = new ArrayList<>();
        final Map<Path, Sprite> largeSprites = new HashMap<>();
        final List<TWLWriter> writers = new ArrayList<>();

        // Walk over all files the sprites dir recursively
        Files.walkFileTree(spritesDir, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                if (file.getFileName().toString().equals("sprites.xml")) {
                    // Process a sprites.xml configuration file
                    try {
                        Document document = new SAXBuilder().build(file.toFile());
                        processConfigXML(document, spritesDir, file, writers);
                    } catch (JDOMException e) {
                        LOGGER.warn("Error while trying to parse sprite xml file under '{}'", file);
                    }
                } else {
                    BufferedImage sourceImage = ImageIO.read(Files.newInputStream(file));
                    // Check whether the source file is an image
                    if (sourceImage != null) {
                        if (sourceImage.getWidth() <= thresholdWidth && sourceImage.getHeight() <= thresholdHeight) {
                            // If the source file matches the specified dimensions, add it to the smallSprites list
                            smallSpriteFiles.add(file);
                        } else {
                            // If the source file does not match, add it to the largeSprites list
                            String spriteName = StringUtils.substringBeforeLast(spritesDir.relativize(file).toString(), ".");
                            Rectangle bounds = new Rectangle(0, 0, sourceImage.getWidth(), sourceImage.getHeight());
                            largeSprites.put(file, new Sprite(spriteName, bounds));
                        }
                    }
                }

                return FileVisitResult.CONTINUE;
            }

        });

        return Triple.of(smallSpriteFiles, largeSprites, writers);
    }

    /*
     * This method takes a sprites.xml configuration document and adds all parsed twl writers to the given output list.
     */
    private static void processConfigXML(Document document, Path spritesDir, Path documentFile, List<TWLWriter> output) {

        Path relativeSpritesDir = spritesDir.relativize(documentFile.getParent());

        for (Element flElement : document.getRootElement().getChildren()) {
            String flElementName = flElement.getName();

            boolean flSprite = flElementName.equals("sprite");
            boolean flSpriteArray = flElementName.equals("spriteArray");
            boolean flAnimation = flElementName.equals("animation");

            if (flSprite || flSpriteArray || flAnimation) {
                // Retrieve the name of the first level element
                String localName = flElement.getAttributeValue("name");

                if (localName == null) {
                    LOGGER.warn("Sprites config: Sprite name for '{}' element inside '{}' is unset", flElementName, documentFile);
                } else {
                    // Retrieve the global name of the first level element
                    // If the config file could be found under default/button/config.xml and the local name would be background,
                    // the resulting global name would be default/button/background
                    String globalName = relativeSpritesDir.resolve(localName).toString();

                    // Parse a SingleSpriteWriter
                    if (flSprite) {
                        Map<String, String> attributes = createXMLAttributeMap(flElement.getAttributes(), "name");
                        output.add(new SingleSpriteWriter(globalName, attributes));
                    }
                    // Parse a SpriteArrayWriter
                    else if (flSpriteArray) {
                        String itemLength = flElement.getAttributeValue("itemLength");

                        if (!StringUtils.isNumeric(itemLength)) {
                            LOGGER.warn("Sprites config: Item length ('{}') for sprite array '{}' is not a positive integer", itemLength, globalName);
                        } else {
                            Map<String, String> attributes = createXMLAttributeMap(flElement.getAttributes(), "name", "itemLength");
                            output.add(new SpriteArrayWriter(globalName, Integer.parseInt(itemLength), attributes));
                        }
                    }
                    // Parse an AnimationWriter
                    else if (flAnimation) {
                        output.add(new AnimationWriter(globalName, flElement, relativeSpritesDir));
                    }
                }
            } else {
                LOGGER.warn("Sprites config: Unknown first level element '{}' inside '{}'", flElementName, documentFile);
            }
        }
    }

    /*
     * This method takes a list of xml dom attributes and creates a [String -> String] map that contains them.
     * It is possible to exclude certain attributes from the mapping.
     */
    private static Map<String, String> createXMLAttributeMap(List<Attribute> attributes, String... exclusions) {

        List<String> exclusionList = Arrays.asList(exclusions);
        Map<String, String> attributeMap = new HashMap<>();

        for (Attribute attribute : attributes) {
            if (!exclusionList.contains(attribute.getName())) {
                attributeMap.put(attribute.getName(), attribute.getValue());
            }
        }

        return attributeMap;
    }

    /*
     * This method maps each spritesheet page file, which is defined inside a texture atlas, to all the sprites it contains.
     */
    private static Map<Path, List<Sprite>> mapPagesToSprites(TextureAtlasData atlas) {

        Map<Path, List<Sprite>> pageSprites = new HashMap<>();

        for (Region region : atlas.getRegions()) {
            Path page = Paths.get(region.page.textureFile.path());

            if (!pageSprites.containsKey(page)) {
                pageSprites.put(page, new ArrayList<Sprite>());
            }

            Rectangle spriteBounds = new Rectangle(region.left, region.top, region.width, region.height);
            pageSprites.get(page).add(new Sprite(region.name, spriteBounds));
        }

        return pageSprites;
    }

    /*
     * This method finally writes a twl theme config.
     * Firstly, it lets the given sprite writers define the given small and large sprites.
     * If there's no sprite writer for a certain sprite, a default sprite writer is used.
     * Note that each sprite is mapped to the file that contain it.
     * Afterwards, each independent writer is ran, outputting sprite-independent information.
     */
    private static void writeTWLConfig(Map<Path, List<Sprite>> smallSprites, Map<Path, Sprite> largeSprites, List<TWLWriter> writers, Writer output) throws IOException {

        // Write header
        output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        output.write("<!DOCTYPE themes PUBLIC \"-//www.matthiasmann.de//TWL-Theme//EN\" ");
        output.write("\"http://hg.l33tlabs.org/twl/raw-file/tip/src/de/matthiasmann/twl/theme/theme.dtd\">");
        output.write("<themes>");

        // Write all pages
        for (Entry<Path, List<Sprite>> page : smallSprites.entrySet()) {
            // Write images start element
            output.write("<images file=\"" + page.getKey().toAbsolutePath() + "\">");

            // Write all regions
            for (Sprite sprite : page.getValue()) {
                getSpriteWriter(writers, sprite.name).write(sprite, false, output);
            }

            // Write images end element
            output.write("</images>");
        }

        // Write all large sprites
        for (Entry<Path, Sprite> largeSprite : largeSprites.entrySet()) {
            output.write(" <images file=\"" + largeSprite.getKey().toAbsolutePath() + "\">");
            getSpriteWriter(writers, largeSprite.getValue().name).write(largeSprite.getValue(), true, output);
            output.write("</images>");
        }

        // Write the start of the empty image element for the independent writers
        output.write("<images>");

        // Let all independent writers write their content
        List<TWLWriter> unmodifiableWriters = Collections.unmodifiableList(writers);
        for (TWLWriter writer : writers) {
            if (writer instanceof IndependentWriter) {
                ((IndependentWriter) writer).write(unmodifiableWriters, output);
            }
        }

        // Write the end of the empty image element for the independent writers
        output.write("</images>");

        // Write footer
        output.write("</themes>");

        // Flush the stream
        output.flush();
    }

    /*
     * This method tries to find a sprite writer for the given sprite.
     * If there's no sprite writer available for that sprite, a default sprite writer is returned.
     */
    private static SpriteWriter getSpriteWriter(List<TWLWriter> writers, String spriteName) {

        for (TWLWriter writer : writers) {
            if (writer instanceof SpriteWriter && ((SpriteWriter) writer).getSpriteName().equals(spriteName)) {
                return (SpriteWriter) writer;
            }
        }

        // Default writer
        return new SingleSpriteWriter(spriteName, new HashMap<String, String>());
    }

    // ----- Writer utilities -----

    private static String toXYWH(Rectangle bounds) {

        return bounds.x + "," + bounds.y + "," + bounds.width + "," + bounds.height;
    }

    private static void writeAttributes(Map<String, String> attributes, Writer output) throws IOException {

        for (Entry<String, String> attribute : attributes.entrySet()) {
            output.write(" " + attribute.getKey() + "=\"" + attribute.getValue() + "\"");
        }
    }

    private TWLSpritesheetGenerator() {

    }

    @RequiredArgsConstructor
    private static class Sprite {

        private final String    name;
        private final Rectangle bounds;

    }

    // Marker interface
    private static interface TWLWriter {

    }

    private static interface SpriteWriter extends TWLWriter {

        public String getSpriteName();

        public void write(Sprite sprite, boolean large, Writer output) throws IOException;

    }

    private static interface IndependentWriter extends TWLWriter {

        public void write(List<TWLWriter> otherWriters, Writer output) throws IOException;

    }

    @RequiredArgsConstructor
    private static class SingleSpriteWriter implements SpriteWriter {

        @Getter
        private final String              spriteName;
        private final Map<String, String> attributes;

        @Override
        public void write(Sprite sprite, boolean large, Writer output) throws IOException {

            output.write("<area name=\"" + spriteName.replace("/", "_") + "\"");
            output.write(" xywh=\"" + (large ? "*" : toXYWH(sprite.bounds)) + "\"");
            writeAttributes(attributes, output);
            output.write(" />");
        }

    }

    @RequiredArgsConstructor
    private static class SpriteArrayWriter implements SpriteWriter {

        @Getter
        private final String              spriteName;
        private final int                 itemLength;
        private final Map<String, String> attributes;

        // "Public" storage variable
        private int                       maxIndex;

        @Override
        public void write(Sprite sprite, boolean large, Writer output) throws IOException {

            String baseName = spriteName.replace("/", "_");
            Rectangle bounds = sprite.bounds;
            maxIndex = (int) Math.floor((double) bounds.width / (double) itemLength);

            // Iterate over all indices which are possible for the array and create an area element for each one
            // The offset on the x-axis is calculated by multiplying the length of each item with the current index
            for (int index = 0; index < maxIndex; index++) {
                int xOffset = index * itemLength;
                int x = bounds.x + xOffset;

                output.write("<area name=\"" + baseName + "$" + index + "\"");
                output.write(" xywh=\"" + toXYWH(new Rectangle(x, bounds.y, itemLength, bounds.height)) + "\"");
                writeAttributes(attributes, output);
                output.write(" />");
            }
        }

    }

    @RequiredArgsConstructor
    static class AnimationWriter implements IndependentWriter {

        private final String  animationName;
        private final Element root;
        private final Path    relativeConfigDir;

        @Override
        public void write(List<TWLWriter> otherWriters, Writer output) throws IOException {

            // Write the start of the base animation element
            output.write("<animation name=\"" + animationName.replace("/", "_") + "\"");
            writeAttributes(createXMLAttributeMap(root.getAttributes(), "name"), output);
            output.write(">");

            for (Element element : root.getChildren()) {
                convertElementAndWrite(element, otherWriters, output);
            }

            // Write the end of the base animation element
            output.write("</animation>");
        }

        private void convertElementAndWrite(Element element, List<TWLWriter> otherWriters, Writer output) throws IOException {

            // Parse the repetitions value; if it is not set, 1 is used
            String repetitionsString = element.getAttributeValue("reps");
            int repetitions = 1;
            if (!StringUtils.isBlank(repetitionsString)) {
                try {
                    repetitions = Integer.parseInt(repetitionsString);
                } catch (NumberFormatException e) {
                    LOGGER.warn("Sprites config: Repetitions ('{}') for animation '{}' is not a positive integer", repetitionsString, animationName);
                }
            }
            boolean repeat = repetitions > 1;

            // If the current element is set to repeat, write the start of the repeat element and the amount of repetitions
            if (repeat) {
                output.write("<repeat count=\"" + repetitions + "\">");
            }

            String elementName = element.getName();

            // Single frame
            if (elementName.equals("frame")) {
                String spriteName = element.getAttributeValue("sprite");

                if (StringUtils.isBlank(spriteName)) {
                    LOGGER.warn("Sprites config: A 'frame' element for animation '{}' does not have a non-blank 'sprite' attribute", animationName);
                } else {
                    Map<String, String> attributes = createXMLAttributeMap(element.getAttributes(), "sprite", "reps");
                    // Write the single frame
                    writeSingleFrame(resolveGlobalSpriteName(spriteName).replace("/", "_"), attributes, output);
                }
            }
            // Frame array using a sprite array
            else if (elementName.equals("frameArray")) {
                String spriteArrayName = element.getAttributeValue("spriteArray");
                String startString = element.getAttributeValue("start");
                String endString = element.getAttributeValue("end");

                if (StringUtils.isBlank(spriteArrayName)) {
                    LOGGER.warn("Sprites config: A 'frameArray' element for animation '{}' does not have a non-blank 'spriteArray' attribute", animationName);
                } else if (!StringUtils.isBlank(startString) && !StringUtils.isNumeric(startString)) {
                    LOGGER.warn("Sprites config: Start index ('{}') for animation '{}' is not zero or a positive integer", startString, animationName);
                } else if (!StringUtils.isBlank(endString) && !StringUtils.isNumeric(endString)) {
                    LOGGER.warn("Sprites config: End index ('{}') for animation '{}' is not zero or a positive integer", endString, animationName);
                } else {
                    // Resolve the global name of the sprite array
                    spriteArrayName = resolveGlobalSpriteName(spriteArrayName);

                    int start = StringUtils.isBlank(startString) ? 0 : Integer.parseInt(startString);
                    int end = 0;
                    if (!StringUtils.isBlank(endString)) {
                        end = Integer.parseInt(endString);
                    } else {
                        TWLWriter spriteArrayWriter = getSpriteWriter(otherWriters, spriteArrayName);
                        if (! (spriteArrayWriter instanceof SpriteArrayWriter)) {
                            LOGGER.warn("Sprites config: Sprite array '{}', referenced by animation '{}', does not exist", spriteArrayName, animationName);
                        } else {
                            end = ((SpriteArrayWriter) spriteArrayWriter).maxIndex - 1;
                        }
                    }

                    if (start >= end) {
                        LOGGER.warn("Sprites config: Start index ('{}') is >= end index ('{}') for animation '{}'", start, end, animationName);
                    } else {
                        Map<String, String> attributes = createXMLAttributeMap(element.getAttributes(), "spriteArray", "start", "end", "reps");
                        // Write all defined frames, one after another
                        for (int index = start; index <= end; index++) {
                            writeSingleFrame(spriteArrayName.replace("/", "_") + "$" + index, attributes, output);
                        }
                    }
                }
            }
            // Repetition for a wrapped element
            else if (elementName.equals("repeat")) {
                if (element.getChildren().isEmpty()) {
                    LOGGER.warn("Sprites config: A 'repeat' element for animation '{}' does not contain any child elements", animationName);
                } else {
                    // Write the content of the repeat element
                    // Note that a wrapping twl repeat element, which actually causes the repetition, has already been written at the top of this method
                    for (Element childElement : element.getChildren()) {
                        convertElementAndWrite(childElement, otherWriters, output);
                    }
                }
            }

            // If the current element is set to repeat, write the end of the repeat element
            if (repeat) {
                output.write("</repeat>");
            }
        }

        private void writeSingleFrame(String frameSprite, Map<String, String> attributes, Writer output) throws IOException {

            output.write("<frame ref=\"" + frameSprite + "\"");
            writeAttributes(attributes, output);
            output.write(" />");
        }

        private String resolveGlobalSpriteName(String spriteName) {

            if (spriteName.startsWith("/")) {
                return StringUtils.stripStart(spriteName, "/");
            } else {
                return relativeConfigDir.resolve(spriteName).toString();
            }
        }

    }

}
