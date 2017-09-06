package com.jetbrains.node.envs


/**
 * Project extension to configure Node.js build environment.
 *
 */
class NodeEnvsExtension {
    File envsDirectory
    Boolean _64Bits = true  // By default 64 bit envs should be installed

    List<Node> nodes = []


    void node(final String envName,
              final String version,
              final String architecture = null) {
        nodes << new Node(envName, envsDirectory, version, is64(architecture))
    }

    private Boolean is64(final String architecture) {
        return (architecture == null) ? _64Bits : !(architecture == "32")
    }
}

class Node {
    final String name
    final File dir
    final String version
    final Boolean is64

    Node(String name, File envsDir, String version, Boolean is64) {
        this.name = name
        this.dir = new File(envsDir, name)
        this.version = version
        this.is64 = is64
    }
}