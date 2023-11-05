Example: Selling pet buff effects to your players
=================================================

This example adds a new gui page where players can see and toggle buff effects for their
pet if they obtained the permission via a shop plugin.

Make sure you have understood the `introduction <gui.html>`_ to this topic.

Goal
~~~~

* All players with the permission 'mydonators.vip' should be able to toggle a speed buff effect.

.. image:: ../_static/images/buff-effect-visible.png

.. image:: ../_static/images/buff-effects-2.JPG

Step by Step
~~~~~~~~~~~~

As mentioned before a powerful editor like Notepad++ in this example is recommend.

.. warning::
 **1. Make a copy of your config.yml!** YAML, the configuration language this config.yml is using, takes spaces and tabs very serious, so be careful otherwise
 the config.yml cannot be parsed when executing the reload command.

2. Create a 2 new gui items called speed-one-enabled and speed-one-disabled at the corresponding place.

3. Add the buff-effect ai.

4. Restrict the gui item by the permission **mydonators.vip**.

5. (Optional) Add the **<permission>** placeholder to the lore in order to let players know if they have got permission to the storage.

.. image:: ../_static/images/buff-effect-config.png

.. raw:: html

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <a class="btn" style="width:100%" href="../_static/samples/config-buffeffects.yml" download="config.yml"><i class="fa fa-download"></i>Download config.yml</a>
    <br/><br/><br/>

.. note::
 If the server console now displays an error then the config.yml cannot be parsed. In this case
 apply your backup you have made and try the steps again being extra carefully.
