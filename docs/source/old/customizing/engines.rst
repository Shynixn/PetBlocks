Engines
=======

Pets in PetBlocks are defined by 2 major options and one additional option.

* Engines
* Skins
* Particles (optional)

This page deals only with engines.

Description
~~~~~~~~~~~

Engines contain all the **configuration values for pets**  which do not belong to skins and particle effects. Still, you can define
the default value for skins and particle effects when the player selects a new engine.

You can find the engines at the following category in the GUI. They are only accessible by this item and **cannot be scripted**.

.. image:: ../_static/images/engine1.JPG

.. image:: ../_static/images/engine2.JPG

Now you can see lots of items representing minecraft animals.

When you now select on of the animals, you get moved back to the main page and replaced your previous engine. Your
pet does not have all the configuration values bound to the selected animal.

Configuration Example
~~~~~~~~~~~~~~~~~~~~~

You can see, add and customize the engines in the config.yml.
Let's take a look at the first engine in the list called Pig.

**config.yml**:
::
  engines:
   1:
     gui:
        id: 397
        damage: 3
        skin: 'textures.minecraft.net/texture/621668ef7cb79dd9c22ce3d1f3f4cb6e2559893b6df4a469514e667c16aa4'
        name: '&d&lPig'
        unbreakable: false
        lore:
          - '&7Permission: <permission>'
     behaviour:
        entity: 'RABBIT'
        riding: 'RUNNING'
     sound:
        ambient:
          name: 'PIG_IDLE'
          volume: 1.0
          pitch: 1.0
        walking:
          name: 'PIG_WALK'
          volume: 1.0
          pitch: 1.0


* The **gui** tag contains the configuration value for the item displayed in the GUI.
* The **behaviour.entity** tag changes the hidden entity used for path finding and movements. There are only 2 available entities: 'ZOMBIE' and 'RABBIT'
* The **behaviour.riding** tag changes if the pet runs on the ground or flies when a player starts riding it. The values are: 'RUNNING' and 'FLYING'
* The **sound.ambient** tag changes the sound being occasionally played by the pet.
* The **sound.walking** tag changes the sound being played by the pet when running.

**Additional settings:**

You can also optionally define the default pet name and particle effect when a player selects this engine.
The configuration below sets the petname to Bob and plays a reddust particle effect.

**config.yml**:
::
  engines:
   1:
     petname: '&eBob'
     gui:
        id: 397
        damage: 3
        skin: 'textures.minecraft.net/texture/621668ef7cb79dd9c22ce3d1f3f4cb6e2559893b6df4a469514e667c16aa4'
        name: '&d&lPig'
        unbreakable: false
        lore:
          - '&7Permission: <permission>'
     behaviour:
        entity: 'RABBIT'
        riding: 'RUNNING'
     sound:
        ambient:
          name: 'PIG_IDLE'
          volume: 1.0
          pitch: 1.0
        walking:
          name: 'PIG_WALK'
          volume: 1.0
          pitch: 1.0
     particle:
        name: 'reddust'
        speed: 0.01
        amount: 20
        red: 0
        green: 0
        blue: 255
















