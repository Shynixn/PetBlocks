package com.github.shynixn.petblocks.sponge.logic.business.entity;

import com.github.shynixn.petblocks.api.business.entity.EffectPipeline;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import com.github.shynixn.petblocks.api.persistence.entity.SoundMeta;
import com.github.shynixn.petblocks.sponge.PetBlocksPlugin;
import com.github.shynixn.petblocks.sponge.logic.business.helper.ExtensionMethodsKt;
import com.github.shynixn.petblocks.sponge.logic.persistence.configuration.Config;
import com.github.shynixn.petblocks.sponge.logic.persistence.entity.SpongeParticleEffect;
import com.github.shynixn.petblocks.sponge.logic.persistence.entity.SpongeSoundBuilder;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

public class Pipeline implements EffectPipeline<Transform<World>> {

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
    public void playParticleEffect(Transform<World> location, ParticleEffectMeta particleEffectMeta) {
        if (Config.INSTANCE.areParticlesForOtherPlayersVisible()) {
            for (final Player player : location.getExtent().getPlayers()) {
                ((SpongeParticleEffect) particleEffectMeta).applyTo(location, player);
            }
        } else {
            ((SpongeParticleEffect) particleEffectMeta).applyTo(location, (Player) this.petBlock.getPlayer());
        }
    }

    /**
     * Plays the given sound and watches for invisibility, other players and actions.
     *
     * @param location  location
     * @param soundMeta soundMeta
     */
    @Override
    public void playSound(Transform<World> location, SoundMeta soundMeta) {
        if (!this.petBlock.getMeta().isSoundEnabled())
            return;
        try {
            if (Config.INSTANCE.isSoundForOtherPlayersHearable()) {
                ((SpongeSoundBuilder) soundMeta).apply(location, location.getExtent().getPlayers().toArray(new Player[location.getExtent().getPlayers().size()]));
            } else {
                ((SpongeSoundBuilder) soundMeta).apply(location, new Player[]{(Player) this.petBlock.getPlayer()});
            }
        } catch (final Exception e) {
            ExtensionMethodsKt.sendMessage(Sponge.getGame(), e.toString());
        }
    }
}