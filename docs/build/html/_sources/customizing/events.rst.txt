Events
======

It is possible to modify the pet when certain events are triggered on the server.

Examples for usages are:

* Adding ais to existing pets
* Modifying pets for all players when the join the next time
* ...

Configuring in your config.yml
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

1. Open the **config.yml** of PetBlocks.
2. Search for the following section.

**config.yml**

.. code-block:: yaml

    ############################

    # Event settings

    # These settings allow to add/replace/remove ais when certain events happen.

    ############################

    events:
    ...

2. Check the available events below. All are optional.

**config.yml**

.. code-block:: yaml

    ############################

    # Event settings

    # These settings allow to add/replace/remove ais when certain events happen.

    ############################

    events:
      onjoin:
      onquit:
      onpetspawn:
      onpetdespawn:
      onsneak:

3. Add ai modifications to them. You can find an example below.

**config.yml**

.. code-block:: yaml

    ############################

    # Event settings

    # These settings allow to add/replace/remove ais when certain events happen.

    ############################

    events:
      onjoin:
        replace-ai:
          1:
            type: 'entity-nbt'
            tag: 'nbt-default'
            armorstand-nbt: '{Invulnerable:1,Invisible:1,PersistenceRequired:1,ShowArms:1,NoBasePlate:1,DisabledSlots:2039583}'
            hitbox-nbt: '{CustomNameVisible:0,ActiveEffects:[{Id:14,Duration:999999999,Amplifier:0,ShowParticles:0b}]}'
          2:
            type: 'entity-nbt'
            tag: 'nbt-custom-name-visibility'
            armorstand-nbt: '{CustomNameVisible:1}'
          3:
            type: 'entity-nbt'
            tag: 'nbt-marker'
            armorstand-nbt: '{Marker:0}'
      onquit:
        remove-ai:
          1:
            type: 'buff-effect'
            tag: 'my-buff-effect'
      onpetspawn:
        replace-ai:
          1:
            type: 'buff-effect'
            tag: 'my-buff-effect'
            cooldown: 5
            effect:
              potion-type: 'SPEED'
              duration: 10
              amplifier: 0
              ambient: true
              particles: true
            particle:
              name: 'reddust'
              amount: 20
              offx: 0
              offy: 255
              offz: 255
            sound:
              name: 'NOTE_PLING'
              volume: 10
              pitch: 2.0
      onpetdespawn:
        remove-ai:
          1:
            type: 'buff-effect'
            tag: 'my-buff-effect'
      onsneak:
        replace-ai:
          1:
            type: 'entity-nbt'
            tag: 'nbt-custom-name-visibility'
            armorstand-nbt: '{CustomNameVisible:1}'
          2:
            type: 'entity-nbt'
            tag: 'nbt-marker'
            armorstand-nbt: '{Marker:0}'
        remove-ai:
          1:
            type: 'wearing'
          2:
            type: 'ground-riding'
          3:
            type: 'fly-riding'

4. Modify the values add or own ais.



Properties
~~~~~~~~~~

* Events.OnJoin: Is triggered when a player is joining the server.
* Events.OnQuit: Is triggered when a player is quitting the server.
* Events.OnPetSpawn: Is triggered when a pet spawns.
* Events.OnPetDeSpawn: Is triggered when a pet despawns.
* Events.OnSneak: Is triggered when player sneaks.
