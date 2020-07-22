Example: Creating small pets
============================

This example changes all of your pets to small pets by changing the default pet ais.

Make sure you have understood the `introduction <gui.html>`_ to this topic.

.. note::
    You can also apply this ai to gui items to toggle small/big pets or set specific skins small.

Goal
~~~~

* All pets should become small.

.. image:: ../_static/images/small-pets-sample.png

Step by Step
~~~~~~~~~~~~

As mentioned before a powerful editor like Notepad++ in this example is recommend.

.. warning::
 **1. Make a copy of your config.yml!** YAML, the configuration language this config.yml is using, takes spaces and tabs very serious, so be careful otherwise
 the config.yml cannot be parsed when executing the reload command.

2. Extend your existing default ais by the **entity-nbt** ai.

.. image:: ../_static/images/sample-small-pets-2.JPG

3. (Optional) Reset your pet via the **/petblocks reset** command

.. raw:: html

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <a class="btn" style="width:100%" href="../_static/samples/config-samplesmallpets.yml" download="config.yml"><i class="fa fa-download"></i>Download config.yml</a>
    <br/><br/><br/>

.. note::
 If the server console now displays an error then the config.yml cannot be parsed. In this case
 apply your backup you have made and try the steps again being extra carefully.
