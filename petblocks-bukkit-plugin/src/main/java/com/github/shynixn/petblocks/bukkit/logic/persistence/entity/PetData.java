package com.github.shynixn.petblocks.bukkit.logic.persistence.entity;

import com.github.shynixn.petblocks.api.persistence.entity.EngineContainer;
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PlayerMeta;
import com.github.shynixn.petblocks.bukkit.logic.business.configuration.Config;
import com.github.shynixn.petblocks.bukkit.logic.business.configuration.ConfigPet;
import com.github.shynixn.petblocks.bukkit.logic.business.helper.PetBlockModifyHelper;
import com.github.shynixn.petblocks.bukkit.logic.business.helper.SkinHelper;
import com.github.shynixn.petblocks.bukkit.nms.v1_12_R1.MaterialCompatibility12;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the petMeta interface which is persistence able to the database.
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
public class PetData extends PersistenceObject implements PetMeta {

    private String petDisplayName;

    private String skin;
    private int id;
    private int damage;
    private boolean unbreakable;

    private long ageTicks;
    private boolean enabled;
    private boolean sounds;

    private PlayerMeta playerInfo;
    private long playerId;

    private ParticleEffectMeta particleEffectBuilder;
    private long particleId;

    private EngineContainer engineContainer;
    private int engineId;

    /**
     * Initializes a new default petData which is ready to be used.
     *
     * @param player player
     * @param name   nameOfThePet
     */
    public PetData(Player player, String name) {
        super();
        this.petDisplayName = name.replace(":player", player.getName());
        this.playerInfo = PlayerData.from(player);
        this.ageTicks = ConfigPet.getInstance().getAge_smallticks();
        this.sounds = true;
        this.particleEffectBuilder = new ParticleEffectData();
        this.particleEffectBuilder.setEffectType(ParticleEffectMeta.ParticleEffectType.NONE);
        this.engineContainer = Config.getInstance().getEngineController().getById(Config.getInstance().getDefaultEngine());
        if (this.engineContainer == null) {
            throw new RuntimeException("Engine cannot be null!");
        }
    }

    /**
     * Initializes a new petData.
     */
    public PetData() {
        super();
    }

    /**
     * Sets the id of the engine.
     *
     * @param engineId id
     */
    public void setEngineId(int engineId) {
        this.engineId = engineId;
    }

    /**
     * Returns the id of the engine.
     *
     * @return id
     */
    public int getEngineId() {
        return this.engineId;
    }

    /**
     * Returns the id of the player
     *
     * @return playerId
     */
    public long getPlayerId() {
        return this.playerId;
    }

    /**
     * Sets the id of the player
     *
     * @param id id
     */
    public void setPlayerId(long id) {
        this.playerId = id;
    }

    /**
     * Returns the id of the particle
     *
     * @return particleId
     */
    public long getParticleId() {
        return this.particleId;
    }

    /**
     * Sets the id of the particle
     *
     * @param id id
     */
    public void setParticleId(long id) {
        this.particleId = id;
    }

    /**
     * Sets the own meta
     *
     * @param meta meta
     */
    public void setPlayerMeta(PlayerMeta meta) {
        this.playerId = meta.getId();
        this.playerInfo = meta;
    }

    /**
     * Sets the particleEffect meta
     *
     * @param meta meta
     */
    public void setParticleEffectMeta(ParticleEffectMeta meta) {
        if (meta == null) {
            throw new IllegalArgumentException("ParticleEffectMeta cannot be null!");
        }
        this.particleId = meta.getId();
        this.particleEffectBuilder = meta;
    }

    /**
     * Returns the particleEffect meta
     *
     * @return meta
     */
    @Override
    public ParticleEffectMeta getParticleEffectMeta() {
        return this.particleEffectBuilder;
    }

    /**
     * Returns the meta of the owner
     *
     * @return player
     */
    @Override
    public PlayerMeta getPlayerMeta() {
        return this.playerInfo;
    }

    /**
     * Returns the id of the item
     *
     * @return itemId
     */
    @Override
    public int getItemId() {
        return this.id;
    }

    /**
     * Returns the damage of the item
     *
     * @return itemDamage
     */
    @Override
    public int getItemDamage() {
        return this.damage;
    }

