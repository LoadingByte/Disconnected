Disconnected
============

Disconnected is a large sandbox-type hacking game which simulates the entire world for giving realistic reactions of the society.

License
-------

Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>

Disconnected may be used under the terms of the GNU General Public License (GPL) v3.0. See the LICENSE.md file or https://www.gnu.org/licenses/gpl-3.0.txt for details.

Compilation
-----------

We use maven to handle our dependencies and build, so you need the Java JDK and Maven for compiling the sourcecode.

* Download & install [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
* Download & install [Maven 3](http://maven.apache.org/download.cgi).
* Check out this repository (clone or download).
* Navigate to the project folder of this repository which contains a `pom.xml` and run:

        mvn clean install

Run
---

If you downloaded a binary package or built your own one, you have several options to run Disconnected:

* Try to execute `disconnected-<version>.jar` (e.g. with a double-click).
* If that doesn't work, open a command prompt, navigate to the binaries folder and run:

        java -jar disconnected-<version>.jar

You need the Java JRE for each of those methods.
If you haven't downloaded it yet, get it from [here](http://www.java.com/download).
