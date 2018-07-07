package com.github.shynixn.petblocks.api;

import com.github.shynixn.petblocks.api.business.controller.PetBlockController;
import com.github.shynixn.petblocks.api.business.service.GUIService;
import com.github.shynixn.petblocks.api.persistence.controller.PetMetaController;

import java.util.Optional;

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
    private static GUIService guiService;
    public static final PetBlocksApi INSTANCE = new PetBlocksApi();

    /**
     * Initializes the api.
     */
    private static void initialize(PetMetaController petMetaController, PetBlockController petBlockController, GUIService guiService) {
        PetBlocksApi.metaController = petMetaController;
        PetBlocksApi.petBlockController = petBlockController;
        PetBlocksApi.guiService = guiService;
    }

    /**
     * Gets a business logic service by resolving the given class.
     *
     * @param service service interface.
     * @param <S>     type of Service.
     * @return optional S.
     */
    public <S> Optional<S> resolve(Class<S> service) {
        if (service == GUIService.class) {
            return Optional.of((S) guiService);
        }

        return Optional.empty();
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
