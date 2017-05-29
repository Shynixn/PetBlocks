package com.github.shynixn.petblocks.business.logic.persistence.entity;

import com.github.shynixn.petblocks.api.entities.*;
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.api.persistence.entity.PlayerMeta;
import com.github.shynixn.petblocks.lib.ParticleBuilder;
import com.github.shynixn.petblocks.lib.ParticleEffect;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class PetData extends PersistenceObject implements PetMeta {
    private static final Long serialVersionUID = 1L;

    private String displayName;
    private PetType petType;
    private Material costume;
    private short durability;
    private String skullName;
    private boolean hidden;
    private MoveType moveType = MoveType.WALKING;
    private int ageTicks;
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

    public PetData(Player player, PetType petType, String name, ItemStack itemStack, String owner) {
        this.petType = petType;
        this.displayName = name;
        this.playerInfo = PlayerData.from(player);
        this.costume = itemStack.getType();
        this.durability = itemStack.getDurability();
        if (owner != null && owner.contains("textures.minecraft")) {
            if (!owner.contains("http://"))
                owner = "http://" + owner;
        }
        if (owner != null && !owner.equals(""))
            this.skullName = owner;
        this.ageTicks = Age.SMALL.getTicks();
        this.sounds = true;
    }

    public PetData() {
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

    public boolean isBuild() {
        return this.build;
    }

    public void setIsBuild(boolean isBuild) {
        this.build = isBuild;
    }

    public boolean isSounds() {
        return this.sounds;
    }

    public void setSounds(boolean sounds) {
        this.sounds = sounds;
    }

    @Override
    public void setSkin(Material material, short durability, String skin) {
        if (skin != null && skin.contains("textures.minecraft")) {
            if (!skin.contains("http://"))
                skin = "http://" + skin;
        }
        this.costume = material;
        this.durability = durability;
        this.skullName = skin;
    }

    @Override
    public void setAge(Age age) {
        this.ageTicks = age.getTicks();
    }

    @Override
    public void setAgeInTicks(int ticks) {
        this.ageTicks = ticks;
    }

    @Override
    public int getAgeInTicks() {
        return this.ageTicks;
    }

    public void setAgeTicks(int ticks) {
        this.ageTicks = ticks;
    }

    public int getAgeTicks() {
        return this.ageTicks;
    }

    @Override
    public Age getAge() {
        return Age.getAgeFromTicks(this.ageTicks);
    }

    @Deprecated
    public PetType getPetType() {
        return this.petType;
    }

    public String getMovement() {
        return this.movement;
    }

    public void setMovement(String movement) {
        this.movement = movement;
    }

    @Override
    public Movement getMovementType() {
        if (this.movement == null)
            this.movement = Movement.HOPPING.name().toUpperCase();
        return Movement.getMovementFromName(this.movement);
    }

    @Override
    public void setMovementType(Movement movementType) {
        if (movementType != null)
            this.movement = movementType.name().toUpperCase();
    }

    @Override
    public PetType getType() {
        return this.petType;
    }

    @Override
    public boolean isHidden() {
        return this.hidden;
    }

    @Override
    public void setHidden(boolean isHidden) {
        this.hidden = isHidden;
    }

    @Override
    public void setSoundsEnabled(boolean enabled) {
        this.sounds = enabled;
    }

    @Override
    public boolean isSoundsEnabled() {
        return this.sounds;
    }

    @Override
    public boolean isUnbreakable() {
        return this.unbreakable;
    }

    @Override
    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    @Override
    public String getHeadDisplayName() {
        return this.headDisplayName;
    }

    @Override
    public void setHeadDisplayName(String headDisplayName) {
        this.headDisplayName = headDisplayName;
    }

    @Override
    public String[] getHeadLore() {
        return this.headLore;
    }

    @Override
    public void setHeadLore(String[] headLore) {
        this.headLore = headLore;
    }

    @Override
    public String getSkin() {
        return this.skullName;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public void setPetType(PetType petType) {
        this.petType = petType;
    }

    @Override
    public void setDisplayName(String name) {
        if (name == null)
            return;
        this.displayName = ChatColor.translateAlternateColorCodes('&', name);
    }

    @Override
    public MoveType getMoveType() {
        return this.moveType;
    }

    @Override
    public void setMoveType(MoveType moveType) {
        this.moveType = moveType;
        if (this.moveType == null)
            this.moveType = MoveType.WALKING;

    }

    @Override
    public Material getSkinMaterial() {
        return this.costume;
    }

    @Override
    public short getSkinDurability() {
        return this.durability;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
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

    @Deprecated
    public PetData copy() {
        PetData petData = new PetData();
        petData.displayName = this.displayName;
        petData.petType = this.petType;
        petData.costume = this.costume;
        petData.durability = this.durability;
        petData.skullName = this.skullName;
        petData.enabled = this.enabled;
        petData.hidden = this.hidden;
        petData.sounds = this.sounds;
        petData.moveType = this.moveType;
        petData.ageTicks = this.ageTicks;

        petData.setEffect(this.getEffect());
        petData.setX(this.getX());
        petData.setY(this.getY());
        petData.setZ(this.getZ());

        petData.setSpeed(this.getSpeed());
        petData.setAmount(this.getAmount());
        petData.setMaterial(this.getMaterial());
        petData.setData(this.getData());
        petData.build = this.build;
        return petData;
    }

    @Deprecated
    public void setParticleEffect(Particle particle) {
        try {
            this.setEffect(particle.getEffect());
            this.setX(particle.getX());
            this.setY(particle.getY());
            this.setZ(particle.getZ());
            this.setSpeed(particle.getSpeed());
            this.setAmount(particle.getAmount());
            this.setMaterial(particle.getMaterial());
            this.setData(particle.getData());
        } catch (Exception ex) {
            this.setEffect(null);
            this.setX(0);
            this.setY(0);
            this.setZ(0);
            this.setSpeed(0);
            this.setAmount(0);
            this.setMaterial(null);
            this.setData((byte) 0);
        }
    }

    @Deprecated
    public Particle getParticleEffect() {
        try {
            if (this.particleEffectBuilder == null)
                return null;
            return new ParticleBuilder().setEffect(this.getEffect())
                    .setOffset(this.getX(), this.getY(), this.getZ()).setSpeed(this.getSpeed()).setAmount(this.getAmount()).setMaterial(this.getMaterial()).setData(this.getData()).build();
        } catch (Exception ex) {
            return null;
        }
    }

    @Deprecated
    public byte getData() {
        return this.particleEffectBuilder.getData();
    }

    @Deprecated
    public void setData(byte data) {
        this.particleEffectBuilder.setData(data);
    }

    @Deprecated
    public ParticleEffect getEffect() {
        return ParticleEffect.getParticleEffectFromName(this.particleEffectBuilder.getEffectName());
    }

    @Deprecated
    public void setEffect(ParticleEffect effect) {
        this.particleEffectBuilder.setEffectName(effect.getName());
    }

    @Deprecated
    public double getX() {
        return this.particleEffectBuilder.getX();
    }

    @Deprecated
    public void setX(double x) {
        this.particleEffectBuilder.setX(x);
    }

    @Deprecated
    public double getY() {
        return this.particleEffectBuilder.getY();
    }

    @Deprecated
    public void setY(double y) {
        this.particleEffectBuilder.setY(y);
    }

    @Deprecated
    public double getZ() {
        return this.particleEffectBuilder.getZ();
    }

    @Deprecated
    public void setZ(double z) {
        this.particleEffectBuilder.setZ(z);
    }

    @Deprecated
    public double getSpeed() {
        return this.particleEffectBuilder.getSpeed();
    }

    @Deprecated
    public void setSpeed(double speed) {
        this.particleEffectBuilder.setSpeed(speed);
    }

    @Deprecated
    public int getAmount() {
        return this.particleEffectBuilder.getAmount();
    }

    @Deprecated
    public void setAmount(int amount) {
        this.particleEffectBuilder.setAmount(amount);
    }

    @Deprecated
    public Material getMaterial() {
        return this.particleEffectBuilder.getMaterial();
    }

    @Deprecated
    public void setMaterial(Material material) {
        this.particleEffectBuilder.setMaterial(material);
    }
}
