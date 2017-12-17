package com.github.shynixn.petblocks.bukkit.logic.business.configuration;

import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import com.github.shynixn.petblocks.api.persistence.entity.SoundMeta;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.logic.persistence.entity.ParticleEffectData;
import com.github.shynixn.petblocks.bukkit.logic.persistence.entity.SoundBuilder;
import org.bukkit.configuration.MemorySection;

import java.util.List;
import java.util.logging.Level;

/**
 * Configuration access to the pet related settings.
 * <p>
 * Version 1.1
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2017 by Shynixn
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
public class ConfigPet extends SimpleConfig {
    private static ConfigPet instance;

    private ParticleEffectMeta feedingClickParticleCache;
    private SoundMeta feedingClickSoundCache;

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
     * Reloads the config.
     */
    @Override
    public void reload() {
        super.reload();
        this.feedingClickParticleCache = null;
        this.feedingClickSoundCache = null;
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
     * @return feeding
     */
    public boolean isFeedingEnabled() {
        return this.getData("pet.feeding.enabled");
    }

    /**
     * Returns the feeding click sound.
     *
     * @return sound
     */
    public SoundMeta getFeedingClickSound() {
        if (this.feedingClickSoundCache == null) {
            try {
                this.feedingClickSoundCache = new SoundBuilder(((MemorySection) this.getData("pet.feeding.click-sound")).getValues(false));
            } catch (final Exception e) {
                PetBlocksPlugin.logger().log(Level.WARNING, "Failed to load feeding click-sound.", e);
            }
        }
        return this.feedingClickSoundCache;
    }

    /**
     * Returns the feeding particleEffect.
     *
     * @return particleEffect
     */
    public ParticleEffectMeta getFeedingClickParticleEffect() {
        if (this.feedingClickParticleCache == null) {
            try {
                this.feedingClickParticleCache = new ParticleEffectData(((MemorySection) this.getData("pet.feeding.click-particle")).getValues(false));
            } catch (final Exception e) {
                PetBlocksPlugin.logger().log(Level.WARNING, "Failed to load feeding click-sound.", e);
            }
        }
        return this.feedingClickParticleCache;
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
}
