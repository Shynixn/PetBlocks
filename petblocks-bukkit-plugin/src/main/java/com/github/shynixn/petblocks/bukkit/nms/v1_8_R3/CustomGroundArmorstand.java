package com.github.shynixn.petblocks.bukkit.nms.v1_8_R3;

import com.github.shynixn.petblocks.api.bukkit.event.PetBlockSpawnEvent;
import com.github.shynixn.petblocks.api.business.entity.EffectPipeline;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.business.entity.PetBlockPartEntity;
import com.github.shynixn.petblocks.api.business.enumeration.RideType;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.logic.business.entity.Pipeline;
import com.github.shynixn.petblocks.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.bukkit.nms.helper.PetBlockHelper;
import com.github.shynixn.petblocks.bukkit.logic.business.configuration.ConfigPet;
import com.github.shynixn.petblocks.bukkit.logic.persistence.entity.PetData;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.logging.Level;

final class CustomGroundArmorstand extends EntityArmorStand implements PetBlock {
    private PetData petMeta;
    private Player owner;

    private boolean isSpecial;
    private boolean isGround;
    private boolean firstRide = true;
    private PetBlockPartEntity rabbit;
    private int counter;

    private double health = 20.0;
    private boolean isDieing;

    private boolean hitflor;

    private Pipeline pipeline;

    public CustomGroundArmorstand(World world) {
        super(world);
    }

    public CustomGroundArmorstand(World world, double d0, double d1, double d2) {
        super(world, d0, d1, d2);
    }

    public CustomGroundArmorstand(Location location, PetMeta meta) {
        super(((CraftWorld) location.getWorld()).getHandle());
        this.isSpecial = true;
        this.petMeta = (PetData) meta;
        this.owner = this.petMeta.getPlayerMeta().getPlayer();
        if (this.petMeta.getEngine().getEntityType().equalsIgnoreCase("RABBIT"))
            this.rabbit = new CustomRabbit(this.owner, this);
        else if (this.petMeta.getEngine().getEntityType().equalsIgnoreCase("ZOMBIE"))
            this.rabbit = new CustomZombie(this.owner, this);

        this.pipeline = new Pipeline(this);
        this.spawn(location);
    }

    private boolean isJumping() {
        final Field jump;
        try {
            jump = EntityLiving.class.getDeclaredField("aY");
            jump.setAccessible(true);
            return jump.getBoolean(this.passenger);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
            PetBlocksPlugin.logger().log(Level.WARNING, "EntityNMS exception.", e1);
        }
        return false;
    }

    @Override
    protected void doTick() {
        if (this.isSpecial) {
            this.counter = PetBlockHelper.doTick(this.counter, this, location -> {
                CustomGroundArmorstand.this.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
                final PacketPlayOutEntityTeleport animation = new PacketPlayOutEntityTeleport(CustomGroundArmorstand.this);
                for (final Player player : ((ArmorStand) this.getArmorStand()).getWorld().getPlayers()) {
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(animation);
                }
            });
        }
        super.doTick();
    }

