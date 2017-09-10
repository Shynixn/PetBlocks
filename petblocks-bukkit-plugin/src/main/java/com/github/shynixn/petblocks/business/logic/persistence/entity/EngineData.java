package com.github.shynixn.petblocks.business.logic.persistence.entity;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;
import com.github.shynixn.petblocks.api.business.enumeration.RideType;
import com.github.shynixn.petblocks.api.persistence.entity.EngineContainer;
import com.github.shynixn.petblocks.api.persistence.entity.SoundMeta;
import com.github.shynixn.petblocks.business.logic.configuration.CustomItemContainer;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Copyright 2017 Shynixn
 * <p>
 * Do not remove this header!
 * <p>
 * Version 1.0
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2017
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
public class EngineData extends PersistenceObject implements EngineContainer {

    private final int position;
    private final GUIPage page;
    private final GUIItemContainer itemContainer;

    private final String entity;
    private final RideType rideType;

    private final SoundMeta ambientSound;
    private final SoundMeta walkingSound;

    /**
     * Initializes a new engine data
     * @param data data
     * @throws Exception exception
     */
    public EngineData(long id, Map<String, Object> data) throws Exception {
        this.setId(id);
        this.position = (int) data.get("gui.position");
        this.page = GUIPage.getGUIPageFromName((String) data.get("gui.page"));
        this.itemContainer = CustomItemContainer.from((int) data.get("gui.id")
                , (int) data.get("gui.damage")
                , (String) data.get("gui.skin")
                , (String) data.get("gui.name")
                , ((List<String>) data.get("gui.lore")).toArray(new String[0]));

        this.entity = (String) data.get("behaviour.entity");
        this.rideType = RideType.valueOf((String) data.get("behaviour.riding"));
        this.ambientSound = new SoundBuilder((String) data.get("sound.ambient.name"), (double) data.get("sound.ambient.volume"), (double) data.get("sound.ambient.pitch"));
        this.walkingSound = new SoundBuilder((String) data.get("sound.walking.name"), (double) data.get("sound.walking.volume"), (double) data.get("sound.walking.pitch"));
    }

    /**
     * Returns the walking sound
     *
     * @return walkingSound
     */
    @Override
    public SoundMeta getWalkingSound() {
        return this.walkingSound;
    }

    /**
     * Returns the ambient sound
     *
     * @return ambientSound
     */
    @Override
    public SoundMeta getAmbientSound() {
        return this.ambientSound;
    }

    /**
     * Returns the rideType
     *
     * @return rideType
     */
    @Override
    public RideType getRideType() {
        return this.rideType;
    }

    /**
     * Returns the entityType
     *
     * @return entityType
     */
    @Override
    public String getEntityType() {
        return this.entity;
    }
}
