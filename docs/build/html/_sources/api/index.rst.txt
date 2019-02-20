Developer API
=============

JavaDocs
~~~~~~~~

https://shynixn.github.io/PetBlocks/apidocs/

Including the PetBlocks API
~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. image:: https://maven-badges.herokuapp.com/maven-central/com.github.shynixn.petblocks/petblocks-api/badge.svg?style=flat-square
:target: https://maven-badges.herokuapp.com/maven-central/com.github.shynixn.petblocks/petblocks-api

PetBlocks is using maven as build system and is available in the central repository.

.. note::  **Maven** - Bukkit

.. parsed-literal::

    <dependency>
        <groupId>com.github.shynixn.petblocks</groupId>
        <artifactId>petblocks-api</artifactId>
        <version>\ |release|\ </version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.github.shynixn.petblocks</groupId>
        <artifactId>petblocks-bukkit-api</artifactId>
        <version>\ |release|\ </version>
        <scope>provided</scope>
    </dependency>

.. note::  **Maven** - Sponge

.. parsed-literal::

   <dependency>
        <groupId>com.github.shynixn.petblocks</groupId>
        <artifactId>petblocks-api</artifactId>
        <version>\ |release|\ </version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.github.shynixn.petblocks</groupId>
        <artifactId>petblocks-sponge-api</artifactId>
        <version>\ |release|\ </version>
        <scope>provided</scope>
   </dependency>

.. note::  **Gradle** - Bukkit

.. parsed-literal::

    dependencies {
        compileOnly 'com.github.shynixn.petblocks:petblocks-api:\ |release|\ '
        compileOnly 'com.github.shynixn.petblocks:petblocks-bukkit-api:\ |release|\ '
    }

.. note::  **Gradle** - Sponge

.. parsed-literal::

    dependencies {
        compileOnly 'com.github.shynixn.petblocks:petblocks-api:\ |release|\ '
        compileOnly 'com.github.shynixn.petblocks:petblocks-sponge-api:\ |release|\ '
    }


Registering the dependency
~~~~~~~~~~~~~~~~~~~~~~~~~~

.. note::  **Bukkit** - Add the following tag to your plugin.yml if you **optionally** want to use PetBlocks.

.. code-block:: yaml

    softdepend: [PetBlocks]

.. note::  **Sponge** - Add the following tag to your mcmod.info if you **optionally** want to use PetBlocks.

.. code-block:: java

 "dependencies": [
    "petblocks"
 ]

.. note::  **Bukkit** - Add the following tag to your plugin.yml if your plugin  **requires** PetBlocks to work.

.. code-block:: yaml

    depend: [PetBlocks]

.. note::  **Sponge** - Add the following tag to your mcmod.info if your plugin **requires** PetBlocks to work.

.. code-block:: java

 "requiredMods": [
    "petblocks"
 ]

Working with the PetBlocks API
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. note::  **Pet** - Bukkit/Sponge - Accessing the pet entity of a player.

.. code-block:: java

    Player player; // Any player instance

    PetService petService = PetBlocksApi.INSTANCE.resolve(PetService.class);
    CompletableFuture<PetBlock<Object, Object>> completeAblePetBlock = petService.getOrSpawnPetBlockFromPlayerUUID(player.getUniqueId());

    completeAblePetBlock.thenAccept(petBlock -> {
        // Let the player ride the pet if he does not already do it.
        // If this code segment is not called check your console for error messages.
        petBlock.ride(player);
    });

.. note::  **Pet Metadata** - Bukkit/Sponge - Manipulating the name of the pet of a player.

.. code-block:: java

    Player player;  // Any player instance
    PersistencePetMetaService petMetaService = PetBlocksApi.INSTANCE.resolve(PersistencePetMetaService.class);

    CompletableFuture<PetMeta> completeAblePetMeta = petMetaService.getOrCreateFromPlayerUUID(player.getUniqueId());

    completeAblePetMeta.thenAccept(petMeta -> {
        // Sets the display to Pikachu so the pet shows up with the name Pikachu when a respawn gets
        // triggered.
        petMeta.setPetDisplayName(ChatColor.YELLOW + "Pikachu");

        // Stores the changed data and triggers a respawn if the pet is already spawned. Does not
        // respawn the pet when the pet is not spawned.
        petMetaService.save(petMeta);
    });

.. note::  **Configuration** - Bukkit/Sponge - Accessing the stored configuration.

.. code-block:: java

   String path = "pet.design.max-petname-length";

   ConfigurationService configurationService = PetBlocksApi.INSTANCE.resolve(ConfigurationService.class);
   int length = configurationService.findValue(path);

