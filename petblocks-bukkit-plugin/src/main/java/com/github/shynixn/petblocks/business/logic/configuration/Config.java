package com.github.shynixn.petblocks.business.logic.configuration;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.persistence.controller.*;
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.business.logic.persistence.Factory;
import com.github.shynixn.petblocks.business.logic.persistence.entity.ParticleEffectData;
import com.github.shynixn.petblocks.business.logic.persistence.entity.PetData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;

import java.util.List;
import java.util.logging.Level;

public final class Config extends SimpleConfig {
    private static final Config instance = new Config();
    private final EngineController engineController = Factory.createEngineController();
    private final OtherGUIItemsController guiItemsController = Factory.createGUIItemsController();
    private final IFileController<GUIItemContainer> particleController = Factory.createParticleConfiguration();
    private final CostumeController ordinaryCostumesController = Factory.createCostumesController("ordinary");
    private final CostumeController colorCostumesController = Factory.createCostumesController("color");
    private final CostumeController rareCostumesController = Factory.createCostumesController("rare");
    private final CostumeController minecraftHeadsCostumesController = Factory.createMinecraftHeadsCostumesController();

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
    }

    public ParticleController getParticleController() {
        return this.particleController;
    }

    public OtherGUIItemsController getGuiItemsController() {
        return this.guiItemsController;
    }

    public EngineController getEngineController() {
        return this.engineController;
    }

    public CostumeController getOrdinaryCostumesController() {
        return this.ordinaryCostumesController;
    }

    public CostumeController getColorCostumesController() {
        return this.colorCostumesController;
    }

    public CostumeController getRareCostumesController() {
        return this.rareCostumesController;
    }

    public CostumeController getMinecraftHeadsCostumesController() {
        return this.minecraftHeadsCostumesController;
    }

    /**
     * Returns if copySkin is enabled
     *
     * @return copySkin
     */
    public boolean isCopySkinEnabled() {
        return this.plugin.getConfig().getBoolean("gui.settings.copy-skin");
    }

    /**
     * Returns if lore is enabled
     *
     * @return lore
     */
    boolean isLoreEnabled() {
        return this.plugin.getConfig().getBoolean("gui.settings.lore");
    }

    /**
     * Returns if emptyClickBack is enabled
     *
     * @return enabled
     */
    public boolean isEmptyClickBackEnabled() {
        return this.plugin.getConfig().getBoolean("gui.settings.click-empty-slot-back");
    }

    /**
     * Returns if disable item is enabled
     *
     * @return displayItem
     */
    public boolean isOnlyDisableItemEnabled() {
        return this.getData("gui.settings.use-only-disable-pet-item");
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

    public boolean isChat_async() {
        return (boolean) this.getData("chat.async");
    }

    public boolean isChatHighestPriority() {
        return (boolean) this.getData("chat.highest-priority");
    }

    public String[] getExcludedWorlds() {
        return this.getDataAsStringArray("world.excluded");
    }

    public String[] getIncludedWorlds() {
        return this.getDataAsStringArray("world.included");
    }

    public String[] getExcludedRegion() {
        return this.getDataAsStringArray("region.excluded");
    }

    public String[] getIncludedRegions() {
        return this.getDataAsStringArray("region.included");
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
        petMeta.setSkin(Material.getMaterial((Integer) this.getData("join.settings.id")), (short) (int) this.getData("join.settings.durability"), this.getData("join.settings.skullname"));
        petMeta.setEngineContainer(this.engineController.getById(this.getData("join.settings.engine")));
        petMeta.setDisplayName(this.getData("join.settings.petname"));
        petMeta.setEnabled(this.getData("join.settings.enabled"));
        petMeta.setAge(this.getData("join.settings.age"));
        if (!((String) this.getData("join.settings.particle.name")).equalsIgnoreCase("none")) {
            final ParticleEffectMeta meta;
            try {
                meta = new ParticleEffectData(((MemorySection) this.getData("effect")).getValues(false));
                petMeta.setParticleEffectMeta(meta);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to load particle effect for join pet.");
            }
        }
    }

    private String[] getDataAsStringArray(String path) {
        return ((List<String>) this.getData(path)).toArray(new String[0]);
    }

    public boolean allowRidingOnRegionChanging() {
        return true;
    }

    public boolean allowPetSpawning(Location location) {
        throw new RuntimeException();
    }
}
