package com.github.shynixn.petblocks.bukkit.logic.business.helper;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.persistence.entity.EngineContainer;
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.bukkit.logic.business.configuration.Config;
import org.bukkit.Material;

/**
 * Created by Shynixn 2017.
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
public class PetBlockModifyHelper {

    /**
     * Sets the engine for the given petMeta and petblock.
     *
     * @param petMeta         petMeta
     * @param petBlock        petblock
     * @param engineContainer engineData
     */
    public static void setEngine(PetMeta petMeta, PetBlock petBlock, EngineContainer engineContainer) {
        if (engineContainer == null)
            return;
        petMeta.setEngine(engineContainer);
        if (Config.getInstance().isCopySkinEnabled()) {
            final GUIItemContainer container = engineContainer.getGUIItem();
            petMeta.setSkin(container.getItemId(), container.getItemDamage(), container.getSkin(), container.isItemUnbreakable());
        }
        if (petBlock != null) {
            petBlock.respawn();
        }
    }

    /**
     * Sets the costume for the given petMeta and petblock.
     *
     * @param petMeta   petMeta
     * @param petBlock  petblock
     * @param container container
     */
    public static void setCostume(PetMeta petMeta, PetBlock petBlock, GUIItemContainer container) {
        if (container == null)
            return;
        petMeta.setSkin(container.getItemId(), container.getItemDamage(), container.getSkin(), container.isItemUnbreakable());
        if (petBlock != null) {
            petBlock.respawn();
        }
    }

    public static void rename(PetMeta petMeta, PetBlock petBlock, String name) {
        petMeta.setPetDisplayName(name);
        if (petBlock != null) {
            petBlock.respawn();
        }
    }

    public static void setParticleEffect(PetMeta petMeta, PetBlock petBlock, GUIItemContainer container) {
        if (container == null)
            return;
        final ParticleEffectMeta transfer = Config.getInstance().getParticleController().getByItem(container);
        petMeta.getParticleEffectMeta().setEffectType(transfer.getEffectType());
        petMeta.getParticleEffectMeta().setSpeed(transfer.getSpeed());
        petMeta.getParticleEffectMeta().setAmount(transfer.getAmount());
        petMeta.getParticleEffectMeta().setOffset(transfer.getOffsetX(), transfer.getOffsetY(), transfer.getOffsetZ());
        petMeta.getParticleEffectMeta().setMaterial(transfer.getMaterial());
        petMeta.getParticleEffectMeta().setData(transfer.getData());
        if (petBlock != null) {
            petBlock.respawn();
        }
    }

    public static void setSkin(PetMeta petMeta, PetBlock petBlock, String skin) {
        String petSkin = skin;
        if (petSkin.contains("textures.minecraft") && !petSkin.contains("http://")) {
            petSkin = "http://" + skin;
        }
        petMeta.setSkin(Material.SKULL_ITEM.getId(), (short) 3, petSkin, false);
        if (petBlock != null) {
            petBlock.respawn();
        }
    }
}
