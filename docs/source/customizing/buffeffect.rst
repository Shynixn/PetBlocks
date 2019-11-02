Buff Effect
=========

The AI called "buff-effect" is an ai which lets your spawned pet apply potion effects to their
owner in a configurable interval.

.. image:: ../_static/images/buff-effect-visible.png

Requirements
~~~~~~~~~~~~

This ai is a **pathfinder based ai**, which is only active when the pet is spawned and walking beside the player.

Configuring in your config.yml
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**config.yml**
::
      type: 'buff-effect'
      tag: 'my-buff-effect'
      cooldown: 5
      effect:
        potion-type: 'SPEED'
        duration: 5
        amplifier: 0
        ambient: true
        particles: true
      particle:
        name: 'reddust'
        amount: 20
        offx: 0
        offy: 255
        offz: 0
      sound:
        name: 'NOTE_PLING'
        volume: 10
        pitch: 2.0

Properties
~~~~~~~~~~

* Type: Unique identifier of the ai.
* Tag: Optional tag to identify a specific ai configuration.
* Cooldown: Cooldown until a pet applies the defined potion effect again
* Effect: Potion effect configuration.
* Effect.potion-type: Type of the potion effect
* Effect.duration: Duration of the potion effect in seconds
* Effect.amplifier: Amplifier of the strength of the effect
* Effect.ambient: Visible ambient effects
* Effect.particle: Visible particles
* Particle: Particle effect
* Sound: Sound effect

.. note::
    You can find all available potion effect types on `this page <https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html>`_. Use the field value for example "ABSORPTION".