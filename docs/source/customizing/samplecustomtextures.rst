Example: Using custom texture and 3d models for your pets
=========================================================

This example shows how to create a `resource pack <https://minecraft.gamepedia.com/Resource_pack>`_ in order to put
custom textures or 3d models on to the pets of PetBlocks.

.. note::
 This tutorial works for all version above and including 1.9+. There are new ways to get the same result
 in more recent versions of minecraft but this is still a working solution.

Goal
~~~~

* All players should see and select a 3d model of a pet in the pet inventory.
* When the pet is being active a 3d model should be shown.

.. image:: ../_static/images/custom-textures-bee-inventory.png

.. image:: ../_static/images/custom-textures-bee-flying.png

.. image:: ../_static/images/bee-gif-1.gif

.. image:: ../_static/images/bee-gif-2.gif

Step by Step
~~~~~~~~~~~~

As mentioned before a powerful editor like Notepad++ in this example is recommend.

.. warning::
 **1. Make a copy of your config.yml!** YAML, the configuration language this config.yml is using, takes spaces and tabs very serious, so be careful otherwise
 the config.yml cannot be parsed when executing the reload command.

2. Download the ready to use resource pack PetBlx which was created by `NullBlox <https://www.spigotmc.org/members/nullblox.3822/>`_.

.. raw:: html

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <a class="btn" style="width:100%" href="../_static/samples/PetBlocks-Resource-Pack.zip" download="PetBlocks-Resource-Pack.zip"><i class="fa fa-download"></i> Download Resource Pack</a>
    <br/><br/><br/>

3. Take a look at the **/assets/minecraft/models/item** directory inside the downloaded .zip file.
   You can see a stone_hoe.json file which contains the whole configuration how minecraft should interprete the models
   in this resource pack.

.. image:: ../_static/images/petblx-1.png

4. (Optional) Editing the resource pack. This resource pack is already finished to work together with PetBlocks to show 2 models (we will only show the bee model).
Anyway, this how you would edit it:

.. note::
  The following section was copied from the guide https://www.spigotmc.org/wiki/custom-item-models-in-1-9-and-up/ created and owned by the spigot community. It was accessed on the 9th November 2019.

In this item folder, create a new file called wooden_hoe.json. This will be the "container" for all of your custom items. It tells the client which models to use for certain durability values of the wooden hoe.

This file will look something like this:

.. code-block:: json

    {
      "parent": "item/handheld",
      "textures": {
        "layer0": "items/wood_hoe"
      },
      "overrides": [
        {"predicate": {"damaged": 0, "damage": 0.01666666666667}, "model": "item/my_cool_custom_item"}
      ]
    }

Ok, so let's break this down.

We are overriding wood hoe, but extending the base version. So the "parent" and "textures" are telling the client to use the wood hoe defaults. This means that a wood hoe with zero damage will look normal.

Now for the new stuff- the "overrides" block lets you specify different models to use depending on certain properties of the item. These properties are called "predicates", and for our purposes we will focus on the "damage" predicate.

This will vary the model based on damage, as a percentage from 0 to 1. So the value "0.016666" in there comes from the ratio "1/60" - because I want to apply this model at a damage/durability value of 1, and a wood hoe has a max durability of 60.

See the mc wiki for all the durabilities of items: http://minecraft.gamepedia.com/Hoe

You can add as many damage predicates as you want, up to the max durability of the item- just do the "x/60" math for each one.

You can also use this tool to auto-generate a template given a specific type of tool, saving you from the maths: http://accidentalgames.com/media/durabilityModels.php
(If you are using 1.13 version or later, here's the updated version of this generator:
https://geenium.github.io/damage-value-generator/ )

Preserving the original items
If you want to still be able to use the vanilla damageable items, this is possible!
Add another entry to your model file, so it ends up looking like this: ssss

.. code-block:: json

    {
      "parent": "item/handheld",
      "textures": {
        "layer0": "items/wood_hoe"
      },
      "overrides": [
        {"predicate": {"damaged": 0, "damage": 0.01666666666667}, "model": "item/my_cool_custom_item"},
        {"predicate": {"damaged": 1, "damage": 0}, "model": "item/wooden_hoe"}
      ]
    }

This will cause damaged versions to continue to use the base model. Note that if you use the JSON generator I linked above, you'll need to edit the final entry- it needs to refer to the model name, so wooden_hoe instead of wood_hoe for instance.

Finally, put your custom item model in the same item folder in the resource pack, in this case it would be in "my_cool_custom_item.json". (Creating custom item models is not in the scope of this tutorial, I think there are some good ones out there already and also tools like Cubik you can use)

Zip up the root folder of your RP, add it to your <minecraft>/resourcepacks folder and you should be able to load it and see your custom item!



If in doubt, feel free to take a look at my resource pack structure on github:

https://github.com/elBukkit/MagicPlugin/tree/master/Magic/src/resource-pack/default

FINAL NOTES

* Once you have this all working, there are some things to consider.
* The higher-level tools will give you more item models to work with. A diamond hoe has a max durability of 1,562 (!)

5. (Optional) Create your own 3d models for minecraft.

`NullBlox <https://www.spigotmc.org/members/nullblox.3822/>`_ who has originally created the PetBlx resource pack has created tons
of amazing and `well explained videos <https://www.youtube.com/watch?v=iS7xeriOu80&list=PLhgAh4tPmxSpgo8y2ZcnAgH5ZndYYCs5z>`_ how to setup resource packs and create 3d models. You can check out his youtube playlist:

.. raw:: html

    <iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/iS7xeriOu80" frameborder="0" allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

6. The downloaded resource pack therefore contains a configuration for the **stone_hoe** item which we now need to setup in PetBlocks.

.. image:: ../_static/images/petblx-1.png

* The numeric **id of the stone_hoe is 291** which we set to both icon and skin.
* Configure the correct damage (which model you would like to show) and set unbreakable to true.
* Additionally, we also setup the walking ai with a high ground offset to set the pet flying.

.. image:: ../_static/images/bee-model-show.png

7. Reload the **config.yml**, activate the resource pack and check out if your model works ingame.

.. raw:: html

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <a class="btn" style="width:100%" href="../_static/samples/config-beemodel.yml" download="config.yml"><i class="fa fa-download"></i>Download config.yml</a>
    <br/><br/><br/>

.. note::
 If the server console now displays an error then the config.yml cannot be parsed. In this case
 apply your backup you have made and try the steps again being extra carefully.
