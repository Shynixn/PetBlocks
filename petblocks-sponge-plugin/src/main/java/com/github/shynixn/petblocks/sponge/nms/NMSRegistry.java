package com.github.shynixn.petblocks.sponge.nms;

import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.core.logic.business.helper.ReflectionUtils;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * Created by Shynixn 2017.
 * <p>
 * Version 1.1
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2017 by Shynixn
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
public class NMSRegistry {

    private static CustomEntityType.WrappedRegistry wrappedRegistry;
    private static final Class<?> rabbitClazz;
    private static final Class<?> zombieClazz;

    static {
        try {
            wrappedRegistry = (CustomEntityType.WrappedRegistry) findClassFromVersion("com.github.shynixn.petblocks.sponge.nms.VERSION.CustomEntityRegistry").newInstance();
            rabbitClazz = findClassFromVersion("com.github.shynixn.petblocks.sponge.nms.VERSION.CustomRabbit");
            zombieClazz = null;
        } catch (final ClassNotFoundException | IllegalAccessException | InstantiationException e) {
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
    public static PetBlock<Player, Transform<World>> createPetBlock(Location<World> location, PetMeta meta) {
        try {
            if (!wrappedRegistry.isRegistered(rabbitClazz)) {
                wrappedRegistry.register(rabbitClazz, CustomEntityType.RABBIT);
                // wrappedRegistry.register(zombieClazz, CustomEntityType.ZOMBIE);
            }
            return (PetBlock) ReflectionUtils.invokeConstructor(findClassFromVersion("com.github.shynixn.petblocks.sponge.nms.VERSION.CustomGroundArmorstand")
                    , new Class[]{location.getClass(), PetMeta.class}, new Object[]{location, meta});
        } catch (final Exception e) {
            throw new RuntimeException("Cannot create petblock.", e);
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
}
