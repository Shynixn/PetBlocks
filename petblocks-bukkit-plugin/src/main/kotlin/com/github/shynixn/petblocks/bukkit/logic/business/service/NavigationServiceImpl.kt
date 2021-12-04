package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.NavigationService
import com.google.inject.Inject
import org.bukkit.Location
import org.bukkit.entity.LivingEntity

class NavigationServiceImpl @Inject constructor(private val version: Version) : NavigationService {
    private val getHandleMethod =
        findClazz("org.bukkit.craftbukkit.VERSION.entity.CraftLivingEntity").getDeclaredMethod("getHandle")!!
    private val navigationAbstractMethod =
        try {
            findClazz("net.minecraft.world.entity.EntityInsentient").getDeclaredMethod("D")
        } catch (e1: Exception) {
            try {
                findClazz("net.minecraft.world.entity.EntityInsentient").getDeclaredMethod("getNavigation")
            } catch (e: Exception) {
                findClazz("net.minecraft.server.VERSION.EntityInsentient").getDeclaredMethod("getNavigation")
            }
        }

    private val goToEntityNavigationMethod =
        try {
            findClazz("net.minecraft.world.entity.ai.navigation.NavigationAbstract").getDeclaredMethod(
                "a",
                Double::class.java,
                Double::class.java,
                Double::class.java,
                Double::class.java
            )
        } catch (e: Exception) {
            findClazz("net.minecraft.server.VERSION.NavigationAbstract").getDeclaredMethod(
                "a",
                Double::class.java,
                Double::class.java,
                Double::class.java,
                Double::class.java
            )
        }

    /**
     * Lets the given [petProxy] navigate to the given [location].
     */
    override fun <L> navigateToLocation(petProxy: PetProxy, location: L, speed: Double) {
        if (location !is Location) {
            throw IllegalArgumentException("Location has to be a BukkitLocation!")
        }

        if (!petProxy.getHitBoxLivingEntity<LivingEntity>().isPresent) {
            return
        }

        val nmsEntity = getHandleMethod.invoke(petProxy.getHitBoxLivingEntity<LivingEntity>().get())
        val navigation = navigationAbstractMethod.invoke(nmsEntity)
        goToEntityNavigationMethod.invoke(navigation, location.x, location.y, location.z, speed)
    }

    /**
     * Clears the current navigation target from the petProxy.
     */
    override fun clearNavigation(petProxy: PetProxy) {
        if (!petProxy.getHitBoxLivingEntity<LivingEntity>().isPresent) {
            return
        }

        val nmsEntity = getHandleMethod.invoke(petProxy.getHitBoxLivingEntity<LivingEntity>().get())
        val navigation = navigationAbstractMethod.invoke(nmsEntity)

        when {
            version.isVersionSameOrGreaterThan(Version.VERSION_1_17_R1) -> findClazz("net.minecraft.world.entity.ai.navigation.NavigationAbstract").getDeclaredMethod(
                "getNodeEvaluator"
            ).invoke(navigation)
            version.isVersionSameOrGreaterThan(Version.VERSION_1_13_R1) -> findClazz("net.minecraft.server.VERSION.NavigationAbstract").getDeclaredMethod(
                "q"
            ).invoke(navigation)
            version.isVersionSameOrGreaterThan(Version.VERSION_1_12_R1) -> findClazz("net.minecraft.server.VERSION.NavigationAbstract").getDeclaredMethod(
                "p"
            ).invoke(navigation)
            version.isVersionSameOrGreaterThan(Version.VERSION_1_9_R1) -> findClazz("net.minecraft.server.VERSION.NavigationAbstract").getDeclaredMethod(
                "o"
            ).invoke(navigation)
            version.isVersionSameOrGreaterThan(Version.VERSION_1_8_R1) -> findClazz("net.minecraft.server.VERSION.NavigationAbstract").getDeclaredMethod(
                "n"
            ).invoke(navigation)
            else -> throw IllegalArgumentException("This version is not supported!")
        }
    }

    /**
     * Finds a version compatible class.
     */
    private fun findClazz(name: String): Class<*> {
        return Class.forName(name.replace("VERSION", version.bukkitId))
    }
}
