package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.annotation.Inject
import com.github.shynixn.petblocks.api.business.enumeration.EntityType
import com.github.shynixn.petblocks.api.business.proxy.NMSPetProxy
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.business.service.EntityRegistrationService
import com.github.shynixn.petblocks.api.business.service.EntityService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.bukkit.logic.business.nms.VersionSupport
import org.bukkit.ChatColor
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

/**
 * Created by Shynixn 2018.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2018 by Shynixn
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
class EntityServiceImpl @Inject constructor(private val configurationService: ConfigurationService, private val proxyService: ProxyService, private val entityRegistrationService: EntityRegistrationService) : EntityService {
    private val version = VersionSupport.getServerVersion()
    private var registered = false

    /**
     * Spawns a new unManaged petProxy.
     */
    override fun <L> spawnPetProxy(location: L, petMeta: PetMeta): PetProxy {
        this.registerEntitiesOnServer()

        val playerProxy = proxyService.findPlayerProxyObjectFromUUID(petMeta.playerMeta.uuid)
        val designClazz = Class.forName("com.github.shynixn.petblocks.bukkit.logic.business.nms.VERSION.PetDesign".replace("VERSION", version.versionText))

        return (designClazz.getDeclaredConstructor(Player::class.java, PetMeta::class.java)
                .newInstance(playerProxy!!.handle, petMeta) as NMSPetProxy).proxy
    }

    /**
     * Registers entities on the server when not already registered.
     * Returns true if registered. Returns false when not registered.
     */
    override fun registerEntitiesOnServer(): Boolean {
        if (registered) {
            return true
        }

        val rabbitClazz = Class.forName("com.github.shynixn.petblocks.bukkit.logic.business.nms.VERSION.PetRabbitHitBox".replace("VERSION", version.versionText))
        entityRegistrationService.register(rabbitClazz, EntityType.RABBIT)

        registered = true

        return true
    }

    /**
     * Kills the nearest entity of the [player].
     */
    override fun <P> killNearestEntity(player: P) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        var distance = 100.0
        var nearest: Entity? = null

        for (entity in player.location.chunk.entities) {
            if (entity !is Player && player.location.distance(entity.location) < distance) {
                distance = player.location.distance(entity.location)
                nearest = entity
            }
        }

        if (nearest != null) {
            nearest.remove()

            val prefix = configurationService.findValue<String>("messages.prefix")
            player.sendMessage(prefix + "" + ChatColor.GREEN + "You removed entity " + nearest.type + '.'.toString())
        }
    }
}