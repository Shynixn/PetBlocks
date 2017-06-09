package com.github.shynixn.petblocks.api.persistence.entity;

public interface PetMeta extends com.github.shynixn.petblocks.api.entities.PetMeta, Persistenceable{
    /**
     * Returns the id of the player
     * @return playerId
     */
    long getPlayerId();

    /**
     * Sets the id of the player
     * @param id id
     */
    void setPlayerId(long id);

    /**
     * Returns the id of the particle
     * @return particleId
     */
    long getParticleId();

    /**
     * Sets the id of the particle
     * @param id id
     */
    void setParticleId(long id);

    /**
     * Sets the particleEffect meta
     * @param meta meta
     */
    void setParticleEffectMeta(ParticleEffectMeta meta);

    /**
     * Returns the particleEffect meta
     * @return meta
     */
    ParticleEffectMeta getParticleEffectMeta();

    /**
     * Sets the own meta
     * @param meta meta
     */
    void setPlayerMeta(PlayerMeta meta);

    /**
     * Returns the meta of the owner
     * @return player
     */
    PlayerMeta getPlayerMeta();
}
