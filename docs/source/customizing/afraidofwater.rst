Afraid of Water
===============

The AI called "Afraid of Water" is a behaviour ai which basically lets the pet avoid swimming in water.

Pets do generally avoid water but they will bravely follow their owner if he decides to go swimming anyway. However,
when applying this ai to the pet, the pet will leap back to their owner on contact with water and display a particle effect.

.. image:: ../_static/images/aiafraidofwater.PNG


Requirements
~~~~~~~~~~~~

This ai is a **pathfinder based ai**, therefore this ai will only be active if one of the following ais is present:

* Walking
* Hopping
* Flying

Configuring in your config.yml
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* Only the type parameter is required, all other parameters are optional.
* You can specify this ai multiple times in order to display multiple particles.
* Set the particle name to none in order to display no particles.

config.yml
::
      type: 'afraid-of-water'
      stop-delay: 5
      particle:
        name: 'VILLAGER_ANGRY'
        speed: 0.1
        amount: 20
        offx: 1.0
        offy: 1.0
        offz: 1.0

You can find all options explained at the bottom of this page.

Properties
~~~~~~~~~~

* Type: Unique identifier of the ai.
* Stop-Delay: Time in seconds until the pet performs another leap. This option gets relevant when a pet immediately touches water again after trying to leap back to the player.
* Particle.Name: Name of the particle effect. All names can be found `here. <https://shynixn.github.io/PetBlocks/apidocs/com/github/shynixn/petblocks/api/business/enumeration/ParticleType.html>`_
* Particle.Speed: Speed of the particle effect.
* Particle.Amount: Amount fo particles being displayed.
* Particle.OffXYZ: Offset values for the particle effect.