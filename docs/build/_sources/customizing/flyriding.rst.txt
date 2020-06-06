Fly Riding
==========

The AI called "Fly Riding" is a behaviour ai which lets the pet owner ride the pet and fly with it via WASD.

Once the Fly Riding ai is applied, it gets executed at a higher priority than any other ai and partially blocks them while it
is being active.

Requirements
~~~~~~~~~~~~

This ai is a **event based ai**, therefore this ai will always be active once applied.

Configuring in your config.yml
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* Only the type parameter is required, all other parameters are optional.

config.yml
::
    type: 'fly-riding'
    speed: 2.0

You can find all options explained at the bottom of this page.

Properties
~~~~~~~~~~

* Type: Unique identifier of the ai.
* Tag: Optional tag to identify a specific ai configuration.
* Speed: The riding speed of the pet.