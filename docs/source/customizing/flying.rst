Flying
======

The AI called "Flying" is a behaviour ai which lets the pet move around the world.

In this case the pet will always stay at the same height as the player eventhough it might be hovering above the ground.
Therefore the **offset** parameter is the offset from the player's head position instead of the ground.

Movement sound and particles can also be defined.

Requirements
~~~~~~~~~~~~

This ai is a **pathfinder based ai**, which is one of the 3 base ais. (Walking, Hopping, Flying)

Configuring in your config.yml
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* Only the type parameter is required, all other parameters are optional.
* You can specify this ai multiple times in order to play multiple movement sounds and particles.
* Set the sound or particle name to none in order to send no sound or particles.

config.yml
::
    type: 'flying'
    speed: 1.0
    offset-y: -1.0
    sound:
      name: 'CHICKEN_WALK'
      volume: 1.0
      itch: 1.0
    particle:
      name: 'reddust'
      speed: 0.01
      amount: 20
      offx: 0
      offy: 0
      offz: 255

You can find all options explained at the bottom of this page.

Properties
~~~~~~~~~~

* Type: Unique identifier of the ai.
* Tag: Optional tag to identify a specific ai configuration.
* Speed: The flying speed of the pet.
* Offset-Y: The offset value from the player's head position.
* ClickParticle.Name: Name of the particle effect. All names can be found `here. <https://shynixn.github.io/PetBlocks/apidocs/com/github/shynixn/petblocks/api/business/enumeration/ParticleType.html>`_
* ClickParticle.Speed: Speed of the particle effect.
* ClickParticle.Amount: Amount fo particles being displayed.
* ClickParticle.OffXYZ: Offset values for the particle effect.
* ClickSound.Name: Name of the sound. All names can be found `here. <https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html>`_
* ClickSound.Volume: Volume of the sound.
* ClickSound.Pitch: Pitch of the sound.