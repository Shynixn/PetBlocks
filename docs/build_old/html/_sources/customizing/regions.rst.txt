Worlds and Regions
==================

It is often the case that you have got a world or region management plugin installed on your server regardless if you are using Spigot or Sponge.

.. note::
 PetBlocks includes a restriction that pets **cannot** be deleted by other plugins.

Worlds
~~~~~~

Restricting the pets to certain worlds is very easy. Decide if you want to whitelist or blacklist (default) worlds by moving
the 'all' tag to excluded or included. Afterwards, add the world names.

**config.yml**:
::
    ############################

    # World settings

    # These settings allow spawning or deny spawning pets in certain worlds.

    # world-excluded: Add worlds where the pets should not be able to spawn.
    # (When 'all' is added to excluded all worlds are disabled for spawning except the added worlds to included)
    # world-included: Add worlds where the pets should be able to spawn.
    # (When 'all' is added to included all worlds are enabled for spawning except the added worlds to excluded)

    # DO NOT PUT 'all' into both included and excluded!

    ############################

    world:
      excluded:
      - ''
      included:
      - 'all'

Regions
~~~~~~~

Pets cannot be restricted by specific plugins regarding regions as there are too many of them.

However, some region plugins allow executing commands when a player enters a region. Simply execute the command **/petblocks disable <player-name-entering-region>** to disable the pet.
To lock the player from calling the pet you should also remove the pet calling permission via a command from your permission plugin.