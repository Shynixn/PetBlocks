package com.github.shynixn.petblocks.business.logic.persistence.entity;

import com.github.shynixn.petblocks.api.persistence.entity.EngineContainer;
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PlayerMeta;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigPet;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PetData extends PersistenceObject implements PetMeta {
    private String displayName;
    private Material costume;
    private short durability;
    private String skullName;
    private boolean hidden;
    private long ageTicks;
    private String movement;
    private boolean unbreakable;
    private boolean enabled;
    private boolean sounds;

    private transient String headDisplayName;
    private transient String[] headLore;
    private boolean build;

    private PlayerMeta playerInfo;
    private long playerId;

    private ParticleEffectMeta particleEffectBuilder;
    private long particleId;
    private int engineId;

    private EngineContainer engineContainer;

    public PetData(Player player, String name, ItemStack itemStack, String owner) {
        super();
        this.displayName = name;
        this.playerInfo = PlayerData.from(player);
        this.costume = itemStack.getType();
        this.durability = itemStack.getDurability();
        if (owner != null && owner.contains("textures.minecraft") && !owner.contains("http://")) {
            owner = "http://" + owner;
        }
        if (owner != null && !owner.isEmpty())
            this.skullName = owner;
        this.ageTicks = ConfigPet.getInstance().getAge_smallticks();
        this.sounds = true;
        this.particleEffectBuilder = new ParticleEffectData();
        this.particleEffectBuilder.setEffectType(ParticleEffectMeta.ParticleEffectType.NONE);
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
    @Override
    public long getPlayerId() {
        return this.playerId;
    }

    /**
     * Sets the id of the player
     *
     * @param id id
     */
    @Override
    public void setPlayerId(long id) {
        this.playerId = id;
    }

    /**
     * Returns the id of the particle
     *
     * @return particleId
     */
    @Override
    public long getParticleId() {
        return this.particleId;
    }

    /**
     * Sets the id of the particle
     *
     * @param id id
     */
    @Override
    public void setParticleId(long id) {
        this.particleId = id;
    }

    /**
     * Sets the particleEffect meta
     *
     * @param meta meta
     */
    @Override
    public void setParticleEffectMeta(ParticleEffectMeta meta) {
        if (meta == null)
            throw new IllegalArgumentException("Meta cannot be null!");
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
    @Override
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

    public void setIsBuild(boolean isBuild) {
        this.build = isBuild;
    }

    public void setSkin(Material material, short durability, String skin) {
        if (skin != null && skin.contains("textures.minecraft")) {
            if (!skin.contains("http://"))
                skin = "http://" + skin;
        }
        this.costume = material;
        this.durability = durability;
        this.skullName = skin;
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

    public String getSkin() {
        return this.skullName;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
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
        return null;
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
        return false;
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

    }

    @Override
    public void setDisplayName(String name) {
        if (name == null)
            return;
        this.displayName = ChatColor.translateAlternateColorCodes('&', name);
    }

    public Material getSkinMaterial() {
        return this.costume;
    }

    public short getSkinDurability() {
        return this.durability;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public Material getCostume() {
        return this.costume;
    }

    public void setCostume(Material costume) {
        this.costume = costume;
    }

    public short getDurability() {
        return this.durability;
    }

    public void setDurability(short durability) {
        this.durability = durability;
    }

    public String getSkullName() {
        return this.skullName;
    }

    public void setSkullName(String skullName) {
        this.skullName = skullName;
    }

    //region Deprecated

    @Deprecated
    public Player getOwner() {
        return this.playerInfo.getPlayer();
    }

    @Deprecated
    public void setOwner(Player player) {
        this.playerInfo.setUuid(player.getUniqueId());
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
