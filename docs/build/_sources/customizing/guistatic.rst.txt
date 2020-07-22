Static GUI Items
================

The first category you can find in the config.yml are static gui items.
This is simply a section for the **empty-slot** item which gets placed everywhere in the GUI where the
slot is empty.

.. image:: ../_static/images/gui-emptyslot.png


Configuring in the config.yml
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Each of the category has got its own page in the GUI.

**config.yml**
::
    ############################

    # Static GUI settings.

    # These settings are global settings for the gui which do not get dynamically parsed.

    # Adding new items is not possible for this settings and you can only change the parameters of the existing declared ones.

    ############################

    static-gui:
      empty-slot:
        icon:
          id: 160
          damage: 15
          skin: 'none'
          name: 'none'
          lore:
          - 'none'

.. note:: **Every gui item supports scripts**, even static gui items. However, it is not recommend to add a script to any empty slot.

* Icon: Specifies the rendered icon in the GUI.