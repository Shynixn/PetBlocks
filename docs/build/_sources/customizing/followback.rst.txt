Follow Back
===========

The AI called "Follow Back" is a behaviour ai which lets the pet stick to the back of the player.

Requirements
~~~~~~~~~~~~

This ai is a **pathfinder based ai**, therefore this ai will only be active if one of the following ais is present:

* Walking
* Hopping
* Flying

Configuring in your config.yml
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

config.yml
::
    type: 'follow-back'

You can find all options explained at the bottom of this page.

Properties
~~~~~~~~~~

* Type: Unique identifier of the ai.
* Tag: Optional tag to identify a specific ai configuration.