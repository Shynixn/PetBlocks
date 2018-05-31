package com.github.shynixn.petblocks.core.logic.persistence.entity

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer
import com.github.shynixn.petblocks.api.persistence.entity.GUIItem
import com.github.shynixn.petblocks.core.logic.business.helper.ChatColor
import java.util.*
import kotlin.collections.ArrayList

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
abstract class CustomGUIItem() : GUIItem {

    /** Returns a executable script */
    final override var executingScript: Optional<String> = Optional.empty()
    /** Returns the item displayName. */
    final override var displayName: String = ""
    /** Returns the type of the item. */
    final override var type: Int = 0
    /** Returns the data of the item. */
    override var data: Int = 0
    /** Returns the lore of the item. */
    override var lore: List<String> = ArrayList()
    /** Returns the skin of the item. */
    final override var skin: String = ""
    /** Returns if this item is enabled. */
    final override var enabled: Boolean = true
    /** Returns the position in the inventory. */
    final override var position: Int = 0
    /** Returns if the item is unbreakable. */
    final override var unbreakable: Boolean = false

    constructor(guiItemContainer: GUIItemContainer<*>) : this() {
        this.enabled = guiItemContainer.isEnabled
        this.displayName = guiItemContainer.displayName.get()
        this.type = guiItemContainer.itemId
        this.data = guiItemContainer.itemDamage
        this.unbreakable = guiItemContainer.isItemUnbreakable
        this.position = guiItemContainer.position
        this.lore = guiItemContainer.lore.get().toList()

        if (guiItemContainer.skin != null) {
            this.skin = guiItemContainer.skin
        }

        this.executingScript = guiItemContainer.executeableScript
    }

    constructor(id: Int, data: Map<String, Any>) : this() {
        this.position = id
        this.enabled = !data.containsKey("enabled") || data["enabled"] as Boolean

        if (data.containsKey("position")) {
            this.position = data["position"] as Int
        }

        if (data.containsKey("id")) {
            this.type = data["id"] as Int
        }

        if (data.containsKey("damage")) {
            this.data = data["damage"] as Int
        }

        if (data.containsKey("skin") && data["skin"] != "none") {
            this.skin = data["skin"] as String
        }

        if (data.containsKey("name")) {
            when {
                data["name"] == "default" -> this.displayName = ""
                data["name"] == "none" -> this.displayName = " "
                else -> this.displayName = ChatColor.translateAlternateColorCodes('&', data["name"] as String)
            }
        }

        if (data.containsKey("script")) {
            this.executingScript = Optional.ofNullable(data["script"] as String)
        }

        if (data.containsKey("unbreakable")) {
            this.unbreakable = data["unbreakable"] as Boolean
        }

        if (data.containsKey("lore")) {
            val m = data["lore"] as List<String>?
            if (m != null) {
                val lore = ArrayList<String>()
                m.filter { it != "none" }.mapTo(lore) { ChatColor.translateAlternateColorCodes('&', it) }
                this.lore = lore
            }
        }
    }
}