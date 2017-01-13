package com.github.shynixn.petblocks.business.logic.configuration;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Created by Shynixn
 */
public class ConfigPet {
    private static ConfigPet instance;
    //Age
    private int age_smallticks;
    private int age_largeticks = 1000;
    private int age_maxticks = 1200;
    private boolean age_deathOnMaxTicks = true;
    //Combat
    private double combat_health = 20.0;
    private boolean combat_invincible = true;
    //Flee
    private boolean fleesInCombat;
    private int reappearsInSeconds;
    //Warp
    private int warpDelay;
    //Follow
    private int follow_maxRangeTeleport = 50;
    private boolean follow_fallOffHead = true;
    private boolean follow_carry;
    private boolean follow_wallcolliding = true;
    private boolean afraidOfwater;
    private boolean afraidwaterParticles;
    //Design
    private int design_maxPetNameLength = 20;
    private boolean design_showDamageAnimation = true;
    private boolean design_allowOtherHearSound = true;
    //Modifer
    private double modifier_petriding;
    private double modifier_petwalking;
    private double modifier_petclimbing;

    private ConfigPet() {
        super();
    }

    public static ConfigPet getInstance() {
        if (instance == null)
            instance = new ConfigPet();
        return instance;
    }

    public void load(FileConfiguration c) {
        this.age_smallticks = c.getInt("pet.age.small-ticks");
        this.age_maxticks = c.getInt("pet.age.max-ticks");
        this.age_largeticks = c.getInt("pet.age.large-ticks");
        this.age_deathOnMaxTicks = c.getBoolean("pet.age.death-on-maxticks");

        this.combat_health = c.getDouble("pet.combat.health");
        this.combat_invincible = c.getBoolean("pet.combat.invincible");

        this.warpDelay = c.getInt("pet.warp.teleports-in-seconds");

        this.fleesInCombat = c.getBoolean("pet.flee.flees-in-combat");
        this.reappearsInSeconds = c.getInt("pet.flee.reappears-in-seconds");

        this.follow_maxRangeTeleport = c.getInt("pet.follow.max-range-teleport");
        this.follow_carry = c.getBoolean("pet.follow.carry");
        this.follow_fallOffHead = c.getBoolean("pet.follow.teleport-fall");
        this.follow_wallcolliding = c.getBoolean("pet.follow.flying-wall-colliding");
        this.afraidOfwater = c.getBoolean("pet.follow.afraid-water");
        this.afraidwaterParticles = c.getBoolean("pet.follow.afraid-water-particles");

        this.design_allowOtherHearSound = c.getBoolean("pet.design.sounds-other-players");
        this.design_maxPetNameLength = c.getInt("pet.design.max-petname-length");
        this.design_showDamageAnimation = c.getBoolean("pet.design.show-damage-animation");

        this.modifier_petriding = c.getDouble("pet.modifier.riding-speed");
        this.modifier_petwalking = c.getDouble("pet.modifier.walking-speed");
        this.modifier_petclimbing = c.getDouble("pet.modifier.climbing-height");
    }

    public boolean isAfraidOfwater() {
        return this.afraidOfwater;
    }

    public boolean isAfraidwaterParticles() {
        return this.afraidwaterParticles;
    }

    public int getAge_smallticks() {
        return this.age_smallticks;
    }

    public int getAge_largeticks() {
        return this.age_largeticks;
    }

    public int getAge_maxticks() {
        return this.age_maxticks;
    }

    public boolean isAge_deathOnMaxTicks() {
        return this.age_deathOnMaxTicks;
    }

    public double getCombat_health() {
        return this.combat_health;
    }

    public boolean isCombat_invincible() {
        return this.combat_invincible;
    }

    public int getFollow_maxRangeTeleport() {
        return this.follow_maxRangeTeleport;
    }

    public boolean isFollow_fallOffHead() {
        return this.follow_fallOffHead;
    }

    public boolean isFollow_carry() {
        return this.follow_carry;
    }

    public int getDesign_maxPetNameLength() {
        return this.design_maxPetNameLength;
    }

    public boolean isDesign_showDamageAnimation() {
        return this.design_showDamageAnimation;
    }

    public boolean isDesign_allowOtherHearSound() {
        return this.design_allowOtherHearSound;
    }

    public double getModifier_petriding() {
        return this.modifier_petriding;
    }

    public double getModifier_petwalking() {
        return this.modifier_petwalking;
    }

    public double getModifier_petclimbing() {
        return this.modifier_petclimbing;
    }

    public boolean isFollow_wallcolliding() {
        return this.follow_wallcolliding;
    }

    public boolean isFleesInCombat() {
        return this.fleesInCombat;
    }

    public int getReappearsInSeconds() {
        return this.reappearsInSeconds;
    }

    public int getWarpDelay() {
        return this.warpDelay;
    }
}
