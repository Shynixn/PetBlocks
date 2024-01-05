# Custom Models

PetBlocks can be used to build custom pets with custom models. A bee model is shown below.

![Gif](assets/custom-textures-bee-inventory.png)

![Gif](assets/custom-textures-bee-flying.png)

![Gif](assets/bee-gif-1.gif)

![Gif](assets/bee-gif-2.gif)

## Tutorial

!!! note "Important"
    There are even more ways to achieve custom model pets in modern Minecraft versions. This is only one way to do it.

A pet in PetBlocks is an Armorstand with an item on its head. The NBT tags of this item can be freely manipulated in your ``pet.yml``. 
Therefore, we can add the NBT tag called ``CustomModelData`` on the itemstack and display a custom model instead of the block item.

The first step is to create a resource pack. A good starting point to create a resource pack with CustomModelData can be found here:
[https://www.planetminecraft.com/forums/communities/texturing/new-1-14-custom-item-models-tuto-578834/](https://www.planetminecraft.com/forums/communities/texturing/new-1-14-custom-item-models-tuto-578834/)

Secondly, update the NBT tags in the ``pet.yml`` file.

```nbt: '{CustomModelData:mycustommodelname}'```
