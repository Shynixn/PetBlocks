Health
======

The AI called "Health" is a behaviour ai which introduces health into the pet environment.

* Every damage source, even fall damage, hurts the pet and other players are able to kill it.
* It will regain +1 health per second until the max health is reached.
* When the health reaches 0, the pet will immediately disappear and not be able to spawn until the **respawning-delay** has reached zero.

.. warning:: This ai is **broken in Sponge** as the hitbox of the Armorstand does not interact to damage events.


Requirements
~~~~~~~~~~~~

This ai is a **event based ai**, therefore this ai will always be active once applied.

Configuring in your config.yml
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* Only the type parameter is required, all other parameters are optional.

config.yml
::
    type: 'health'
    max-health: 20.0
    health: 20.0
    respawning-delay: 5

You can find all options explained at the bottom of this page.

Properties
~~~~~~~~~~

* Type: Unique identifier of the ai.
* Max-Health: The max amount of health points a pet can reach while regaining health.
* Health: The current amount of health points of a pet.
* Respawning-Delay: The time in seconds until the pet is able to respawn after it has died.