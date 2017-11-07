package com.github.shynixn.petblocks.bukkit.nms;

import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Information about Custom Entity Types.
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
public enum CustomEntityType {
    RABBIT(101, "RABBIT", "Rabbit", "rabbit", "EntityRabbit"),
    ZOMBIE(54, "ZOMBIE", "Zombie", "zombie", "EntityZombie");
    final Integer entityId;
    final String name;
    final String saveGame_18_19_10;
    final String saveGame_11;
    final Class<?> nmsClass;

    /**
     * Initializes a new CustomEntityRegistry entry.
     *
     * @param entityId          entityId
     * @param name              name
     * @param saveGame_18_19_10 game Id prior to 1.11
     * @param saveGame_11       game Id for 1.11 and later
     * @param nmsClassName      nmsClassName
     */
    CustomEntityType(int entityId, String name, String saveGame_18_19_10, String saveGame_11, String nmsClassName) {
        try {
            this.entityId = entityId;
            this.name = name;
            this.saveGame_18_19_10 = saveGame_18_19_10;
            this.saveGame_11 = saveGame_11;
            this.nmsClass = findClass("net.minecraft.server.VERSION." + nmsClassName);
        } catch (final Exception ex) {
            throw new RuntimeException("Failed to register entity " + name, ex);
        }
    }

    /**
     * Wrapper Interface for different registry implementations.
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
    public interface WrappedRegistry {
        /**
         * Registers a new customEntityClass with the given parameters. Overrides any existing registrations of this customEntityClazz.
         *
         * @param customEntityClazz customEntityClass
         * @param customEntityType  type
         * @throws Exception exception
         */
        void register(Class<?> customEntityClazz, CustomEntityType customEntityType) throws Exception;

        /**
         * Unregisters the customEntityClass with the given parameters. Throws an exception if already unregistered.
         *
         * @param customEntityClazz customEntityClass
         * @param customEntityType  type
         * @throws Exception exception
         */
        void unregister(Class<?> customEntityClazz, CustomEntityType customEntityType) throws Exception;

