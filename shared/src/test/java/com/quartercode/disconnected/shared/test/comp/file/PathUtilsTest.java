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

package com.quartercode.disconnected.shared.test.comp.file;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.quartercode.disconnected.shared.comp.file.PathUtils;

public class PathUtilsTest {

    @Test
    public void testNormalizePath() {

        assertEquals("Resolved path of relative one", "/user/homes/test", PathUtils.normalize("user/homes/test"));
        assertEquals("Resolved path of relative one", "/user/homes/test", PathUtils.normalize("user/homes/test/"));
        assertEquals("Resolved path of relative one", "/system/bin", PathUtils.normalize("user/homes/test/../test2/../../../system/bin"));

        assertEquals("Resolved path of absolute one", "/user/homes/test", PathUtils.normalize("/user/homes/test/"));
        assertEquals("Resolved path of absolute one", "/system/bin", PathUtils.normalize("/user/homes/test/../test2/../../../system/bin"));

        assertEquals("Resolved path of relative one", "/", PathUtils.normalize(""));
        assertEquals("Resolved path of relative one", "/", PathUtils.normalize("/"));
        assertEquals("Resolved path of relative one", "/", PathUtils.normalize("../"));
    }

    @Test
    public void testResolvePath() {

        assertEquals("Resolved path of relative one", "/user/homes/test2/docs", PathUtils.resolve("/user/homes/test/", "../test2/docs/"));
        assertEquals("Resolved path of relative one", "/system/bin/kernel", PathUtils.resolve("/user/homes/test/", "../../../system/bin/kernel"));
        assertEquals("Resolved path of relative one", "/", PathUtils.resolve("/user/homes/test/", "../../../"));

        assertEquals("Resolved path of absolute one", "/system/bin/kernel", PathUtils.resolve("/user/homes/test/", "/system/bin/kernel"));
        assertEquals("Resolved path of absolute one", "/system/bin/kernel", PathUtils.resolve("/", "/system/bin/kernel/"));
        assertEquals("Resolved path of absolute one", "/system/bin/kernel", PathUtils.resolve("/", "/system//bin/kernel"));
        assertEquals("Resolved path of absolute one", "/system/bin/kernel", PathUtils.resolve("/", "/system//bin/kernel/"));

        assertEquals("Resolved path of relative one", "/", PathUtils.resolve("/", ""));
        assertEquals("Resolved path of relative one", "/", PathUtils.resolve("/", "/"));
        assertEquals("Resolved path of relative one", "/", PathUtils.resolve("/", "../"));
        assertEquals("Resolved path of relative one", "/", PathUtils.resolve("/user", "../../"));
    }

    @Test
    public void testSplit() {

        assertArrayEquals("Split path array", new String[] { "system", "etc", "test" }, PathUtils.split("system/etc/test"));
        assertArrayEquals("Split path array", new String[] { "system", "etc", "test" }, PathUtils.split("/system/etc/test"));
        assertArrayEquals("Split path array", new String[] { "system", "etc", "test" }, PathUtils.split("system/etc/test/"));
        assertArrayEquals("Split path array", new String[] { "system", "etc", "test" }, PathUtils.split("/system/etc/test/"));

        assertArrayEquals("Split path array", new String[] { "system", "etc", "..", "test" }, PathUtils.split("system/etc/../test"));
        assertArrayEquals("Split path array", new String[] { "system", "etc", "test" }, PathUtils.split("system/etc///test"));
    }

    @Test
    public void testJoin() {

        assertEquals("Joined path string", "/system/etc/test", PathUtils.join(new String[] { "system", "etc", "test" }));
        assertEquals("Joined path string", "/system/etc/test/", PathUtils.join(new String[] { "system", "etc", "test/" }));
        assertEquals("Joined path string", "/system/etc/test//", PathUtils.join(new String[] { "system", "etc", "test", "/" }));
        assertEquals("Joined path string", "/system/etc//test", PathUtils.join(new String[] { "system", "etc/", "test" }));

        assertEquals("Joined path string", "/system/etc/../test", PathUtils.join(new String[] { "system", "etc", "..", "test" }));
        assertEquals("Joined path string", "system/etc/test", PathUtils.join(new String[] { "system", "etc", "test" }, false));
    }

