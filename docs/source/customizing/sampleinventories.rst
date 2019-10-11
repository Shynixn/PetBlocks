Example: Increasing the pet storage by a premium storage
========================================================

This example adds a new premium storage for players who have got the required permissions.

Make sure you have understood the `introduction <gui.html>`_ to this topic.

Goal
~~~~

* All players with the permission 'mydonators.vip' should be able to access a large premium storage.

.. image:: ../_static/images/sample-premiumstorage-1.png

.. image:: ../_static/images/sample-premiumstorage2.JPG

Step by Step
~~~~~~~~~~~~

As mentioned before a powerful editor like Notepad++ in this example is recommend.

.. warning::
 **1. Make a copy of your config.yml!** YAML, the configuration language this config.yml is using, takes spaces and tabs very serious, so be careful otherwise
 the config.yml cannot be parsed when executing the reload command.

2. Create a new gui item called equipment-premium at the corresponding place.

3. Add the script **show-inventory 55 106**. If the slot range (55 - 106) is larger than 27 then a big inventory will be displayed.

4. Restrict the gui item by the permission **mydonators.vip**.

5. (Optional) Add the **<permission>** placeholder to the lore in order to let players know if they have got permission to the storage.

.. image:: ../_static/images/sample-premiumstorage-3.png

.. raw:: html

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <a class="btn" style="width:100%" href="../_static/samples/config-premiuminventory.yml" download="config.yml"><i class="fa fa-download"></i>Download config.yml</a>
    <br/><br/><br/>

.. note::
 If the server console now displays an error then the config.yml cannot be parsed. In this case
 apply your backup you have made and try the steps again being extra carefully.
