# Selectable particle effects

This is a tutorial to get into building selectable parts for pets. It can also be used
to define selectable sounds, potion effects, etc.

## 1. Edit the pet template

Add a basic vanilla particle command as action to one of the loops. In this case the ``idle`` loop.

* Instead of absolute coordinates we use the pet coordinate placeholders.
* The placeholder ``%petblocks_js_mypart_selected%`` is a custom placeholder defined by you, which stores the **currently selected particle**. You can also name it ``%petblocks_js_mysound_selected%`` to store sound names for example.
* The condition checks if the placeholder can actually be resolved e.g. a particle has been set.

```yaml
loops:
  idle:
    actions:
      - name: "Play particle effect"
        condition:
          type: STRING_NOT_CONTAINS
          left: "%petblocks_js_mypart_selected%"
          right: "petblocks_js_mypart_selected"
        type: "COMMAND"
        level: "SERVER"
        run:
          - "/particle %petblocks_js_mypart_selected% %petblocks_pet_locationX% %petblocks_pet_locationY% %petblocks_pet_locationZ%"       
```

## 2. Adjust vanilla command settings

A vanilla command may automatically log to the console and to the chat of every op player. You can disable the output for op players using vanilla game rules such as ``gamerule sendCommandFeedback false``, ``/gamerule logAdminCommands false``, ``/gamerule commandBlockOutput false``. If you want to hide the output in your console, you need to install a LogFilter plugin.
 
## 3. Check if it works ingame

Execute the following commands and take a look if the pet displays heart particles.

```
/petblocks reload
/petblocks variable pet mypart heart
```

##  4. GUI buttons

Open the GUI of petblocks or your favourite GUI plugin and add a button where the command ``/petblocks variable pet mypart heart %petblocks_owner_name%`` is executed with server level permissions.
You can add more particles by creating new buttons with new commands e.g. ``/petblocks variable pet mypart angry_villager %petblocks_owner_name%``
