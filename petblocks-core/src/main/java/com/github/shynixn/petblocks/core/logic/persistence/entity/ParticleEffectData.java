package com.github.shynixn.petblocks.core.logic.persistence.entity;

import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;

import java.util.Map;
import java.util.Objects;

/**
 * Copyright 2017 Shynixn
 * <p>
 * Do not remove this header!
 * <p>
 * Version 1.0
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2016
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
public abstract class ParticleEffectData extends PersistenceObject implements ParticleEffectMeta{
    protected String effect;
    private int amount;
    private double speed;
    private double offsetX;
    private double offsetY;
    private double offsetZ;

    protected Integer materialId;
    private Byte data;

    /**
     * Initializes a new ParticleEffectData
     */
    public ParticleEffectData() {
        super();
    }

    /**
     * Initializes a new ParticleEffectData with the given params
     *
     * @param effectName effect
     * @param amount     amount
     * @param speed      speed
     * @param offsetX    x
     * @param offsetY    y
     * @param offsetZ    z
     */
    public ParticleEffectData(String effectName, int amount, double speed, double offsetX, double offsetY, double offsetZ) {
        super();
        if (effectName == null)
            throw new IllegalArgumentException("Effect cannot be null!");
        if (amount < 0)
            throw new IllegalArgumentException("Amount cannot be less than 0");
        if (getParticleEffectFromName(effectName) == null)
            throw new IllegalArgumentException("Cannot find particleEffect for name!");
        this.effect = effectName;
        this.amount = amount;
        this.speed = speed;
        this.offsetX = (float) offsetX;
        this.offsetY = (float) offsetY;
        this.offsetZ = (float) offsetZ;
    }

    /**
     * Parses the potioneffect out of the map
     *
     * @param items items
     * @throws Exception mapParseException
     */
    public ParticleEffectData(Map<String, Object> items) throws Exception {
        super();
        this.effect = (String) items.get("name");
        this.amount = (int) items.get("amount");
        this.speed = (double) items.get("speed");
        if (items.containsKey("offx"))
            this.offsetX = (float) (double) items.get("offx");
        if (items.containsKey("offy"))
            this.offsetY = (float) (double) items.get("offy");
        if (items.containsKey("offz"))
            this.offsetZ = ((float) (double) items.get("offz"));
        if (items.containsKey("id"))
            this.materialId = (Integer) items.get("id");
        if (items.containsKey("damage"))
            this.data = (byte) (int) (Integer) items.get("damage");
        if (items.containsKey("red"))
            this.setRed((Integer) items.get("red"));
        if (items.containsKey("green"))
            this.setGreen((Integer) items.get("green"));
        if (items.containsKey("blue"))
            this.setBlue((Integer) items.get("blue"));
    }

    public abstract <Location, Player> void applyTo(Location location, Player... players);

    /**
     * Sets the RGB colors of the particleEffect
     *
     * @param red   red
     * @param green green
     * @param blue  blue
     * @return builder
     */
    @Override
    public ParticleEffectData setColor(int red, int green, int blue) {
        this.setRed(red);
        this.setBlue(blue);
        this.setGreen(green);
        return this;
    }

    /**
     * Sets the color of the particleEffect
     *
     * @param particleColor particleColor
     * @return builder
     */
    @Override
    public ParticleEffectData setColor(ParticleColor particleColor) {
        if (particleColor == null)
            throw new IllegalArgumentException("Color cannot be null!");
        this.setColor(particleColor.getRed(), particleColor.getGreen(), particleColor.getBlue());
        return this;
    }

    /**
     * Sets the color for note particleEffect
     *
     * @param color color
     * @return builder
     */
    @Override
    public ParticleEffectData setNoteColor(int color) {
        if (color > 20 || color < 0) {
            this.offsetX = 5;
        } else {
            this.offsetX = color;
        }
        return this;
    }

    /**
     * Sets the amount of particles of the particleEffect
     *
     * @param amount amount
     * @return builder
     */
    @Override
    public ParticleEffectData setAmount(int amount) {
        if (amount < 0)
            throw new IllegalArgumentException("Amount cannot be less than 0");
        this.amount = amount;
        return this;
    }

    /**
     * Sets the speed of the particleEffect
     *
     * @param speed speed
     * @return builder
     */
    @Override
    public ParticleEffectData setSpeed(double speed) {
        this.speed = speed;
        return this;
    }

    /**
     * Sets the offsetX of the particleEffect
     *
     * @param offsetX offsetX
     * @return builder
     */
    @Override
    public ParticleEffectData setOffsetX(double offsetX) {
        this.offsetX = offsetX;
        return this;
    }

    /**
     * Sets the offsetY of the particleEffect
     *
     * @param offsetY offsetY
     * @return builder
     */
    @Override
    public ParticleEffectData setOffsetY(double offsetY) {
        this.offsetY = offsetY;
        return this;
    }

    /**
     * Sets the offsetZ of the particleEffect
     *
     * @param offsetZ offsetZ
     * @return builder
     */
    @Override
    public ParticleEffectData setOffsetZ(double offsetZ) {
        this.offsetZ = offsetZ;
        return this;
    }

    /**
     * Sets the offset of the particleEffect
     *
     * @param offsetX offsetX
     * @param offsetY offsetY
     * @param offsetZ offsetZ
     * @return instance
     */
    @Override
    public ParticleEffectData setOffset(double offsetX, double offsetY, double offsetZ) {
        this.setOffsetX(offsetX);
        this.setOffsetY(offsetY);
        this.setOffsetZ(offsetZ);
        return this;
    }

    /**
     * Sets the effectType of the particleEffect
     *
     * @param name name
     * @return builder
     */
    @Override
    public ParticleEffectData setEffectName(String name) {
        if (name == null)
            throw new IllegalArgumentException("Name cannot be null!");
        this.effect = name;
        return this;
    }

    /**
     * Sets the effectType of the particlEffect
     *
     * @param type type
     * @return builder
     */
    @Override
    public ParticleEffectData setEffectType(ParticleEffectType type) {
        if (type == null)
            throw new IllegalArgumentException("Type cannot be null!");
        this.effect = type.getSimpleName();
        return this;
    }

    /**
     * Sets the blue of the RGB color
     *
     * @param blue blue
     * @return builder
     */
    @Override
    public ParticleEffectData setBlue(int blue) {
        this.offsetZ = blue / 255.0F;
        return this;
    }

    /**
     * Sets the red of the RGB color
     *
     * @param red red
     * @return builder
     */
    @Override
    public ParticleEffectData setRed(int red) {
        this.offsetX = red / 255.0F;
        if (red == 0) {
            this.offsetX = Float.MIN_NORMAL;
        }
        return this;
    }

    /**
     * Sets the green of the RGB color
     *
     * @param green green
     * @return builder
     */
    @Override
    public ParticleEffectData setGreen(int green) {
        this.offsetY = green / 255.0F;
        return this;
    }

    /**
     * Sets the data of the material of the particleEffect
     *
     * @param data data
     * @return builder
     */
    @Override
    public ParticleEffectData setData(Byte data) {
        this.data = data;
        return this;
    }

    /**
     * Returns the effect of the particleEffect
     *
     * @return effectName
     */
    @Override
    public String getEffectName() {
        return this.effect;
    }

    /**
     * Returns the particleEffectType of the particleEffect
     *
     * @return effectType
     */
    @Override
    public ParticleEffectType getEffectType() {
        return getParticleEffectFromName(this.effect);
    }

    /**
     * Returns the amount of particles of the particleEffect
     *
     * @return amount
     */
    @Override
    public int getAmount() {
        return this.amount;
    }

    /**
     * Returns the speed of the particleEffect
     *
     * @return speed
     */
    @Override
    public double getSpeed() {
        return this.speed;
    }

    /**
     * Returns the offsetX of the particleEffect
     *
     * @return offsetX
     */
    @Override
    public double getOffsetX() {
        return this.offsetX;
    }

    /**
     * Returns the offsetY of the particleEffect
     *
     * @return offsetY
     */
    @Override
    public double getOffsetY() {
        return this.offsetY;
    }

    /**
     * Returns the offsetZ of the particleEffect
     *
     * @return offsetZ
     */
    @Override
    public double getOffsetZ() {
        return this.offsetZ;
    }

    /**
     * Returns the RGB color blue of the particleEffect
     *
     * @return blue
     */
    @Override
    public int getBlue() {
        return (int) (this.offsetZ * 255.0);
    }

    /**
     * Returns the RGB color red of the particleEffect
     *
     * @return red
     */
    @Override
    public int getRed() {
        return (int) (this.offsetX * 255.0);
    }

    /**
     * Returns the RGB color green of the particleEffect
     *
     * @return green
     */
    @Override
    public int getGreen() {
        return (int) (this.offsetY * 255.0);
    }

    /**
     * Returns the data of the particleEffect
     *
     * @return data
     */
    @Override
    public Byte getData() {
        return this.data;
    }

    /**
     * Returns if the particleEffect is a color particleEffect
     *
     * @return isColor
     */
    @Override
    public boolean isColorParticleEffect() {
        return this.effect.equalsIgnoreCase(ParticleEffectType.SPELL_MOB.getSimpleName())
                || this.effect.equalsIgnoreCase(ParticleEffectType.SPELL_MOB_AMBIENT.getSimpleName())
                || this.effect.equalsIgnoreCase(ParticleEffectType.REDSTONE.getSimpleName())
                || this.effect.equalsIgnoreCase(ParticleEffectType.NOTE.getSimpleName());
    }

    /**
     * Returns if the particleEffect is a note particleEffect
     *
     * @return isNote
     */
    @Override
    public boolean isNoteParticleEffect() {
        return this.effect.equalsIgnoreCase(ParticleEffectType.NOTE.getSimpleName());
    }

    /**
     * Returns if the particleEffect is a materialParticleEffect
     *
     * @return isMaterial
     */
    @Override
    public boolean isMaterialParticleEffect() {
        return this.effect.equalsIgnoreCase(ParticleEffectType.BLOCK_CRACK.getSimpleName())
                || this.effect.equalsIgnoreCase(ParticleEffectType.BLOCK_DUST.getSimpleName())
                || this.effect.equalsIgnoreCase(ParticleEffectType.ITEM_CRACK.getSimpleName());
    }

    /**
     * Checks if 2 builders are equal
     *
     * @param o secondBuilder
     * @return isSame
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final ParticleEffectData that = (ParticleEffectData) o;
        return this.amount == that.amount
                && Double.compare(that.speed, this.speed) == 0
                && Double.compare(that.offsetX, this.offsetX) == 0
                && Double.compare(that.offsetY, this.offsetY) == 0
                && Double.compare(that.offsetZ, this.offsetZ) == 0
                & (this.effect != null ? this.effect.equals(that.effect) : that.effect == null)
                && Objects.equals(this.materialId, that.materialId) && (this.data != null ? this.data.equals(that.data) : that.data == null);
    }

    /**
     * Displays the builder as string
     *
     * @return string
     */
    @Override
    public String toString() {
        return "effect {" + "name " + this.effect + " amound " + this.amount + " speed " + this.speed + '}';
    }



    /**
     * Returns a text of all particleEffects to let the user easily view them
     *
     * @return potionEffects
     */
    public static String getParticlesText() {
        final StringBuilder builder = new StringBuilder();
        for (final ParticleEffectType particleEffect : ParticleEffectType.values()) {
            if (builder.length() != 0) {
                builder.append(", ");
            }
            builder.append(particleEffect.getSimpleName());
        }
        return builder.toString();
    }

    /**
     * Returns the particleEffectType from name
     *
     * @param name name
     * @return particleEffectType
     */
    public static ParticleEffectType getParticleEffectFromName(String name) {
        for (final ParticleEffectType particleEffect : ParticleEffectType.values()) {
            if (name != null && particleEffect.getSimpleName().equalsIgnoreCase(name))
                return particleEffect;
        }
        return null;
    }
}