    @Override
    public void g(float sideMot, float forMot) {
        if (this.isSpecial) {
            if (this.passenger != null && this.passenger instanceof EntityHuman) {
                if (this.petMeta.getEngine().getRideType() == RideType.RUNNING) {
                    this.lastYaw = (this.yaw = this.passenger.yaw);
                    this.pitch = (this.passenger.pitch * 0.5F);
                    this.setYawPitch(this.yaw, this.pitch);
                    this.aK = (this.aI = this.yaw);
                    sideMot = ((EntityLiving) this.passenger).aZ * 0.5F;
                    forMot = ((EntityLiving) this.passenger).ba;
                    if (forMot <= 0.0F) {
                        forMot *= 0.25F;
                    }
                    if (this.onGround && this.isJumping()) {
                        this.motY = 0.5D;
                    }
                    this.S = (float) ConfigPet.getInstance().getModifier_petclimbing();
                    this.aM = (this.bI() * 0.1F);
                    if (!this.world.isClientSide) {
                        this.k(0.35F);
                        super.g(sideMot * (float) ConfigPet.getInstance().getModifier_petriding(), forMot * (float) ConfigPet.getInstance().getModifier_petriding());
                    }

                    this.aA = this.aB;
                    final double d0 = this.locX - this.lastX;
                    final double d1 = this.locZ - this.lastZ;
                    float f4 = MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0F;

                    if (f4 > 1.0F) {
                        f4 = 1.0F;
                    }

                    this.aB += (f4 - this.aB) * 0.4F;
                    this.aC += this.aB;
                } else {
                    final float side = ((EntityLiving) this.passenger).aZ * 0.5F;
                    final float forw = ((EntityLiving) this.passenger).ba;
                    final Vector v = new Vector();
                    final Location l = new Location(this.world.getWorld(), this.locX, this.locY, this.locZ);
                    if (side < 0.0F) {
                        l.setYaw(this.passenger.yaw - 90);
                        v.add(l.getDirection().normalize().multiply(-0.5));
                    } else if (side > 0.0F) {
                        l.setYaw(this.passenger.yaw + 90);
                        v.add(l.getDirection().normalize().multiply(-0.5));
                    }

                    if (forw < 0.0F) {
                        l.setYaw(this.passenger.yaw);
                        v.add(l.getDirection().normalize().multiply(0.5));
                    } else if (forw > 0.0F) {
                        l.setYaw(this.passenger.yaw);
                        v.add(l.getDirection().normalize().multiply(0.5));
                    }

                    this.lastYaw = this.yaw = this.passenger.yaw - 180;
                    this.pitch = this.passenger.pitch * 0.5F;
                    this.lastYaw = (this.yaw = this.passenger.yaw);
                    this.setYawPitch(this.yaw, this.pitch);
                    if (this.firstRide) {
                        this.firstRide = false;
                        v.setY(1F);
                    }
                    if (this.isJumping()) {
                        v.setY(0.5F);
                        this.isGround = true;
                        this.hitflor = false;
                    } else if (this.isGround) {
                        v.setY(-0.2F);
                    }
                    if (this.hitflor) {
                        v.setY(0);
                        l.add(v.multiply(2.25).multiply(ConfigPet.getInstance().getModifier_petriding()));
                        this.setPosition(l.getX(), l.getY(), l.getZ());
                    } else {
                        l.add(v.multiply(2.25).multiply(ConfigPet.getInstance().getModifier_petriding()));
                        this.setPosition(l.getX(), l.getY(), l.getZ());
                    }
                    final Vec3D vec3d = new Vec3D(this.locX, this.locY, this.locZ);
                    final Vec3D vec3d1 = new Vec3D(this.locX + this.motX, this.locY + this.motY, this.locZ + this.motZ);
                    final MovingObjectPosition movingobjectposition = this.world.rayTrace(vec3d, vec3d1);
                    if (movingobjectposition == null) {
                        this.bumper = l.toVector();
                    } else {
                        if (this.bumper != null && ConfigPet.getInstance().isFollow_wallcolliding())
                            this.setPosition(this.bumper.getX(), this.bumper.getY(), this.bumper.getZ());
                    }
                }
            } else
                this.firstRide = true;
        } else {
            super.g(sideMot, forMot);
        }
    }

    private Vector bumper;

