# PetBlocks [![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://raw.githubusercontent.com/Shynixn/PetBlocks/master/LICENSE)

| branch        | status        | download      |
| ------------- | --------------| --------------| 
| master        | [![Build Status](https://travis-ci.org/Shynixn/PetBlocks.svg?branch=master)](https://travis-ci.org/Shynixn/PetBlocks) |[Download latest release (recommend)](https://github.com/Shynixn/PetBlocks/releases)|
| development   | [![Build Status](https://travis-ci.org/Shynixn/PetBlocks.svg?branch=workflow)](https://travis-ci.org/Shynixn/PetBlocks) | [Download snapshots](https://oss.sonatype.org/content/repositories/snapshots/com/github/shynixn/petblocks/petblocks-bukkit-plugin/) |

JavaDocs: https://shynixn.github.io/PetBlocks/apidocs/

## Description
Spigot plugin to use blocks as pets in minecraft.

## Features

* Use blocks as pets in minecraft.
* Customizable UI.
* Version support 1.8.R1 - 1.12.R1
* Check out the [PetBlocks-Spigot-Page](https://www.spigotmc.org/resources/petblocks-mysql-bungeecord-customizeable-gui-1-8-1-9-1-10-1-11.12056/) to get more information. 

## Installation

* [Download the plugin PetBlocks](https://github.com/Shynixn/PetBlocks/releases)
* Put the plugin into your plugin folder
* Start the server (1.8.0 - 1.12.2, Java 8)
* Join and play :)

## API

* Reference the PetBlocks.jar in your own projects.
* If you are using maven or gradle you can add it from the central maven repository

### Framework independent API
```xml
<dependency>
     <groupId>com.github.shynixn.petblocks</groupId>
     <artifactId>petblocks-api</artifactId>
     <version>6.2.2</version>
     <scope>provided</scope>
</dependency>
```

```xml
dependencies {
    compileOnly 'com.github.shynixn.petblocks:petblocks-api:6.2.2'
}
```

### Bukkit API

```xml
<dependency>
     <groupId>com.github.shynixn.petblocks</groupId>
     <artifactId>petblocks-bukkit-api</artifactId>
     <version>6.2.2</version>
     <scope>provided</scope>
</dependency>
```

```xml
dependencies {
    compileOnly 'com.github.shynixn.petblocks:petblocks-bukkit-api:6.2.2'
}
```

#### Modify the PetMetadata 

```java
Plugin plugin; //Your plugin instance
Player player; //Your player instance

//Create and manipulate data
PetMetaController petMetaController = PetBlocksApi.getDefaultPetMetaController();
PetMeta petMeta = petMetaController.create(player);
petMeta.setPetDisplayName(ChatColor.BLACK + "Amazing Pet"); //Modify the petMeta
Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
    @Override
    public void run() {
        petMetaController.store(petMeta);   //It is recommend to save the petMeta asynchronously into the database
    }
});
```
#### Teleport the PetBlock to a location

```java
 Player player; //Your player instance
 PetMeta petMeta; //Your PetMeta instance
 Location location; //Target location instance
 PetBlockController petBlockController = PetBlocksApi.getDefaultPetBlockController();
 PetBlock petBlock = petBlockController.create(player, petMeta);
 petBlockController.store(petBlock); //Store the petblock to be managed by the plugin. Does not involve a database so it can be used on the main thread.
 petBlock.teleport(petBlock); //Teleport the petblock
```

#### Listen to PetBlock-Events (Checkout the [JavaDocs](https://shynixn.github.io/PetBlocks/apidocs/) for all events)

```java
@EventHandler
public void onPetBlockSpawnEvent(PetBlockSpawnEvent event) {
     Bukkit.getLogger().log(Level.INFO, "PetBlock " + event.getPetBlock().getDisplayName() + " has spawned.");
}
```

### PetBlocks Plugin (Includes all APIs but changes more frequently)

```xml
<dependency>
     <groupId>com.github.shynixn.petblocks</groupId>
     <artifactId>petblocks-bukkit-plugin</artifactId>
     <version>6.2.2</version>
     <scope>provided</scope>
</dependency>
```

```xml
dependencies {
    compileOnly 'com.github.shynixn.petblocks:petblocks-bukkit-plugin:6.2.2'
}
```

* Check out the [PetBlocks-Spigot-Page](https://www.spigotmc.org/resources/petblocks-mysql-bungeecord-customizeable-gui-1-8-1-9-1-10-1-11.12056/) to get more information. 

## Screenshots

![alt tag](http://www.mediafire.com/convkey/2c79/3c4a0jhycshdd2zzg.jpg)
![alt tag](http://www.mediafire.com/convkey/697e/ddk043hgdj57d7jzg.jpg)

## Licence

Copyright 2017 Shynixn

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.