    @Test
    public void testSplitAfterMountpoint() {

        assertArrayEquals("Resolved components", new String[] { "system", "etc/test" }, PathUtils.splitAfterMountpoint("/system/etc/test"));
        assertArrayEquals("Resolved components", new String[] { "system", "etc/test/" }, PathUtils.splitAfterMountpoint("/system/etc/test/"));
        assertArrayEquals("Resolved components", new String[] { "system", "etc/test" }, PathUtils.splitAfterMountpoint("//system/etc/test"));
        assertArrayEquals("Resolved components", new String[] { "system", "etc/test//" }, PathUtils.splitAfterMountpoint("/system/etc/test//"));
        assertArrayEquals("Resolved components", new String[] { "system", "etc/test/" }, PathUtils.splitAfterMountpoint("//system/etc/test/"));
        assertArrayEquals("Resolved components", new String[] { "system", "etc/test//" }, PathUtils.splitAfterMountpoint("//system/etc/test//"));
        assertArrayEquals("Resolved components", new String[] { "system", "etc//test" }, PathUtils.splitAfterMountpoint("/system//etc//test"));

        assertArrayEquals("Resolved components", new String[] { "system", null }, PathUtils.splitAfterMountpoint("/system"));
        assertArrayEquals("Resolved components", new String[] { "system", null }, PathUtils.splitAfterMountpoint("/system/"));
        assertArrayEquals("Resolved components", new String[] { "system", null }, PathUtils.splitAfterMountpoint("//system"));
        assertArrayEquals("Resolved components", new String[] { "system", null }, PathUtils.splitAfterMountpoint("/system//"));
        assertArrayEquals("Resolved components", new String[] { "system", null }, PathUtils.splitAfterMountpoint("//system/"));
        assertArrayEquals("Resolved components", new String[] { "system", null }, PathUtils.splitAfterMountpoint("//system//"));

        assertArrayEquals("Resolved components", new String[] { null, "etc/test" }, PathUtils.splitAfterMountpoint("etc/test"));
        assertArrayEquals("Resolved components", new String[] { null, "etc/test/" }, PathUtils.splitAfterMountpoint("etc/test/"));
        assertArrayEquals("Resolved components", new String[] { null, "etc/test//" }, PathUtils.splitAfterMountpoint("etc/test//"));

        assertArrayEquals("Resolved components", new String[] { null, null }, PathUtils.splitAfterMountpoint(""));
        assertArrayEquals("Resolved components", new String[] { null, null }, PathUtils.splitAfterMountpoint("/"));
        assertArrayEquals("Resolved components", new String[] { null, null }, PathUtils.splitAfterMountpoint("//"));
    }

    @Test
    public void testSplitBeforeName() {

        assertArrayEquals("Resolved components", new String[] { "system/etc", "test" }, PathUtils.splitBeforeName("system/etc/test"));
        assertArrayEquals("Resolved components", new String[] { "/system/etc", "test" }, PathUtils.splitBeforeName("/system/etc/test"));
        assertArrayEquals("Resolved components", new String[] { "/system/etc", "test" }, PathUtils.splitBeforeName("/system/etc/test/"));
        assertArrayEquals("Resolved components", new String[] { "//system/etc", "test" }, PathUtils.splitBeforeName("//system/etc/test"));
        assertArrayEquals("Resolved components", new String[] { "/system/etc", "test" }, PathUtils.splitBeforeName("/system/etc/test//"));
        assertArrayEquals("Resolved components", new String[] { "//system/etc", "test" }, PathUtils.splitBeforeName("//system/etc/test/"));
        assertArrayEquals("Resolved components", new String[] { "//system/etc", "test" }, PathUtils.splitBeforeName("//system/etc/test//"));

        assertArrayEquals("Resolved components", new String[] { null, "test" }, PathUtils.splitBeforeName("test"));
        assertArrayEquals("Resolved components", new String[] { null, "test" }, PathUtils.splitBeforeName("/test"));
        assertArrayEquals("Resolved components", new String[] { null, "test" }, PathUtils.splitBeforeName("/test/"));
        assertArrayEquals("Resolved components", new String[] { null, "test" }, PathUtils.splitBeforeName("//test"));
        assertArrayEquals("Resolved components", new String[] { null, "test" }, PathUtils.splitBeforeName("/test//"));
        assertArrayEquals("Resolved components", new String[] { null, "test" }, PathUtils.splitBeforeName("//test/"));
        assertArrayEquals("Resolved components", new String[] { null, "test" }, PathUtils.splitBeforeName("//test//"));

        assertArrayEquals("Resolved components", new String[] { null, null }, PathUtils.splitBeforeName(""));
        assertArrayEquals("Resolved components", new String[] { null, null }, PathUtils.splitBeforeName("/"));
        assertArrayEquals("Resolved components", new String[] { null, null }, PathUtils.splitBeforeName("//"));
    }

}
