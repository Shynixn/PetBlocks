package com.github.shynixn.petblocks.business.logic.configuration;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;
import com.github.shynixn.petblocks.business.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.business.logic.configuration.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Copyright 2017 Shynixn
 * <p>
 * Do not remove this header!
 * <p>
 * Version 1.0
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2017
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
public class ItemContainer implements GUIItemContainer {

    private ItemStack cache;

    private boolean enabled;
    private int position = -1;
    private final GUIPage page;
    private final int id;
    private final int damage;
    private final String skin;
    private final boolean unbreakable;
    private final String name;
    private final String[] lore;

    /**
     * Initializes a new itemContainer
     * @param enabled enabled
     * @param position position
     * @param page page
     * @param id id
     * @param damage damage
     * @param skin skin
     * @param unbreakable unbreakablöe
     * @param name name
     * @param lore lore
     * */
    public ItemContainer(boolean enabled, int position, GUIPage page, int id, int damage, String skin, boolean unbreakable, String name, String[] lore) {
        this.enabled = enabled;
        this.position = position;
        this.page = page;
        this.id = id;
        this.damage = damage;
        this.skin = skin;
        this.unbreakable = unbreakable;
        this.name = name;
        this.lore = lore.clone();
    }

    /**
     * Initializes a new itemContainer
     *
     * @param data data
     * @throws Exception exception
     */
    public ItemContainer(int orderNumber, Map<String, Object> data) throws Exception {
        this.position = orderNumber;
        this.enabled = (boolean) data.get("enabled");
        this.position = (int) data.get("position");
        this.page = GUIPage.valueOf((String) data.get("page"));
        this.id = (int) data.get("id");
        this.damage = (int) data.get("damage");
        this.skin = (String) data.get("skin");
        this.name = ChatColor.translateAlternateColorCodes('&', (String) data.get("name"));
        this.unbreakable = (boolean) data.get("unbreakable");
        final List<String> m = (List<String>) data.get("lore");
        this.lore = new String[m.size()];
        for (int i = 0; i < this.lore.length; i++) {
            this.lore[i] = ChatColor.translateAlternateColorCodes('&', m.get(i));
        }
    }

    /**
     * Generates a new itemStack for the player and his permissions
     *
     * @param player      player
     * @param permissions permission
     * @return itemStack
     */
    @Override
    public Object generate(Object player, String... permissions) {
        if (this.cache != null) {
            this.updateLore((Player) player, permissions);
            return this.cache.clone();
        }
        try {
            if (this.enabled) {
                ItemStack itemStack = new ItemStack(Material.getMaterial(this.id), 1, (short) this.damage);
                if (this.id == Material.SKULL_ITEM.getId()) {
                    if (this.skin.contains("textures.minecraft.net")) {
                        itemStack = NMSRegistry.changeSkullSkin(itemStack, "http://" + this.skin);
                    } else {
                        final SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
                        meta.setOwner(this.skin);
                        itemStack.setItemMeta(meta);
                    }
                }
                final ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(this.name);
                itemStack.setItemMeta(itemMeta);
                this.cache = itemStack;
                this.updateLore((Player) player, permissions);
                return itemStack;
            }
        } catch (final Exception ex) {
            Bukkit.getConsoleSender().sendMessage(PetBlocksPlugin.PREFIX_CONSOLE + ChatColor.RED + "Invalid config file. Fix the following error or recreate it!");
            Bukkit.getLogger().log(Level.WARNING, "Failed to generate itemStack.", ex);
        }
        return new ItemStack(Material.AIR);
    }

    /**
     * Returns if the itemContainer is enabled
     *
     * @return enabled
     */
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Sets the itemContainer enabled
     *
     * @param enabled enabled
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the guiPage of this container
     *
     * @return guiPage
     */
    @Override
    public GUIPage getPage() {
        return null;
    }

    /**
     * Returns the displayName of the itemStack if present
     *
     * @return displayName
     */
    @Override
    public Optional<String> getDisplayName() {
        return Optional.ofNullable(this.name);
    }

    /**
     * Returns the lore of the itemStack if present
     *
     * @return lore
     */
    @Override
    public Optional<String[]> getLore() {
        return Optional.ofNullable(this.lore);
    }

    /**
     * Returns the position of the itemStack in the ui
     *
     * @return position
     */
    @Override
    public int getPosition() {
        return this.position;
    }

    private void updateLore(Player player, String... permissions) {
        final String[] lore = this.provideLore(player, permissions);
        if (lore != null) {
            final ItemMeta meta = this.cache.getItemMeta();
            meta.setLore(Arrays.asList(lore));
            this.cache.setItemMeta(meta);
        }
    }

    private String[] provideLore(Player player, String... permissions) {
        if (permissions != null) {
            if (permissions.length == 1 && permissions[0].equals("minecraft-heads")) {
                return new String[]{ChatColor.GRAY + "sponsored by", ChatColor.GRAY + "Minecraft-Heads.com"};
            }
            if (permissions.length == 1 && permissions[0].equals("head-database")) {
                final Plugin plugin = Bukkit.getPluginManager().getPlugin("HeadDatabase");
                if (plugin == null) {
                    return new String[]{ChatColor.DARK_RED + "" + ChatColor.ITALIC + "Plugin is not installed - " + ChatColor.YELLOW + "Click me!"};
                }
            }
        }
        final String[] modifiedLore = new String[this.lore.length];
        for (int i = 0; i < modifiedLore.length; i++) {
            modifiedLore[i] = this.lore[i];
            if (this.lore[i].contains("<permission>")) {
                if (permissions != null && this.hasPermission(player, permissions)) {
                    modifiedLore[i] = this.lore[i].replace("<permission>", Config.getInstance().getPermissionIconYes());
                } else {
                    modifiedLore[i] = this.lore[i].replace("<permission>", Config.getInstance().getPermissionIconNo());
                }
            }
        }
        return modifiedLore;
    }

    private boolean hasPermission(Player player, String... permissions) {
        for (final String permission : permissions) {
            if (player.hasPermission(permission))
                return true;
        }
        return false;
    }
}
