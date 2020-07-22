Example: Hiding GUI items when the player does not have certain permissions
===========================================================================

This example hides one selected gui item when a player does not have permissions.

Make sure you have understood the `introduction <gui.html>`_ to this topic.

Goal
~~~~

* All players without the permission 'mydonators.vip' should not be able to open the wardrobe page. (you can replace the permission with anything)

.. image:: ../_static/images/sample-hiding-gui-item.png

Step by Step
~~~~~~~~~~~~

As mentioned before a powerful editor like Notepad++ in this example is recommend.

.. warning::
 **1. Make a copy of your config.yml!** YAML, the configuration language this config.yml is using, takes spaces and tabs very serious, so be careful otherwise
 the config.yml cannot be parsed when executing the reload command.

2. Add the permission tag with any permission.

3. Add the hidden-on tag list with the item - no-permission.

.. image:: ../_static/images/sample-hiding-gui-item-2.png

.. raw:: html

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <a class="btn" style="width:100%" href="../_static/samples/config-samplehidingpermission.yml" download="config.yml"><i class="fa fa-download"></i>Download config.yml</a>
    <br/><br/><br/>

.. note::
 If the server console now displays an error then the config.yml cannot be parsed. In this case
 apply your backup you have made and try the steps again being extra carefully.