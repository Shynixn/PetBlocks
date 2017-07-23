# PetBlocks [![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://raw.githubusercontent.com/Shynixn/PetBlocks/master/LICENSE)

| branch        | status        | download      |
| ------------- | --------------| --------------| 
| master        | [![Build Status](https://travis-ci.org/Shynixn/PetBlocks.svg?branch=master)](https://travis-ci.org/Shynixn/PetBlocks) |[Download latest release (recommend)](https://github.com/Shynixn/PetBlocks/releases)|
| workflow      | [![Build Status](https://travis-ci.org/Shynixn/PetBlocks.svg?branch=workflow)](https://travis-ci.org/Shynixn/PetBlocks) | [Download snapshots](https://oss.sonatype.org/content/repositories/snapshots/com/github/shynixn/petblocks/) |

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
* Start the server (1.8.0 - 1.12.0, Java 8)
* Join and play :)

## API

* Reference the PetBlocks.jar in your own projects.
* If you are using maven you can add it from the central maven repository

### Maven

```xml
<dependency>
     <groupId>com.github.shynixn</groupId>
     <artifactId>petblocks</artifactId>
     <version>6.0.1</version>
</dependency>
```

## How to use the it

#### Set Petmeta

```java
    Player player = Bukkit.getPlayer("Shynixn");
    if(PetBlocksApi.hasPetMeta(player)){
         //Stored data of the entity
          PetMeta petMeta = PetBlocksApi.getPetMeta(player);
          petMeta.setDisplayName(ChatColor.RED + "That's my new petname");
          PetBlocksApi.persistPetMeta(petMeta);
    }
```
#### Set Petblock

```java
    if(PetBlocksApi.hasPetBlock(player)){
        //The current entity petblock
        PetBlock petblock = PetBlocksApi.getPetBlock(player);
        petblock.teleport(player);
    }
```

* Check out the [PetBlocks-Spigot-Page](https://www.spigotmc.org/resources/petblocks-mysql-bungeecord-customizeable-gui-1-8-1-9-1-10-1-11.12056/) to get more information. 

## Screenshots

![alt tag](http://www.mediafire.com/convkey/9d02/r92bshjdva755d3zg.jpg)
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