A journey of ais, skins and user settings
=========================================

Let's take a look at our default pet which gets spawned via the default config, by entering the command
and clicking on the GUI. Of course, you need to have **all recommend permissions** to perform this guide.

.. note::
 If you have already modified your pet or changed the config.yml, you can always delete (renaming is better) the config, reload the server and
 enter **/petblocks reset** to go back to this step.


.. image:: ../_static/images/petblock-command.png


.. image:: ../_static/images/petblock-default-menu.png


Taking a look at your pet
~~~~~~~~~~~~~~~~~~~~~~~~~

We will ignore the GUI for now and just focus on the pet itself.


.. image:: ../_static/images/grasspet-basic.png


Looks good right? The pet could be spawned and is walking after you.

However, now let's take a look at the **technical configuration** of the pet which is
the most interesting part for server owners.

.. image:: ../_static/images/grasspet-technical.png

This graphic shows the 3 major parts of each pet in PetBlocks. Every pet stores all
3 parts but the containing data can be different per pet (per player).

Taking an even deeper look at your pet
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

We can actually take a look into the inside of a pet by using the **/petblocks debug** command.

.. note::
 When you are customizing pets, it is highly recommend to turn on the **/petblocks debug** menu.

.. image:: ../_static/images/pet-debug.png

Move your mouse over the automatically refreshing UI to take a look at the values.

Let's continue on the next page with taking a deep look at skins.