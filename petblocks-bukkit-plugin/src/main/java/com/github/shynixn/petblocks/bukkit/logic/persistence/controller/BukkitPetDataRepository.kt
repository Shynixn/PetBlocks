package com.github.shynixn.petblocks.bukkit.logic.persistence.controller

import com.github.shynixn.petblocks.api.persistence.controller.ParticleEffectMetaController
import com.github.shynixn.petblocks.api.persistence.controller.PlayerMetaController
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin
import com.github.shynixn.petblocks.bukkit.logic.business.helper.LoggingBridge
import com.github.shynixn.petblocks.bukkit.logic.persistence.entity.BukkitPetData
import com.github.shynixn.petblocks.core.logic.business.entity.DbContext
import com.github.shynixn.petblocks.core.logic.persistence.controller.PetDataRepository
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetData
import org.bukkit.entity.Player
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
class BukkitPetDataRepository(playerMetaController: PlayerMetaController<Player>, particleController: ParticleEffectMetaController, connectionContext: DbContext
) : PetDataRepository<Player>(playerMetaController, particleController, connectionContext, LoggingBridge(PetBlocksPlugin.logger())) {
    /**
     * Creates a new [BukkitPetData] from the given [player] and [name].
     */
    override fun create(player: Any?, name: String?): PetData {
        return BukkitPetData(player as Player, name)
    }

    /**
     * Returns the name of the [player].
     */
    override fun getPlayerName(player: Any?): String {
        return (player as Player).name
    }

    /**
     * Returns the [UUID] of the [player].
     */
    override fun getPlayerUUID(player: Any?): UUID {
        return (player as Player).uniqueId
    }

    /**
     * Creates a new PetData.
     * @return petData
     */
    override fun create(): PetData {
        return BukkitPetData()
    }
}
