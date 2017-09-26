package com.github.shynixn.petblocks.bukkit.logic.persistence.entity;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.business.enumeration.RideType;
import com.github.shynixn.petblocks.api.persistence.entity.EngineContainer;
import com.github.shynixn.petblocks.api.persistence.entity.SoundMeta;
import com.github.shynixn.petblocks.bukkit.logic.business.entity.ItemContainer;
import org.bukkit.configuration.MemorySection;

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

    private GUIItemContainer itemContainer;

    private String entity;
    private RideType rideType;

    private SoundMeta ambientSound;
    private SoundMeta walkingSound;

    /**
     * Initializes a new engine data
     *
     * @param id   id
     * @param data data
     * @throws Exception exception
     */
    public EngineData(long id, Map<String, Object> data) throws Exception {
        super();
        this.setId(id);
        this.itemContainer = new ItemContainer((int) id, ((MemorySection) data.get("gui")).getValues(false));
        this.entity = (String) data.get("behaviour.entity");
        this.rideType = RideType.valueOf((String) data.get("behaviour.riding"));
        this.ambientSound = new SoundBuilder((String) data.get("sound.ambient.name"), (double) data.get("sound.ambient.volume"), (double) data.get("sound.ambient.pitch"));
        this.walkingSound = new SoundBuilder((String) data.get("sound.walking.name"), (double) data.get("sound.walking.volume"), (double) data.get("sound.walking.pitch"));
    }

    /**
     * Initializes a new engine data
     *
     * @param id id
     */
    public EngineData(long id) {
        super();
        this.setId(id);
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

    /**
     * Returns the guiItem of the engine
     *
     * @return guiItem
     */
    @Override
    public GUIItemContainer getGUIItem() {
        return this.itemContainer;
    }
}
