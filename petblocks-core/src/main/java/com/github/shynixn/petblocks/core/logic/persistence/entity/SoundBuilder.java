package com.github.shynixn.petblocks.core.logic.persistence.entity;

import com.github.shynixn.petblocks.api.persistence.entity.SoundMeta;

import java.util.HashMap;
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
@SuppressWarnings("ALL")
public abstract class SoundBuilder implements SoundMeta {

    protected String text;
    protected float volume;
    protected float pitch;

    /**
     * Initializes a new soundBuilder
     */
    public SoundBuilder() {
        super();
    }

    /**
     * Initializes a new soundBuilder
     *
     * @param text text
     */
    public SoundBuilder(String text) {
        super();
        this.text = text;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.convertSounds();
    }

    /**
     * Initializes a new soundBuilder
     *
     * @param text   text
     * @param volume volume
     * @param pitch  pitch
     */
    public SoundBuilder(String text, double volume, double pitch) {
        super();
        this.text = text;
        this.volume = (float) volume;
        this.pitch = (float) pitch;
        this.convertSounds();
    }

    /**
     * Initializes a new soundBuilder from serialized Content
     *
     * @param items items
     * @throws Exception exception
     */
    public SoundBuilder(Map<String, Object> items) throws Exception {
        super();
        this.text = (String) items.get("name");
        this.volume = (float) (double) items.get("volume");
        this.pitch = (float) (double) items.get("pitch");
        this.convertSounds();
    }

    public abstract  <Location, Player> void apply(Location location, Player[] players);

    /**
     * Returns the name of the sound
     *
     * @return name
     */
    @Override
    public String getName() {
        return this.text;
    }

    /**
     * Sets the name of the sound
     *
     * @param name name
     * @return builder
     */
    @Override
    public SoundBuilder setName(String name) {
        this.text = name;
        return this;
    }

    /**
     * Returns the volume of the sound
     *
     * @return volume
     */
    @Override
    public double getVolume() {
        return this.volume;
    }

    /**
     * Sets the volume of the sound
     *
     * @param volume volume
     * @return builder
     */
    @Override
    public SoundBuilder setVolume(double volume) {
        this.volume = (float) volume;
        return this;
    }

    /**
     * Returns the pitch of the sound
     *
     * @return pitch
     */
    @Override
    public double getPitch() {
        return this.pitch;
    }

    /**
     * Sets the pitch of the sound
     *
     * @param pitch pitch
     * @return builder
     */
    @Override
    public SoundBuilder setPitch(double pitch) {
        this.pitch = (float) pitch;
        return this;
    }

    /**
     * Serializes the builder
     *
     * @return serializedContent
     */
    public Map<String, Object> serialize() {
        final Map<String, Object> items = new HashMap<>();
        items.put("name", this.text);
        items.put("volume", this.volume);
        items.put("pitch", this.pitch);
        return items;
    }

    protected void convertSounds() {
    }

    /**
     * Returns the id of the object
     *
     * @return id
     */
    @Override
    public long getId() {
        return this.hashCode();
    }
}