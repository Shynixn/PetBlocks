Feeding
=======

The AI called "Feeding" is a behaviour ai which lets the pet owner feed the pet via right clicking it with the specified food.

Requirements
~~~~~~~~~~~~

This ai is a **event based ai**, therefore this ai will always be active once applied.


Configuring in your config.yml
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* Only the type parameter is required, all other parameters are optional.
* You can specify this ai multiple times in order to allow the pet to eat different kind of food.
* Set the sound or particle name to none in order to send no sound or particles.

config.yml
::
    type: 'feeding'
    item-id: 391
    item-damage: 0
    click-particle:
      name: 'heart'
      speed: 0.1
      amount: 20
      offx: 1.0
      offy: 1.0
      offz: 1.0
    click-sound:
      name: 'EAT'
      volume: 5.0
      pitch: 1.0

You can find all options explained at the bottom of this page.

Properties
~~~~~~~~~~

* Type: Unique identifier of the ai.
* Tag: Optional tag to identify a specific ai configuration.
* ItemId: Id of the item.
* ItemDamage: Damage/DataValue of the item.
* ClickParticle.Name: Name of the particle effect. All names can be found `here. <https://shynixn.github.io/PetBlocks/apidocs/com/github/shynixn/petblocks/api/business/enumeration/ParticleType.html>`_
* ClickParticle.Speed: Speed of the particle effect.
* ClickParticle.Amount: Amount fo particles being displayed.
* ClickParticle.OffXYZ: Offset values for the particle effect.
* ClickSound.Name: Name of the sound. All names can be found `here. <https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html>`_
* ClickSound.Volume: Volume of the sound.
* ClickSound.Pitch: Pitch of the sound.