.. note::  **GUI** - Bukkit/Sponge - Using the GUI.

.. code-block:: java

    Player player; // Any player instance
    final GUIService guiService = PetBlocksApi.INSTANCE.resolve(GUIService.class)

    guiService.open(player);

.. note::  **Scripts** - Bukkit/Sponge - Parsing GUI scripts.

.. code-block:: java

   Inventory inventory; // Any inventory instance.
   String script = "binding collection minecraft-heads-com.pet petblocks.selection.petcostumes";

   GUIScriptService guiScriptService = PetBlocksApi.INSTANCE.resolve(GUIScriptService.class);
   ScriptResult scriptResult = guiScriptService.executeScript(inventory, script);

   if (scriptResult.getAction() == ScriptAction.LOAD_COLLECTION) {
         // Parsed script is a loaded collection.
   }

.. note::  **Sounds** - Bukkit/Sponge - Creating and sending sounds.

.. code-block:: java

     Player player; // Any player instance.
     Location location; // Any location instance.

     Sound sound = PetBlocksApi.INSTANCE.create(Sound.class);
     sound.setName("AMBIENT_CAVE"); // Name of the sound for Minecraft 1.13.
     sound.setVolume(1.0);
     sound.setPitch(1.0);

     SoundService soundService = PetBlocksApi.INSTANCE.resolve(SoundService.class);
     soundService.playSound(location, sound, player);

.. note::  **Particles** - Bukkit/Sponge - Creating and sending particles.

.. code-block:: java

    Player player; // Any player instance.
    Location location; // Any location instance.

    Particle particle = PetBlocksApi.INSTANCE.create(Particle.class);
    particle.setType(ParticleType.PORTAL);
    particle.setSpeed(0.1);
    particle.setAmount(20);
    particle.setOffSetX(5);
    particle.setOffSetY(5);
    particle.setOffSetZ(5);

    ParticleService particleService = PetBlocksApi.INSTANCE.resolve(ParticleService.class);
    particleService.playParticle(location, particle, player);

.. note::  **Carry** - Bukkit/Sponge - Let the player carry his pet.

.. code-block:: java

     Player player; // Any player instance

     CarryPetService carryPetService = PetBlocksApi.INSTANCE.resolve(CarryPetService.class);
     carryPetService.carryPet(player);

.. note::  **Feeding** - Bukkit/Sponge - Execute the feeding effect.

.. code-block:: java

    Player player; // Any player instance

    FeedingPetService feedingPetService = PetBlocksApi.INSTANCE.resolve(FeedingPetService.class);
    feedingPetService.feedPet(player);

.. note::  **Messages** - Bukkit/Sponge - Sending a clickable chat message.

.. code-block:: java

    Player player; // Any player instance

    MessageService messageService = PetBlocksApi.INSTANCE.resolve(MessageService.class);
    ChatMessage chatMessage = PetBlocksApi.INSTANCE.create(ChatMessage.class);

    chatMessage.append("This is a ")
            .appendComponent()
            .append(ChatColor.YELLOW)
            .append("<<clickable link>>")
            .setClickAction(ChatClickAction.OPEN_URL, "https://shynixn.github.io/PetBlocks/build/html/index.html")
            .appendHoverComponent().append("Opens the PetBlocks wiki.")
            .getRoot() // Goes back to the root chatMessage builder.
            .append(ChatColor.WHITE)
            .append(" to the PetBlocks wiki.");

    messageService.sendPlayerMessage(player, chatMessage);

.. note::  **WorldGuard** - Bukkit - Accessing the WorldGuard dependency.

.. code-block:: java

    Location location; // Any location instance.

    DependencyService dependencyService = PetBlocksApi.INSTANCE.resolve(DependencyService.class);

    if (dependencyService.isInstalled(PluginDependency.WORLDGUARD)) {
         DependencyWorldGuardService dependencyWorldGuardService = PetBlocksApi.INSTANCE.resolve(DependencyWorldGuardService.class);
         dependencyWorldGuardService.prepareSpawningRegion(location);
    }

.. note::  **Updates** - Bukkit/Sponge - Checking for updates.

.. code-block:: java

    UpdateCheckService updateCheckService = PetBlocksApi.INSTANCE.resolve(UpdateCheckService.class);

    updateCheckService.checkForUpdates();

Listen to Events
~~~~~~~~~~~~~~~~

There are many PetBlock events in order to listen to actions. Please take a look into the `JavaDocs <https://shynixn.github.io/PetBlocks/apidocs/>`__  for all events.

