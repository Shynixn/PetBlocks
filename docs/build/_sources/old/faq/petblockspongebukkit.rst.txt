Are there any differences between Sponge and Bukkit PetBlocks?
==============================================================

There are only little differences between the features of Sponge-PetBlocks and
Bukkit-PetBlocks, however you need to understand that the implementation is completely different.

This means even when a feature completely works when being used on a Bukkit server, there can be some
errors when using the Sponge version of it.

Things to point out:

* **config.yml** and **PetBlocks.db** file are completely interchangeable which means you can simply copy these files from the bukkit to the sponge server ✔
* Cross server pets are possible between Bukkit and Sponge server as the MySQL database is also interchangeable ✔
* There is no Sponge version of the Head-Database plugin available, so this feature is disabled in Sponge-PetBlocks ✘
* There is no Sponge version of the WorldGuard plugin available, so region restricting is disabled in Sponge-PetBlocks ✘
* PetBlocks-Sponge is not available for as many Minecraft versions as PetBlocks-Bukkit ✘
* All other features are available ✔