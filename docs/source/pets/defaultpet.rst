Default Pet
===========

It is recommend to setup the default pet players are receiving when they are trying to use PetBlocks for the first time.

.. note:: Make sure you have confirmed that PetBlocks is working correctly on your server and
          **pets can spawn at the position of your player**. Otherwise, please read this guide.


As everything is customizeable via the **config.yml** in the PetBlocks folder, open this file to start editing.
An editor like Notepad++ or VisualStudio Code is highly recommend for this task.

1. Locate the following section in your config.yml.

**config.yml**
::
    ############################

    # Default Pet settings

    # The pets of this plugin are very, very customizeable.
    # Please take a look into the official documentation mentioned at the top of this file
    # in order to configure your pet correctly.

    # These are the settings players are starting with when they get their first pet. However,
    # you can add all of these settings to templates in order to change them per skin or particle
    # or whatever.

    # You can find a list of all available goals on the documentation.

    ############################

    global-configuration:
      respawn-delay: 4
      teleport-delay: 0
      apply-pet-on-spawn: false
      overwrite-previous-pet: false
      sounds-other-players: true
      particles-other-players: true
      max-petname-length: 20
      petname-blacklist:
      - 'petty'
      - 'shitty'

    default-pet:
      enabled: false
      name: "<player>'s Pet"
      health: 20.0
      invincible: true
      hitbox-entitytype: RABBIT
      sound-enabled: true
      particle-enabled: true
      skin:
        typename: 2
        datavalue: 0
        unbreakable: false
        owner: ''
      modifier:
        climbing-height: 0.0
        movement-speed: 0.0
      goals:
        1:
          id: 'follow-owner'
          max-distance: 50
        2:
          id: 'walking-particle'
          name: 'largesmoke'
          speed: 0.01
          amount: 2
          offx: 0.5
          offy: 0.5
          offz: 0.5
        3:
          id: 'ambient-sound'
          name: 'CHICKEN_IDLE'
          volume: 1.0
          pitch: 1.0
        4:
          id: 'walking-sound'
          name: 'CHICKEN_WALK'
          volume: 1.0
          pitch: 1.0

    ############################










