rm -r spigot/plugins/PetBlocks
rm -r spigot/world
rm -r spigot/world_nether
rm -r spigot/world_the_end
rm -r sponge/config/petblocks
rm -r sponge/world
rm -r sponge/world_nether
rm -r sponge/world_the_end
server="$1"
version=$(echo "$2" | tr -d '.')
if [ "$version" -le 113 ]; then
   cp -r world-1.8/world spigot
   cp -r world-1.8/world sponge
else
   cp -r world-1.14/world spigot
   cp -r world-1.14/world sponge
fi;

if [ "$server" = "sponge" ]; then
   cd sponge
   ls
   java -Xmx1024M -Xmx1024M -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar forge-1.12.2-14.23.5.2825-universal.jar -o false
else
   cd spigot
   java -Xmx1024M -Xmx1024M -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar spigot-"$2".jar -o false
fi;
