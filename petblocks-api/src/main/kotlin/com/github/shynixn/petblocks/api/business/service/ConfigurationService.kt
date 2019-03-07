package com.github.shynixn.petblocks.api.business.service

import com.github.shynixn.petblocks.api.persistence.entity.AIBase
import com.github.shynixn.petblocks.api.persistence.entity.GuiItem
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import java.io.InputStream
import java.nio.file.Path

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
interface ConfigurationService {
    /**
     * Gets the [Path] to the configuration folder.
     */
    val dataFolder: Path

    /**
     * Opens a new inputStream to the given [resource].
     */
    fun openResourceInputStream(resource: String) : InputStream

    /**
     * Tries to load the config value from the given [path].
     * Throws a [IllegalArgumentException] if the path could not be correctly
     * loaded.
     * @param C the type of the returned value.
     */
    fun <C> findValue(path: String): C

    /**
     * Checks if the given path is containing in the config.yml.
     */
    fun contains(path: String): Boolean

    /**
     * Tries to return a [GuiItem] matching the displayName and the lore of the given [item].
     * Can be called asynchronly. Uses the [path] parameter for faster fetching.
     * @param I the type of the itemstack.
     */
    fun <I> findClickedGUIItem(path: String, item: I): GuiItem?

    /**
     * Tries to return a list of [GuiItem] matching the given path from the config.
     * Can be called asynchronly.
     */
    fun findGUIItemCollection(path: String): List<GuiItem>?

    /**
     * Generates the default pet meta.
     */
    fun generateDefaultPetMeta(uuid: String, name: String): PetMeta

    /**
     * Clears cached resources and refreshes the used configuration.
     */
    fun refresh()
}