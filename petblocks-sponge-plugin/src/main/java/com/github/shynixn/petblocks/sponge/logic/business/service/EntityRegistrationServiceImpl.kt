package com.github.shynixn.petblocks.sponge.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.EntityType
import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.business.service.EntityRegistrationService
import com.github.shynixn.petblocks.sponge.logic.business.nms.v1_12_R1.NMSPetVillager
import com.google.inject.Inject
import org.spongepowered.api.Sponge
import net.minecraft.entity.EntityList
import net.minecraft.util.registry.RegistryNamespaced



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
class EntityRegistrationServiceImpl : EntityRegistrationService {
    private val classes = HashMap<Class<*>, EntityType>()

    /**
     * Registers a new customEntity Clazz as the given [entityType].
     * Does nothing if the class is already registered.
     */
    override fun <C> register(customEntityClazz: C, entityType: EntityType) {
        Sponge.getRegistry().getAllOf(org.spongepowered.api.entity.EntityType::class.java).forEach { type ->
            if (type.id.split(":")[0].toLowerCase() == "petblocks") {
                return
            }
        }

        try {
            val entityTypeRegistryModuleClazz = Class.forName("org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule")
            val entityRegistrationMethod = entityTypeRegistryModuleClazz.getDeclaredMethod("registerEntityType",org.spongepowered.api.entity.EntityType::class.java)
            val spongeEntityTypeClazzConstructor = Class.forName("org.spongepowered.common.entity.SpongeEntityType")
                .getDeclaredConstructor(Int::class.java, String::class.java, String::class.java, Class::class.java, Translation::class.java)

            val registryInstance = entityTypeRegistryModuleClazz.getDeclaredMethod("getInstance").invoke(null)
            val rabbitSpongeEntityType = spongeEntityTypeClazzConstructor.newInstance(EntityType.RABBIT.entityId, EntityType.RABBIT.saveGame_11, "petblocks", CustomRabbit::class.java, null)
            val zombieSpongeEntityType = spongeEntityTypeClazzConstructor.newInstance(EntityType.VILLAGER.entityId, EntityType.VILLAGER.saveGame_11, "petblocks", NMSPetVillager::class.java, null)
            val armorstandSpongeEntityType = spongeEntityTypeClazzConstructor.newInstance(EntityType.ARMORSTAND.entityId, EntityType.ARMORSTAND.saveGame_11, "petblocks", CustomGroundArmorstand::class.java, null)

            entityRegistrationMethod.invoke(registryInstance, rabbitSpongeEntityType)
            entityRegistrationMethod.invoke(registryInstance, zombieSpongeEntityType)
            entityRegistrationMethod.invoke(registryInstance, armorstandSpongeEntityType)
        } catch (e: Exception) {
            logger.warn("Failed to register Entities in Sponge Internal registry. GriefPreventionFlags using petblocks may not work correctly.", e)
            return false
        }

        val minecraftKey = ResourceLocation("PetBlocks", customEntityType.getSaveGame_11())
        try {
            val materialRegistry = EntityList.REGISTRY
            materialRegistry.register(
                customEntityType.getEntityId(),
                minecraftKey,
                customEntityClazz as Class<out Entity>
            )
            this.registeredClasses.add(customEntityClazz)
        } catch (error: NoSuchFieldError) {
            // SpongeForge does not allow server only entities.
        }
    }

    /**
     * Clears all resources this service has allocated and reverts internal
     * nms changes.
     */
    override fun clearResources() {
    }
}