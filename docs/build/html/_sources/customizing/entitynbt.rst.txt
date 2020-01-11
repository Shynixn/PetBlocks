Entity-NBT
==========

The AI called "Entity-NBT" is an ai which allows you to apply custom
`NBT Tags <https://minecraft.gamepedia.com/Tutorials/Command_NBT_tags#Entities>`_ to your pet.

Configuration
~~~~~~~~~~~~~

* This ai can be added multiple times to the same pet.
* A pet entity always consists of a hitbox entity which is being used for pathfinding and hitbox calculation and an armorstand entity which is being used to show the block.
* You can use this ai to apply nbt tags to both of this entities.

================================  ========= ======================================================== =======================================================================
Tag                               Required  Description                                              Samples
================================  ========= ======================================================== =======================================================================
type                              yes       Identifier of the ai                                     entity-nbt
armorstand-nbt                    no        NBT Tags for the armorstand entity                       {Small:1}
hitbox-nbt                        no        NBT Tags for the hitbox entity                           {ActiveEffects:[{Id:8,Duration:2000,Amplifier:8,ShowParticles:0}]}
================================  ========= ======================================================== =======================================================================

Sample
~~~~~~

config.yml
::
  type: 'entity-nbt'
  armorstand-nbt: '{Small:1}'
  hitbox-nbt: '{ActiveEffects:[{Id:8,Duration:2000,Amplifier:8,ShowParticles:0}]}'