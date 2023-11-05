Rename Commands
~~~~~~~~~~~~~~~

You are also allowed to rename the command /petblock in the config.yml. You can even customize the permissions.

**config.yml**
::
    ############################

    # Command settings

    # There are commands available in PetBlocks you can customize by yourself.
    # This means you can disable the command entirely, change the name, description
    # and even permission.

    ############################

    commands:
      petblock:
        enabled: true
        command: petblock
        useage: /<command>
        description: Opens the pet GUI.
        permission: petblocks.command.use
        permission-message: You don't have permission
