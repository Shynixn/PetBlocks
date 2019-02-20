Regions
=======

It is often the case that you have got a region management plugin installed on your server regardless if you are using Spigot or Sponge.
They can cause issues when spawning pets when certain region restrictions are enabled.

Simply allow spawning **rabbits and zombies** in your regions and PetBlocks should work correctly. If it is not possible to allow this,
PetBlocks also hooks into the following popular region plugins to allow spawning its pets anyway. Please request a region management plugin
if it is not supported on this list.

The following **Bukkit plugins** are supported:

* WorldGuard

The following **Sponge plugins** are supported:

* GriefPrevention

WorldGuard
~~~~~~~~~~

* PetBlocks should automatically hook into any installed WorldGuard plugin and allows spawning pets in any region you configure.
  You can restrict the regions by taking a look at the region settings below.

GriefPrevention
~~~~~~~~~~~~~~~

* PetBlocks uses the same way as the popular Pixelmon mod to allow or deny pets in certain regions.
* Use the modid **petblocks** and **any** for settings flags. (rabbit,zombie,armorstand)

**command**:
::
 /cf entity-spawn petblocks:any true

Region Settings
~~~~~~~~~~~~~~~

You can also specify where pets are allowed to spawn. The world settings are available for both Spigot and Sponge, however
the region settings are only available when WorldGuard is installed.

**config.yml**:
::
 # World-region settings
    world:
        excluded:
          - ''
        included:
          - 'all'
    region:
        excluded:
          - ''
        included:
          - 'all'