    /**
     * Returns if the item is unbreakable
     *
     * @return unbreakable
     */
    @Override
    public boolean isItemUnbreakable() {
        return this.unbreakable;
    }

    /**
     * Returns if the petblock is enabled
     *
     * @return enabled
     */
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Sets the petblock enabled
     *
     * @param enabled enabled
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the age in ticks
     *
     * @return age
     */
    @Override
    public long getAge() {
        return this.ageTicks;
    }

    /**
     * Returns the skin of the pet
     *
     * @return skin
     */
    @Override
    public String getSkin() {
        return this.skin;
    }

    /**
     * Sets the age in ticks
     *
     * @param ticks ticks
     */
    @Override
    public void setAge(long ticks) {
        this.ageTicks = ticks;
    }

    /**
     * Returns the data of the engine
     *
     * @return engine
     */
    @Override
    public EngineContainer getEngine() {
        return this.engineContainer;
    }

    /**
     * Sets the data of the engine
     *
     * @param engine engine
     */
    @Override
    public void setEngine(EngineContainer engine) {
        this.engineContainer = engine;
    }

    /**
     * Sets the pet sound enabled.
     *
     * @param enabled enabled
     */
    @Override
    public void setSoundEnabled(boolean enabled) {
        this.sounds = enabled;
    }

    /**
     * Returns if the pet-sound is enabled.
     *
     * @return enabled
     */
    @Override
    public boolean isSoundEnabled() {
        return this.sounds;
    }

    /**
     * Returns if the itemStack is unbreakable.
     *
     * @return unbreakable
     */
    @Override
    public boolean isItemStackUnbreakable() {
        return this.unbreakable;
    }

    /**
     * Sets the itemStack.
     *
     * @param id          id
     * @param damage      damage
     * @param skin        skin
     * @param unbreakable unbreakable
     */
    @Override
    public void setSkin(int id, int damage, String skin, boolean unbreakable) {
        String s = skin;
        if (s != null && s.contains("textures.minecraft")) {
            if (!s.contains("http://")) {
                s = "http://" + s;
            }
        }
        this.id = id;
        this.damage = damage;
        this.skin = s;
        this.unbreakable = unbreakable;
    }

    /**
     * Returns the itemStack for the head
     *
     * @return headItemStack
     */
    @Override
    public Object getHeadItemStack() {
        ItemStack itemStack;
        if (this.getSkin() != null) {
            if (this.getSkin().contains("textures.minecraft")) {
                itemStack = new ItemStack(MaterialCompatibility12.getMaterialFromId(this.getItemId()), 1, (short) this.getItemDamage());
                SkinHelper.setItemStackSkin(itemStack, this.getSkin());
            } else {
                itemStack = new ItemStack(MaterialCompatibility12.getMaterialFromId(this.getItemId()), 1, (short) this.getItemDamage());
                final ItemMeta meta = itemStack.getItemMeta();
                if (meta instanceof SkullMeta) {
                    ((SkullMeta) meta).setOwner(this.skin);
                }
                itemStack.setItemMeta(meta);
            }
        } else {
            itemStack = new ItemStack(MaterialCompatibility12.getMaterialFromId(this.getItemId()), 1, (short) this.getItemDamage());
        }
        final ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(this.petDisplayName);
        itemStack.setItemMeta(meta);
        final Map<String, Object> data = new HashMap<>();
        data.put("Unbreakable", this.isItemStackUnbreakable());
        itemStack = PetBlockModifyHelper.setItemStackNBTTag(itemStack, data);
        return itemStack;
    }

    /**
     * Sets the stored display name of the pet which appears above it's head on respawn.
     *
     * @param name name
     */
    @Override
    public void setPetDisplayName(String name) {
        if (name == null)
            return;
        if (ConfigPet.getInstance().getPetNameBlackList() != null) {
            for (final String blackName : ConfigPet.getInstance().getPetNameBlackList()) {
                if (name.toUpperCase().contains(blackName.toUpperCase())) {
                    throw new RuntimeException("Name is not valid!");
                }
            }
        }
        this.petDisplayName = ChatColor.translateAlternateColorCodes('&', name);
    }

    /**
     * Returns the stored display name of the pet which appear above it's head on respawn.
     *
     * @return name
     */
    @Override
    public String getPetDisplayName() {
        return this.petDisplayName;
    }
}
