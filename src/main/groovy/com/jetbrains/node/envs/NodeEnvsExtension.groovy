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
              final String architecture = null,
              final List<String> packages = null) {
        nodes << new Node(envName, envsDirectory, version, is64(architecture), packages)
    }

    void node(final String envName,
              final String version,
              final List<String> packages) {
        node(envName, version, null, packages)
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
    final List<String> packages

    Node(String name, File envsDir, String version, Boolean is64, List<String> packages) {
        this.name = name
        this.dir = new File(envsDir, name)
        this.version = version
        this.is64 = is64
        this.packages = packages
    }
}