.. note::  **SpawnEvent** - Bukkit - Listening to the spawn event.

.. code-block:: java

    @EventHandler
    public void onPetBlockSpawnEvent(PetBlockSpawnEvent event){
        Player owner = event.getPlayer();
        PetBlock petBlock = event.getPetBlock();

        //Do something
    }

.. note::  **SpawnEvent** - Sponge - Listening to the spawn event.

.. code-block:: java

    @Listener
    public void onPetBlockSpawnEvent(PetBlockSpawnEvent event){
        Player owner = event.getPlayer();
        PetBlock petBlock = event.getPetBlock();

        //Do something
    }


Contributing and setting up your workspace
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. warning:: PetBlocks is **partially** written in `Kotlin <https://kotlinlang.org/>`__ instead of pure Java. Especially the sponge implementation. If you are not familiar with Kotlin, modifying PetBlocks might be a difficult task.

* Fork the PetBlocks project on github and clone it to your local environment.

* Use BuildTools.jar from spigotmc.org to build the following dependencies.

.. code-block:: java

    - java -jar BuildTools.jar --rev 1.8
    - java -jar BuildTools.jar --rev 1.8.3
    - java -jar BuildTools.jar --rev 1.8.8
    - java -jar BuildTools.jar --rev 1.9
    - java -jar BuildTools.jar --rev 1.9.4
    - java -jar BuildTools.jar --rev 1.10
    - java -jar BuildTools.jar --rev 1.11
    - java -jar BuildTools.jar --rev 1.12
    - java -jar BuildTools.jar --rev 1.13
    - java -jar BuildTools.jar --rev 1.13.1

* Install the created libraries to your local maven repository.

.. code-block:: java

    - mvn install:install-file -Dfile=spigot-1.8.jar -DgroupId=org.spigotmc -DartifactId=spigot18R1 -Dversion=1.8.0-R1.0 -Dpackaging=jar
    - mvn install:install-file -Dfile=spigot-1.8.3.jar -DgroupId=org.spigotmc -DartifactId=spigot18R2 -Dversion=1.8.3-R2.0 -Dpackaging=jar
    - mvn install:install-file -Dfile=spigot-1.8.8.jar -DgroupId=org.spigotmc -DartifactId=spigot18R3 -Dversion=1.8.8-R3.0 -Dpackaging=jar
    - mvn install:install-file -Dfile=spigot-1.9.jar -DgroupId=org.spigotmc -DartifactId=spigot19R1 -Dversion=1.9.0-R1.0 -Dpackaging=jar
    - mvn install:install-file -Dfile=spigot-1.9.4.jar -DgroupId=org.spigotmc -DartifactId=spigot19R2 -Dversion=1.9.4-R2.0 -Dpackaging=jar
    - mvn install:install-file -Dfile=spigot-1.10.2.jar -DgroupId=org.spigotmc -DartifactId=spigot110R1 -Dversion=1.10.2-R1.0 -Dpackaging=jar
    - mvn install:install-file -Dfile=spigot-1.11.jar -DgroupId=org.spigotmc -DartifactId=spigot111R1 -Dversion=1.11.0-R1.0 -Dpackaging=jar
    - mvn install:install-file -Dfile=spigot-1.12.jar -DgroupId=org.spigotmc -DartifactId=spigot112R1 -Dversion=1.12.0-R1.0 -Dpackaging=jar
    - mvn install:install-file -Dfile=spigot-1.13.jar -DgroupId=org.spigotmc -DartifactId=spigot113R1 -Dversion=1.13.0-R1.0 -Dpackaging=jar
    - mvn install:install-file -Dfile=spigot-1.13.1.jar -DgroupId=org.spigotmc -DartifactId=spigot113R2 -Dversion=1.13.1-R2.0 -Dpackaging=jar

* Execute the following maven goal on the petblocks-sponge-plugin project.

.. code-block:: java

    mvn anchornms:generate-mcp-libraries


* Go to the petblocks-sponge-plugin/target/nms-tools folder and install the generated libraries to your local maven repository.

.. code-block:: java

    mvn install:install-file -Dfile=mcp-1.12.jar -DgroupId=org.mcp -DartifactId=minecraft112R1 -Dversion=1.12.0-R1.0 -Dpackaging=jar

* Reimport the PetBlocks maven project and execute 'mvn package' afterwards.

* The generated petblocks-bukkit-plugin/target/petblocks-bukkit-plugin-###.jar or petblocks-sponge-plugin/target/petblocks-sponge-plugin-###.ja can be used for testing on a server.