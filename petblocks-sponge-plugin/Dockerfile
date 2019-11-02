FROM openjdk:8

WORKDIR /minecraft

COPY ["petblocks-tools", "petblocks-sponge-plugin/build/libs", "./"]

RUN wget "https://files.minecraftforge.net/maven/net/minecraftforge/forge/1.12.2-14.23.5.2825/forge-1.12.2-14.23.5.2825-installer.jar" && \
    java -jar forge-1.12.2-14.23.5.2825-installer.jar --installServer && \
    echo "eula=true" > eula.txt && \
    mkdir mods && \
    cd mods && \
    wget "https://repo.spongepowered.org/maven/org/spongepowered/spongeforge/1.12.2-2825-7.1.6/spongeforge-1.12.2-2825-7.1.6.jar" && \
    cd ..

CMD rm -r config/petblocks -f && \
    rm -r world -f && \
    rm -r world_nether -f && \
    rm -r world_the_end -f && \
    cp -r world-1.8/world . && \
    java -Xmx1024M -Xmx1024M -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar forge-1.12.2-14.23.5.2825-universal.jar -o false