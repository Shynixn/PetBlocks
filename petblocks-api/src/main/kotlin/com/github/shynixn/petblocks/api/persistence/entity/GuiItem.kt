package com.github.shynixn.petblocks.api.persistence.entity

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
interface GuiItem {
    /**
     * Gets a nullable script
     * which can be used for defining the action of the gui item.
     */
    var script: String?

    /**
     * Icon of the gui item.
     */
    val icon: GuiIcon

    /**
     * Should this icon always be hidden?
     */
    var hidden: Boolean

    /**
     * Is this gui item hidden when a player has certain condition.
     */
    var hiddenCondition: Array<String>?

    /**
     * Is this gui item not clickable when a player has certain condition.
     */
    var blockedCondition: Array<String>?

    /**
     * Required permission to perform this action.
     */
    var permission: String

    /** Returns the position in the inventory. */
    var position: Int

    /**
     * Position in the inventory which cannot be scrolled.
     */
    var fixed: Boolean

    /**
     * Target skin.
     */
    var targetSkin: Skin?

    /**
     * Ai which should be added on click.
     */
    val addAIs: MutableList<AIBase>

    /**
     * Ai which should be removed on click.
     */
    val removeAIs: MutableList<AIBase>
}