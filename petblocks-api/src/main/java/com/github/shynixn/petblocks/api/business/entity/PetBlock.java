package com.github.shynixn.petblocks.api.business.entity;

import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;

/**
 * PetBlock entity which can be perform the defined actions.
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
public interface PetBlock {

    @Deprecated
    void setSkin(String skin);

    @Deprecated
    void setSkin(Object material, byte data);

    @Deprecated
    void setDieing();

    @Deprecated
    void teleportWithOwner(Object location);

    @Deprecated
    boolean isDieing();

    /**
     * Returns the pipeline for managed effect playing.
     *
     * @return effectPipeLine
     */
    EffectPipeline getEffectPipeline();

    /**
     * Returns the meta of the petblock.
     *
     * @return meta
     */
    PetMeta getMeta();

    /**
     * Returns the owner of the petblock.
     *
     * @return player
     */
    Object getPlayer();

    /**
     * Removes the petblock.
     */
    void remove();

    /**
     * Lets the given player ride on the petblock.
     *
     * @param player player
     */
    void ride(Object player);

    /**
     * Lets the given player wear the petblock.
     *
     * @param player player
     */
    void wear(Object player);

    /**
     * Ejects the given player riding from the petblock.
     *
     * @param player player
     */
    void eject(Object player);

    /**
     * Sets the displayName of the petblock.
     *
     * @param name name
     */
    void setDisplayName(String name);

    /**
     * Returns the displayName of the petblock.
     *
     * @return name
     */
    String getDisplayName();

    /**
     * Respawns the petblock
     */
    void respawn();

    /**
     * Returns if the petblock is already removed or dead.
     *
     * @return dead
     */
    boolean isDead();

    /**
     * Returns the armorstand of the petblock.
     *
     * @return armorstand
     */
    Object getArmorStand();

    /**
     * Returns the entity being used as engine.
     *
     * @return entity
     */
    Object getEngineEntity();

    /**
     * Returns the location of the entity.
     *
     * @return position
     */
    Object getLocation();

    /**
     * Damages the petblock the given amount of damage.
     *
     * @param amount amount
     */
    void damage(double amount);

    /**
     * Lets the petblock perform a jump.
     */
    void jump();

    /**
     * Sets the velocity of the petblock.
     *
     * @param vector vector
     */
    void setVelocity(Object vector);

    /**
     * Teleports the the petblock to the given location.
     *
     * @param location location
     */
    void teleport(Object location);
}
