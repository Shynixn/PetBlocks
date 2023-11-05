# FAQ

### Can I use PetBlocks to attack enemies (e.g. Zombies) or other players?

Pets in PetBlocks can be programmed in the pet.yml file to perform such behaviors. You would need a PlaceHolderApi placeholder which contains the location of the entity you want to attack (will be added in a future update to PetBlocks, but there may already be plugins out there, which provide that). Then, create a new loop  
where the pet moves to to the entity using the ``/petblocks moveTo <x> <y> <z>`` command. Once the pet is near the entity, make it jump using ``/petblocks velocity <x> <y> <z>`` and execute command to damage the entity ``/damage @e[type=zombie,limit=1,sort=nearest] 20``

### How can I use PetBlocks for an older Minecraft version?

Maintaining backwards compatibility is very hard and actively discouraged by spigot. I do my best to add more and more workarounds.
However, it has become so much work, that you can only download them by becoming a Patreon https://www.patreon.com/Shynixn.

