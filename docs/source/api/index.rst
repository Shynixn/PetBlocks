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
        <version>7.2.0</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.github.shynixn.petblocks</groupId>
        <artifactId>petblocks-bukkit-api</artifactId>
        <version>7.2.0</version>
        <scope>provided</scope>
    </dependency>

**(Bukkit) Gradle**:

.. code-block:: groovy

    dependencies {
        compileOnly 'com.github.shynixn.petblocks:petblocks-api:7.2.0'
        compileOnly 'com.github.shynixn.petblocks:petblocks-bukkit-api:7.2.0'
    }

**(Sponge) Maven**:
::
   <dependency>
        <groupId>com.github.shynixn.petblocks</groupId>
        <artifactId>petblocks-api</artifactId>
        <version>7.2.0</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.github.shynixn.petblocks</groupId>
        <artifactId>petblocks-sponge-api</artifactId>
        <version>7.2.0</version>
        <scope>provided</scope>
    </dependency>

**(Sponge) Gradle**:
::
    dependencies {
        compileOnly 'com.github.shynixn.petblocks:petblocks-api:7.2.0'
        compileOnly 'com.github.shynixn.petblocks:petblocks-sponge-api:7.2.0'
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

The PetBlocks plugin allows to access some (not all) parts of the Business Logic too.

* Accessing the Configuration.

**Bukkit/Sponge:**

.. code-block:: java

   ConfigurationService configurationService = PetBlocksApi.INSTANCE.resolve(ConfigurationService.class);
   int length = configurationService.findValue("pet.design.max-petname-length");

* Accessing the GUI.

**Bukkit/Sponge:**

.. code-block:: java

    Player player; // Any player instance
    final GUIService guiService = PetBlocksApi.INSTANCE.resolve(GUIService.class)

    guiService.open(player);

* Parsing GUI Scripts.

**Bukkit/Sponge:**

.. code-block:: java

   Inventory inventory; // Any inventory instance.
   String script = "binding collection minecraft-heads-com.pet petblocks.selection.petcostumes";

   GUIScriptService guiScriptService = PetBlocksApi.INSTANCE.resolve(GUIScriptService.class);
   ScriptResult scriptResult = guiScriptService.executeScript(inventory, script);

   if (scriptResult.getAction() == ScriptAction.LOAD_COLLECTION) {
         // Parsed script is a loaded collection.
   }

* Playing sounds.

**Bukkit/Sponge:**

.. code-block:: java

     Player player; // Any player instance.
     Location location; // Any location instance.

     Sound sound = PetBlocksApi.INSTANCE.create(Sound.class);
     sound.setName("AMBIENT_CAVE"); // Name of the sound for Minecraft 1.13.
     sound.setVolume(1.0);
     sound.setPitch(1.0);

     SoundService soundService = PetBlocksApi.INSTANCE.resolve(SoundService.class);
     soundService.playSound(location, sound, player);

* Playing particles.

**Bukkit/Sponge:**

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

Listen to Events
~~~~~~~~~~~~~~~~

There are many PetBlock events in order to listen to actions. Please take a look into the `JavaDocs <https://shynixn.github.io/PetBlocks/apidocs/>`__  for all events:

**Bukkit:**

.. code-block:: java

    @EventHandler
    public void onPetBlockSpawnEvent(PetBlockSpawnEvent event){
        Player owner = event.getPlayer();
        PetBlock petBlock = event.getPetBlock();

        //Do something
    }

**Sponge:**

.. code-block:: java

    @Listener
    public void onPetBlockSpawnEvent(PetBlockSpawnEvent event){
        Player owner = event.getPlayer();
        PetBlock petBlock = event.getPetBlock();

        //Do something
    }


Setup your personal PetBlocks Workspace
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**Important!** PetBlocks is *partially* written in `Kotlin <https://kotlinlang.org/>`__ instead of pure Java.
Especially the sponge implementation. If you are not familiar with Kotlin, modifying PetBlocks might be a difficult task.

It is sometimes necessary to customize PetBlocks itself instead of using the Developer API. The following steps
help you to get started with developing for PetBlocks.

Before you continue you should be familiar with **git**, **github**, **maven** and any preferred **Java IDE**.

1. Open `PetBlocks on github <https://github.com/Shynixn/PetBlocks>`__
2. Log in or create a github account and press the **Fork** button in the top right corner.
3. Github will create a new repository with PetBlocks on your account
4. Click on the green **Clone or download** button and copy the text inside of the textbox
5. Open a terminal on your pc, go into a target folder and enter the command

Terminal:
::
   git clone <your copied text>
::

6. After PetBlocks folder is created you can open the Project with any Java IDE supporting **Maven**
7. Create a new **lib** folder in your PetBlocks folder (ignore the .idea, docs and headdatabase folder)
8. Download all spigot libraries from 1.8.0 until the latest version and put it into the lib folder

.. image:: ../_static/images/help1.jpg

9. Make sure you understand that PetBlocks uses custom generated and relocated `mcp libraries <http://www.modcoderpack.com/>`__ for NMS in sponge.
10. As gradle is necessary for developing NMS sponge you need to install gradle
11. Execute the maven goal **anchornms:generate-mcp-libraries** on the petblocks-sponge-plugin module.
12. Copy the generated mcp-...jar files from the target/nms-tools folder into your lib folder
13. Try to compile the root project with **mvn compile**
14. If successful you can start editing the source code and create jar files via **mvn package**

**Optional**

15. To share your changes with the world push your committed changes into your github repository.
16. Click on the **New pull request** button and start a pull request against PetBlocks

(base:fork Shynixn/PetBlocks, base: development <- head fork: <your repository> ...)