package com.github.shynixn.petblocks.sponge.logic.persistence.configuration

import com.github.shynixn.petblocks.core.logic.business.helper.ChatColor
import com.github.shynixn.petblocks.core.logic.persistence.configuration.FixedItemConfiguration
import com.github.shynixn.petblocks.sponge.logic.persistence.entity.SpongeItemContainer
import com.google.inject.Inject
import org.slf4j.Logger
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.inventory.ItemStack
import java.util.logging.Level

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
class SpongeStaticGUIItems : FixedItemConfiguration<Player>(){

    @Inject
    private lateinit var config: Config

    @Inject
    private lateinit var logger: Logger

    /**
     * Reloads the content from the fileSystem
     */
    override fun reload() {
        this.items.clear()
        val data = Config.getData<Map<String, Any>>("gui.items")
        for (key in data!!.keys) {
            try {
                val container = SpongeItemContainer(0, (data[key] as Map<String, Any>))
                if (key == "suggest-heads") {
                    container.setDisplayName(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "Suggest Heads")
                }
                this.items[key] = container
            } catch (e: Exception) {
                logger.error("Failed to load guiItem " + key + '.'.toString(), e)
            }
        }
    }


    /**
     * Returns if the given itemStack is a guiItemStack with the given name
     *
     * @param itemStack itemStack
     * @param name      name
     * @return itemStack
     */
    override fun isGUIItem(itemStack: Any?, name: String?): Boolean {
        if (itemStack == null || name == null)
            return false
        val optGUIContainer = this.getGUIItemFromName(name)
        if (!optGUIContainer.isPresent) {
            throw RuntimeException("GUIItem for PetBlocks with the name $name is not loaded correctly!")
        }
        val mItemStack = itemStack as ItemStack?
   /*     return (mItemStack!!.itemMeta != null && optGUIContainer.get().displayName.isPresent
                && mItemStack.itemMeta.displayName != null
                && mItemStack.itemMeta.displayName.equals(optGUIContainer.get().displayName.get(), ignoreCase = true))*/
        return false;
    }
}