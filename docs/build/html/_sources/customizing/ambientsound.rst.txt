Ambient Sound
=============

The AI called "Ambient Sound" is a effect ai which simply plays a specified sound with random times.

Requirements
~~~~~~~~~~~~

This ai is a **pathfinder based ai**, therefore this ai will only be active if one of the following ais is present:

* Walking
* Hopping
* Flying

Configuring in your config.yml
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* Only the type parameter is required, all other parameters are optional.
* You can specify this ai multiple times in order to play multiple random sounds.
* Set the sound name to none in order to send no sound.

config.yml
::
     type: 'ambient-sound'
     sound:
        name: 'ENTITY_ENDER_DRAGON_AMBIENT'
        volume: 5.0
        pitch: 1.0

You can find all options explained at the bottom of this page.

Properties
~~~~~~~~~~

* Type: Unique identifier of the ai.
* Tag: Optional tag to identify a specific ai configuration.
* Sound.Name: Name of the sound. All names can be found `here. <https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html>`_
* Sound.Volume: Volume of the sound.
* Sound.Pitch: Pitch of the sound.