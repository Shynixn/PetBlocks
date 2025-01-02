# Bedrock 

PetBlocks supports crossplay with Bedrock clients (SmartPhone,Windows,Console, etc.) using [GeyserMC](https://geysermc.org/).

### Initial Setup

* Install [GeyserMC](https://geysermc.org/) on your server  or on your proxy server.
* Install PetBlocks on your Spigot/Paper based server
* Join your server with a BedRock client
* Observe that the pet will be displayed as a simple player_head because bedrock does not support custom player heads.
* Observe, that the pet will not be able to rotate.

### Fixing skin and rotations via GeyserMC (PatreonOnly)

Thanks to my **Patreon supporters**, who have funded this feature :heart: . 
They can download my [prepared zip file](https://www.patreon.com/Shynixn) to easily setup PetBlocks for Bedrock. 

If you are using the free version of PetBlocks, you can still configure PetBlocks for Bedrock, but it is more work for you. If you want
to save time, become a patreon member at [https://www.patreon.com/Shynixn](https://www.patreon.com/Shynixn).

=== "Spigot/Paper"

    * Download the ``PetBlocks-GeyserMC.zip`` file from [https://www.patreon.com/Shynixn](https://www.patreon.com/Shynixn).
    * Extract the ``PetBlocks-GeyserMC.zip`` contents into your ``plugins\Geyser-Spigot``.
    * Extract the ``custom-skulls_petblocks.yml`` into ``plugins\Geyser-Spigot\custom-skulls_petblocks.yml``
    * Extract the ``packs/PetBlocksPack.mcpack`` into ``plugins\Geyser-Spigot\packs\PetBlocksPack.mcpack``
    * Copy the ``player-profiles`` values from ``plugins\Geyser-Spigot\custom-skulls_petblocks.yml`` into the ``plugins\Geyser-Spigot\custom-skulls.yml`` file.

=== "Proxies (BungeeCord, Velocity, etc.)"

    * Download the ``PetBlocks-GeyserMC.zip`` file from [https://www.patreon.com/Shynixn](https://www.patreon.com/Shynixn).
    * Extract the ``PetBlocks-GeyserMC.zip`` contents into your ``plugins\Geyser-<Proxy>``.
    * Extract the ``custom-skulls_petblocks.yml`` into ``plugins\Geyser-<Proxy>\custom-skulls_petblocks.yml``
    * Extract the ``packs/PetBlocksPack.mcpack`` into ``plugins\Geyser-<Proxy>\packs\PetBlocksPack.mcpack``
    * Copy the ``player-profiles`` values from ``plugins\Geyser-<Proxy>\custom-skulls_petblocks.yml`` into the ``plugins\Geyser-<Proxy>\custom-skulls.yml`` file.

### Fixing Riding 

* Riding does not work very well with Bedrock right now. Disable riding entirely or request a ticket on Discord to implement riding for Bedrock.

### GUIs

* The GUI works for all clients without any changes. However, the GUI layout may be different for some clients (e.g. SmartPhone). Change the GUI layout to fit the needs of your playerbase.
