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
                            String folderNameFromArchive = null
                            switch (archive.name as String) {
                                case { it.endsWith("zip") }:
                                    project.ant.unzip(src: archive, dest: project.buildDir)
                                    folderNameFromArchive = archive.name.replaceFirst(".zip", "")
                                    break
                                case { it.endsWith("tar.gz") }:
                                    project.ant.gunzip(src: archive)
                                    new File(project.buildDir, archive.name[0..-1 - ".gz".length()]).with { tar ->
                                        project.ant.untar(src: tar, dest: project.buildDir)
                                        project.ant.delete(file: tar)
                                    }
                                    folderNameFromArchive = archive.name.replaceFirst(".tar.gz", "")
                                    break
                            }

                            new File(project.buildDir, folderNameFromArchive).with { src ->
                                project.ant.move(file: src, tofile: env.dir)
                            }

                        }
                    }
                }

            }
        }
    }
}
