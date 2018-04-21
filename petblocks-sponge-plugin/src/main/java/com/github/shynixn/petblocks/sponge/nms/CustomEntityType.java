package com.github.shynixn.petblocks.sponge.nms;


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
        } catch (final Exception ex) {
            throw new RuntimeException("Failed to register entity " + name, ex);
        }
    }

    public Integer getEntityId() {
        return this.entityId;
    }

    public String getName() {
        return this.name;
    }

    public String getSaveGame_18_19_10() {
        return this.saveGame_18_19_10;
    }

    public String getSaveGame_11() {
        return this.saveGame_11;
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
}
