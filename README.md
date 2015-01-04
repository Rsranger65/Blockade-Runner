Blockade-Runner ([Releases](http://rsranger65.github.io/Blockade-Runner/releases.html))
===============

Contributors:

- Dave Chen (Graphics)
- Miles Fogle (Code)
- Alex Gittemeier (Code)


Building Directions (Eclipse Java 8)
------------------------------------

1. After cloning into the active workspace, create a **New Java Project** named `Blockade-Runner`.
2. Configure project to use jdk1.8:
  1. **Project Settings > Java Compiler > Compiler Compliance Level**: `1.8`
  2. **Project Settings > Java Build Path > Libraries** Remove any existing libraries
  3. **Project Settings > Java Build Path > Libraries > Add Library > JRE System** choose jdk1.8
3. Add [`core.jar`](http://rsranger65.github.io/Blockade-Runner/3rd-party/processing_core-2.2.1.jar) for **Processing 2.2.1** to the classpath.
4. Add `build.xml` as a **Builder**: (**Project Settings > Builders > New > Ant Builder**)
  1. **Buildfile**: `${project_loc}/build.xml`
  2. **Base Directory**: `${project_loc}`
5. Deselect **Project > Build Automatically**.
6. Build and run as necessary. (Launch path: `net.kopeph.ld31.LD31`)
7. When ready to export, export as a **Runnable Jar**, and place the `res` folder next to the `.jar` file.


Building Directions (Eclipse Java 7)
------------------------------------

1. After cloning into the active workspace, create a **New Java Project** named `Blockade-Runner`.
2. Configure project to use jdk1.7:
  1. **Project Settings > Java Compiler > Compiler Compliance Level**: `1.7`
  2. **Project Settings > Java Build Path > Libraries** Remove any existing libraries
  3. **Project Settings > Java Build Path > Libraries > Add Library > JRE System** choose jdk1.7
3. Add [`core.jar`](http://rsranger65.github.io/Blockade-Runner/libs/processing_core-2.2.1.jar) for **Processing 2.2.1** to the classpath.
4. Add `build.xml` as a **Builder**: (**Project Settings > Builders > New > Ant Builder**)
  1. **Buildfile**: `${project_loc}/build.xml`
  2. **Base Directory**: `${project_loc}`
5. Deselect **Project > Build Automatically**.
6. Setup source folders: **Project Settings > Java Build Path > Source Folders**
  1. Remove `src/`
  2. Add `convert/`
6. Build and run as `net.kopeph.convert.Java8to7`.
7. Add `src-7` to the source folder list.
8. Build and run as necessary as `net.kopeph.ld31.LD31`.
9. When ready, to export, export as a **Runnable Jar**, and place the `res` folder next to the `.jar` file.

Caveats with supporting Java 7
------------------------------
- No API added in Java 8 can be used, the only new feature that is translated are lambda functions.
- Function pointer syntax is not supported.
- Lambdas must use curly brackets
- Lambda arguments *must* match those in the interface's method signature.
- All functional interfaces must reside in `net.kopeph.ld31.spi` in the `src/` folder
- Functional interfaces in the Java API must have a dummy interface located in the concatenation of `net.kopeph.ld31.spi` and the fully qualified interface name (`class.getName()`)
  - For example `Java.lang.Runnable` has a dummy interface located at `net.kopeph.ld31.spi.java.lang.Runnable`
- Every time a lambda is used, the following comment marker must come somewhere before the lambda itself: `//$LAMBDA:InterfaceName` where InterfaceName is:
  - Unqualified if directly inside `net.kopeph.ld31.spi`.
  - Fully qualified if part of the Java API.
