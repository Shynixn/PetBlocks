package com.github.shynixn.petblocks.api.persistence.entity;

/**
 * Properties of petblocks meta data.
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
public interface PetMeta extends Persistenceable {
    /**
     * Returns if the petblock is enabled.
     *
     * @return enabled
     */
    boolean isEnabled();

    /**
     * Sets the petblock enabled.
     *
     * @param enabled enabled
     */
    void setEnabled(boolean enabled);

    /**
     * Returns the age in ticks.
     *
     * @return age
     */
    long getAge();

    /**
     * Returns the skin of the pet.
     *
     * @return skin
     */
    String getSkin();

    /**
     * Sets the age in ticks.
     *
     * @param ticks ticks
     */
    void setAge(long ticks);

    /**
     * Returns the data of the engine.
     *
     * @return engine
     */
    EngineContainer getEngine();

    /**
     * Sets the data of the engine.
     *
     * @param engine engine
     */
    void setEngine(EngineContainer engine);

    /**
     * Sets the pet sound enabled.
     *
     * @param enabled enabled
     */
    void setSoundEnabled(boolean enabled);

    /**
     * Returns if the pet-sound is enabled.
     *
     * @return enabled
     */
    boolean isSoundEnabled();

    /**
     * Returns if the itemStack is unbreakable.
     *
     * @return unbreakable
     */
    boolean isItemStackUnbreakable();

    /**
     * Sets the itemStack.
     *
     * @param id          id
     * @param damage      damage
     * @param skin        skin
     * @param unbreakable unbreakable
     */
    void setSkin(int id, int damage, String skin, boolean unbreakable);

    /**
     * Sets the stored display name of the pet which appears above it's head on respawn.
     *
     * @param name name
     */
    void setPetDisplayName(String name);

    /**
     * Returns the stored display name of the pet which appear above it's head on respawn.
     *
     * @return name
     */
    String getPetDisplayName();

    /**
     * Returns the particleEffect meta.
     *
     * @return meta
     */
    ParticleEffectMeta getParticleEffectMeta();

    /**
     * Returns the meta of the owner.
     *
     * @return player
     */
    PlayerMeta getPlayerMeta();

    /**
     * Returns the id of the item.
     *
     * @return itemId
     */
    int getItemId();

    /**
     * Returns the damage of the item.
     *
     * @return itemDamage
     */
    int getItemDamage();

    /**
     * Returns if the item is unbreakable.
     *
     * @return unbreakable
     */
    boolean isItemUnbreakable();

    /**
     * Returns the itemStack for the head.
     *
     * @return headItemStack
     */
    Object getHeadItemStack();
}
