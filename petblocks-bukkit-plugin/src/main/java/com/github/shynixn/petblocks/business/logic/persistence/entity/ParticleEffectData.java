package com.github.shynixn.petblocks.business.logic.persistence.entity;

import com.github.shynixn.petblocks.api.persistence.entity.IPosition;
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
public class ParticleEffectData extends PersistenceObject implements ConfigurationSerializable, ParticleEffectMeta {
    private String effect;
    private int amount;
    private double speed;
    private double x;
    private double y;
    private double z;

    private Integer material;
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
     * @param x          x
     * @param y          y
     * @param z          u
     */
    public ParticleEffectData(String effectName, int amount, double speed, double x, double y, double z) {
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
    public ParticleEffectData(Map<String, Object> items) throws Exception {
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
    @Override
    public ParticleEffectData setColor(int red, int green, int blue) {
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
     * Sets the x coordinate of the particleEffect
     *
     * @param x x
     * @return builder
     */
    @Override
    public ParticleEffectData setX(double x) {
        this.x = x;
        return this;
    }

    /**
     * Sets the y coordinate of the particleEffect
     *
     * @param y y
     * @return builder
     */
    @Override
    public ParticleEffectData setY(double y) {
        this.y = y;
        return this;
    }

    /**
     * Sets the z coordinate of the particleEffect
     *
     * @param z z
     * @return builder
     */
    @Override
    public ParticleEffectData setZ(double z) {
        this.z = z;
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
        this.z = (double) blue / 255.0;
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
    @Override
    public ParticleEffectData setGreen(int green) {
        this.y = green / 255.0;
        return this;
    }

    /**
     * Sets the material of the particleEffect
     *
     * @param material material
     * @return builder
     */
    @Override
    public ParticleEffectData setMaterial(Integer material) {
        this.material = material;
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
     * Returns the x coordinate of the particleEffect
     *
     * @return x
     */
    @Override
    public double getX() {
        return this.x;
    }

    /**
     * Returns the y coordinate of the particleEffect
     *
     * @return y
     */
    @Override
    public double getY() {
        return this.y;
    }

    /**
     * Returns the z coordinate of the particleEffect
     *
     * @return z
     */
    @Override
    public double getZ() {
        return this.z;
    }

    /**
     * Returns the RGB color blue of the particleEffect
     *
     * @return blue
     */
    @Override
    public int getBlue() {
        return (int) this.z * 255;
    }

    /**
     * Returns the RGB color red of the particleEffect
     *
     * @return red
     */
    @Override
    public int getRed() {
        return (int) this.x * 255;
    }

    /**
     * Returns the RGB color green of the particleEffect
     *
     * @return green
     */
    @Override
    public int getGreen() {
        return (int) this.y * 255;
    }

    /**
     * Returns the material of the particleEffect
     *
     * @return material
     */
    @Override
    public Integer getMaterial() {
        return this.material;
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
     * Copies the current builder
     *
     * @return copyOfBuilder
     */
    public ParticleEffectData copy() {
        final ParticleEffectData particle = new ParticleEffectData();
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
        if (clazz == null)
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
     * Plays the effect at the given location to the given players.
     *
     * @param location location
     * @param players  players
     */
    @Override
    public <T> void apply(IPosition location, T... players) {
        if (location == null)
            throw new IllegalArgumentException("Location cannot be null!");
        if (players.length == 0)
            ParticleUtils.sendParticle(this, ((LocationBuilder)location).toLocation());
        else
            ParticleUtils.sendParticle(this, ((LocationBuilder)location).toLocation(), (Player[]) players);
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
        return "effect {" + "name " + this.effect + " amound " + this.amount + " speed " + this.speed + '}';
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
        static boolean sendParticle(ParticleEffectData builder, Location location) {
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
        static boolean sendParticle(ParticleEffectData builder, Location location, Player[] players) {
            switch (getServerVersion()) {
                case "v1_12_R1":
                    ParticleUtils12R1.sendParticle(builder, location, players);
                    break;
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
            static void sendParticle(ParticleEffectData particleBuilder, Location location, Player[] players) {
                final net.minecraft.server.v1_8_R1.EnumParticle particle = net.minecraft.server.v1_8_R1.EnumParticle.valueOf(particleBuilder.getEffectType().name().toUpperCase());
                int[] additionalInfo = null;
                if (particleBuilder.getMaterial() != null) {
                    if (particleBuilder.getEffectType() == ParticleEffectData.ParticleEffectType.ITEM_CRACK)
                        additionalInfo = new int[]{particleBuilder.getMaterial(), particleBuilder.getData()};
                    else
                        additionalInfo = new int[]{particleBuilder.getMaterial(), particleBuilder.getData() << 12};
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
            static void sendParticle(ParticleEffectData particleBuilder, Location location, Player[] players) {
                final net.minecraft.server.v1_8_R2.EnumParticle particle = net.minecraft.server.v1_8_R2.EnumParticle.valueOf(particleBuilder.getEffectType().name().toUpperCase());
                int[] additionalInfo = null;
                if (particleBuilder.getMaterial() != null) {
                    if (particleBuilder.getEffectType() == ParticleEffectData.ParticleEffectType.ITEM_CRACK)
                        additionalInfo = new int[]{particleBuilder.getMaterial(), particleBuilder.getData()};
                    else
                        additionalInfo = new int[]{particleBuilder.getMaterial(), particleBuilder.getData() << 12};
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
            static void sendParticle(ParticleEffectData particleBuilder, Location location, Player[] players) {
                final net.minecraft.server.v1_8_R3.EnumParticle particle = net.minecraft.server.v1_8_R3.EnumParticle.valueOf(particleBuilder.getEffectType().name().toUpperCase());
                int[] additionalInfo = null;
                if (particleBuilder.getMaterial() != null) {
                    if (particleBuilder.getEffectType() == ParticleEffectData.ParticleEffectType.ITEM_CRACK)
                        additionalInfo = new int[]{particleBuilder.getMaterial(), particleBuilder.getData()};
                    else
                        additionalInfo = new int[]{particleBuilder.getMaterial(), particleBuilder.getData() << 12};
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
            static void sendParticle(ParticleEffectData particleBuilder, Location location, Player[] players) {
                final net.minecraft.server.v1_9_R1.EnumParticle particle = net.minecraft.server.v1_9_R1.EnumParticle.valueOf(particleBuilder.getEffectType().name().toUpperCase());
                int[] additionalInfo = null;
                if (particleBuilder.getMaterial() != null) {
                    if (particleBuilder.getEffectType() == ParticleEffectData.ParticleEffectType.ITEM_CRACK)
                        additionalInfo = new int[]{particleBuilder.getMaterial(), particleBuilder.getData()};
                    else
                        additionalInfo = new int[]{particleBuilder.getMaterial(), particleBuilder.getData() << 12};
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
            static void sendParticle(ParticleEffectData particleBuilder, Location location, Player[] players) {
                final net.minecraft.server.v1_9_R2.EnumParticle particle = net.minecraft.server.v1_9_R2.EnumParticle.valueOf(particleBuilder.getEffectType().name().toUpperCase());
                int[] additionalInfo = null;
                if (particleBuilder.getMaterial() != null) {
                    if (particleBuilder.getEffectType() == ParticleEffectData.ParticleEffectType.ITEM_CRACK)
                        additionalInfo = new int[]{particleBuilder.getMaterial(), particleBuilder.getData()};
                    else
                        additionalInfo = new int[]{particleBuilder.getMaterial(), particleBuilder.getData() << 12};
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
            static void sendParticle(ParticleEffectData particleBuilder, Location location, Player[] players) {
                final net.minecraft.server.v1_10_R1.EnumParticle particle = net.minecraft.server.v1_10_R1.EnumParticle.valueOf(particleBuilder.getEffectType().name().toUpperCase());
                int[] additionalInfo = null;
                if (particleBuilder.getMaterial() != null) {
                    if (particleBuilder.getEffectType() == ParticleEffectData.ParticleEffectType.ITEM_CRACK)
                        additionalInfo = new int[]{particleBuilder.getMaterial(), particleBuilder.getData()};
                    else
                        additionalInfo = new int[]{particleBuilder.getMaterial(), particleBuilder.getData() << 12};
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
            static void sendParticle(ParticleEffectData particleBuilder, Location location, Player[] players) {
                final net.minecraft.server.v1_11_R1.EnumParticle particle = net.minecraft.server.v1_11_R1.EnumParticle.valueOf(particleBuilder.getEffectType().name().toUpperCase());
                int[] additionalInfo = null;
                if (particleBuilder.getMaterial() != null) {
                    if (particleBuilder.getEffectType() == ParticleEffectData.ParticleEffectType.ITEM_CRACK)
                        additionalInfo = new int[]{particleBuilder.getMaterial(), particleBuilder.getData()};
                    else
                        additionalInfo = new int[]{particleBuilder.getMaterial(), particleBuilder.getData() << 12};
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

        static class ParticleUtils12R1 {
            /**
             * Sends a particleEffect for v1_12_R1
             *
             * @param particleBuilder particleBuilder
             * @param location        location
             * @param players         players
             */
            static void sendParticle(ParticleEffectData particleBuilder, Location location, Player[] players) {
                final net.minecraft.server.v1_12_R1.EnumParticle particle = net.minecraft.server.v1_12_R1.EnumParticle.valueOf(particleBuilder.getEffectType().name().toUpperCase());
                int[] additionalInfo = null;
                if (particleBuilder.getMaterial() != null) {
                    if (particleBuilder.getEffectType() == ParticleEffectData.ParticleEffectType.ITEM_CRACK)
                        additionalInfo = new int[]{particleBuilder.getMaterial(), particleBuilder.getData()};
                    else
                        additionalInfo = new int[]{particleBuilder.getMaterial(), particleBuilder.getData() << 12};
                }
                final net.minecraft.server.v1_12_R1.PacketPlayOutWorldParticles packet = new net.minecraft.server.v1_12_R1.PacketPlayOutWorldParticles(
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
                    ((org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                }
            }
        }
    }
}