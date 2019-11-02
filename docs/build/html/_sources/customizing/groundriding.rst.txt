Ground Riding
=============

The AI called "Ground Riding" is a behaviour ai which lets the pet owner ride the pet and ride with it via WASD.

Once the Ground Riding ai is applied, it gets executed at a higher priority than any other ai and partially blocks them while it
is being active.

Requirements
~~~~~~~~~~~~

This ai is a **event based ai**, therefore this ai will always be active once applied.

Configuring in your config.yml
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* Only the type parameter is required, all other parameters are optional.

config.yml
::
    type: 'ground-riding'
    climbing-height: 1.0
    speed: 2.0
    offset-y: -1

You can find all options explained at the bottom of this page.

Properties
~~~~~~~~~~

* Type: Unique identifier of the ai.
* Tag: Optional tag to identify a specific ai configuration.
* Climbing Height: Climbing height while riding the pet.
* Speed: The riding speed of the pet.
* Offset-Y: Offset from the ground.