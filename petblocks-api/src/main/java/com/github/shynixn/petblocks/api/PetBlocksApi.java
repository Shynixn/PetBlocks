package com.github.shynixn.petblocks.api;

import com.github.shynixn.petblocks.api.business.controller.PetBlockController;
import com.github.shynixn.petblocks.api.business.proxy.PluginProxy;
import com.github.shynixn.petblocks.api.persistence.controller.PetMetaController;

/**
 * PetBlocksApi for accessing and modifying PetBlocks and PetMeta.
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
public class PetBlocksApi {

    private static PetMetaController metaController;
    private static PetBlockController petBlockController;
    public static final PetBlocksApi INSTANCE = new PetBlocksApi();
    private static PluginProxy plugin;

    /**
     * Initializes the api.
     */
    private static void initialize(PetMetaController petMetaController, PetBlockController petBlockController, PluginProxy plugin) {
        PetBlocksApi.metaController = petMetaController;
        PetBlocksApi.petBlockController = petBlockController;
        PetBlocksApi.plugin = plugin;
    }

    /**
     * Gets a business logic service by resolving the given class.
     *
     * @param service service interface.
     * @param <S>     type of Service.
     * @return optional S.
     */
    public <S> S resolve(Class<S> service) {
        return plugin.resolve(service);
    }

    /**
     * Creates a new entity from the given class.
     * Throws a IllegalArgumentException if not found.
     *
     * @param entity entityClazz
     * @param <E>    type
     * @return entity.
     */
    public <E> E create(Class<E> entity) {
        return plugin.create(entity);
    }

    /**
     * Returns the default meta controller.
     *
     * @param <T> type of petMetaController.
     * @return metaController
     */
    public static <T> PetMetaController<T> getDefaultPetMetaController() {
        return metaController;
    }

    /**
     * Returns the petBlock controller.
     *
     * @param <T> type of petblockController.
     * @return petBlockController
     */
    public static <T> PetBlockController<T> getDefaultPetBlockController() {
        return petBlockController;
    }
}
