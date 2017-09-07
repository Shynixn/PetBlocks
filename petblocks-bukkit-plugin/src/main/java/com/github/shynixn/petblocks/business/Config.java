package com.github.shynixn.petblocks.business;

import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigCommands;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigGUI;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigParticle;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigPet;
import com.github.shynixn.petblocks.lib.BukkitUtilities;
import com.github.shynixn.petblocks.lib.ParticleBuilder;
import com.github.shynixn.petblocks.lib.ParticleEffect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public final class Config {
    private static Plugin plugin;
    private static Config instance;
    private static FileConfiguration c;

    private boolean chat_async = true;
    private boolean chatHighestPriority = true;

    private boolean world_allowInAllWorlds = true;
    private String[] world_worlds;
    private boolean region_allowInAllRegions = true;
    private String[] region_regions;

    private boolean join_enabled;
    private boolean join_overwriteExistingPet;

    private boolean metrics = true;
    private final boolean allowRidingOnRegionChanging = true;

    private Config(Plugin plugin) {
        super();
        Config.plugin = plugin;
        this.reload();
    }

    public static void initialize(Plugin plugin) {
        instance = new Config(plugin);
    }

    public static Config getInstance() {
        return instance;
    }

    public void reload() {
        try {
            plugin.reloadConfig();
            c = plugin.getConfig();

            this.metrics = c.getBoolean("metrics");

            this.chat_async = c.getBoolean("chat.async");
            this.chatHighestPriority = c.getBoolean("chat.highest-priority");

            this.world_allowInAllWorlds = plugin.getConfig().getBoolean("world.all");
            this.world_worlds = plugin.getConfig().getStringList("world.worlds").toArray(new String[plugin.getConfig().getStringList("world.worlds").size()]);

            this.region_allowInAllRegions = plugin.getConfig().getBoolean("region.all");
            this.region_regions = plugin.getConfig().getStringList("region.regions").toArray(new String[plugin.getConfig().getStringList("region.regions").size()]);

            this.join_enabled = c.getBoolean("join.enabled");
            this.join_overwriteExistingPet = plugin.getConfig().getBoolean("join.overwrite-previous-pet");

            //  this.allowRidingOnRegionChanging = c.getBoolean("region.allow-riding-on");

            ConfigCommands.getInstance().load(c);
            ConfigGUI.getInstance().load(c);
            ConfigParticle.getInstance().load(c);
            ConfigPet.getInstance().load(c);
        } catch (final Exception ex) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to reload config.", ex);
        }
    }

    public boolean allowPetSpawning(Location location) {
        return location != null && (this.world_allowInAllWorlds || this.isInWorld(location.getWorld())) && (this.region_allowInAllRegions || NMSRegistry.canSpawnInRegion(this.region_regions, location));
    }

    public int getTicksFromAge(Age age) {
        if (age == Age.LARGE)
            return ConfigPet.getInstance().getAge_largeticks();
        if (age == Age.SMALL)
            return ConfigPet.getInstance().getAge_smallticks();
        return -1;
    }

    public void fixJoinDefaultPet(PetMeta petMeta) {
        petMeta.setSkin(Material.getMaterial(c.getInt("join.settings.id")), (short) c.getInt("join.settings.durability"), c.getString("join.settings.skullname"));
        petMeta.setPetType(PetType.getPetTypeFromName(c.getString("join.settings.type")));
        petMeta.setDisplayName(c.getString("join.settings.petname"));
        petMeta.setEnabled(c.getBoolean("join.settings.enabled"));
        petMeta.setAgeInTicks(c.getInt("join.settings.age"));
        if (MoveType.getMoveTypeFromName(c.getString("join.settings.moving")) == null)
            petMeta.setMoveType(MoveType.WALKING);
        else
            petMeta.setMoveType(MoveType.getMoveTypeFromName(c.getString("join.settings.moving")));
        if (!c.getString("join.settings.particle.name").equalsIgnoreCase("none") && ParticleEffect.getParticleEffectFromName(c.getString("join.settings.particle.name")) != null) {
            final Particle particle = new ParticleBuilder()
                    .setEffect(ParticleEffect.getParticleEffectFromName(c.getString("join.settings.particle.name")))
                    .setOffset(c.getDouble("join.settings.particle.x"), c.getDouble("join.settings.particle.y"), c.getDouble("join.settings.particle.z"))
                    .setSpeed(c.getDouble("join.settings.particle.speed"))
                    .setAmount(c.getInt("join.settings.particle.amount"))
                    .setMaterialId(c.getInt("join.settings.particle.block.id"))
                    .setData((byte) c.getInt("join.settings.particle.block.damage")).build();
            petMeta.setParticleEffect(particle);
        }
    }

    public void setMyContainer(GUIPage guiPage, Inventory inventory, String title, ItemContainer container, Permission... permission) {
        if(!this.canSetMyContainer(guiPage, container))
            return;
        if (permission == null) {
            inventory.setItem(container.getPosition(), BukkitUtilities.nameItem(container.generate(), title, container.getLore()));
        } else {
            inventory.setItem(container.getPosition(), BukkitUtilities.nameItem(container.generate(), title, container.getLore((Player) inventory.getHolder(), permission)));
        }
    }

    public void setMyContainer(GUIPage guiPage, Inventory inventory, String title, ItemContainer container, String... permission) {
        if(!this.canSetMyContainer(guiPage, container))
            return;
        if (permission == null) {
            inventory.setItem(container.getPosition(), BukkitUtilities.nameItem(container.generate(), title, container.getLore()));
        } else {
            inventory.setItem(container.getPosition(), BukkitUtilities.nameItem(container.generate(), title, container.getLore((Player) inventory.getHolder(), permission)));
        }
    }

    public ItemStack getMyItemStack(Player player, String title, ItemContainer container, String... permission) {
        if (permission == null)
            return BukkitUtilities.nameItem(container.generate(), title, container.getLore());
        else
            return BukkitUtilities.nameItem(container.generate(), title, container.getLore(player, permission));
    }

    public MoveType getMovingType(PetType petType) {
        return ConfigGUI.getInstance().getContainer(petType).getMoveType();
    }

    public boolean isJoin_enabled() {
        return this.join_enabled;
    }

    public boolean isJoin_overwriteExistingPet() {
        return this.join_overwriteExistingPet;
    }

    public boolean isChat_async() {
        return this.chat_async;
    }

    public boolean isChatHighestPriority() {
        return this.chatHighestPriority;
    }

    public boolean allowRidingOnRegionChanging() {
        return this.allowRidingOnRegionChanging;
    }

    private boolean isInWorld(World world) {
        for (final String s : this.world_worlds) {
            if (world.getName().equalsIgnoreCase(s))
                return true;
        }
        return false;
    }

    /**
     * Returns if metrics is enabled
     *
     * @return enabled
     */
    public boolean isMetricsEnabled() {
        return this.metrics;
    }

    /**
     * Returns if my container should be set
     *
     * @param guiPage   guiPage
     * @param container container
     * @return should be set
     */
    private boolean canSetMyContainer(GUIPage guiPage, ItemContainer container) {
        if (guiPage == GUIPage.MAIN && (container.getGUIPage() == null))
            return true;
        else if ((guiPage == GUIPage.MAIN && container.getGUIPage() == GUIPage.MAIN)
                || (guiPage == GUIPage.WARDROBE && container.getGUIPage() == GUIPage.WARDROBE))
            return true;
        return false;
    }
}
