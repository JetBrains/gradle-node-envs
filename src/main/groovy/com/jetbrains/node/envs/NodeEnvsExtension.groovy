package com.jetbrains.node.envs

import groovy.json.JsonSlurper


/**
 * Project extension to configure Node.js build environment.
 *
 */
class NodeEnvsExtension {
    File envsDirectory
    Boolean _64Bits = true  // By default 64 bit envs should be installed

    List<Node> nodes = []
    List<Dart> darts = []


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

    void dart(final String envName,
              final String channel,
              final String version,
              final String architecture = null) {
        darts << new Dart(envName, envsDirectory, channel, version, is64(architecture))
    }

    void dart(final String envName,
              final String channel) {
        darts << new Dart(envName, envsDirectory, channel, "latest", is64(null))
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


class Dart {
    final String name
    final File dir
    final String channel
    final String version
    final Boolean is64

    Dart(String name, File envsDir, String channel, String version, Boolean is64) {
        this.name = name
        this.dir = new File(envsDir, name)
        this.channel = (channel in ["dev", "stable"]) ? channel : "dev"
        this.version = (version == "latest") ? getLatestVersion() : version
        this.is64 = is64
    }

    def getLatestVersion() {
        URL apiUrl = new URL("https://storage.googleapis.com/dart-archive/channels/$channel/release/latest/VERSION")
        def object = new JsonSlurper().parseText(apiUrl.text)
        return object.version as String
    }
}