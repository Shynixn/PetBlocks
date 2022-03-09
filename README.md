# PetBlocks  [![Build Status](https://maven-badges.herokuapp.com/maven-central/com.github.shynixn.petblocks/petblocks-api/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.github.shynixn.petblocks/petblocks-api) [![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat-square)](https://raw.githubusercontent.com/Shynixn/PetBlocks/master/LICENCE) 

| branch        | status        |  version | download |
| ------------- | ------------- |  --------| ---------| 
| master        | [![Build Status](https://github.com/Shynixn/PetBlocks/workflows/PetBlocks/badge.svg?branch=master)](https://github.com/Shynixn/PetBlocks/actions)| ![GitHub license](https://img.shields.io/nexus/r/https/oss.sonatype.org/com.github.shynixn.petblocks/petblocks-bukkit-plugin.svg?style=flat-square)  |[Download latest release](https://github.com/Shynixn/PetBlocks/releases)|
| development   | [![Build Status](https://github.com/Shynixn/PetBlocks/workflows/PetBlocks/badge.svg?branch=development)](https://github.com/Shynixn/PetBlocks/actions) |![GitHub license](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.github.shynixn.petblocks/petblocks-bukkit-plugin.svg?style=flat-square) |  [Download snapshots](https://oss.sonatype.org/content/repositories/snapshots/com/github/shynixn/petblocks) |
## Description

PetBlocks is a spigot and also a sponge plugin to use blocks and custom heads as pets in Minecraft.

## Features

* Use blocks as pets in minecraft
* The GUI and pets are completely customizable
* Version support 1.8.R1 - 1.18.R2
* Check out the [PetBlocks-Spigot-Page](https://www.spigotmc.org/resources/12056/) to get more information. 

## Installation

* Please check out the [PetBlocks Documentation](https://shynixn.github.io/PetBlocks/) for further information.

## Screenshots

![alt tag](http://www.mediafire.com/convkey/8853/81wf7uswm0xh9qgzg.jpg)

## Contributing

### Setting up development environment

* Install Java 17 or higher
* Fork the PetBlocks project on github and clone it to your local environment.
* PetBlocks requires spigot server implementations from 1.8.8 to 1.18 to be correctly installed in your local Maven cache.
  As this requires multiple java version to build different versions, a Dockerfile is provided to build these dependencies in a docker container
  and then copy it to your local Maven cache.

Note: If using Windows, execute the commands using Git Bash.
````sh
mkdir -p ~/.m2/repository/org/spigotmc/
docker build --target dependencies-jdk8 -t petblocks-dependencies-jdk8 .
docker create --name petblocks-dependencies-jdk8 petblocks-dependencies-jdk8 bash
docker cp petblocks-dependencies-jdk8:/root/.m2/repository/org/spigotmc ~/.m2/repository/org/
docker rm -f petblocks-dependencies-jdk8
docker build --target dependencies-jdk17 -t petblocks-dependencies-jdk17 .
docker create --name petblocks-dependencies-jdk17 petblocks-dependencies-jdk17 bash
docker cp petblocks-dependencies-jdk17:/root/.m2/repository/org/spigotmc ~/.m2/repository/org/
docker rm -f petblocks-dependencies-jdk17
````

* Open the project with an IDE, gradle sync for dependencies.

### Testing

#### Option 1

* Setup your own minecraft server
* Change ``// val destinationDir = File("C:/temp/plugins")`` to your plugins folder in the ``petblocks-bukkit-plugin/build.gradle.kts`` file.
* Run the ``setupDecompWorkspace`` task to setup forge dependencies.
* Run the ``shadowJar`` task to generate a plugin.jar file.
* Run your minecraft server

#### Option 2 :whale:

* Run the provided docker file.
* The source code is copied to a new docker container and built to a plugin.
* This plugin is installed on a new minecraft server which is accessible on the host machine on the default port on ``localhost``.

````sh
docker build -t petblocks .
docker run --name=petblocks -p 25565:25565 -p 5005:5005 petblocks
````

## Licence

Licensed under the Apache License, Version 2.0 (the "License");
