Flee in Combat
==============

The AI called "Flee in Combat" is a behaviour ai which lets the pet disappear immediately when one of the following conditions is met:

* The pet owner is getting damaged
* The pet itself is getting damaged

When this condition gets triggered the pet will disappear and not be able to spawn until the **reappear-delay** has reached 0.

If the player is taking additional damage while the **reappear-delay** is counting towards zero, the counter will be reset and start
again counting towards zero.

Requirements
~~~~~~~~~~~~

This ai is a **event based ai**, therefore this ai will always be active once applied.


Configuring in your config.yml
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* Only the type parameter is required, all other parameters are optional.

config.yml
::
    type: 'flee-in-combat'
    reappear-delay: 5

You can find all options explained at the bottom of this page.

Properties
~~~~~~~~~~

* Type: Unique identifier of the ai.
* Tag: Optional tag to identify a specific ai configuration.
* Reappear delay: Time in seconds until the pet will be able to spawn again.