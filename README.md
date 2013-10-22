Disconnected
============

Disconnected is a large sandbox-type hacking game which simulates the entire world for giving realistic reactions of the society.

License
-------

Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>

Disconnected may be used under the terms of either the GNU General Public License (GPL). See the LICENSE.md file for details.

Compilation
-----------

We use maven to handle our dependencies and build, so you need the Java JDK and Maven for compiling the sourcecode.

* Download & install [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
* Download & install [Maven 3](http://maven.apache.org/download.cgi).
* Check out this repository (clone or download).
* Download the [TWL-Library](http://twl.l33tlabs.org/demo/twl.zip) and unzip the package.
* Open a command prompt, navigate to the folder which contains the folder `TWL` (it should be the root of the fresh archive) and install TWL.
  - On an UNIX-system (like Linux), run the following command:

            cd TWL/ && mv pom.xml_inactive pom.xml && (sed 's/2.8.5/2.9.0/' pom.xml > pom2.xml && mv pom2.xml pom.xml); mvn clean install && cd ../

     If `cd` outputs an error you're in the wrong directory.
     If `mv` outputs an error you modified some files. Try it again with a fresh archive.
     If `mvn` outputs an error the system can't build TWL. That's an individual maven problem.

  - On a DOS-system (like Windows), it's a bit more complicated:

            > Go into the TWL-folder of the archive.
            > Rename pom.xml_inactive to pom.xml.
            > Edit pom.xml and change "2.8.5" in line 26 to "2.9.0".
            > Open a command prompt in this folder and type: mvn clean install

     If `mvn` outputs an error the system can't build TWL. That's an individual maven problem.

* Navigate to the project folder of this repository which contains a `pom.xml` and run:

        mvn clean install

Run
---

If you downloaded a binaries package or built your own one, you have several options to run Disconnected:

* If you're on Windows, you can execute `disconnected.exe`.
* On every other system, you can try to execute `disconnected-<version>.jar` (e.g. with a double-click).
* If that doesn't work, open a command prompt, navigate to the binaries folder and run:

        java -jar disconnected-<version>.jar

You need the Java JRE for every of those methods.
If you haven't downloaded it yet, get it from [here](http://www.java.com/download).