    public void spawn(Location location) {
        final PetBlockSpawnEvent event = new PetBlockSpawnEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCanceled()) {
            NMSRegistry.accessWorldGuardSpawn(location);
            this.rabbit.spawn(location);
            final net.minecraft.server.v1_8_R3.World mcWorld = ((org.bukkit.craftbukkit.v1_8_R3.CraftWorld) location.getWorld()).getHandle();
            this.setPosition(location.getX(), location.getY(), location.getZ());
            mcWorld.addEntity(this, SpawnReason.CUSTOM);
            final net.minecraft.server.v1_8_R3.NBTTagCompound compound = new net.minecraft.server.v1_8_R3.NBTTagCompound();
            compound.setBoolean("invulnerable", true);
            compound.setBoolean("Invisible", true);
            compound.setBoolean("PersistenceRequired", true);
            compound.setBoolean("ShowArms", true);
            compound.setBoolean("NoBasePlate", true);
            this.a(compound);
            ((ArmorStand)this.getArmorStand()).setBodyPose(new EulerAngle(0, 0, 2878));
            ((ArmorStand)this.getArmorStand()).setLeftArmPose(new EulerAngle(2878, 0, 0));
            ((ArmorStand)this.getArmorStand()).setMetadata("keep", this.getKeepField());
            NMSRegistry.rollbackWorldGuardSpawn(location);
            ((ArmorStand)this.getArmorStand()).setCustomNameVisible(true);
            ((ArmorStand)this.getArmorStand()).setCustomName(this.petMeta.getPetDisplayName());
            ((ArmorStand)this.getArmorStand()).setRemoveWhenFarAway(false);
            ((LivingEntity) this.getEngineEntity()).setRemoveWhenFarAway(false);
            this.health = ConfigPet.getInstance().getCombat_health();
            if (this.petMeta == null)
                return;
            PetBlockHelper.setItemConsideringAge(this);
        }
    }

    @Override
    public void teleportWithOwner(Object mLocation) {
        final Location location = (Location) mLocation;
        final EntityPlayer player = ((CraftPlayer) this.owner).getHandle();
        player.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        final PacketPlayOutEntityTeleport teleport = new PacketPlayOutEntityTeleport(player);
        for (final Player player1 : this.owner.getWorld().getPlayers()) {
            ((CraftPlayer) player1).getHandle().playerConnection.sendPacket(teleport);
        }
    }

    @Override
    public void damage(double amount) {
        if (amount < -1.0) {
            this.hitflor = true;
        } else {
            this.health = PetBlockHelper.setDamage(this, this.health, amount, location -> {
                final PacketPlayOutAnimation animation = new PacketPlayOutAnimation(CustomGroundArmorstand.this, 1);
                for (final Player player : ((ArmorStand) this.getArmorStand()).getWorld().getPlayers()) {
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(animation);
                }
            });
        }
    }

    @Override
    public void respawn() {
        PetBlockHelper.respawn(this, CustomGroundArmorstand.this::spawn);
    }

    @Override
    public void setDieing() {
        this.isDieing = PetBlockHelper.setDieing(this);
    }

    @Override
    public boolean isDieing() {
        return this.isDieing;
    }

    /**
     * Returns the pipeline for managed effect playing.
     *
     * @return effectPipeLine
     */
    @Override
    public EffectPipeline getEffectPipeline() {
        return this.pipeline;
    }

    @Override
    public void setSkin(String skin) {
        PetBlockHelper.setSkin(this,skin);
    }

    @Override
    public void setSkin(Object material, byte data) {
        PetBlockHelper.setSkin(this, (org.bukkit.Material) material, data);
    }

    /**
     * Lets the petblock perform a jump
     */
    @Override
    public void jump() {
        PetBlockHelper.jump(this);
    }

    /**
     * Returns if the petblock is already removed or dead
     *
     * @return dead
     */
    @Override
    public boolean isDead() {
        return PetBlockHelper.isDead(this);
    }


    /**
     * Lets the given player ride on the petblock
     *
     * @param player player
     */
    @Override
    public void ride(Object player) {
        PetBlockHelper.setRiding(this, (Player) player);
    }

    /**
     * Lets the given player wear the petblock
     *
     * @param player oplayer
     */
    @Override
    public void wear(Object player) {
        if (this.getBukkitEntity().getPassenger() == null && ((Player)player).getPassenger() == null) {
            final NBTTagCompound compound = new NBTTagCompound();
            this.b(compound);
            compound.setBoolean("Marker", true);
            this.a(compound);
            this.setCustomNameVisible(false);
            PetBlockHelper.wear(this, (Player) player, null);
        }
    }

    /**
     * Ejects the given player riding from the petblock
     *
     * @param player player
     */
    @Override
    public void eject(Object player) {
        final NBTTagCompound compound = new NBTTagCompound();
        this.b(compound);
        compound.setBoolean("Marker", false);
        this.a(compound);
        this.setCustomNameVisible(true);
        PetBlockHelper.eject(this, (Player) player, null);
    }

    /**
     * Sets the displayName of the petblock
     *
     * @param name name
     */
    @Override
    public void setDisplayName(String name) {
        PetBlockHelper.setDisplayName(this, name);
    }


    /**
     * Returns the armorstand of the petblock
     *
     * @return armorstand
     */
    @Override
    public Object getArmorStand() {
        return this.getBukkitEntity();
    }


    /**
     * Sets the velocity of the petblock
     *
     * @param vector vector
     */
    @Override
    public void setVelocity(Object vector) {
        PetBlockHelper.launch(this, (Vector) vector);
    }

    /**
     * Teleports the the petblock to the given location
     *
     * @param location location
     */
    @Override
    public void teleport(Object location) {
        PetBlockHelper.teleport(this, (Location) location);
    }

    /**
     * Returns the displayName of the petblock
     *
     * @return name
     */
    @Override
    public String getDisplayName() {
        return ((ArmorStand)this.getArmorStand()).getCustomName();
    }

    /**
     * Returns the meta of the petblock
     *
     * @return meta
     */
    @Override
    public PetMeta getMeta() {
        return this.petMeta;
    }

    /**
     * Returns the owner of the petblock
     *
     * @return player
     */
    @Override
    public Object getPlayer() {
        return this.owner;
    }

    /**
     * Removes the petblock
     */
    @Override
    public void remove() {
        PetBlockHelper.remove(this);
    }

    /**
     * Returns the entity being used as engine
     *
     * @return entity
     */
    @Override
    public Object getEngineEntity() {
        if (this.rabbit == null)
            return null;
        return this.rabbit.getEntity();
    }

    /**
     * Returns the location of the entity
     *
     * @return position
     */
    @Override
    public Object getLocation() {
        return ((ArmorStand)this.getArmorStand()).getLocation();
    }

    /**
     * Returns the fixedMetaDataValue
     *
     * @return value
     */
    private FixedMetadataValue getKeepField() {
        return new FixedMetadataValue(Bukkit.getPluginManager().getPlugin("PetBlocks"), true);
    }
}
