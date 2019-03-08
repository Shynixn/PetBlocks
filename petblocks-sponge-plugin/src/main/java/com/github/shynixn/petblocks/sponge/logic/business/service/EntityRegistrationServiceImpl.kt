@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.sponge.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.EntityType
import com.github.shynixn.petblocks.api.business.service.EntityRegistrationService
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.google.inject.Inject
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityList
import net.minecraft.util.ResourceLocation
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
class EntityRegistrationServiceImpl @Inject constructor(private val loggingService: LoggingService) : EntityRegistrationService {
    private val classes = HashMap<Class<*>, EntityType>()

    /**
     * Registers a new customEntity Clazz as the given [entityType].
     * Does nothing if the class is already registered.
     */
    override fun <C> register(customEntityClazz: C, entityType: EntityType) {
        if (customEntityClazz !is Class<*>) {
            throw IllegalArgumentException("CustomEntityClass has to be a Class!")
        }

        if (classes.containsKey(customEntityClazz)) {
            return
        }

        try {
            val entityTypeRegistryModuleClazz = Class.forName("org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule")
            val entityRegistrationMethod = entityTypeRegistryModuleClazz.getDeclaredMethod("registerEntityType", org.spongepowered.api.entity.EntityType::class.java)
            val spongeEntityTypeClazzConstructor = Class.forName("org.spongepowered.common.entity.SpongeEntityType")
                .getDeclaredConstructor(Int::class.java, String::class.java, String::class.java, Class::class.java, Translation::class.java)

            val registryInstance = entityTypeRegistryModuleClazz.getDeclaredMethod("getInstance").invoke(null)
            val customEntityType = spongeEntityTypeClazzConstructor.newInstance(entityType.entityId, entityType.saveGame_11, "petblocks", customEntityClazz, null)

            entityRegistrationMethod.invoke(registryInstance, customEntityType)
        } catch (e: Exception) {
            loggingService.warn("Failed to register Entities in Sponge Internal registry. GriefPreventionFlags using petblocks may not work correctly.", e)
        }

        try {
            val minecraftKey = ResourceLocation("petblocks", entityType.saveGame_11)
            val materialRegistry = EntityList.REGISTRY
            materialRegistry.register(entityType.entityId, minecraftKey, customEntityClazz as Class<out Entity>)
        } catch (error: NoSuchFieldError) {
            // SpongeForge does not allow registering custom server entities.
        }

        classes[customEntityClazz] = entityType
    }

    /**
     * Clears all resources this service has allocated and reverts internal
     * nms changes.
     */
    override fun clearResources() {
        try {
            val materialRegistry = EntityList.REGISTRY

            classes.forEach { _, entityType ->
                val minecraftKey = ResourceLocation("PetBlocks", entityType.saveGame_11)
                materialRegistry.registryObjects.remove(minecraftKey)
            }
        } catch (error: NoSuchFieldError) {
            // @Forge is responsible for removing the key if this happens.
        }

        classes.clear()
    }
}