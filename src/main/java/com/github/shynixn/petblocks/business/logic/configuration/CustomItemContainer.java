package com.github.shynixn.petblocks.business.logic.configuration;

import com.github.shynixn.petblocks.api.entities.MoveType;
import com.github.shynixn.petblocks.business.Permission;
import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.api.entities.ItemContainer;
import com.github.shynixn.petblocks.api.entities.Movement;
import com.github.shynixn.petblocks.business.Language;
import com.github.shynixn.petblocks.business.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.lib.BukkitUtilities;
import com.github.shynixn.petblocks.lib.ParticleBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.regex.Pattern;

/**
 * Created by Shynixn
 */

class CustomItemContainer implements ItemContainer {
    private ItemStack cache;

    private final int id;
    private final int damage;
    private final String skullName;
    private final int position;
    private boolean enabled;
    private String loreName;
    private MoveType type = MoveType.WALKING;
    private Movement movement = Movement.HOPPING;

    CustomItemContainer(int id, int damage, String skullName, int position, String lore, boolean enabled, String type, String movement) {
        super();
        this.id = id;
        this.damage = damage;
        this.skullName = skullName;
        this.position = position;
        this.enabled = enabled;
        if (lore != null)
            this.loreName = ChatColor.translateAlternateColorCodes('&', lore);
        if (MoveType.getMoveTypeFromName(type) != null)
            this.type = MoveType.getMoveTypeFromName(type);
        if (Movement.getMovementFromName(movement) != null)
            this.movement = Movement.getMovementFromName(movement);
    }

    static ItemContainer resolveItemContainer(String identifier, FileConfiguration c) {
        return new CustomItemContainer(c.getInt(identifier + ".id"), c.getInt(identifier + ".damage"), c.getString(identifier + ".owner"), c.getInt(identifier + ".position"), c.getString(identifier + ".lore"), c.getBoolean(identifier + ".enabled"), c.getString(identifier + ".type"), c.getString(identifier + ".movement"));
    }

    @Override
    public Movement getMovement() {
        return this.movement;
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
    public String[] getLore() {
        if (!ConfigGUI.getInstance().isSettings_allowLore())
            return null;
        return this.toLines(this.loreName);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private String[] toLines(String lore) {
        if (lore == null)
            return null;
        if (ChatColor.stripColor(lore).contains("\\n")) {
            return lore.split(Pattern.quote("\\n"));
        }
        return new String[]{lore};
    }

    @Override
    public String[] getLore(Player player, Permission... permission) {
        if (!ConfigGUI.getInstance().isSettings_allowLore())
            return null;
        if (this.loreName != null && this.loreName.contains("<permission>")) {
            if (this.hasPermission(player, permission))
                return this.toLines(this.loreName.toString().replace("<permission>", Language.ICO_PERMS_YES));
            else
                return this.toLines(this.loreName.toString().replace("<permission>", Language.ICO_PERMS_NO));
        }
        return this.toLines(this.loreName);
    }

    @Override
    public String[] getLore(Player player, String... permission) {
        if (!ConfigGUI.getInstance().isSettings_allowLore())
            return null;
        if (this.loreName != null && this.loreName.contains("<permission>")) {
            if (this.hasPermission(player, permission))
                return this.toLines(this.loreName.toString().replace("<permission>", Language.ICO_PERMS_YES));
            else
                return this.toLines(this.loreName.toString().replace("<permission>", Language.ICO_PERMS_NO));
        }
        return this.toLines(this.loreName);
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

    @Override
    @SuppressWarnings("deprecation")
    public ItemStack generate() {
        if (this.cache != null)
            return this.cache.clone();
        try {
            if (this.enabled) {
                ItemStack itemStack = new ItemStack(Material.getMaterial(this.id), 1, (short) this.damage);
                if (this.id == Material.SKULL_ITEM.getId()) {
                    if (this.skullName.contains("textures.minecraft.net")) {
                        itemStack = NMSRegistry.changeSkullSkin(itemStack, "http://" + this.skullName);
                    } else {
                        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
                        meta.setOwner(this.skullName);
                        itemStack.setItemMeta(meta);
                    }
                }
                this.cache = itemStack;
                return itemStack;
            }
        } catch (final Exception ex) {
            BukkitUtilities.sendColorMessage("Invalid config file. Consider recreating it! " + ex.getMessage(), ChatColor.RED, PetBlocksPlugin.PREFIX_CONSOLE);
        }
        return new ItemStack(Material.AIR);
    }

    @Override
    public int getPosition() {
        return this.position;
    }

    protected static class ParticleItemContainer extends CustomItemContainer {
        private final ParticleBuilder particle;

        ParticleItemContainer(int id, int damage, String skullName, int position, String lore, boolean enabled, String type, ParticleBuilder particle) {
            super(id, damage, skullName, position, lore, enabled, type, null);
            this.particle = particle;
        }

        public ParticleBuilder getParticle() {
            return this.particle;
        }
    }
}
