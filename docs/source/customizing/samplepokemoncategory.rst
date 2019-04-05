Example: Adding a new pokemon category
======================================

This example adds a new category to the wardrobe with 3 pokemon skins.

Make sure you have understood the `introduction <gui.html>`_ to this topic.

Goal
~~~~

* The player should be able to select pokemon specific skins from a new category in gui.

.. image:: ../_static/images/pokemon-category-1.png

.. image:: ../_static/images/pokemon-category-2.png

Step by Step
~~~~~~~~~~~~

As mentioned before a powerful editor like Notepad++ in this example is recommend.

.. warning::
 **1. Make a copy of your config.yml!** YAML, the configuration language this config.yml is using, takes spaces and tabs very serious, so be careful otherwise
 the config.yml cannot be parsed when executing the reload command.

2. Let's add a new category and name it pokemon-skins.

3. Also, let's copy the default navigation items to our new category.

.. image:: ../_static/images/sample-pokemon-1.JPG

4. Now add our skins which we have got from minecraft-heads.com.

.. image:: ../_static/images/sample-pokemon-3.JPG

5. Now we need to connect our new page to the wardrobe page.

.. image:: ../_static/images/sample-pokemon-2.JPG

6. Execute **/petblockreload** and open the gui to use the new category.

.. raw:: html

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <a class="btn" style="width:100%" href="../_static/samples/config-samplepokemoncategory.yml" download="config.yml"><i class="fa fa-download"></i>Download config.yml</a>
    <br/><br/><br/>

.. note::
 If the server console now displays an error then the config.yml cannot be parsed. In this case
 apply your backup you have made and try the steps again being extra carefully.