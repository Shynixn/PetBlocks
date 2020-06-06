Example: Integrating PVE and buff effects
=========================================

This example integrates the health ai to the buff effects to make buff effects more fair during fights.

Make sure you have understood the `introduction <gui.html>`_ to this topic.

Goal
~~~~

* When a player activates the speed buff effect, the pet should also become vulnerable to attacks which means that other players and entities can attack it. If the pet gets killed it will not be able to supply the owner with effects until the respawn cooldown reaches 0.

.. image:: ../_static/images/buff-effect-visible.png

Step by Step
~~~~~~~~~~~~

As mentioned before a powerful editor like Notepad++ in this example is recommend.

.. warning::
 **1. Make a copy of your config.yml!** YAML, the configuration language this config.yml is using, takes spaces and tabs very serious, so be careful otherwise
 the config.yml cannot be parsed when executing the reload command.

2. Create a 2 new gui items called speed-one-enabled and speed-one-disabled at the corresponding place.

3. Add the buff-effect ai.

4. Add the health ai with a high respawn delay to punish players not to take care of their pet.

5. (Optional) Try it out ingame by punching your poor pet.

.. image:: ../_static/images/pvp-buffeffect-config.png

.. raw:: html

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <a class="btn" style="width:100%" href="../_static/samples/config-pvebuffeffects.yml" download="config.yml"><i class="fa fa-download"></i>Download config.yml</a>
    <br/><br/><br/>

.. note::
 If the server console now displays an error then the config.yml cannot be parsed. In this case
 apply your backup you have made and try the steps again being extra carefully.
