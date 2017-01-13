package com.github.shynixn.petblocks.business.logic.persistence;

import com.github.shynixn.petblocks.api.entities.*;
import com.github.shynixn.petblocks.lib.ParticleBuilder;
import com.github.shynixn.petblocks.lib.ParticleEffect;
import com.github.shynixn.petblocks.api.events.PetMetaEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.persistence.*;

@Entity
@Table(name = "petblock")
public class PetData implements PetMeta {
    private static final Long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "name")
    private String displayName;

    @Column(name = "type")
    private PetType petType;

    @Column(name = "material")
    private Material costume;

    @Column(name = "metavalue")
    private short durability;

    @Column(name = "skin")
    private String skullName;

    @Column(name = "enabled")
    protected boolean enabled;

    @Column(name = "hidden")
    private boolean hidden;

    @Column(name = "moving")
    private MoveType moveType = MoveType.WALKING;

    @Column(name = "age")
    private int ageTicks;

    @Column(name = "movement")
    private String movement;

    @Column(name = "unbreakable")
    private boolean unbreakable;

    //

    @Column(name = "effect_name")
    @Enumerated(EnumType.ORDINAL)
    private ParticleEffect effect;

    @Column(name = "effect_x")
    private double x;

    @Column(name = "effect_y")
    private double y;

    @Column(name = "effect_z")
    private double z;

    @Column(name = "effect_speed")
    private double speed;

    @Column(name = "effect_amount")
    private int amount;

    @Column(name = "effect_material")
    private Material material;

    @Column(name = "effect_metavalue")
    private byte data;

    @Column(name = "sounds")
    private boolean sounds;

    //

    @Transient
    private Player player;

    @Transient
    private transient String headDisplayName;

    @Transient
    private transient String[] headLore;

    @Transient
    private boolean build;


    public PetData(Player player, PetType petType, String name, ItemStack itemStack, String owner) {
        this.petType = petType;
        this.displayName = name;
        this.player = player;
        this.uuid = player.getUniqueId().toString();
        this.costume = itemStack.getType();
        this.durability = itemStack.getDurability();
        if (owner != null && owner.contains("textures.minecraft")) {
            if (owner.contains("http://") == false)
                owner = "http://" + owner;
        }
        if (owner != null && !owner.equals(""))
            this.skullName = owner;
        this.ageTicks = Age.SMALL.getTicks();
        this.sounds = true;
    }

    public PetData(Player player, PetType petType) {
        this.player = player;
        this.petType = petType;
        this.displayName = "My Pet";
        this.costume = Material.GRASS;
        this.uuid = player.getUniqueId().toString();
        this.ageTicks = Age.SMALL.getTicks();
        this.sounds = true;
    }

    protected PetData(PetType petType) {
        this.petType = petType;
        this.displayName = "My Pet";
        this.costume = Material.GRASS;
        this.ageTicks = Age.SMALL.getTicks();
        this.sounds = true;
    }

    public boolean isBuild() {
        return this.build;
    }

    public void setIsBuild(boolean isBuild) {
        this.build = isBuild;
    }

    public PetData() {
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
        if (this.build)
            Bukkit.getServer().getPluginManager().callEvent(new PetMetaEvent(this));
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
    public Particle getParticleEffect() {
        try {
            if (this.effect == null)
                return null;
            return new ParticleBuilder().setEffect(this.effect).setOffset(this.x, this.y, this.z).setSpeed(this.speed).setAmount(this.amount).setMaterial(this.material).setData(this.data).build();
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public void setSoundsEnabled(boolean enabled) {
        this.sounds = enabled;
        if (this.build)
            Bukkit.getServer().getPluginManager().callEvent(new PetMetaEvent(this));
    }

    @Override
    public boolean isSoundsEnabled() {
        return this.sounds;
    }

    @Override
    public Player getOwner() {
        return this.player;
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
    public void setParticleEffect(Particle particle) {
        try {
            this.effect = particle.getEffect();
            this.x = particle.getX();
            this.y = particle.getY();
            this.z = particle.getZ();
            this.speed = particle.getSpeed();
            this.amount = particle.getAmount();
            this.material = particle.getMaterial();
            this.data = particle.getData();
        } catch (Exception ex) {
            this.effect = null;
            this.x = 0;
            this.y = 0;
            this.z = 0;
            this.speed = 0;
            this.amount = 0;
            this.material = null;
            this.data = 0;
        }
        if (this.build)
            Bukkit.getServer().getPluginManager().callEvent(new PetMetaEvent(this));
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
        if (this.build)
            Bukkit.getServer().getPluginManager().callEvent(new PetMetaEvent(this));
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
        if (this.build)
            Bukkit.getServer().getPluginManager().callEvent(new PetMetaEvent(this));
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setOwner(Player player) {
        this.player = player;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public ParticleEffect getEffect() {
        return this.effect;
    }

    public void setEffect(ParticleEffect effect) {
        this.effect = effect;
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getSpeed() {
        return this.speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Material getMaterial() {
        return this.material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public byte getData() {
        return this.data;
    }

    public void setData(byte data) {
        this.data = data;
    }

    public PetData copy() {
        PetData petData = new PetData();
        petData.id = this.id;
        petData.uuid = this.uuid;
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

        petData.effect = this.effect;
        petData.x = this.x;
        petData.y = this.y;
        petData.z = this.z;

        petData.speed = this.speed;
        petData.amount = this.amount;
        petData.material = this.material;
        petData.data = this.data;

        petData.player = this.player;
        petData.build = this.build;

        return petData;
    }
}
