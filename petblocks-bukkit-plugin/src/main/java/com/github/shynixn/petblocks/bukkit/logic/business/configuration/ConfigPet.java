package com.github.shynixn.petblocks.bukkit.logic.business.configuration;

public class ConfigPet extends SimpleConfig {
    private static ConfigPet instance;

    /**
     * Initializes a new pet config
     */
    private ConfigPet() {
        super();
    }

    /**
     * Returns the config pet instance
     *
     * @return instance
     */
    public static ConfigPet getInstance() {
        if (instance == null)
            instance = new ConfigPet();
        return instance;
    }

    /**
     * Returns the amount of blocks the pet has to stay away from the player
     *
     * @return amount
     */
    public int getBlocksAwayFromPlayer() {
        return (int) this.getData("pet.follow.amount-blocks-away");
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

    public boolean isDesign_allowOtherHearSound() {
        return (boolean) this.getData("pet.design.sounds-other-players");
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
}
