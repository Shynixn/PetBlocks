# PetBlocks  [![Build Status](https://maven-badges.herokuapp.com/maven-central/com.github.shynixn.petblocks/petblocks-api/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.github.shynixn.petblocks/petblocks-api) [![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat-square)](https://raw.githubusercontent.com/Shynixn/PetBlocks/master/LICENCE) 


| branch        | status        | coverage | version | download |
| ------------- | ------------- | -------- | --------| ---------| 
| master        | [![Build Status](https://img.shields.io/travis/Shynixn/PetBlocks/master.svg?style=flat-square)](https://travis-ci.org/Shynixn/PetBlocks) | [![Coverage](https://img.shields.io/codecov/c/github/shynixn/petblocks/master.svg?style=flat-square)](https://codecov.io/gh/Shynixn/PetBlocks/branch/master)|![GitHub license](https://img.shields.io/nexus/r/https/oss.sonatype.org/com.github.shynixn.petblocks/petblocks-bukkit-plugin.svg?style=flat-square)  |[Download latest release](https://github.com/Shynixn/PetBlocks/releases)|
| development   | [![Build Status](https://img.shields.io/travis/Shynixn/PetBlocks/development.svg?style=flat-square)](https://travis-ci.org/Shynixn/PetBlocks)|[![Coverage](https://img.shields.io/codecov/c/github/shynixn/petblocks/development.svg?style=flat-square)](https://codecov.io/gh/Shynixn/PetBlocks/branch/development) |![GitHub license](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.github.shynixn.petblocks/petblocks-bukkit-plugin.svg?style=flat-square) |  [Download snapshots](https://oss.sonatype.org/content/repositories/snapshots/com/github/shynixn/petblocks) |
## Description

PetBlocks is a spigot and also a sponge plugin to use blocks and custom heads as pets in Minecraft.

## Features

* Use blocks as pets in minecraft
* The GUI and pets are completely customizable
* Version support 1.8.R1 - 1.16.R1
* Check out the [PetBlocks-Spigot-Page](https://www.spigotmc.org/resources/12056/) to get more information. 

## Installation

* Please check out the [PetBlocks Documentation](https://shynixn.github.io/PetBlocks/) for further information.

## Screenshots

![alt tag](http://www.mediafire.com/convkey/8853/81wf7uswm0xh9qgzg.jpg)

## Contributing

* Clone the repository to your local environment
* Make sure ``java`` and ``mvn`` can be called in your terminal
* Execute gradle sync for dependencies
* Install the additional spigot dependencies by executing the following gradle task

```xml
[./gradlew|gradlew.bat] downloadDependencies
```

* Install the ForgeGradle development workspace for sponge

```xml
[./gradlew|gradlew.bat] setupDecompWorkspace
```

* Build the plugin by executing

```xml
[./gradlew|gradlew.bat] shadowJar
```

* The PetBlocks-Bukkit.jar file gets generated at petblocks-bukkit-plugin/build/libs/petblocks-bukkit-plugin.jar
* The PetBlocks-Sponge.jar file gets generated at petblocks-sponge-plugin/build/libs/petblocks-sponge-plugin.jar

## Licence

Copyright 2015-2020 Shynixn

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
