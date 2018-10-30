package com.jetbrains.node.envs

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Delete

class NodeEnvsPlugin implements Plugin<Project> {
    private OS os = OS.getOs()

    enum OS {
        WIN("win", "zip", "windows"),
        LINUX("linux", "tar.gz", "linux"),
        MAC("darwin", "tar.gz", "macos")

        private final String nodeOsName
        private final String nodeArchiveExtension
        private final String dartOsName

        OS(String nodeOsName, String nodeArchiveExtension, String dartOsName) {
            this.nodeOsName = nodeOsName
            this.nodeArchiveExtension = nodeArchiveExtension
            this.dartOsName = dartOsName
        }

        static getOs() {
            switch (System.getProperty('os.name').toLowerCase()) {
                case { it.contains("windows") }:
                    return WIN
                case { it.contains("linux") }:
                    return LINUX
                case { it.contains("mac") }:
                    return MAC
            }
        }
    }

    private static File getArchiveFromURL(Project project, URL urlToArchive) {
        File archive = new File(project.buildDir, urlToArchive.toString().split("/").last())

        if (!archive.exists()) {
            project.logger.quiet("Downloading $archive.name")
            project.ant.get(dest: archive) {
                url(url: urlToArchive)
            }
        }
        return archive
    }

    private URL getUrlToDownloadNode(Node node) {
        final String version = (node.version.startsWith("v")) ? node.version : "v$node.version"
        final String arch = node.is64 ? 'x64' : 'x86'

        return new URL("https://nodejs.org/dist/$version/node-$version-$os.nodeOsName-$arch.$os.nodeArchiveExtension")
    }

    private Task createNodeJs(Project project, Node node) {
        return project.tasks.create(name: "Bootstrap Node.js '$node.name'") {
            onlyIf {
                !node.dir.exists()
            }

            doLast {
                File archive = getArchiveFromURL(project, getUrlToDownloadNode(node))

                project.logger.quiet("Bootstraping to $node.dir")
                switch (archive.name as String) {
                    case { it.endsWith("zip") }:
                        project.ant.unzip(src: archive, dest: project.buildDir)
                        new File(project.buildDir, archive.name.replaceFirst(".zip", "")).with { src ->
                            project.ant.move(file: src, tofile: node.dir)
                        }
                        break
                    case { it.endsWith("tar.gz") }:
                        project.ant.mkdir(dir: node.dir)
                        "tar --strip-components 1 -xzf $archive -C $node.dir".execute().waitFor()
                        break
                }

                if (node.packages != null) installNpmPackages(project, node, node.packages)
            }
        }
    }

    private void installNpmPackages(Project project, Node env, List<String> packages) {
        project.logger.quiet("Install $env.packages to $env.name")
        project.exec({
            switch (os) {
                case OS.WIN:
                    environment 'PATH', env.dir
                    executable new File(env.dir, "npm.cmd").absolutePath
                    break
                case [OS.LINUX, OS.MAC]:
                    environment 'PATH', new File(env.dir, "bin")
                    executable new File(env.dir, "bin/npm").absolutePath
                    break
            }
            args = ["install", "-g", "--allow-root", "--unsafe-perm=true", *packages]
        })
    }

    private URL getUrlToDownloadDart(Dart dart) {
        final String arch = dart.is64 ? 'x64' : 'ia32'

        return new URL("https://storage.googleapis.com/dart-archive/channels/${dart.channel}/release/${dart.version}/sdk/dartsdk-${os.dartOsName}-$arch-release.zip")
    }

    private Task createDart(Project project, Dart dart) {
        return project.tasks.create(name: "Bootstrap Dart '$dart.name'") {
            onlyIf {
                !dart.dir.exists()
            }

            doLast {
                File archive = getArchiveFromURL(project, getUrlToDownloadDart(dart))

                project.logger.quiet("Bootstraping to $dart.dir")
                project.ant.unzip(src: archive, dest: project.buildDir)
                new File(project.buildDir, "dart-sdk").with { src ->
                    project.ant.move(file: src, tofile: dart.dir)
                }

                // Make dart and pub being executable
                if (os in [OS.LINUX, OS.MAC]) {
                    project.exec {
                        executable "chmod"
                        args = ["+x", "${dart.dir}/bin/dart", "${dart.dir}/bin/pub"]
                    }
                }

                // This is necessary, because archive name isn't unique
                archive.delete()
            }
        }
    }

    @Override
    void apply(Project project) {
        project.mkdir("build")
        NodeEnvsExtension envs = project.extensions.create("envs", NodeEnvsExtension.class)

        project.afterEvaluate {

            project.tasks.create(name: "clean_envs_directory", type: Delete) {
                delete envs.envsDirectory
            }

            project.tasks.create(name: 'build_nodes') {
                onlyIf { !envs.nodes.empty }

                envs.nodes.each { Node node ->
                    dependsOn createNodeJs(project, node)
                }
            }

            project.tasks.create(name: 'build_darts') {
                onlyIf { !envs.darts.empty }

                envs.darts.each { Dart dart ->
                    dependsOn createDart(project, dart)
                }
            }
        }
    }
}