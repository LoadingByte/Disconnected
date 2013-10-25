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
            > Edit pom.xml and change "2.8.5" in line 16 to "2.9.0".
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

Branching & Release Model
-------------------------

First of all, there are two main branches with an infinite lifetime: master and develop.
Active development is commited to the develop branch, the master branch is reserved for releases.

Small feature are generally directly added to the develop branch. This branch must always be usable!
If you want to add a feature to the develop branch, you have to complete it before commiting.
Because of that, adding things directly to the develop branch is only recommended for very small features and bugfixes.

If you want to create a feature which is larger than "very small", you should consider creating a new feature branch from develop.
Feature branches can have any name beside from "master", "develop", "alpha-*", "beta-*" and "release-*".
On this branch, you can do what you want, nobody expects anything fully usable on a feature branch.
You can develop your feature and commit every change, even if you break the feature with a new commit.
When you're done with your feature, you can simply merge your feature branch into the develop branch. Of course, you can also throw your feature away.

During the development of a new version, the develop branch code holds the next version with the format <version>-SNAPSHOT.
If there are enough changes made for creating an alpha for the next release, we create a new alpha-<version> branch and bump the code version to <version>-ALPHA.
Until we finish the branch, every commit is an "alpha candidate" which can be tested by the users. Any bugs found will be fixed in this branch.
Small feature changes are also allowed, but you can't merge a feature branch into an alpha branch.
When the alpha is finished, we publish the alpha as "stable", merge the alpha branch into the develop and master branch, create a tag at master and bump the version back to <version>-SNAPSHOT.

The alpha release should be after some of the core features of the new version are implemented.
There is also a beta which should be placed after the second third of the development process. It is handled exactly like the alpha.
The beta demonstrates how the new release should look like, every large feature of the new version should be implemented at this time.
There is the possibility of having multiple alphas or betas by using something like alpha-<version>-2 for the branch and <version>-ALPHA-2 for the code.
Finally, the release is the final state of the new version. Of course, there are also release candidates which should be nearly finished (apart from some bugs).

Here's a graph demonstrating the branching model:

                master    releases    betas    alphas    develop            features

                  |
                  |            inital branching
                  o---------------------------------------> o
                  |                                         | for future release
                  |                                         o-------------------> o
                  |                                         |                     |
                  |                                         o                     o
                  |                                         |  for next r.        |
                  |                                         o--------------> o    o
                  |                                   alpha |                |    |
                  |                               o <-------o                o    o
                  |                               |         |                |    |
                  |                               o         |                o    o
                  |                               |         |                |    |
            alpha o <-----------------------------o-------> o                o    o
                  |                                         |                |    |
                  |                                         o <--------------o    o
                  |                                         |                     |
                  |                                         o                     o
                  |                              beta       |  cool feature       |
                  |                     o <-----------------o--------------> o    o
                  |                     |                   |                |    |
                  |                     o                   |                o    o
                  |                     |                   |               bad   |
             beta o <-------------------o-----------------> o                     o
                  |                                         |                     |
                  |                                         o--------------> o    o
                  |                                         |                |    |
                  |                                         o                o    o
                  |                                         |                |    |
                  |                                         o                o    o
                  |                                         |                |    |
                  |                                         o <--------------o    o
                  |                       release           |                     |
                  |           o <---------------------------o                     o
                  |           |                             |                     |
                  |           o                             |                     o
                  |           |                             |                     |
                  |           o                             |                     o
                  |           |                             |                     |
          release o <---------o---------------------------> o                     o
                  |                                         |                     |
                  |                                         o                     o
                  |                                         |       complete      |
                  |                                         o <-------------------o
                  |                                         |
                  V                                         V

Release Model for Noobs (and users)
---------------------------------

For the user, every new version starts with an alpha. The alpha is very experimental, but it's "stable".
If you want to take an early look on a new version, try the alpha.

The beta is the next stage of development. The major features should be implemented there.

Finally, there is the release of the new version.

Every stage can be split into "candidates" and "release".
Before an alpha (as an example) gets released, we publish alpha candidates which aren't "stable" (they can crash!).
If you want to help us fixing those bugs and publishing the alpha release, try the alpha candidates and report bugs.
Of course, this also applies to betas and final releases.

