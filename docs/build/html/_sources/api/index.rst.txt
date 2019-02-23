Developer API
=============

JavaDocs
~~~~~~~~

https://shynixn.github.io/PetBlocks/apidocs/

Including the PetBlocks API
~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. image:: https://maven-badges.herokuapp.com/maven-central/com.github.shynixn.petblocks/petblocks-api/badge.svg?style=flat-square
  :target: https://maven-badges.herokuapp.com/maven-central/com.github.shynixn.petblocks/petblocks-api

PetBlocks is using gradle as build system and is available in the central repository.

.. note::  The following dependencies are needed to access the PetBlocks **Bukkit** Api.

**Maven:**

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

**Gradle:**

.. parsed-literal::

    compileOnly 'com.github.shynixn.petblocks:petblocks-api:\ |release|\ '
    compileOnly 'com.github.shynixn.petblocks:petblocks-bukkit-api:\ |release|\ '


**Jar files:**

* `Download PetBlocks-Api <http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.github.shynixn.petblocks&a=petblocks-api&v=LATEST>`__
* `Download PetBlocks-Bukkit-Api <http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.github.shynixn.petblocks&a=petblocks-bukkit-api&v=LATEST>`__

Registering the dependency
~~~~~~~~~~~~~~~~~~~~~~~~~~

.. note::  **Bukkit** - Add the following tag to your plugin.yml if you **optionally** want to use PetBlocks.

.. code-block:: yaml

    softdepend: [PetBlocks]

.. note::  **Bukkit** - Add the following tag to your plugin.yml if your plugin  **requires** PetBlocks to work.

.. code-block:: yaml

    depend: [PetBlocks]

.. note::  **Sponge** - Add the following tag to your mcmod.info if you **optionally** want to use PetBlocks.

.. code-block:: java

 "dependencies": [
    "petblocks"
 ]

.. note::  **Sponge** - Add the following tag to your mcmod.info if your plugin **requires** PetBlocks to work.

.. code-block:: java

 "requiredMods": [
    "petblocks"
 ]

Working with the PetBlocks API
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. note::  There are 4 simple steps to access the **whole** business logic of PetBlocks.

* Check out the package **com.github.shynixn.petblocks.api.business.service** in the JavaDocs to find the part of the business logic you want to access.
* Get the instance by using the following line.

.. code-block:: java

    YourBusinessService service = PetBlocksApi.INSTANCE.resolve(YourBusinessService.class);

* If the service methods require additional data entities, check out the JavaDocs to find other services which provide these data entities
  or create new entities by checking out the package **com.github.shynixn.petblocks.api.persistence.entity**.

.. code-block:: java

    YourPersistenceEntity entity = PetBlocksApi.INSTANCE.create(YourPersistenceEntity.class);

* There are some samples below to get your started.

.. note::  **Pets -** Changing the displayname of a pet of a player.

.. code-block:: java

    Player player; // Any player instance

    PersistencePetMetaService petMetaService = PetBlocksApi.INSTANCE.resolve(PersistencePetMetaService.class);
    PetMeta petMeta = petMetaService.getPetMetaFromPlayer(player); // Does always return a correct PetMeta instance. Never returns null.

    petMeta.setDisplayName(ChatColor.GREEN + "This is a cool Pet.");

**Things to notice** regarding PetMeta:

* PetMeta instance will be always available for each player.
* Changes to the PetMeta instance are persistent. Changes will be saved.
* PetMeta changes will automatically applied to a spawned pet if it is present.
* Prefer using the PetMeta instance instead of using the Pet instance.
* Saving the PetMeta is automatically handled and does not have to be explicitly called.

.. note::  **Pets -** Changing the skin of a pet of a player.

.. code-block:: java

        Player player; // Any player instance

        PersistencePetMetaService petMetaService = PetBlocksApi.INSTANCE.resolve(PersistencePetMetaService.class);
        PetMeta petMeta = petMetaService.getPetMetaFromPlayer(player); // Does always return a correct PetMeta instance. Never returns null.

        Skin skin = petMeta.getSkin();
        skin.setTypeName("397"); //Typename accepts (deprecated) ids and Bukkit Material names.
        skin.setDataValue(3);
        // Owner accepts player names, base64EncodedUrls and skin urls.
        skin.setOwner("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzc4NzhiYmYxYjA5ZjdmMjFhZjBiNDA2ZWY3MzEyZWUyMjViOGNjMTAyY2QwOWVlZmYyNDAyNDkzYzUwMzQ0MiJ9fX0=")

.. note::  **Pets -** Setting the player fly riding his pet by modifying ais.

.. code-block:: java

        Player player; // Any player instance

        PersistencePetMetaService petMetaService = PetBlocksApi.INSTANCE.resolve(PersistencePetMetaService.class);
        PetMeta petMeta = petMetaService.getPetMetaFromPlayer(player); // Does always return a correct PetMeta instance. Never returns null.

        AIFlyRiding aiFlyRiding = PetBlocksApi.INSTANCE.create(AIFlyRiding.class); // Create a new registered ai instance.
        aiFlyRiding.setRidingSpeed(2.0);

        petMeta.getAiGoals().add(aiFlyRiding); // Apply the ai to the pet.


.. note::  **Pets -** Teleporting the pet entity

.. code-block:: java

        Player player; // Any player instance
        Location targetLocation; // Any location instance

        PetService petService = PetBlocksApi.INSTANCE.resolve(PetService.class);
        // The pet of a player might not be able to spawn due to defined world restrictions or cancelled PetBlocksSpawnEvent.
        Optional<PetProxy> optPet = petService.getOrSpawnPetFromPlayer(player);

        if (optPet.isPresent()) {
            PetProxy pet = optPet.get();
             // When the location is too far away, the current ais will probably teleport the pet back to the player.
            pet.teleport(targetLocation);
        }

**Things to notice** regarding PetProxy:

* PetProxy instance is not always available for each player.
* Calling remove() allows you to remove the pet.
* Changes to the PetProxy instance are transient. Changes will be lost.
* Prefer using the PetMeta instance instead of using the Pet instance.

.. warning::
  If you want to perform changes to the PetMeta or PetProxy when a player logs into your server, use
  the **PetBlocksLoginEvent** instead of the PlayerJoinEvent. The plugin performs async operations which
  complete later. You can find an example below.

Listening to Events
~~~~~~~~~~~~~~~~~~~

There are many PetBlock events in order to listen to actions. Please take a look into the `JavaDocs <https://shynixn.github.io/PetBlocks/apidocs/>`__  for all events.

.. note::  **PreSpawnEvent** - Listening to the pre spawn event and cancel it if necessary.

.. code-block:: java

    @EventHandler
    public void onPetPreSpawnEvent(PetPreSpawnEvent event){
        Player player = event.getPlayer();
        PetMeta petMeta = event.getPetMeta();

        //Do something
    }

.. note::  **PetBlocksLoginEvent** - Listening to the login event.

.. code-block:: java

    @EventHandler
    public void onPetBlocksLoginEvent(PetBlocksLoginEvent event){
        PetMeta petMeta = event.getPetMeta();

        LocalDateTime localDateTime = LocalDateTime.now();
        petMeta.setDisplayName("Pet login at " + localDateTime.toString() + ".");

        if(event.getPet().isPresent()){
            PetProxy petProxy = event.getPet().get();

            petProxy.setVelocity(new Vector(0,2,0));
        }
    }