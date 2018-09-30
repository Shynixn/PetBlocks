package com.github.shynixn.petblocks.bukkit.nms;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.business.enumeration.PluginDependency;
import com.github.shynixn.petblocks.api.business.service.DependencyService;
import com.github.shynixn.petblocks.api.business.service.DependencyWorldGuardService;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.core.logic.compatibility.ReflectionUtils;
import org.bukkit.Location;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

public final class NMSRegistry {

    private static CustomEntityType.WrappedRegistry wrappedRegistry;
    private static final Class<?> rabbitClazz;
    private static final Class<?> zombieClazz;

    static {
        try {
            rabbitClazz = findClassFromVersion("com.github.shynixn.petblocks.bukkit.nms.VERSION.CustomRabbit");
            zombieClazz = findClassFromVersion("com.github.shynixn.petblocks.bukkit.nms.VERSION.CustomZombie");
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private NMSRegistry() {
        super();
    }

    /**
     * Creates a new petblock from the given location and meta.
     *
     * @param location location
     * @param meta     meta
     * @return petblock
     */
    public static PetBlock createPetBlock(Location location, PetMeta meta) {
        try {
            if (wrappedRegistry == null) {
                if (VersionSupport.getServerVersion().isVersionLowerThan(VersionSupport.VERSION_1_11_R1)) {
                    wrappedRegistry = new CustomEntityType.Registry10();
                } else if (VersionSupport.getServerVersion().isVersionLowerThan(VersionSupport.VERSION_1_13_R1)) {
                    wrappedRegistry = new CustomEntityType.Registry11();
                } else if (VersionSupport.getServerVersion() == VersionSupport.VERSION_1_13_R1) {
                    wrappedRegistry = new CustomEntityType.Registry13();
                } else {
                    wrappedRegistry = new CustomEntityType.Registry131();
                }
            }
            if (!wrappedRegistry.isRegistered(rabbitClazz)) {
                wrappedRegistry.register(rabbitClazz, CustomEntityType.RABBIT);
                wrappedRegistry.register(zombieClazz, CustomEntityType.ZOMBIE);
            }
            return (PetBlock) ReflectionUtils.invokeConstructor(findClassFromVersion("com.github.shynixn.petblocks.bukkit.nms.VERSION.CustomGroundArmorstand")
                    , new Class[]{location.getClass(), PetMeta.class}, new Object[]{location, meta});
        } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException | ClassNotFoundException e) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Cannot create petblock.", e);
            return null;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Unregisters all custom entities.
     *
     * @throws Exception exception
     */
    public static void unregisterCustomEntities() throws Exception {
        if (wrappedRegistry != null) {
            wrappedRegistry.unregister(rabbitClazz, CustomEntityType.RABBIT);
            wrappedRegistry.unregister(zombieClazz, CustomEntityType.ZOMBIE);
            wrappedRegistry = null;
        }
    }

    /**
     * Returns the class managed by version
     *
     * @param path path
     * @return class
     * @throws ClassNotFoundException exception
     */
    private static Class<?> findClassFromVersion(String path) throws ClassNotFoundException {
        return Class.forName(path.replace("VERSION", VersionSupport.getServerVersion().getVersionText()));
    }

    /**
     * Compatibility method.
     *
     * @param location location
     */
    @Deprecated
    public static void accessWorldGuardSpawn(Location location) {
        final DependencyService dependencyService = PetBlocksApi.INSTANCE.resolve(DependencyService.class);

        if (!dependencyService.isInstalled(PluginDependency.WORLDGUARD)) {
            return;
        }

        final DependencyWorldGuardService dependencyWorldGuardService = PetBlocksApi.INSTANCE.resolve(DependencyWorldGuardService.class);

        try {
            dependencyWorldGuardService.prepareSpawningRegion(location);
        } catch (final Exception e) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Failed to handle region spawning.", e);
        }
    }

    /**
     * Compatibility method.
     *
     * @param location location
     */
    @Deprecated
    public static void rollbackWorldGuardSpawn(Location location) {
        final DependencyService dependencyService = PetBlocksApi.INSTANCE.resolve(DependencyService.class);

        if (!dependencyService.isInstalled(PluginDependency.WORLDGUARD)) {
            return;
        }

        final DependencyWorldGuardService dependencyWorldGuardService = PetBlocksApi.INSTANCE.resolve(DependencyWorldGuardService.class);

        try {
            dependencyWorldGuardService.resetSpawningRegion(location);
        } catch (final Exception e) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Failed to handle region spawning.", e);
        }
    }
}
