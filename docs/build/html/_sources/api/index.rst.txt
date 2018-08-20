Developer API
=============

JavaDocs
~~~~~~~~

https://shynixn.github.io/PetBlocks/apidocs/

Including the PetBlocks Api
~~~~~~~~~~~~~~~~~~~~~~~~~~~

PetBlocks is using maven as build system but you can include the api via different ways:

**(Bukkit) Maven**:

.. code-block:: maven

    <dependency>
        <groupId>com.github.shynixn.petblocks</groupId>
        <artifactId>petblocks-api</artifactId>
        <version>7.2.2</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.github.shynixn.petblocks</groupId>
        <artifactId>petblocks-bukkit-api</artifactId>
        <version>7.2.2</version>
        <scope>provided</scope>
    </dependency>

**(Bukkit) Gradle**:

.. code-block:: groovy

    dependencies {
        compileOnly 'com.github.shynixn.petblocks:petblocks-api:7.2.2'
        compileOnly 'com.github.shynixn.petblocks:petblocks-bukkit-api:7.2.2'
    }

**(Sponge) Maven**:
::
   <dependency>
        <groupId>com.github.shynixn.petblocks</groupId>
        <artifactId>petblocks-api</artifactId>
        <version>7.2.2</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.github.shynixn.petblocks</groupId>
        <artifactId>petblocks-sponge-api</artifactId>
        <version>7.2.2</version>
        <scope>provided</scope>
    </dependency>

**(Sponge) Gradle**:
::
    dependencies {
        compileOnly 'com.github.shynixn.petblocks:petblocks-api:7.2.2'
        compileOnly 'com.github.shynixn.petblocks:petblocks-sponge-api:7.2.2'
    }

**Reference the jar file**:

If you are not capable of using one of these above you can also manually download the
api from the `repository <https://oss.sonatype.org/content/repositories/releases/com/github/shynixn/petblocks/>`__  and reference it in your project.

Registering your dependency
~~~~~~~~~~~~~~~~~~~~~~~~~~~

**(Bukkit) plugin.yml**


Your plugin optionally uses PetBlocks.
::
    softdepend: [PetBlocks]

Your plugin requires PetBlocks to work.
::
    depend: [PetBlocks]

**(Sponge) mcmod.info**

Your plugin optionally uses PetBlocks.
::
 "dependencies": [
    "petblocks"
 ]

Your plugin requires PetBlocks to work.
::
 "requiredMods": [
    "petblocks"
 ]


Modifying PetMeta and PetBlock
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


**(Bukkit) Creating a new PetMeta for a player:**

.. code-block:: java

    Player player; //Any player instance
    Plugin plugin; //Any plugin instance

    PetMetaController<Player> metaController = PetBlocksApi.getDefaultPetMetaController();
    PetMeta petMeta = metaController.create(player);
    petMeta.setPetDisplayName(ChatColor.GREEN + "This is my new pet."); //Modify the petMeta

    Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
        @Override
        public void run() {
            metaController.store(petMeta); //It is recommend to save the petMeta asynchronously into the database
        }
    });

**(Sponge) Creating a new PetMeta for a player:**

.. code-block:: java

    Player player; //Any player instance
    PluginContainer plugin; //Any plugin instance

    PetMetaController<Player> metaController = PetBlocksApi.getDefaultPetMetaController();
    PetMeta petMeta = metaController.create(player);
    petMeta.setPetDisplayName("This is my new pet."); //Modify the petMeta

    Task.builder().async().execute(new Runnable() {
        @Override
        public void run() {
            metaController.store(petMeta); //It is recommend to save the petMeta asynchronously into the database
        }
    }).submit(plugin);


**(Bukkit) Obtaining an existing PetMeta for a player from the database:**

You can see that this gets easily very complicated if
you need to manage asynchronous and synchronous server tasks.

.. code-block:: java

            final Player player; //Any player instance
            final Plugin plugin; //Any plugin instance
            PetMetaController<Player> metaController = PetBlocksApi.getDefaultPetMetaController();

            Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    Optional<PetMeta> optPetMeta = metaController.getFromPlayer(player);   //Acquire the PetMeta async from the database.
                    if (optPetMeta.isPresent()) { //Check if the player has got a petMeta?
                        Bukkit.getServer().getScheduler().runTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                PetMeta petMeta = optPetMeta.get();
                                petMeta.setSkin(5, 0, null, false); //Change skin to a wooden block

                                Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                                    @Override
                                    public void run() {
                                        metaController.store(petMeta);
                                    }
                                });
                            }
                        });
                    }
                }
            });
::

Using lamda expressions can reduce the code above significantly.

