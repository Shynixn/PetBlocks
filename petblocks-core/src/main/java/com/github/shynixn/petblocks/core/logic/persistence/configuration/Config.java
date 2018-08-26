package com.github.shynixn.petblocks.core.logic.persistence.configuration;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.persistence.controller.CostumeController;
import com.github.shynixn.petblocks.api.persistence.controller.EngineController;
import com.github.shynixn.petblocks.api.persistence.controller.OtherGUIItemsController;
import com.github.shynixn.petblocks.api.persistence.controller.ParticleController;
import com.github.shynixn.petblocks.api.persistence.entity.EngineContainer;
import com.github.shynixn.petblocks.api.persistence.entity.Particle;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.api.persistence.entity.Sound;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.util.List;

public abstract class Config<Player> {
    private static Config instance;

    protected Particle feedingClickParticleCache;
    protected Sound feedingClickSoundCache;

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

    public Config() {
        super();
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

    public abstract void fixJoinDefaultPet(PetMeta petMeta);

    public abstract boolean allowPetSpawning(Object location);

    /**
     * Reloads the config
     */
    public void reload() {
        this.feedingClickParticleCache = null;
        this.feedingClickSoundCache = null;
        this.ordinaryCostumesController.reload();
        this.colorCostumesController.reload();
        this.rareCostumesController.reload();
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
     * Returns the forbidden pet names.
     *
     * @return names
     */
    public List<String> getPetNameBlackList() {
        return this.getData("pet.design.petname-blacklist");
    }

    /**
     * Returns the amount of blocks the pet has to stay away from the player.
     *
     * @return amount
     */
    public int getBlocksAwayFromPlayer() {
        return (int) this.getData("pet.follow.amount-blocks-away");
    }

    /**
     * Returns if feeding is enabled.
     *
     * @return feeding
     */
    public boolean isFeedingEnabled() {
        return this.getData("pet.feeding.enabled");
    }

    /**
     * Returns the value of the hitbox y axe modification.
     *
     * @return value
     */
    public double getHitBoxYAxeModification() {
        return this.getData("pet.follow.running-hitbox-height");
    }

    public boolean isAfraidOfwater() {
        return (boolean) this.getData("pet.follow.afraid-water");
    }

    public boolean isAfraidwaterParticles() {
        return (boolean) this.getData("pet.follow.afraid-water-particles");
    }

    public int getAge_smallticks() {
        return (int) this.getData("pet.age.small-ticks");
    }

    public int getAge_largeticks() {
        return (int) this.getData("pet.age.large-ticks");
    }

    public int getAge_maxticks() {
        return (int) this.getData("pet.age.max-ticks");
    }

    public boolean isAge_deathOnMaxTicks() {
        return (boolean) this.getData("pet.age.death-on-maxticks");
    }

    public double getCombat_health() {
        return (double) this.getData("pet.combat.health");
    }

    public boolean isCombat_invincible() {
        return (boolean) this.getData("pet.combat.invincible");
    }

    public int getFollow_maxRangeTeleport() {
        return (int) this.getData("pet.follow.max-range-teleport");
    }

    public boolean isFollow_fallOffHead() {
        return (boolean) this.getData("pet.follow.teleport-fall");
    }

    public boolean isFollow_carry() {
        return (boolean) this.getData("pet.follow.carry");
    }

    public int getDesign_maxPetNameLength() {
        return (int) this.getData("pet.design.max-petname-length");
    }

    public boolean isDesign_showDamageAnimation() {
        return (boolean) this.getData("pet.design.show-damage-animation");
    }

    public boolean isSoundForOtherPlayersHearable() {
        return (boolean) this.getData("pet.design.sounds-other-players");
    }

    /**
     * Returns if particles are visible for other players.
     *
     * @return visible
     */
    public boolean areParticlesForOtherPlayersVisible() {
        return this.getData("pet.design.particles-other-players");
    }

    public double getModifier_petriding() {
        return (double) this.getData("pet.modifier.riding-speed");
    }

    public double getModifier_petwalking() {
        return (double) this.getData("pet.modifier.walking-speed");
    }

    public double getModifier_petclimbing() {
        return (double) this.getData("pet.modifier.climbing-height");
    }

    public boolean isFollow_wallcolliding() {
        return (boolean) this.getData("pet.follow.flying-wall-colliding");
    }

    public boolean isFleesInCombat() {
        return (boolean) this.getData("pet.flee.flees-in-combat");
    }

    public int getReappearsInSeconds() {
        return (int) this.getData("pet.flee.reappears-in-seconds");
    }

    public int getWarpDelay() {
        return (int) this.getData("pet.warp.teleports-in-seconds");
    }

    /**
     * Returns the message for the pet being successfully called.
     *
     * @return message
     */
    public String getCallPetSuccessMessage() {
        return this.getData("messages.called-success");
    }

    /**
     * Returns the message for the pet being spawned.
     *
     * @return message
     */
    public String getToggleSpawnMessage() {
        return this.getData("messages.toggle-spawn");
    }

    /**
     * Returns the message for the pet being despawned.
     *
     * @return message
     */
    public String getToggleDeSpawnMessage() {
        return this.getData("messages.toggle-despawn");
    }

    /**
     * Returns if metrics is enabled
     *
     * @return enabled
     */
    public boolean isMetricsEnabled() {
        return (boolean) this.getData("metrics");
    }

    private List<String> getDataAsStringList(String path) {
        return ((List<String>) this.getData(path));
    }

    public boolean allowRidingOnRegionChanging() {
        return true;
    }
}
