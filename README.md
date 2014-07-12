Disconnected
============

Disconnected is a large sandbox-type hacking game which simulates the entire world for giving realistic reactions of the society.
More information can be found on the [wiki page](http://quartercode.com/wiki/index.php?title=Disconnected).

License
-------

Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>

Disconnected may be used under the terms of the GNU General Public License (GPL) v3.0. See the LICENSE.md file or https://www.gnu.org/licenses/gpl-3.0.txt for details.

Compilation
-----------

We use maven to handle our dependencies and build, so you need the Java JDK and Maven for compiling the sourcecode.

* Download & install [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* Download & install [Maven 3](http://maven.apache.org/download.cgi).
* Check out this repository (clone or download).
* Navigate to the project folder of this repository which contains a `pom.xml` and run:

        mvn clean install

Builds
------

* Disconnected is built by a [Jenkins job](http://ci.quartercode.com/job/Disconnected/) on the QuarterCode Jenkins instance.
* Finished builds can be downloaded from the [QuarterCode DL website](http://quartercode.com/dl/projects/details?projectId=Disconnected).
* Builds are also available on the [QuarterCode maven repository](http://repo.quartercode.com).
  In order to reference Disconnected in another maven project (e.g. a mod), the following lines must be added to the project's pom:

        <repositories>
            ...
            <repository>
                <id>quartercode-repository</id>
                <url>http://repo.quartercode.com/content/groups/public/</url>
            </repository>
            ...
        </repositories>

        ...

        <dependencies>
            ...
            <dependency>
                <groupId>com.quartercode</groupId>
                <artifactId>disconnected</artifactId>
                <version>...</version>
            </dependency>
            ...
        </dependencies>

Run
---

If you downloaded a binary package or built your own one, you have several options to run Disconnected:

* Try to execute `disconnected-<version>.jar` (e.g. with a double-click).
* If that doesn't work, open a command prompt, navigate to the binaries folder and run:

        java -jar disconnected-<version>.jar

You need the Java JRE for each of those methods.
If you haven't downloaded it or the Java JDK yet, get it from [here](http://www.java.com/download).