.. code-block:: java

            final Player player; //Any player instance
            final Plugin plugin; //Any plugin instance
            PetMetaController<Player> metaController = PetBlocksApi.getDefaultPetMetaController();

            Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                Optional<PetMeta> optPetMeta = metaController.getFromPlayer(player);   //Acquire the PetMeta async from the database.
                if (optPetMeta.isPresent()) { //Check if the player has got a petMeta?
                    Bukkit.getServer().getScheduler().runTask(plugin, () -> {
                        PetMeta petMeta = optPetMeta.get();
                        petMeta.setSkin(5, 0, null, false); //Change skin to a wooden block
                        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> metaController.store(petMeta));
                    });
                }
            });

**(Sponge) Obtaining an existing PetMeta for a player from the database:**

You can see that this gets easily very complicated if
you need to manage asynchronous and synchronous server tasks.

.. code-block:: java

            final Player player; //Any player instance
            final PluginContainer plugin; //Any plugin instance
            PetMetaController<Player> metaController = PetBlocksApi.getDefaultPetMetaController();

            Task.builder().async().execute(new Runnable() {
                @Override
                public void run() {
                    Optional<PetMeta> optPetMeta = metaController.getFromPlayer(player);   //Acquire the PetMeta async from the database.
                    if (optPetMeta.isPresent()) { //Check if the player has got a petMeta?
                           Task.builder().async().execute(new Runnable() {
                            @Override
                            public void run() {
                                PetMeta petMeta = optPetMeta.get();
                                petMeta.setSkin(5, 0, null, false); //Change skin to a wooden block

                                 Task.builder().async().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        metaController.store(petMeta);
                                    }
                                }).submit(plugin);
                            }
                        }).submit(plugin);
                    }
                }
            }).submit(plugin);
::

Using lamda expressions can reduce the code above significantly.

.. code-block:: java

            final Player player; //Any player instance
            final PluginContainer plugin; //Any plugin instance
            PetMetaController<Player> metaController = PetBlocksApi.getDefaultPetMetaController();

            Task.builder().async().execute(() -> {
                Optional<PetMeta> optPetMeta = metaController.getFromPlayer(player);   //Acquire the PetMeta async from the database.
                if (optPetMeta.isPresent()) { //Check if the player has got a petMeta?
                      Task.builder().execute(() -> {
                        PetMeta petMeta = optPetMeta.get();
                        petMeta.setSkin(5, 0, null, false); //Change skin to a wooden block
                        Task.builder().async().execute(() -> metaController.store(petMeta)).submit(plugin);
                    }).submit(plugin);
                }
            }).submit(plugin);

**(Bukkit/Sponge) Spawning a petblock for a player:**

.. code-block:: java

    final Player player; //Any player instance
    final PetMeta petMeta; //Any PetMeta instance
    final Location location; //Any target location

    final PetBlockController<Player> petBlockController = PetBlocksApi.getDefaultPetBlockController();
    final PetBlock petBlock = petBlockController.create(player, petMeta); //Spawn PetBlock
    petBlockController.store(petBlock); //Set it managed by the PetBlocks plugin

    petBlock.teleport(location);    //Teleport the petblock to the target location

**(Bukkit/Sponge) Obtaining an existing petblock for a player:**

.. code-block:: java

            final Player player; //Any player instance
            final Location location; //Any target location

            final PetBlockController<Player> petBlockController = PetBlocksApi.getDefaultPetBlockController();
            final Optional<PetBlock> optPetBlock = petBlockController.getFromPlayer(player); //PetBlock is already managed
            if (optPetBlock.isPresent()) {
                final PetBlock petBlock = optPetBlock.get();
                petBlock.teleport(location);    //Teleport the petblock to the target location
            }

**(Bukkit/Sponge) Applying changes to the PetBlock**

You can also directly change the meta data of the spawned PetBlock:

.. code-block:: java

    final PetBlock petBlock; //Any PetBlock instance
    petBlock.getMeta().setPetDisplayName("New name");

However, for applying the changes you need to respawn the PetBlock:

.. code-block:: java

    final PetBlock petBlock; //Any PetBlock instance
    petBlock.respawn();

Accessing Business Logic
~~~~~~~~~~~~~~~~~~~~~~~~

The PetBlocks plugin allows to access some parts of the Business Logic.

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

* Use BuildTools.jar from spigotmc.org to build to following dependencies.

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

* Execute the following maven goal on the petblocks-sponge-plugin project.

.. code-block:: java

    mvn anchornms:generate-mcp-libraries


* Go to the petblocks-sponge-plugin/target/nms-tools folder and install the generated libraries to your local maven repository.

.. code-block:: java

    mvn install:install-file -Dfile=mcp-1.12.jar -DgroupId=org.mcp -DartifactId=minecraft112R1 -Dversion=1.12.0-R1.0 -Dpackaging=jar

* Reimport the PetBlocks maven project and execute 'mvn package' afterwards.

* The generated petblocks-bukkit-plugin/target/petblocks-bukkit-plugin-###.jar or petblocks-sponge-plugin/target/petblocks-sponge-plugin-###.ja can be used for testing on a server.