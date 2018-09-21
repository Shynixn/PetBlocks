package com.github.shynixn.petblocks.sponge.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.EntityType
import com.github.shynixn.petblocks.api.business.service.EntityService
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.sponge.nms.v1_12_R1.CustomGroundArmorstand
import com.github.shynixn.petblocks.sponge.nms.v1_12_R1.CustomRabbit
import com.github.shynixn.petblocks.sponge.nms.v1_12_R1.CustomZombie
import com.google.inject.Inject
import org.spongepowered.api.Sponge
import org.spongepowered.api.text.translation.Translation

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
class EntityServiceImpl @Inject constructor(private val logger: LoggingService) : EntityService {
    /**
     * Registers entities on the server when not already registered.
     * Returns true if registered. Returns false when not registered.
     */
    override fun registerEntitiesOnServer(): Boolean {
        Sponge.getRegistry().getAllOf(org.spongepowered.api.entity.EntityType::class.java).forEach { type ->
            if (type.id.split(":")[0].toLowerCase() == "petblocks") {
                return true
            }
        }

        try {
            val entityTypeRegistryModuleClazz = Class.forName("org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule")
            val entityRegistrationMethod = entityTypeRegistryModuleClazz.getDeclaredMethod("registerEntityType",org.spongepowered.api.entity.EntityType::class.java)
            val spongeEntityTypeClazzConstructor = Class.forName("org.spongepowered.common.entity.SpongeEntityType")
                    .getDeclaredConstructor(Int::class.java, String::class.java, String::class.java, Class::class.java, Translation::class.java)

            val registryInstance = entityTypeRegistryModuleClazz.getDeclaredMethod("getInstance").invoke(null)
            val rabbitSpongeEntityType = spongeEntityTypeClazzConstructor.newInstance(EntityType.RABBIT.entityId, EntityType.RABBIT.saveGame_11, "petblocks", CustomRabbit::class.java, null)
            val zombieSpongeEntityType = spongeEntityTypeClazzConstructor.newInstance(EntityType.ZOMBIE.entityId, EntityType.ZOMBIE.saveGame_11, "petblocks", CustomZombie::class.java, null)
            val armorstandSpongeEntityType = spongeEntityTypeClazzConstructor.newInstance(EntityType.ARMORSTAND.entityId, EntityType.ARMORSTAND.saveGame_11, "petblocks", CustomGroundArmorstand::class.java, null)

            entityRegistrationMethod.invoke(registryInstance, rabbitSpongeEntityType)
            entityRegistrationMethod.invoke(registryInstance, zombieSpongeEntityType)
            entityRegistrationMethod.invoke(registryInstance, armorstandSpongeEntityType)
        } catch (e: Exception) {
            logger.warn("Failed to register Entities in Sponge Internal registry. GriefPreventionFlags using petblocks may not work correctly.", e)
            return false
        }

        return true
    }
}