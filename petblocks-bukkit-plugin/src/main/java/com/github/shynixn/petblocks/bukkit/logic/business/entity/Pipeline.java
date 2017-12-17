package com.github.shynixn.petblocks.bukkit.logic.business.entity;

import com.github.shynixn.petblocks.api.business.entity.EffectPipeline;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import com.github.shynixn.petblocks.api.persistence.entity.SoundMeta;
import com.github.shynixn.petblocks.bukkit.nms.helper.PetBlockHelper;
import org.bukkit.Location;

public class Pipeline implements EffectPipeline {

    private final PetBlock petBlock;

    public Pipeline(PetBlock petBlock) {
        super();
        this.petBlock = petBlock;
    }

    /**
     * Plays the given particleEffect and watches for invisibility, other players and actions.
     *
     * @param location           location
     * @param particleEffectMeta particleEffectMeta
     */
    @Override
    public void playParticleEffect(Object location, ParticleEffectMeta particleEffectMeta) {
        PetBlockHelper.playParticleEffectForPipeline((Location) location, particleEffectMeta, this.petBlock);
    }

    /**
     * Plays the given sound and watches for invisibility, other players and actions.
     *
     * @param location  location
     * @param soundMeta soundMeta
     */
    @Override
    public void playSound(Object location, SoundMeta soundMeta) {
        PetBlockHelper.playSoundEffectForPipeline((Location) location, soundMeta, this.petBlock);
    }
}