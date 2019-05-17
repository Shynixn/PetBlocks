Follow Owner
===========

The AI called "Follow Owner" is a behaviour ai which lets the pet follow the pet owner around.

Requirements
~~~~~~~~~~~~

This ai is a **pathfinder based ai**, therefore this ai will only be active if one of the following ais is present:

* Walking
* Hopping
* Flying

Configuring in your config.yml
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* Only the type parameter is required, all other parameters are optional.

config.yml
::
    type: 'follow-owner'
    min-distance: 3.0
    max-distance: 50.0
    speed: 1.5

You can find all options explained at the bottom of this page.

Properties
~~~~~~~~~~

* Type: Unique identifier of the ai.
* Min-Distance: The minimum amount of distance the pet tries to stay away from the player.
* Max-Distance: The maximum amount of distance the pet tries to stay away from the player.
* Speed: Additional, speed modifier. Should be carefully adjusted together with the speed of the (Walking, Hopping, Flying) ai.