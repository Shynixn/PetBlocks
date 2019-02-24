List of all ais
===============

Here you can find all available ai tags with a description and full configuration examples.

.. note:: You can put **none** into the sound name or particle name to disable the particle.

.. note:: If there are any problems, missing ai parameters or even missing full ais, I am happy to discuss them
 with you on my discord server. https://discordapp.com/invite/Y27tx8Y

.. note:: Modify the offset-y value if you want to fit **Custom Models** with the pets of PetBlocks.

.. toctree::
 aialltags

Afraid of Water
~~~~~~~~~~~~~~~

This ai lets the pet avoid water and immediately leave it with optional displaying particles.

* Can be added multiple times to a pet to display more than one particle.

config.yml
::
      type: 'afraid-of-water'
      stop-delay: 5
      particle:
        name: 'heart'
        speed: 0.1
        amount: 20
        offx: 1.0
        offy: 1.0
        offz: 1.0

Ambient Sound
~~~~~~~~~~~~~

This ai lets the pet play sounds randomly when moving around.

* Can be added multiple times to a pet to play more than one sound.

config.yml
::
      type: 'ambient-sound'
      sound:
        name: 'CHICKEN_IDLE'
        volume: 5.0
        pitch: 1.0

Carry
~~~~~

This ai lets the pet be carried by the player when he clicks on it.

config.yml
::
      type: 'carry'

Feeding
~~~~~~~

This ai lets the pet eat the food the player right clicks on it.

* Can be added multiple times to allow a pet eating different items and play more than one particle or sound.

config.yml
::
      type: 'feeding'
      item-id: 391
      item-damage: 0
      click-particle:
        name: 'heart'
        speed: 0.1
        amount: 20
        offx: 1.0
        offy: 1.0
        offz: 1.0
      click-sound:
        name: 'EAT'
        volume: 5.0
        pitch: 1.0

Flee in Combat
~~~~~~~~~~~~~~

This ai lets the pet disappear immediately if the player takes or deals damage.

config.yml
::
      type: 'flee-in-combat'
      reappear-delay: 5


Float in Water
~~~~~~~~~~~~~~

This ai lets the pet float in water instead of sinking to the ground.

config.yml
::
      type: 'float-in-water'

Flying
~~~~~~

This ai lets the pet hover above ground even if the ground is below several blocks.

* Can be added multiple times to allow a pet playing more than one sound and particle.

config.yml
::
      type: 'flying'
      climbing-height: 1.0
      speed: 1.0
      offset-y: -1.0
      sound:
        name: 'CHICKEN_WALK'
        volume: 1.0
        itch: 1.0
      particle:
        name: 'reddust'
        speed: 0.01
        amount: 20
        offx: 0
        offy: 0
        offz: 255

Fly Riding
~~~~~~~~~~

This ai lets the player ride (fly) the pet immediately when it has been called.

* This ai has got a higher priority than other ais and as long it is added this
  ai gets primary executed.

config.yml
::
      type: 'fly-riding'
      speed: 2.0
      offset-y: -1


Follow Back
~~~~~~~~~~~

This ai lets the pet stick to the back of the player.

* This ai is not compatible to Follow Owner.
* This ai requires at least one movement ai (Flying, Walking, Hopping) to be executed.

config.yml
::
      type: 'follow-back'

Follow Owner
~~~~~~~~~~~~

This ai lets the pet follow the player.

* This ai is not compatible to Follow Back.
* This ai requires at least one movement ai (Flying, Walking, Hopping) to be executed.

config.yml
::
      type: 'follow-owner'
      min-distance: 3.0
      max-distance: 50.0
      speed: 1.5

Ground Riding
~~~~~~~~~~~~~

This ai lets the player ride the pet immediately when it has been called.

* This ai has got a higher priority than other ais and as long it is added this
  ai gets primary executed.

config.yml
::
      type: 'ground-riding'
      climbing-height: 1.0
      speed: 2.0
      offset-y: -1

Health
~~~~~~

This ai makes the pet no longer immortal and introduces health.

config.yml
::
      type: 'health'
      max-health: 20.0
      health: 20.0
      respawning-delay: 5

Hopping
~~~~~~~

This ai lets the pet hop like a rabbit on ground.

* Can be added multiple times to allow a pet playing more than one sound and particle.

config.yml
::
      type: 'hopping'
      climbing-height: 1.0
      speed: 1.0
      offset-y: -1.0
      sound:
        name: 'CHICKEN_WALK'
        volume: 1.0
        itch: 1.0
      particle:
        name: 'reddust'
        speed: 0.01
        amount: 20
        offx: 0
        offy: 0
        offz: 255

Walking
~~~~~~~

This ai lets the pet walk on ground.

* Can be added multiple times to allow a pet playing more than one sound and particle.

config.yml
::
      type: 'walking'
      climbing-height: 1.0
      speed: 1.0
      offset-y: -1.0
      sound:
        name: 'CHICKEN_WALK'
        volume: 1.0
        itch: 1.0
      particle:
        name: 'reddust'
        speed: 0.01
        amount: 20
        offx: 0
        offy: 0
        offz: 255

Wearing
~~~~~~~

This ai lets the pet ride the player immediately when it has been called.

* This ai has got a higher priority than other ais and as long it is added this
  ai gets primary executed.

config.yml
::
      type: 'wearing'