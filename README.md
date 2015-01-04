Blockade-Runner
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
3. Add the following libraries to the class path:
  1. [`core.jar`](http://rsranger65.github.io/Blockade-Runner/3rd-party/processing_core-2.2.1.jar) from **Processing 2.2.1**.
  2. [`library/*.jar`](http://code.compartmental.net/minim/distro/minim-2.2.0.zip) from **Minim 2.2.0**.
4. Build and run as necessary. (Launch path: `net.kopeph.ld31.LD31`)
5. When ready to export, export as a **Runnable Jar**, and place the `res` folder next to the `.jar` file.

[Building under Java 7](https://github.com/Rsranger65/Blockade-Runner/wiki/Java-7-Building-Directions)
