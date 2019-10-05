mkdir sponge
cd sponge
wget "https://files.minecraftforge.net/maven/net/minecraftforge/forge/1.12.2-14.23.5.2825/forge-1.12.2-14.23.5.2825-installer.jar"
java -jar forge-1.12.2-14.23.5.2825-installer.jar --installServer
echo "eula=true" > eula.txt
echo '[
  {
    "uuid": "dd6f3d9b-86e6-497f-b22a-2fc99e046e65",
    "name": "Shynixn",
    "level": 4,
    "bypassesPlayerLimit": false
  }
]' > ops.json
mkdir mods
cd mods
wget "https://repo.spongepowered.org/maven/org/spongepowered/spongeforge/1.12.2-2825-7.1.6/spongeforge-1.12.2-2825-7.1.6.jar"
cd ..
cd ..
mv petblocks-sponge-plugin* sponge/mods
mkdir spigot
cd spigot
wget "https://hub.spigotmc.org/jenkins/job/BuildTools/lastStableBuild/artifact/target/BuildTools.jar"
java -Xmx1024M -jar BuildTools.jar --rev "1.8.8"
java -Xmx1024M -jar BuildTools.jar --rev "1.9.4"
java -Xmx1024M -jar BuildTools.jar --rev "1.10.0"
java -Xmx1024M -jar BuildTools.jar --rev "1.11.0"
java -Xmx1024M -jar BuildTools.jar --rev "1.12.2"
java -Xmx1024M -jar BuildTools.jar --rev "1.13.2"
java -Xmx1024M -jar BuildTools.jar --rev "1.14.4"
echo "eula=true" > eula.txt
echo '[
  {
    "uuid": "dd6f3d9b-86e6-497f-b22a-2fc99e046e65",
    "name": "Shynixn",
    "level": 4,
    "bypassesPlayerLimit": false
  }
]' > ops.json
mkdir plugins
cd ..
mv petblocks-bukkit-plugin* spigot/plugins