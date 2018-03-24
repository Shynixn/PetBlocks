package com.github.shynixn.petblocks.bukkit.logic.business.configuration;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.persistence.controller.*;
import com.github.shynixn.petblocks.api.persistence.entity.EngineContainer;
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.logic.business.helper.ChatBuilder;
import com.github.shynixn.petblocks.bukkit.logic.Factory;
import com.github.shynixn.petblocks.bukkit.logic.persistence.entity.PetData;
import com.github.shynixn.petblocks.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.core.logic.business.helper.ExtensionHikariConnectionContext;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public final class Config extends SimpleConfig {
    private static final Config instance = new Config();
    private final EngineController<EngineContainer<GUIItemContainer<Player>>, GUIItemContainer<Player>> engineController = Factory.createEngineController();
    private final OtherGUIItemsController<GUIItemContainer<Player>> guiItemsController = Factory.createGUIItemsController();
    private final ParticleController<GUIItemContainer<Player>> particleController = Factory.createParticleConfiguration();
    private final CostumeController<GUIItemContainer<Player>> ordinaryCostumesController = Factory.createCostumesController("ordinary");
    private final CostumeController<GUIItemContainer<Player>> colorCostumesController = Factory.createCostumesController("color");
    private final CostumeController<GUIItemContainer<Player>> rareCostumesController = Factory.createCostumesController("rare");
    private final CostumeController<GUIItemContainer<Player>> minecraftHeadsCostumesController = Factory.createMinecraftHeadsCostumesController();

    private Config() {
        super();
    }

    public static Config getInstance() {
        return instance;
    }

    /**
     * Reloads the config
     */
    @Override
    public void reload() {
        super.reload();
        ConfigPet.getInstance().reload();
        this.ordinaryCostumesController.reload();
        this.colorCostumesController.reload();
        this.rareCostumesController.reload();
        this.minecraftHeadsCostumesController.reload();
        this.guiItemsController.reload();
        this.particleController.reload();
        this.engineController.reload();
    }

    public ParticleController<GUIItemContainer<Player>> getParticleController() {
        return this.particleController;
    }

    public OtherGUIItemsController<GUIItemContainer<Player>> getGuiItemsController() {
        return this.guiItemsController;
    }

    public EngineController<EngineContainer<GUIItemContainer<Player>>, GUIItemContainer<Player>> getEngineController() {
        return this.engineController;
    }

    public CostumeController<GUIItemContainer<Player>> getOrdinaryCostumesController() {
        return this.ordinaryCostumesController;
    }

    public CostumeController<GUIItemContainer<Player>> getColorCostumesController() {
        return this.colorCostumesController;
    }

    public CostumeController<GUIItemContainer<Player>> getRareCostumesController() {
        return this.rareCostumesController;
    }

    public CostumeController<GUIItemContainer<Player>> getMinecraftHeadsCostumesController() {
        return this.minecraftHeadsCostumesController;
    }

    public int getDefaultEngine() {
        return this.plugin.getConfig().getInt("gui.settings.default-engine");
    }

    /**
     * Returns if copySkin is enabled.
     *
     * @return copySkin
     */
    public boolean isCopySkinEnabled() {
        return this.plugin.getConfig().getBoolean("gui.settings.copy-skin");
    }

    /**
     * Returns if lore is enabled.
     *
     * @return lore
     */
    boolean isLoreEnabled() {
        return this.plugin.getConfig().getBoolean("gui.settings.lore");
    }

    /**
     * Returns if emptyClickBack is enabled.
     *
     * @return enabled
     */
    public boolean isEmptyClickBackEnabled() {
        return this.plugin.getConfig().getBoolean("gui.settings.click-empty-slot-back");
    }

    /**
     * Returns if disable item is enabled.
     *
     * @return displayItem
     */
    public boolean isOnlyDisableItemEnabled() {
        return this.getData("gui.settings.use-only-disable-pet-item");
    }

    /**
     * Returns the pet naming message.
     *
     * @return message
     */
    public ChatBuilder getPetNamingMessage() {
        return new ChatBuilder()
                .text(this.getPrefix())
                .component(this.getData("messages.naming-suggest-prefix")).builder()
                .component(this.getData("messages.naming-suggest-clickable"))
                .setClickAction(ChatBuilder.ClickAction.SUGGEST_COMMAND, "/" + this.getData("petblocks-gui.command") + " rename ")
                .setHoverText(this.getData("messages.naming-suggest-hover")).builder()
                .component(this.getData("messages.naming-suggest-suffix")).builder();
    }

    /**
     * Returns the skin naming message.
     *
     * @return message
     */
    public ChatBuilder getPetSkinNamingMessage() {
        return new ChatBuilder()
                .text(this.getPrefix())
                .component(this.getData("messages.skullnaming-suggest-prefix")).builder()
                .component(this.getData("messages.skullnaming-suggest-clickable"))
                .setClickAction(ChatBuilder.ClickAction.SUGGEST_COMMAND, "/" + this.getData("petblocks-gui.command") + " skin ")
                .setHoverText(this.getData("messages.skullnaming-suggest-hover")).builder()
                .component(this.getData("messages.skullnaming-suggest-suffix")).builder();
    }

    public String getPermissionIconYes() {
        return this.getData("messages.perms-ico-yes");
    }

    public String getPermissionIconNo() {
        return this.getData("messages.perms-ico-no");
    }

    public String getNoPermission() {
        return this.getData("messages.no-perms");
    }

    public String getGUITitle() {
        return this.getData("gui.settings.title");
    }

    public String getPrefix() {
        return this.getData("messages.prefix");
    }

    public String getDefaultPetName() {
        return this.getData("messages.default-petname");
    }

    public String getNamingMessage() {
        return this.getData("messages.naming-message");
    }

    public String getNamingSuccessMessage() {
        return this.getData("messages.naming-success");
    }

    public String getNamingErrorMessage() {
        return this.getData("messages.naming-error");
    }

    public String getSkullNamingMessage() {
        return this.getData("messages.skullnaming-message");
    }

    public String getSkullNamingSuccessMessage() {
        return this.getData("messages.skullnaming-success");
    }

    public String getSkullNamingErrorMessage() {
        return this.getData("messages.skullnaming-error");
    }

    public boolean isJoin_enabled() {
        return (boolean) this.getData("join.enabled");
    }

    public boolean isJoin_overwriteExistingPet() {
        return (boolean) this.getData("join.overwrite-previous-pet");
    }

    public List<String> getExcludedWorlds() {
        return this.getDataAsStringList("world.excluded");
    }

    public List<String> getIncludedWorlds() {
        return this.getDataAsStringList("world.included");
    }

    public List<String> getExcludedRegion() {
        return this.getDataAsStringList("region.excluded");
    }

    public List<String> getIncludedRegions() {
        return this.getDataAsStringList("region.included");
    }

    /**
     * Returns if metrics is enabled
     *
     * @return enabled
     */
    public boolean isMetricsEnabled() {
        return (boolean) this.getData("metrics");
    }

    public void fixJoinDefaultPet(PetMeta petData) {
        final PetData petMeta = (PetData) petData;
        petMeta.setSkin(this.getData("join.settings.id"), (short) (int) this.getData("join.settings.damage"), this.getData("join.settings.skin"), this.getData("unbreakable"));
        final Optional<EngineContainer<GUIItemContainer<Player>>> optEngineContainer = this.engineController.getContainerFromPosition(this.getData("join.settings.engine"));
        if (!optEngineContainer.isPresent()) {
            throw new IllegalArgumentException("Join.settings.engine engine could not be loaded!");
        }
        petMeta.setEngine(optEngineContainer.get());
        petMeta.setPetDisplayName(this.getData("join.settings.petname"));
        petMeta.setEnabled(this.getData("join.settings.enabled"));
        petMeta.setAge(this.getData("join.settings.age"));
        if (!((String) this.getData("join.settings.particle.name")).equalsIgnoreCase("none")) {
            final ParticleEffectMeta meta;
            try {
                meta = createParticleComp(((MemorySection) this.getData("effect")).getValues(false));
                petMeta.setParticleEffectMeta(meta);
            } catch (final Exception e) {
                PetBlocksPlugin.logger().log(Level.WARNING, "Failed to load particle effect for join pet.");
            }
        }
    }

    public static ParticleEffectMeta createParticleComp(Map<String, Object> data) {
        try {
            final Class<?> clazz = Class.forName("com.github.shynixn.petblocks.bukkit.logic.persistence.entity.BukkitParticleEffect");
            final Constructor constructor = clazz.getDeclaredConstructor(Map.class);
            return (ParticleEffectMeta) constructor.newInstance(data);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getDataAsStringList(String path) {
        return ((List<String>) this.getData(path));
    }

    public boolean allowRidingOnRegionChanging() {
        return true;
    }

    public boolean allowPetSpawning(Location location) {
        final List<String> includedWorlds = this.getIncludedWorlds();
        final List<String> excludedWorlds = this.getExcludedWorlds();
        if (includedWorlds.contains("all")) {
            return !excludedWorlds.contains(location.getWorld().getName()) && this.handleRegionSpawn(location);
        } else if (excludedWorlds.contains("all")) {
            return includedWorlds.contains(location.getWorld().getName()) && this.handleRegionSpawn(location);
        } else {
            Bukkit.getConsoleSender().sendMessage(PetBlocksPlugin.PREFIX_CONSOLE + ChatColor.RED + "Please add 'all' to excluded or included worlds inside of the config.yml");
        }
        return true;
    }

    private boolean handleRegionSpawn(Location location) {
        final List<String> includedRegions = this.getIncludedRegions();
        final List<String> excludedRegions = this.getExcludedRegion();
        if (includedRegions.contains("all")) {
            for (final String k : NMSRegistry.getWorldGuardRegionsFromLocation(location)) {
                if (excludedRegions.contains(k)) {
                    return false;
                }
            }
            return true;
        } else if (excludedRegions.contains("all")) {
            for (final String k : NMSRegistry.getWorldGuardRegionsFromLocation(location)) {
                if (includedRegions.contains(k)) {
                    return true;
                }
            }
            return false;
        } else {
            Bukkit.getConsoleSender().sendMessage(PetBlocksPlugin.PREFIX_CONSOLE + ChatColor.RED + "Please add 'all' to excluded or included regions inside of the config.yml");
        }
        return true;
    }
}
