Gradle Node Envs Plugin
========================


Usage
-----
                                                
Apply the plugin to your project following
**TODO**,
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

