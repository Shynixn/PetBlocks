package com.github.shynixn.petblocks.api.persistence.controller;

import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;

public interface ParticleEffectMetaController extends IDatabaseController<ParticleEffectMeta> {
    /**
     * Creates a new particleEffectMeta
     * @return meta
     */
    ParticleEffectMeta create();
}
