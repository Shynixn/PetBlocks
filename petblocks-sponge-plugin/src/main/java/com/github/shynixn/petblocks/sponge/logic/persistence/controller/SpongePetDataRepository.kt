package com.github.shynixn.petblocks.sponge.logic.persistence.controller

import com.github.shynixn.petblocks.api.persistence.controller.ParticleEffectMetaController
import com.github.shynixn.petblocks.core.logic.business.entity.DbContext
import com.github.shynixn.petblocks.core.logic.persistence.controller.PetDataRepository
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetData
import com.github.shynixn.petblocks.sponge.logic.persistence.entity.SpongePetData
import com.google.inject.Inject
import org.slf4j.Logger
import org.spongepowered.api.entity.living.player.Player
import java.util.*

/**
 * Created by Shynixn 2018.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2018 by Shynixn
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
class SpongePetDataRepository @Inject constructor(playerMetaController: SpongePlayerDataRepository, particleController: ParticleEffectMetaController, connectionContext: DbContext
                                                  , logger: Logger) : PetDataRepository<Player>(playerMetaController, particleController, connectionContext, logger) {
    override fun create(player: Any?, name: String?): PetData {
        return SpongePetData(player as Player, name)
    }

    override fun getPlayerName(player: Any?): String {
        return (player as Player).name
    }

    override fun getPlayerUUID(player: Any?): UUID {
        return (player as Player).uniqueId
    }

    /**
     * Creates a new PetData.
     * @return petData
     */
    override fun create(): PetData {
        return SpongePetData()
    }
}
