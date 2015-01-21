Blockade-Runner
===============

Contributors:

- Dave Chen (Graphics)
- Miles Fogle (Code)
- Alex Gittemeier (Code)


Building Directions (Eclipse Java 8)
------------------------------------

1. **Clone** into an active workspace.
2. Create a **New Java Project** named `Blockade-Runner`.
2. Configure project to use **jdk1.8**:
  1. *Project Settings > Java Compiler > Compiler Compliance Level*: `1.8`
  2. *Project Settings > Java Build Path > Libraries* Remove any existing libraries
  3. *Project Settings > Java Build Path > Libraries > Add Library > JRE System* choose jdk1.8
3. **Add required libraries** to the class path:
  1. [`core.jar`](http://rsranger65.github.io/Blockade-Runner/3rd-party/processing_core-2.2.1.jar) for **Processing 2.2.1**.
  2. [`library/*.jar`](http://code.compartmental.net/minim/distro/minim-2.2.0.zip) for **Minim 2.2.0**.
4. **Run/Debug** with Main class: `net.kopeph.ld31.LD31`
5. **Export** as a *Runnable Jar*, and place the `res` folder next to the `.jar` file.

[Building under Java 7](https://github.com/Rsranger65/Blockade-Runner/wiki/Java-7-Building-Directions)
