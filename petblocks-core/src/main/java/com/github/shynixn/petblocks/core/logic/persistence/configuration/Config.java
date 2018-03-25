package com.github.shynixn.petblocks.core.logic.persistence.configuration;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.persistence.controller.CostumeController;
import com.github.shynixn.petblocks.api.persistence.controller.EngineController;
import com.github.shynixn.petblocks.api.persistence.controller.OtherGUIItemsController;
import com.github.shynixn.petblocks.api.persistence.controller.ParticleController;
import com.github.shynixn.petblocks.api.persistence.entity.EngineContainer;
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public abstract class Config<Player> {
    private static Config instance;

    @Inject
    private EngineController engineController;

    @Inject
    private OtherGUIItemsController guiItemsController;

    @Inject
    private ParticleController particleController;

    @Inject
    @Named("ordinary")
    private CostumeController ordinaryCostumesController;

    @Inject
    @Named("color")
    private CostumeController colorCostumesController;

    @Inject
    @Named("rare")
    private CostumeController rareCostumesController;

    @Inject
    @Named("minecraft-heads")
    private CostumeController minecraftHeadsCostumesController;

    public Config() {
        instance = this;
    }

    public static <T> Config<T> getInstance() {
        return instance;
    }

    /**
     * Returns data.
     *
     * @param path path
     * @return data
     */
    public abstract <T> T getData(String path);

    public abstract Object getPetNamingMessage();

    public abstract Object getPetSkinNamingMessage();

    public abstract void fixJoinDefaultPet(PetMeta petMeta);

    public abstract boolean allowPetSpawning(Object location);

    /**
     * Reloads the config
     */
    public void reload() {
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
        return this.getData("gui.settings.default-engine");
    }

    /**
     * Returns if copySkin is enabled.
     *
     * @return copySkin
     */
    public boolean isCopySkinEnabled() {
        return this.getData("gui.settings.copy-skin");
    }

    /**
     * Returns if lore is enabled.
     *
     * @return lore
     */
    boolean isLoreEnabled() {
        return this.getData("gui.settings.lore");
    }

    /**
     * Returns if emptyClickBack is enabled.
     *
     * @return enabled
     */
    public boolean isEmptyClickBackEnabled() {
        return this.getData("gui.settings.click-empty-slot-back");
    }

    /**
     * Returns if disable item is enabled.
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
}
