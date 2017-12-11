Gradle Node Envs Plugin [![JetBrains team project](http://jb.gg/badges/team.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
========================


Usage
-----
                                                
Apply the plugin to your project following
https://plugins.gradle.org/plugin/com.jetbrains.node.envs,
and configure the associated extension:

```gradle
envs {
      envsDirectory = new File(buildDir, 'envs')
      // By default 64 bit envs should be installed
      // _64Bits = true
  
  
      //usage node "envName", "version"
      node "test1", "v7.1.0"
  
      //usage node "envName", "version", [<packages>]
      node "test2", "7.1.0", ["typescript"]
      //version can contain first 'v' character
      node "test3", "v7.1.0", ["lodash", "tslint"]
      node "test4", "8.1.0", ["jscs", "standard"]
      
      //usage node "envName", "version", "architecture", [<packages>]
      node "test5", "v7.1.0", "32", ["eslint"]
}
```

Then invoke the `build_nodes` task. 

This will download and install specified Node.Js envs to `buildDir/envs`.

Libraries listed will be installed correspondingly. Packages in list are installed with `npm install -g <package>` command.


```gradle
envs {
      envsDirectory = new File(buildDir, 'envs')
      // By default 64 bit envs should be installed
      // _64Bits = true
  
  
      //usage dart "envName", "channel"
      dart "devLatest", "dev"
      dart "stableLatest", "stable"
  
      //usage dart "envName", "channel", "version"
      dart "dart-1.24.1", "stable", "1.24.1"
      
      //usage dart "envName", "channel", "version", "architecture"
      dart "dart-1.24.1", "dev", "2.0.0-dev.0.0", "32"
}
```

Then invoke the `build_darts` task. 

This will download and install specified Dart envs to `buildDir/envs`.