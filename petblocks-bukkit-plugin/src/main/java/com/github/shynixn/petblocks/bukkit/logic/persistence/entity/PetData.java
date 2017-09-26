package com.github.shynixn.petblocks.bukkit.logic.persistence.entity;

import com.github.shynixn.petblocks.api.persistence.entity.EngineContainer;
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PlayerMeta;
import com.github.shynixn.petblocks.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.bukkit.logic.business.configuration.Config;
import com.github.shynixn.petblocks.bukkit.logic.business.configuration.ConfigPet;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PetData extends PersistenceObject implements PetMeta {

    private String petDisplayName;

    private String skin;
    private int id;
    private int damage;
    private boolean unbreakable;

    private boolean hidden;
    private long ageTicks;
    private boolean enabled;
    private boolean sounds;

    private transient String headDisplayName;
    private transient String[] headLore;

    private PlayerMeta playerInfo;
    private long playerId;

    private ParticleEffectMeta particleEffectBuilder;
    private long particleId;
    private int engineId;

    private EngineContainer engineContainer;

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

    public void setEngineId(int engineId) {
        this.engineId = engineId;
    }

    public int getEngineId() {
        return this.engineId;
    }

    public PetData() {
        super();
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
     * Sets the own meta
     *
     * @param meta meta
     */
    public void setPlayerMeta(PlayerMeta meta) {
        this.playerId = meta.getId();
        this.playerInfo = meta;
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
     * Returns the itemStack for the head
     *
     * @return headItemStack
     */
    @Override
    public Object getHeadItemStack() {
        ItemStack itemStack;
        if (this.getSkin() != null) {
            if (this.getSkin().contains("textures.minecraft")) {
                itemStack = NMSRegistry.changeSkullSkin(new ItemStack(Material.getMaterial(this.getItemId()), 1, (short) this.getItemDamage()), this.getSkin());
            } else {
                itemStack = new ItemStack(Material.getMaterial(this.getItemId()), 1, (short) this.getItemDamage());
                final ItemMeta meta = itemStack.getItemMeta();
                if (meta instanceof SkullMeta) {
                    ((SkullMeta) meta).setOwner(this.skin);
                }
                itemStack.setItemMeta(meta);
            }
        } else {
            itemStack = new ItemStack(this.getItemId(), 1, (short) this.getItemDamage());
        }
        final ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(this.petDisplayName);
        itemStack.setItemMeta(meta);
        final Map<String, Object> data = new HashMap<>();
        data.put("Unbreakable", this.isItemStackUnbreakable());
        itemStack = NMSRegistry.setItemStackTag(itemStack, data);
        return itemStack;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean isHidden) {
        this.hidden = isHidden;
    }

    public boolean isUnbreakable() {
        return this.unbreakable;
    }

    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    public String getHeadDisplayName() {
        return this.headDisplayName;
    }

    public void setHeadDisplayName(String headDisplayName) {
        this.headDisplayName = headDisplayName;
    }

    public String[] getHeadLore() {
        if (this.headLore == null)
            return null;
        return this.headLore.clone();
    }

    public void setHeadLore(String[] headLore) {
        if (this.headLore != null) {
            this.headLore = headLore.clone();
        } else {
            this.headLore = null;
        }
    }

    @Override
    public String getPetDisplayName() {
        return this.petDisplayName;
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
     * Returns if the pet is visible to other players
     *
     * @return visible
     */
    @Override
    public boolean isVisible() {
        return !this.hidden;
    }

    /**
     * Sets if the pet should be visible to other players
     *
     * @param enabled enabled
     */
    @Override
    public void setVisible(boolean enabled) {
        this.hidden = !enabled;
    }

    /**
     * Sets the pet sound enabled
     *
     * @param enabled enabled
     */
    @Override
    public void setSoundEnabled(boolean enabled) {
        this.sounds = enabled;
    }

    /**
     * Returns if the pet-sound is enabled
     *
     * @return enabled
     */
    @Override
    public boolean isSoundEnabled() {
        return this.sounds;
    }

    /**
     * Returns if the itemStack is unbreakable
     *
     * @return unbreakable
     */
    @Override
    public boolean isItemStackUnbreakable() {
        return this.unbreakable;
    }

    /**
     * Sets the itemStack
     *
     * @param id          id
     * @param damage      damage
     * @param skin        skin
     * @param unbreakable unbreakable
     */
    @Override
    public void setSkin(int id, int damage, String skin, boolean unbreakable) {
        if (skin != null && skin.contains("textures.minecraft")) {
            if (!skin.contains("http://")) {
                skin = "http://" + skin;
            }
        }
        this.id = id;
        this.damage = damage;
        this.skin = skin;
        this.unbreakable = unbreakable;
    }

    @Override
    public void setPetDisplayName(String name) {
        if (name == null)
            return;
        this.petDisplayName = ChatColor.translateAlternateColorCodes('&', name);
    }

    @Deprecated
    public String getUuid() {
        return this.playerInfo.getUUID().toString();
    }

    @Deprecated
    public void setUuid(String uuid) {
        this.playerInfo.setUuid(UUID.fromString(uuid));
    }
}
