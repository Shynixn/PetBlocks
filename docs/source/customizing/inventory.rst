Inventory
=========

The AI called "Inventory" is an ai which lets you store items in your pet.

**Introduction**

This inventory can only be accessed via the gui of PetBlock by adding the following
script to a gui item of your choice.

config.yml
::
     script: 'show-inventory 1 27'

The storage from index 1 to index 27 will open.

.. image:: ../_static/images/petblocks-storage-27.JPG

In this inventory, players can freely move and store items in their pet.

.. note:: This storage is persistent and items will be kept even when the
 player dies or leaves the server.

.. image:: ../_static/images/petblocks-storage-27-item.JPG

.. warning:: As this storage is bound to an ai, all items will be lost if you
 try to remove or replace the ai. This also happens if you reset a pet.

**Increasing the storage**

Theoretically, there no limit of the amount of items you can store in the storage. It is up
to the server owner to decide how many slots are available to their players.

.. note:: By restricting gui items with **permissions** you can **sell more storage** to your players.
  You can find a full inventory example in the examples section.


Requirements
~~~~~~~~~~~~

This ai is always present on the pet and does not require anything.

Configuring in your config.yml
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**config.yml**
::
    type: 'inventory'

Properties
~~~~~~~~~~

* Type: Unique identifier of the ai.