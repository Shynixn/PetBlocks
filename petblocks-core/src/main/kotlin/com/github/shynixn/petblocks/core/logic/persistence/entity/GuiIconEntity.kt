package com.github.shynixn.petblocks.core.logic.persistence.entity

import com.github.shynixn.petblocks.api.legacy.persistence.entity.GuiIcon
import com.github.shynixn.petblocks.api.legacy.persistence.entity.Skin

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
class GuiIconEntity : GuiIcon {
    private var backedDisplayName = " "
    private var backedLore = ArrayList<String>()

    /**
     * Gets the skin of the icon.
     */
    override val skin: Skin = SkinEntity()

    /** Returns the item displayName. */
    override var displayName: String
        get() = backedDisplayName
        set(value) {
            if (value == "none") {
                this.backedDisplayName = " "
            } else {
                this.backedDisplayName = value
            }
        }

    /** Returns the lore of the item. */
    override var lore: List<String>
        get() = backedLore
        set(value) {
            backedLore.clear()

            value.forEach { line ->
                if (line == "none") {
                    if (value.size != 1) {
                        backedLore.add("")
                    }
                } else {
                    backedLore.add(line)
                }
            }
        }

    /**
     * Gets a nullable script
     * which can be used for special rendering processes.
     */
    override var script: String? = null
}
