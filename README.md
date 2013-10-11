Disconnected
============

Disconnected is a large sandbox-type hacking game which simulates the entire world for giving realistic reactions of the society.

License
-------

Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>

Disconnected may be used under the terms of either the GNU General Public License (GPL). See the LICENSE.md file for details.

Compilation
-----------

We use maven to handle our dependencies and build. To compile Disconnected, follow these steps:

* Install [Maven 3](http://maven.apache.org/download.html).
* Check out this repository (clone or download).
* Download the [TWL-Library](http://twl.l33tlabs.org/demo/twl.zip) and unzip the package.
* Open a command prompt, navigate to the folder which contains `TWL.jar` and run:

        $ mvn install:install-file -Dfile=TWL.jar -DgroupId=de.matthiasmann.twl -DartifactId=twl -Dversion=1.0 -Dpackaging=jar

* Navigate to the project folder of this repository which contains a `pom.xml` and run:

        $ mvn clean install


Run
---

If you downloaded a binaries package or built your own one, you have several options to run Disconnected:

* If you're on Windows, you can execute disconnected.exe.
* On every other system, you can try to execute disconnected-<version>.jar (e.g. with a double-click).
* If that doesn't work, open a command prompt, navigate to the binaries folder and run:

        $ java -jar disconnected-<version>.jar

You need the Java JRE for every of those methods. If you haven't done yet, download it [here](www.java.com/download).

