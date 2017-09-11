package com.jetbrains.node.envs

import org.gradle.api.Plugin
import org.gradle.api.Project

class NodeEnvsPlugin implements Plugin<Project> {
    private OS os = OS.getOs()

    enum OS {
        WIN("win", "zip"),
        LINUX("linux", "tar.gz"),
        MAC("darwin", "tar.gz")

        private final String archiveName
        private final String archiveExtension

        OS(String archiveName, String archiveExtension) {
            this.archiveName = archiveName
            this.archiveExtension = archiveExtension
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

    private URL getUrlToDownloadNode(Node node) {
        final String version = (node.version.startsWith("v")) ? node.version : "v$node.version"
        final String arch = node.is64 ? 'x64' : 'x86'

        return new URL("https://nodejs.org/dist/$version/node-$version-$os.archiveName-$arch.$os.archiveExtension")
    }

    @Override
    void apply(Project project) {
        project.mkdir("build")
        NodeEnvsExtension envs = project.extensions.create("envs", NodeEnvsExtension.class)

        project.afterEvaluate {
            project.tasks.create(name: 'build_nodes') {
                onlyIf { !envs.nodes.empty }

                envs.nodes.each { env ->
                    dependsOn project.tasks.create(name: "Bootstrap Node.js '$env.name'") {
                        onlyIf {
                            !env.dir.exists()
                        }

                        doLast {
                            URL urlToNode = getUrlToDownloadNode(env)
                            File archive = new File(project.buildDir, urlToNode.toString().split("/").last())

                            if (!archive.exists()) {
                                project.logger.quiet("Downloading $archive.name")
                                project.ant.get(dest: archive) {
                                    url(url: urlToNode)
                                }
                            }

                            project.logger.quiet("Bootstraping to $env.dir")
                            switch (archive.name as String) {
                                case { it.endsWith("zip") }:
                                    project.ant.unzip(src: archive, dest: project.buildDir)
                                    new File(project.buildDir, archive.name.replaceFirst(".zip", "")).with { src ->
                                        project.ant.move(file: src, tofile: env.dir)
                                    }
                                    break
                                case { it.endsWith("tar.gz") }:
                                    project.ant.mkdir(dir: env.dir)
                                    "tar --strip-components 1 -xzf $archive -C $env.dir".execute()
                                    break
                            }

                        }
                    }
                }

            }
        }
    }
}