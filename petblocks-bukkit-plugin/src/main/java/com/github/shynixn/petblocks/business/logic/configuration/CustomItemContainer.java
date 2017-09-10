package com.github.shynixn.petblocks.business.logic.configuration;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;
import com.github.shynixn.petblocks.api.entities.ItemContainer;
import com.github.shynixn.petblocks.api.entities.MoveType;
import com.github.shynixn.petblocks.api.entities.Movement;
import com.github.shynixn.petblocks.business.Language;
import com.github.shynixn.petblocks.business.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.lib.ParticleBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Created by Shynixn
 */

public class CustomItemContainer implements ItemContainer, GUIItemContainer {
    private ItemStack cache;

    private final int id;
    private final int damage;
    private final String skullName;
    private final int position;
    private boolean enabled;
    private MoveType type = MoveType.WALKING;
    private Movement movement = Movement.HOPPING;

    private GUIPage guiPage;
    private String[] lore;
    private String displayName;

    CustomItemContainer(int id, int damage, String skullName, int position, boolean enabled, String type, String movement) {
        super();
        this.id = id;
        this.damage = damage;
        this.skullName = skullName;
        this.position = position;
        this.enabled = enabled;
        if (MoveType.getMoveTypeFromName(type) != null)
            this.type = MoveType.getMoveTypeFromName(type);
        if (Movement.getMovementFromName(movement) != null)
            this.movement = Movement.getMovementFromName(movement);
    }

    public static GUIItemContainer from(int id, int damage, String skin, String displayName, String[] lore)
    {
        return null;
    }





    static CustomItemContainer resolveItemContainer(String identifier, FileConfiguration c) {
        final CustomItemContainer container = new CustomItemContainer(c.getInt(identifier + ".id"), c.getInt(identifier + ".damage"), c.getString(identifier + ".owner"), c.getInt(identifier + ".position"), c.getBoolean(identifier + ".enabled"), c.getString(identifier + ".type"), c.getString(identifier + ".movement"));
        if (c.contains(identifier + ".lore")) {
            final List<String> data = c.getStringList(identifier + ".lore");
            container.lore = new String[data.size()];
            for (int i = 0; i < data.size(); i++) {
                container.lore[i] = ChatColor.translateAlternateColorCodes('&', data.get(i));
            }
        }
        if (c.contains(identifier + ".name")) {
            container.displayName = ChatColor.translateAlternateColorCodes('&', c.getString(identifier + ".name"));
        }
        if (c.contains(identifier + ".page")) {
            final String pageName = c.getString(identifier + ".page");
            final GUIPage guiPage = GUIPage.getGUIPageFromName(pageName);
            if (guiPage == null) {
                Bukkit.getLogger().log(Level.WARNING, "Cannot find page named " + pageName + ".");
            }
            container.guiPage = guiPage;
        }
        return container;
    }

    @Override
    public Movement getMovement() {
        return this.movement;
    }

    /**
     * Returns the guiPage of this container
     *
     * @return guiPage
     */
    @Override
    public GUIPage getPage() {
        return this.guiPage;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public int getDamage() {
        return this.damage;
    }

    @Override
    public String getSkullName() {
        return this.skullName;
    }

    @Override
    public MoveType getMoveType() {
        return this.type;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private boolean hasPermission(Player player, String... permissions) {
        for (final String permission : permissions) {
            if (player.hasPermission(permission))
                return true;
        }
        return false;
    }

    private boolean hasPermission(Player player, Permission... permissions) {
        for (final Permission permission : permissions) {
            if (player.hasPermission(permission.get()))
                return true;
        }
        return false;
    }

    /**
     * Generates a new itemStack for the player and his permissions
     *
     * @param player      player
     * @param permissions permission
     * @return itemStack
     */
    @Override
    public ItemStack generate(Player player, String... permissions) {
        if (this.cache != null) {
            this.updateLore(player, permissions);
            return this.cache.clone();
        }
        try {
            if (this.enabled) {
                ItemStack itemStack = new ItemStack(Material.getMaterial(this.id), 1, (short) this.damage);
                if (this.id == Material.SKULL_ITEM.getId()) {
                    if (this.skullName.contains("textures.minecraft.net")) {
                        itemStack = NMSRegistry.changeSkullSkin(itemStack, "http://" + this.skullName);
                    } else {
                        final SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
                        meta.setOwner(this.skullName);
                        itemStack.setItemMeta(meta);
                    }
                }
                final ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(this.displayName);
                itemStack.setItemMeta(itemMeta);
                this.cache = itemStack;
                this.updateLore(player, permissions);
                return itemStack;
            }
        } catch (final Exception ex) {
            Bukkit.getConsoleSender().sendMessage(PetBlocksPlugin.PREFIX_CONSOLE + ChatColor.RED + "Invalid config file. Fix the following error or recreate it!");
            Bukkit.getLogger().log(Level.WARNING, "Failed to generate itemStack.", ex);
        }
        return new ItemStack(Material.AIR);
    }

    /**
     * Returns the displayName of the itemStack if present
     *
     * @return displayName
     */
    @Override
    public Optional<String> getDisplayName() {
        return Optional.ofNullable(this.displayName);
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
        if (!ConfigGUI.getInstance().isLoreEnabled()) {
            return null;
        }
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
                    modifiedLore[i] = this.lore[i].replace("<permission>", Language.ICO_PERMS_YES);
                } else {
                    modifiedLore[i] = this.lore[i].replace("<permission>", Language.ICO_PERMS_NO);
                }
            }
        }
        return modifiedLore;
    }

    protected static class ParticleItemContainer extends CustomItemContainer {
        private final ParticleBuilder particle;

        ParticleItemContainer(int id, int damage, String skullName, int position, boolean enabled, String type, ParticleBuilder particle) {
            super(id, damage, skullName, position, enabled, type, null);
            this.particle = particle;
        }

        public ParticleBuilder getParticle() {
            return this.particle;
        }
    }
}
