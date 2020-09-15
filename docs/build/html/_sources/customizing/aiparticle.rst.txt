Particle
========

The AI called "Particle" is a pathfinder based ai and is only active when a pet is spawned.

Examples for usages are:

* Display particle every second or less when the pet is spawned.
* Build particle categories
* Add particles to skins
* Add multiple particles
* ...

Configuring in your config.yml
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* Only the type parameter is required, all other parameters are optional.
* You can specify this ai multiple times in order to display multiple particles.
* Set the particle name to none in order to display no particles.

config.yml
::
      type: 'particle'
      tag: 'my-particle'
      delay-between: 1.0
      offset:
        x: 0.0
        y: 0.0
        z: 0.0
      particle:
        name: 'reddust'
        amount: 1
        offx: 255.0
        offy: 0.0
        offz: 0.0

You can find all options explained at the bottom of this page.

Properties
~~~~~~~~~~

* Type: Unique identifier of the ai.
* Tag: Optional tag to identify a specific ai configuration.
* Delay-Between: Time in seconds until the particle is played again. Accepts comma values like 0.5 or 1.8.
* Offset: Relative offset from the center of the pet to the looking direction of the pet. A positive x value always displays on the left side of the pet, a negative x value on the right side of the pet.
* Particle.Name: Name of the particle effect. All names can be found `here. <https://shynixn.github.io/PetBlocks/apidocs/com/github/shynixn/petblocks/api/business/enumeration/ParticleType.html>`_
* Particle.Speed: Speed of the particle effect.
* Particle.Amount: Amount fo particles being displayed.
* Particle.OffXYZ: Offset values for the particle effect.
