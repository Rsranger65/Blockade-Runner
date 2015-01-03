Blockade-Runner
===============

Contributors:

- Dave Chen (Graphics)
- Miles Fogle (Code)
- Alex Gittemeier (Code)

Building Directions (Eclipse)
-----------------------------

1. After cloning into the active workspace, create a **New Java Project** named `Blockade-Runner`
2. Configure project to use jdk1.8:
    a. **Project Settings > Java Compiler > Compiler Compliance Level**: `1.8`
    b. **Project Settings > Java Build Path > Libraries** Remove any existing libraries
    c. **Project Settings > Java Build Path > Libraries > Add Library > JRE System** choose jdk1.8
3. Add [`core.jar`](http://rsranger65.github.io/Blockade-Runner/libs/processing_core-2.2.1.jar) for **Processing 2.2.1** to the classpath.
4. Add `build.xml` as a **Builder**: (**Project Settings > Builders > New > Ant Builder**)
    a. **Buildfile**: `${project_loc}/build.xml`
    b. **Base Directory**: `${project_loc}`
5. Deselect **Project > Build Automatically**
6. Build and run as necessary. (Launch path: `net.kopeph.ld31.LD31`)
7. When ready to export as a **Runnable Jar**, and place the `res` folder next to the `.jar` file.
