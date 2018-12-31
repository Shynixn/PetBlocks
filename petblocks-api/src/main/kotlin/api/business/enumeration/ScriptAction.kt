package com.github.shynixn.petblocks.api.business.enumeration

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
enum class ScriptAction {

    /**
     * Action for not interpreting the script.
     */
    NONE,

    /**
     * Calls the pet to the player.
     */
    CALL_PET,

    /**
     * Scrolls the whole gui page.
     */
    SCROLL_PAGE,

    /**
     * Closes the gui.
     */
    CLOSE_GUI,

    /**
     * Opens a GUI page.
     */
    OPEN_PAGE,

    /**
     * Copies the pet skin to the icon.
     */
    COPY_PET_SKIN,

    /**
     * Prints the suggest head message.
     */
    PRINT_SUGGEST_HEAD_MESSAGE,

    /**
     * Prints the custom skin message.
     */
    PRINT_CUSTOM_SKIN_MESSAGE,

    /**
     * Connects to the head database plugin.
     */
    CONNECT_HEAD_DATABASE,

    /**
     * Hides the item on left scroll outside of the bounding box.
     */
    HIDE_LEFT_SCROLL,

    /**
     * Hides the item on right scroll outside of the bounding box.
     */
    HIDE_RIGHT_SCROLL
}