        /**
         * Returns if the customEntityClazz is still registered.
         *
         * @param customEntityClazz customEntityClass
         * @return isRegistered
         */
        boolean isRegistered(Class<?> customEntityClazz);
    }

    /**
     * Registry for minecraft 1.8.0 - 1.10.2.
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
    static class Registry10 implements WrappedRegistry {
        private Class<?> entityTypeClazz;
        private final Set<Class<?>> registeredClasses = new HashSet<>();
        private boolean notInitialized = true;

        private Map<String, Class<?>> entityTypeC;
        private Map<Class<?>, String> entityTypeD;
        private Map<Class<?>, Integer> entityTypeF;
        private Map<String, Integer> entityTypeG;

        /**
         * Registers a new customEntityClass with the given parameters. Overrides any existing registrations of this customEntityClazz.
         *
         * @param customEntityClazz customEntityClass
         * @param customEntityType  type
         * @throws Exception exception
         */
        @Override
        public void register(Class<?> customEntityClazz, CustomEntityType customEntityType) throws Exception {
            this.initialize();
            this.entityTypeC.put(customEntityType.saveGame_18_19_10, customEntityClazz);
            this.entityTypeD.put(customEntityClazz, customEntityType.saveGame_18_19_10);
            this.entityTypeF.put(customEntityClazz, customEntityType.entityId);
            this.entityTypeG.put(customEntityType.saveGame_18_19_10, customEntityType.entityId);
            this.registeredClasses.add(customEntityClazz);
        }

        /**
         * Unregisters the customEntityClass with the given parameters. Throws an exception if already unregistered.
         *
         * @param customEntityClazz customEntityClass
         * @param customEntityType  type
         * @throws Exception exception
         */
        @Override
        public void unregister(Class<?> customEntityClazz, CustomEntityType customEntityType) throws Exception {
            this.initialize();
            if (!this.isRegistered(customEntityClazz)) {
                throw new IllegalArgumentException("Entity is already unregisterd!");
            }
            this.entityTypeC.remove(customEntityType.saveGame_18_19_10);
            this.entityTypeD.remove(customEntityClazz);
            this.entityTypeF.remove(customEntityClazz);
            this.entityTypeG.remove(customEntityType.saveGame_18_19_10);
            this.registeredClasses.remove(customEntityClazz);
        }

        /**
         * Returns if the customEntityClazz is still registered.
         *
         * @param customEntityClazz customEntityClass
         * @return isRegistered
         */
        @Override
        public boolean isRegistered(Class<?> customEntityClazz) {
            return this.registeredClasses.contains(customEntityClazz);
        }

        private void initialize() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
            if (this.notInitialized) {
                this.entityTypeClazz = findClass("net.minecraft.server.VERSION.EntityTypes");
                this.entityTypeC = this.findMap(this.entityTypeClazz, "c");
                this.entityTypeD = this.findMap(this.entityTypeClazz, "d");
                this.entityTypeF = this.findMap(this.entityTypeClazz, "f");
                this.entityTypeG = this.findMap(this.entityTypeClazz, "g");
                this.notInitialized = false;
            }
        }

        private <T, G> Map<T, G> findMap(Class<?> clazz, String name) throws IllegalAccessException, NoSuchFieldException {
            final Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return (Map<T, G>) field.get(null);
        }
    }

    /**
     * Registry for minecraft 1.11.0 and above.
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
    static class Registry11 implements WrappedRegistry {
        private Class<?> entityTypeClazz;
        private final Set<Class<?>> registeredClasses = new HashSet<>();
        private boolean notInitialized = true;

        private Constructor minecraftKeyConstructor;
        private Field removeMaterialField;
        private Field materialField;
        private Method appendEntityMethod;

        /**
         * Registers a new customEntityClass with the given parameters. Overrides any existing registrations of this customEntityClazz.
         *
         * @param customEntityClazz customEntityClass
         * @param customEntityType  type
         * @throws Exception exception
         */
        @Override
        public void register(Class<?> customEntityClazz, CustomEntityType customEntityType) throws Exception {
            this.initialize();
            final Object minecraftKey = this.minecraftKeyConstructor.newInstance("PetBlocks", customEntityType.saveGame_11);
            final Object materialRegistry = this.materialField.get(null);
            this.appendEntityMethod.invoke(materialRegistry, customEntityType.entityId, minecraftKey, customEntityClazz);
            this.registeredClasses.add(customEntityClazz);
        }

        /**
         * Unregisters the customEntityClass with the given parameters. Throws an exception if already unregistered.
         *
         * @param customEntityClazz customEntityClass
         * @param customEntityType  type
         * @throws Exception exception
         */
        @Override
        public void unregister(Class<?> customEntityClazz, CustomEntityType customEntityType) throws Exception {
            this.initialize();
            if (!this.isRegistered(customEntityClazz)) {
                throw new IllegalArgumentException("Entity is already unregisterd!");
            }
            final Object minecraftKey = this.minecraftKeyConstructor.newInstance("PetBlocks", customEntityType.saveGame_11);
            final Object materialRegistry = this.materialField.get(null);
            ((Map<?, ?>) this.removeMaterialField.get(materialRegistry)).remove(minecraftKey);
            this.registeredClasses.remove(customEntityClazz);
        }

        /**
         * Returns if the customEntityClazz is still registered.
         *
         * @param customEntityClazz customEntityClass
         * @return isRegistered
         */
        @Override
        public boolean isRegistered(Class<?> customEntityClazz) {
            return this.registeredClasses.contains(customEntityClazz);
        }

        private void initialize() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
            if (this.notInitialized) {
                this.entityTypeClazz = findClass("net.minecraft.server.VERSION.EntityTypes");
                this.removeMaterialField = findClass("net.minecraft.server.VERSION.RegistrySimple").getDeclaredField("c");
                this.removeMaterialField.setAccessible(true);
                this.minecraftKeyConstructor = findClass("net.minecraft.server.VERSION.MinecraftKey").getDeclaredConstructor(String.class, String.class);
                this.materialField = this.entityTypeClazz.getDeclaredField("b");
                this.appendEntityMethod = findClass("net.minecraft.server.VERSION.RegistryMaterials").getDeclaredMethod("a", int.class, Object.class, Object.class);
                this.notInitialized = false;
            }
        }
    }

    /**
     * Finds a class regarding of the server Version.
     *
     * @param name name
     * @return clazz
     * @throws ClassNotFoundException exception
     */
    private static Class<?> findClass(String name) throws ClassNotFoundException {
        return Class.forName(name.replace("VERSION", Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3]));
    }
}
