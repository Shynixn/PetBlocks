package com.github.shynixn.petblocks.lib.util;

import org.bukkit.*;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.LinkedHashMap;
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
public class ParticleEffectBuilder implements ConfigurationSerializable {
    private String effect;
    private int amount;
    private double speed;
    private double x;
    private double y;
    private double z;

    private Integer material;
    private Byte data;

    /**
     * Initializes a new ParticleEffectBuilder
     */
    public ParticleEffectBuilder() {
        super();
    }

    /**
     * Initializes a new ParticleEffectBuilder with the given params
     *
     * @param effectName effect
     * @param amount     amount
     * @param speed      speed
     * @param x          x
     * @param y          y
     * @param z          u
     */
    public ParticleEffectBuilder(String effectName, int amount, double speed, double x, double y, double z) {
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
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Parses the potioneffect out of the map
     *
     * @param items items
     * @throws Exception mapParseException
     */
    public ParticleEffectBuilder(Map<String, Object> items) throws Exception {
        super();
        this.effect = (String) items.get("effect");
        this.amount = (int) items.get("amount");
        this.speed = (double) items.get("speed");
        this.x = (double) items.get("size.x");
        this.y = (double) items.get("size.y");
        this.z = (double) items.get("size.z");
        if (items.containsKey("block.material"))
            this.material = (Integer) items.get("block.material");
        if (items.containsKey("block.damage"))
            this.data = (Byte) items.get("block.damage");
    }

    /**
     * Sets the RGB colors of the particleEffect
     *
     * @param red   red
     * @param green green
     * @param blue  blue
     * @return builder
     */
    public ParticleEffectBuilder setColor(int red, int green, int blue) {
        this.setRed(red);
        this.setBlue(blue);
        this.setGreen(green);
        this.setAmount(0);
        return this;
    }

    /**
     * Sets the color of the particleEffect
     *
     * @param particleColor particleColor
     * @return builder
     */
    public ParticleEffectBuilder setColor(ParticleColor particleColor) {
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
    public ParticleEffectBuilder setNoteColor(int color) {
        if (color > 20 || color < 0)
            this.x = 5;
        else
            this.x = color;
        return this;
    }

    /**
     * Sets the amount of particles of the particleEffect
     *
     * @param amount amount
     * @return builder
     */
    public ParticleEffectBuilder setAmount(int amount) {
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
    public ParticleEffectBuilder setSpeed(double speed) {
        this.speed = speed;
        return this;
    }

    /**
     * Sets the x coordinate of the particleEffect
     *
     * @param x x
     * @return builder
     */
    public ParticleEffectBuilder setX(double x) {
        this.x = x;
        return this;
    }

    /**
     * Sets the y coordinate of the particleEffect
     *
     * @param y y
     * @return builder
     */
    public ParticleEffectBuilder setY(double y) {
        this.y = y;
        return this;
    }

    /**
     * Sets the z coordinate of the particleEffect
     *
     * @param z z
     * @return builder
     */
    public ParticleEffectBuilder setZ(double z) {
        this.z = z;
        return this;
    }

    /**
     * Sets the effectType of the particleEffect
     *
     * @param name name
     * @return builder
     */
    public ParticleEffectBuilder setEffectName(String name) {
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
    public ParticleEffectBuilder setEffectType(ParticleEffectType type) {
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
    public ParticleEffectBuilder setBlue(int blue) {
        this.z = (double) blue / 255.0;
        return this;
    }

    /**
     * Sets the red of the RGB color
     *
     * @param red red
     * @return builder
     */
    public ParticleEffectBuilder setRed(int red) {
        this.x = (double) red / 255.0;
        if (red == 0)
            this.x = Float.MIN_NORMAL;
        return this;
    }

    /**
     * Sets the green of the RGB color
     *
     * @param green green
     * @return builder
     */
    public ParticleEffectBuilder setGreen(int green) {
        this.y = green / 255.0;
        return this;
    }

    /**
     * Sets the material of the particleEffect
     *
     * @param material material
     * @return builder
     */
    public ParticleEffectBuilder setMaterial(Material material) {
        if (material == null)
            throw new IllegalArgumentException("Material cannot be null!");
        this.material = material.getId();
        return this;
    }

    /**
     * Sets the data of the material of the particleEffect
     *
     * @param data data
     * @return builder
     */
    public ParticleEffectBuilder setData(Byte data) {
        this.data = data;
        return this;
    }

    /**
     * Returns the effect of the particleEffect
     *
     * @return effectName
     */
    public String getEffectName() {
        return this.effect;
    }

    /**
     * Returns the particleEffectType of the particleEffect
     *
     * @return effectType
     */
    public ParticleEffectType getEffectType() {
        return getParticleEffectFromName(this.effect);
    }

    /**
     * Returns the amount of particles of the particleEffect
     *
     * @return amount
     */
    public int getAmount() {
        return this.amount;
    }

    /**
     * Returns the speed of the particleEffect
     *
     * @return speed
     */
    public double getSpeed() {
        return this.speed;
    }

    /**
     * Returns the x coordinate of the particleEffect
     *
     * @return x
     */
    public double getX() {
        return this.x;
    }

    /**
     * Returns the y coordinate of the particleEffect
     *
     * @return y
     */
    public double getY() {
        return this.y;
    }

    /**
     * Returns the z coordinate of the particleEffect
     *
     * @return z
     */
    public double getZ() {
        return this.z;
    }

    /**
     * Returns the RGB color blue of the particleEffect
     *
     * @return blue
     */
    public int getBlue() {
        return (int) this.z * 255;
    }

    /**
     * Returns the RGB color red of the particleEffect
     *
     * @return red
     */
    public int getRed() {
        return (int) this.x * 255;
    }

    /**
     * Returns the RGB color green of the particleEffect
     *
     * @return green
     */
    public int getGreen() {
        return (int) this.y * 255;
    }

    /**
     * Returns the material of the particleEffect
     *
     * @return material
     */
    public Material getMaterial() {
        if (this.material == null || Material.getMaterial(this.material) == null)
            return null;
        return Material.getMaterial(this.material);
    }

    /**
     * Returns the data of the particleEffect
     *
     * @return data
     */
    public Byte getData() {
        return this.data;
    }

    /**
     * Copies the current builder
     *
     * @return copyOfBuilder
     */
    public ParticleEffectBuilder copy() {
        final ParticleEffectBuilder particle = new ParticleEffectBuilder();
        particle.effect = this.effect;
        particle.amount = this.amount;
        particle.x = this.x;
        particle.y = this.y;
        particle.z = this.z;
        particle.speed = this.speed;
        particle.material = this.material;
        particle.data = this.data;
        return particle;
    }

    /**
     * Converts the effect to a bukkitParticle
     *
     * @param clazz Clazz to be given for compatibility
     * @param <T>   Particle
     * @return bukkitParticle
     */
    public <T extends Enum<T>> T toParticle(Class<?> clazz) {
        if(clazz == null)
            throw new IllegalArgumentException("Class cannot be null!");
        for (final Object item : clazz.getEnumConstants()) {
            final Enum<T> eItem = (Enum<T>) item;
            if (eItem.name().equalsIgnoreCase(this.effect))
                return (T) eItem;
        }
        return null;
    }

    /**
     * Returns if the particleEffect is a color particleEffect
     *
     * @return isColor
     */
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
    public boolean isNoteParticleEffect() {
        return this.effect.equalsIgnoreCase(ParticleEffectType.NOTE.getSimpleName());
    }

    /**
     * Returns if the particleEffect is a materialParticleEffect
     *
     * @return isMaterial
     */
    public boolean isMaterialParticleEffect() {
        return this.effect.equalsIgnoreCase(ParticleEffectType.BLOCK_CRACK.getSimpleName())
                || this.effect.equalsIgnoreCase(ParticleEffectType.BLOCK_DUST.getSimpleName())
                || this.effect.equalsIgnoreCase(ParticleEffectType.ITEM_CRACK.getSimpleName());
    }

    /**
     * Plays the effect at the given location to the given players.
     *
     * @param location location
     * @param players  players
     */
    public void apply(Location location, Player... players) {
        if(location == null)
            throw new IllegalArgumentException("Location cannot be null!");
        if (players.length == 0)
            ParticleUtils.sendParticle(this, location);
        else
            ParticleUtils.sendParticle(this, location, players);
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
        final ParticleEffectBuilder that = (ParticleEffectBuilder) o;
        return this.amount == that.amount
                && Double.compare(that.speed, this.speed) == 0
                && Double.compare(that.x, this.x) == 0
                && Double.compare(that.y, this.y) == 0
                && Double.compare(that.z, this.z) == 0
                & (this.effect != null ? this.effect.equals(that.effect) : that.effect == null)
                && this.material == that.material && (this.data != null ? this.data.equals(that.data) : that.data == null);
    }

    /**
     * Displays the builder as string
     *
     * @return string
     */
    @Override
    public String toString() {
        return "effect {" + "name " + this.effect + " amound " + this.amount + " speed " + this.speed + "}";
    }

    /**
     * Serializes the particleEffect data to be stored to the filesystem
     *
     * @return serializedContent
     */
    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("effect", this.effect.toUpperCase());
        map.put("amount", this.amount);
        map.put("speed", this.speed);
        final Map<String, Object> tmp3 = new LinkedHashMap<>();
        tmp3.put("x", this.x);
        tmp3.put("y", this.y);
        tmp3.put("z", this.z);
        map.put("size", tmp3);
        final Map<String, Object> tmp2 = new LinkedHashMap<>();
        if (this.material != null)
            tmp2.put("material", this.material);
        else
            tmp2.put("material", null);
        tmp2.put("damage", this.data);
        map.put("block", tmp2);
        return map;
    }

    /**
     * Returns a text of all particleEffects to let the user easily view them
     *
     * @return potionEffects
     */
    public static String getParticlesText() {
        String s = "";
        for (final ParticleEffectType particleEffect : ParticleEffectType.values()) {
            if (s.isEmpty())
                s += particleEffect.getSimpleName();
            else
                s += ", " + particleEffect.getSimpleName();
        }
        return s;
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

    /**
     * ParticleColors
     */
    public enum ParticleColor {
        BLACK(0, 0, 0),
        DARK_BLUE(0, 0, 170),
        DARK_GREEN(0, 170, 0),
        DARK_AQUA(0, 170, 170),
        DARK_RED(170, 0, 0),
        DARK_PURPLE(170, 0, 170),
        GOLD(255, 170, 0),
        GRAY(170, 170, 170),
        DARK_GRAY(85, 85, 85),
        BLUE(85, 85, 255),
        GREEN(85, 255, 85),
        AQUA(85, 255, 255),
        RED(255, 85, 85),
        LIGHT_PURPLE(255, 85, 255),
        YELLOW(255, 255, 85),
        WHITE(255, 255, 255);

        private final int red;
        private final int green;
        private final int blue;

        /**
         * Initializes a new particleColor
         *
         * @param red   red
         * @param green green
         * @param blue  blue
         */
        ParticleColor(int red, int green, int blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        /**
         * Returns the RGB value red
         *
         * @return red
         */
        public int getRed() {
            return this.red;
        }

        /**
         * Returns the RGB value green
         *
         * @return green
         */
        public int getGreen() {
            return this.green;
        }

        /**
         * Returns the RGB value blue
         *
         * @return blue
         */
        public int getBlue() {
            return this.blue;
        }
    }

    /**
     * ParticleEffectTypes
     */
    public enum ParticleEffectType {
        EXPLOSION_NORMAL("explode", 0),
        EXPLOSION_LARGE("largeexplode", 1),
        EXPLOSION_HUGE("hugeexplosion", 2),
        FIREWORKS_SPARK("fireworksSpark", 3),
        WATER_BUBBLE("bubble", 4),
        WATER_SPLASH("splash", 5),
        WATER_WAKE("wake", 6),
        SUSPENDED("suspended", 7),
        SUSPENDED_DEPTH("depthsuspend", 8),
        CRIT("crit", 9),
        CRIT_MAGIC("magicCrit", 10),
        SMOKE_NORMAL("smoke", 11),
        SMOKE_LARGE("largesmoke", 12),
        SPELL("spell", 13),
        SPELL_INSTANT("instantSpell", 14),
        SPELL_MOB("mobSpell", 15),
        SPELL_MOB_AMBIENT("mobSpellAmbient", 16),
        SPELL_WITCH("witchMagic", 17),
        DRIP_WATER("dripWater", 18),
        DRIP_LAVA("dripLava", 19),
        VILLAGER_ANGRY("angryVillager", 20),
        VILLAGER_HAPPY("happyVillager", 21),
        TOWN_AURA("townaura", 22),
        NOTE("note", 23),
        PORTAL("portal", 24),
        ENCHANTMENT_TABLE("enchantmenttable", 25),
        FLAME("flame", 26),
        LAVA("lava", 27),
        FOOTSTEP("footstep", 28),
        CLOUD("cloud", 29),
        REDSTONE("reddust", 30),
        SNOWBALL("snowballpoof", 31),
        SNOW_SHOVEL("snowshovel", 32),
        SLIME("slime", 33),
        HEART("heart", 34),
        BARRIER("barrier", 35),
        ITEM_CRACK("iconcrack", 36),
        BLOCK_CRACK("blockcrack", 37),
        BLOCK_DUST("blockdust", 38),
        WATER_DROP("droplet", 39),
        ITEM_TAKE("take", 40),
        MOB_APPEARANCE("mobappearance", 41),
        DRAGON_BREATH("dragonbreath", 42),
        END_ROD("endRod", 43),
        DAMAGE_INDICATOR("damageIndicator", 44),
        SWEEP_ATTACK("sweepAttack", 45),
        FALLING_DUST("fallingdust", 46),
        TOTEM("totem", 47),
        SPIT("spit", 48);

        private final String simpleName;
        private final int id;

        /**
         * Initializes a new particleEffectType
         *
         * @param name name
         * @param id   id
         */
        ParticleEffectType(String name, int id) {
            this.simpleName = name;
            this.id = id;
        }

        /**
         * Returns the id of the particleEffectType
         *
         * @return id
         */
        public int getId() {
            return this.id;
        }

        /**
         * Returns the name of the particleEffectType
         *
         * @return name
         */
        public String getSimpleName() {
            return this.simpleName;
        }
    }

    /**
     * Particle Utils
     */
    private static class ParticleUtils {

        /**
         * Sends a particleEffect
         *
         * @param builder  builder
         * @param location location
         * @return hasBeenSend
         */
        static boolean sendParticle(ParticleEffectBuilder builder, Location location) {
            return sendParticle(builder, location, location.getWorld().getPlayers().toArray(new Player[location.getWorld().getPlayers().size()]));
        }

        /**
         * Sends a particleEffect
         *
         * @param builder  builder
         * @param location location
         * @param players  players
         * @return hasBeenSend
         */
        static boolean sendParticle(ParticleEffectBuilder builder, Location location, Player[] players) {
            switch (getServerVersion()) {
                case "v1_11_R1":
                    ParticleUtils11R1.sendParticle(builder, location, players);
                    break;
                case "v1_10_R1":
                    ParticleUtils10R1.sendParticle(builder, location, players);
                    break;
                case "v1_9_R2":
                    ParticleUtils9R2.sendParticle(builder, location, players);
                    break;
                case "v1_9_R1":
                    ParticleUtils9R1.sendParticle(builder, location, players);
                    break;
                case "v1_8_R3":
                    ParticleUtils8R3.sendParticle(builder, location, players);
                    break;
                case "v1_8_R2":
                    ParticleUtils8R2.sendParticle(builder, location, players);
                    break;
                case "v1_8_R1":
                    ParticleUtils8R1.sendParticle(builder, location, players);
                    break;
                default:
                    return false;
            }
            return true;
        }

        /**
         * Checks if longDistance attribute is necessary
         *
         * @param location location
         * @param players  players
         * @return isNecessary
         */
        private static boolean isLongDistance(Location location, List<Player> players) {
            for (final Player player : players) {
                if (location.getWorld().getName().equals(player.getLocation().getWorld().getName())
                        && player.getLocation().distanceSquared(location) > 65536) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns the server version.
         *
         * @return version
         */
        private static String getServerVersion() {
            return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        }

        static class ParticleUtils8R1 {
            /**
             * Sends a particleEffect for v1_8_R1
             *
             * @param particleBuilder particleBuilder
             * @param location        location
             * @param players         players
             */
            static void sendParticle(ParticleEffectBuilder particleBuilder, Location location, Player[] players) {
                final net.minecraft.server.v1_8_R1.EnumParticle particle = net.minecraft.server.v1_8_R1.EnumParticle.valueOf(particleBuilder.getEffectType().name().toUpperCase());
                int[] additionalInfo = null;
                if (particleBuilder.getMaterial() != null) {
                    if (particleBuilder.getEffectType() == ParticleEffectBuilder.ParticleEffectType.ITEM_CRACK)
                        additionalInfo = new int[]{particleBuilder.getMaterial().getId(), particleBuilder.getData()};
                    else
                        additionalInfo = new int[]{particleBuilder.getMaterial().getId(), particleBuilder.getData() << 12};
                }
                final net.minecraft.server.v1_8_R1.PacketPlayOutWorldParticles packet = new net.minecraft.server.v1_8_R1.PacketPlayOutWorldParticles(
                        particle,
                        isLongDistance(location, Arrays.asList(players)),
                        (float) location.getX(),
                        (float) location.getY(),
                        (float) location.getZ(),
                        (float) particleBuilder.getX(),
                        (float) particleBuilder.getY(),
                        (float) particleBuilder.getZ(),
                        (float) particleBuilder.getSpeed(),
                        particleBuilder.getAmount(),
                        additionalInfo);
                for (final Player player : players) {
                    ((org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                }
            }
        }

        static class ParticleUtils8R2 {
            /**
             * Sends a particleEffect for v1_8_R2
             *
             * @param particleBuilder particleBuilder
             * @param location        location
             * @param players         players
             */
            static void sendParticle(ParticleEffectBuilder particleBuilder, Location location, Player[] players) {
                final net.minecraft.server.v1_8_R2.EnumParticle particle = net.minecraft.server.v1_8_R2.EnumParticle.valueOf(particleBuilder.getEffectType().name().toUpperCase());
                int[] additionalInfo = null;
                if (particleBuilder.getMaterial() != null) {
                    if (particleBuilder.getEffectType() == ParticleEffectBuilder.ParticleEffectType.ITEM_CRACK)
                        additionalInfo = new int[]{particleBuilder.getMaterial().getId(), particleBuilder.getData()};
                    else
                        additionalInfo = new int[]{particleBuilder.getMaterial().getId(), particleBuilder.getData() << 12};
                }
                final net.minecraft.server.v1_8_R2.PacketPlayOutWorldParticles packet = new net.minecraft.server.v1_8_R2.PacketPlayOutWorldParticles(
                        particle,
                        isLongDistance(location, Arrays.asList(players)),
                        (float) location.getX(),
                        (float) location.getY(),
                        (float) location.getZ(),
                        (float) particleBuilder.getX(),
                        (float) particleBuilder.getY(),
                        (float) particleBuilder.getZ(),
                        (float) particleBuilder.getSpeed(),
                        particleBuilder.getAmount(),
                        additionalInfo);
                for (final Player player : players) {
                    ((org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                }
            }
        }

        static class ParticleUtils8R3 {
            /**
             * Sends a particleEffect for v1_8_R3
             *
             * @param particleBuilder particleBuilder
             * @param location        location
             * @param players         players
             */
            static void sendParticle(ParticleEffectBuilder particleBuilder, Location location, Player[] players) {
                final net.minecraft.server.v1_8_R3.EnumParticle particle = net.minecraft.server.v1_8_R3.EnumParticle.valueOf(particleBuilder.getEffectType().name().toUpperCase());
                int[] additionalInfo = null;
                if (particleBuilder.getMaterial() != null) {
                    if (particleBuilder.getEffectType() == ParticleEffectBuilder.ParticleEffectType.ITEM_CRACK)
                        additionalInfo = new int[]{particleBuilder.getMaterial().getId(), particleBuilder.getData()};
                    else
                        additionalInfo = new int[]{particleBuilder.getMaterial().getId(), particleBuilder.getData() << 12};
                }
                final net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles packet = new net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles(
                        particle,
                        isLongDistance(location, Arrays.asList(players)),
                        (float) location.getX(),
                        (float) location.getY(),
                        (float) location.getZ(),
                        (float) particleBuilder.getX(),
                        (float) particleBuilder.getY(),
                        (float) particleBuilder.getZ(),
                        (float) particleBuilder.getSpeed(),
                        particleBuilder.getAmount(),
                        additionalInfo);
                for (final Player player : players) {
                    ((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                }
            }
        }

        static class ParticleUtils9R1 {
            /**
             * Sends a particleEffect for v1_9_R1
             *
             * @param particleBuilder particleBuilder
             * @param location        location
             * @param players         players
             */
            static void sendParticle(ParticleEffectBuilder particleBuilder, Location location, Player[] players) {
                final net.minecraft.server.v1_9_R1.EnumParticle particle = net.minecraft.server.v1_9_R1.EnumParticle.valueOf(particleBuilder.getEffectType().name().toUpperCase());
                int[] additionalInfo = null;
                if (particleBuilder.getMaterial() != null) {
                    if (particleBuilder.getEffectType() == ParticleEffectBuilder.ParticleEffectType.ITEM_CRACK)
                        additionalInfo = new int[]{particleBuilder.getMaterial().getId(), particleBuilder.getData()};
                    else
                        additionalInfo = new int[]{particleBuilder.getMaterial().getId(), particleBuilder.getData() << 12};
                }
                final net.minecraft.server.v1_9_R1.PacketPlayOutWorldParticles packet = new net.minecraft.server.v1_9_R1.PacketPlayOutWorldParticles(
                        particle,
                        isLongDistance(location, Arrays.asList(players)),
                        (float) location.getX(),
                        (float) location.getY(),
                        (float) location.getZ(),
                        (float) particleBuilder.getX(),
                        (float) particleBuilder.getY(),
                        (float) particleBuilder.getZ(),
                        (float) particleBuilder.getSpeed(),
                        particleBuilder.getAmount(),
                        additionalInfo);
                for (final Player player : players) {
                    ((org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                }
            }
        }

        static class ParticleUtils9R2 {
            /**
             * Sends a particleEffect for v1_9_R2
             *
             * @param particleBuilder particleBuilder
             * @param location        location
             * @param players         players
             */
            static void sendParticle(ParticleEffectBuilder particleBuilder, Location location, Player[] players) {
                final net.minecraft.server.v1_9_R2.EnumParticle particle = net.minecraft.server.v1_9_R2.EnumParticle.valueOf(particleBuilder.getEffectType().name().toUpperCase());
                int[] additionalInfo = null;
                if (particleBuilder.getMaterial() != null) {
                    if (particleBuilder.getEffectType() == ParticleEffectBuilder.ParticleEffectType.ITEM_CRACK)
                        additionalInfo = new int[]{particleBuilder.getMaterial().getId(), particleBuilder.getData()};
                    else
                        additionalInfo = new int[]{particleBuilder.getMaterial().getId(), particleBuilder.getData() << 12};
                }
                final net.minecraft.server.v1_9_R2.PacketPlayOutWorldParticles packet = new net.minecraft.server.v1_9_R2.PacketPlayOutWorldParticles(
                        particle,
                        isLongDistance(location, Arrays.asList(players)),
                        (float) location.getX(),
                        (float) location.getY(),
                        (float) location.getZ(),
                        (float) particleBuilder.getX(),
                        (float) particleBuilder.getY(),
                        (float) particleBuilder.getZ(),
                        (float) particleBuilder.getSpeed(),
                        particleBuilder.getAmount(),
                        additionalInfo);
                for (final Player player : players) {
                    ((org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                }
            }
        }

        static class ParticleUtils10R1 {
            /**
             * Sends a particleEffect for v1_11_R1
             *
             * @param particleBuilder particleBuilder
             * @param location        location
             * @param players         players
             */
            static void sendParticle(ParticleEffectBuilder particleBuilder, Location location, Player[] players) {
                final net.minecraft.server.v1_10_R1.EnumParticle particle = net.minecraft.server.v1_10_R1.EnumParticle.valueOf(particleBuilder.getEffectType().name().toUpperCase());
                int[] additionalInfo = null;
                if (particleBuilder.getMaterial() != null) {
                    if (particleBuilder.getEffectType() == ParticleEffectBuilder.ParticleEffectType.ITEM_CRACK)
                        additionalInfo = new int[]{particleBuilder.getMaterial().getId(), particleBuilder.getData()};
                    else
                        additionalInfo = new int[]{particleBuilder.getMaterial().getId(), particleBuilder.getData() << 12};
                }
                final net.minecraft.server.v1_10_R1.PacketPlayOutWorldParticles packet = new net.minecraft.server.v1_10_R1.PacketPlayOutWorldParticles(
                        particle,
                        isLongDistance(location, Arrays.asList(players)),
                        (float) location.getX(),
                        (float) location.getY(),
                        (float) location.getZ(),
                        (float) particleBuilder.getX(),
                        (float) particleBuilder.getY(),
                        (float) particleBuilder.getZ(),
                        (float) particleBuilder.getSpeed(),
                        particleBuilder.getAmount(),
                        additionalInfo);
                for (final Player player : players) {
                    ((org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                }
            }
        }

        static class ParticleUtils11R1 {
            /**
             * Sends a particleEffect for v1_11_R1
             *
             * @param particleBuilder particleBuilder
             * @param location        location
             * @param players         players
             */
            static void sendParticle(ParticleEffectBuilder particleBuilder, Location location, Player[] players) {
                final net.minecraft.server.v1_11_R1.EnumParticle particle = net.minecraft.server.v1_11_R1.EnumParticle.valueOf(particleBuilder.getEffectType().name().toUpperCase());
                int[] additionalInfo = null;
                if (particleBuilder.getMaterial() != null) {
                    if (particleBuilder.getEffectType() == ParticleEffectBuilder.ParticleEffectType.ITEM_CRACK)
                        additionalInfo = new int[]{particleBuilder.getMaterial().getId(), particleBuilder.getData()};
                    else
                        additionalInfo = new int[]{particleBuilder.getMaterial().getId(), particleBuilder.getData() << 12};
                }
                final net.minecraft.server.v1_11_R1.PacketPlayOutWorldParticles packet = new net.minecraft.server.v1_11_R1.PacketPlayOutWorldParticles(
                        particle,
                        isLongDistance(location, Arrays.asList(players)),
                        (float) location.getX(),
                        (float) location.getY(),
                        (float) location.getZ(),
                        (float) particleBuilder.getX(),
                        (float) particleBuilder.getY(),
                        (float) particleBuilder.getZ(),
                        (float) particleBuilder.getSpeed(),
                        particleBuilder.getAmount(),
                        additionalInfo);
                for (final Player player : players) {
                    ((org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                }
            }
        }
    }
}