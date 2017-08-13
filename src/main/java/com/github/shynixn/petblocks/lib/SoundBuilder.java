package com.github.shynixn.petblocks.lib;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.Collection;
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
public class SoundBuilder implements ConfigurationSerializable {

    private String text;
    private float volume;
    private float pitch;

    /**
     * Initializes a new soundBuilder
     */
    public SoundBuilder() {
        super();
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
        this.volume = (float) items.get("volume");
        this.pitch = (float) items.get("pitch");
    }

    /**
     * Plays the sound to all given players at their location
     *
     * @param players players
     * @throws Exception exception
     */
    public void apply(Collection<Player> players) throws Exception {
        this.apply(players.toArray(new Player[players.size()]));
    }

    /**
     * Plays the sound to all given players at their location
     *
     * @param players players
     * @throws Exception exception
     */
    public void apply(Player... players) throws Exception {
        for (final Player player : players) {
            player.playSound(player.getLocation(), Sound.valueOf(this.text), this.pitch, this.volume);
        }
    }

    /**
     * Plays the sound to all players in the world at the given location. Players to far away cannot hear the sound.
     *
     * @param location location
     * @throws Exception exception
     */
    public void apply(Location location) throws Exception {
        for (final Player player : location.getWorld().getPlayers()) {
            player.playSound(location, Sound.valueOf(this.text), this.pitch, this.volume);
        }
    }

    /**
     * Plays the sound to the given players at the given location. Given players to far away cannot hear the sound.
     *
     * @param location location
     * @param players  players
     * @throws Exception exception
     */
    public void apply(Location location, Collection<Player> players) throws Exception {
        this.apply(location, players.toArray(new Player[players.size()]));
    }

    /**
     * Plays the sound to the given players at the given location. Given players to far away cannot hear the sound.
     *
     * @param location location
     * @param players  players
     * @throws Exception exception
     */
    public void apply(Location location, Player... players) throws Exception {
        for (final Player player : players) {
            player.playSound(location, Sound.valueOf(this.text), this.pitch, this.volume);
        }
    }

    /**
     * Returns the name of the sound
     *
     * @return name
     */
    public String getName() {
        return this.text;
    }

    /**
     * Sets the name of the sound
     *
     * @param name name
     * @return builder
     */
    public SoundBuilder setName(String name) {
        this.text = name;
        return this;
    }

    /**
     * Returns the sound and throws exception if the sound does not exist
     *
     * @return sound
     * @throws Exception exception
     */
    public Sound getSound() throws Exception {
        return Sound.valueOf(this.text);
    }

    /**
     * Sets the bukkit sound of the sound
     *
     * @param sound sound
     * @return builder
     */
    public SoundBuilder setSound(Sound sound) {
        this.text = sound.name();
        return this;
    }

    /**
     * Returns the volume of the sound
     *
     * @return volume
     */
    public double getVolume() {
        return this.volume;
    }

    /**
     * Sets the volume of the sound
     *
     * @param volume volume
     * @return builder
     */
    public SoundBuilder setVolume(double volume) {
        this.volume = (float) volume;
        return this;
    }

    /**
     * Returns the pitch of the sound
     *
     * @return pitch
     */
    public double getPitch() {
        return this.pitch;
    }

    /**
     * Sets the pitch of the sound
     *
     * @param pitch pitch
     * @return builder
     */
    public SoundBuilder setPitch(double pitch) {
        this.pitch = (float) pitch;
        return this;
    }

    /**
     * Serializes the builder
     *
     * @return serializedContent
     */
    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> items = new HashMap<>();
        items.put("name", this.text);
        items.put("volume", this.volume);
        items.put("pitch", this.pitch);
        return items;
    }
}
