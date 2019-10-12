FROM openjdk:8

ENV MINECRAFT_VERSION 1.14.4
ENV SERVER_FRAMEWORK "spigot"

WORKDIR minecraft
COPY ["petblocks-tools", "petblocks-bukkit-plugin/build/libs", "petblocks-sponge-plugin/build/libs", "./"]
RUN ["sh","-c","sed -i 's/\r//g' docker-install.sh && sed -i 's/\r//g' docker-start.sh && ./docker-install.sh"]
CMD ["sh","-c","./docker-start.sh ${SERVER_FRAMEWORK} ${MINECRAFT_VERSION} && tail -f /dev/null"]

EXPOSE 25565