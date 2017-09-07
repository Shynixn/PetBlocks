package com.github.shynixn.petblocks.business.bukkit.nms.v1_11_R1;

import com.github.shynixn.petblocks.api.bukkit.event.PetBlockSpawnEvent;
import com.github.shynixn.petblocks.api.entities.CustomEntity;
import com.github.shynixn.petblocks.api.entities.MoveType;
import com.github.shynixn.petblocks.api.entities.Movement;
import com.github.shynixn.petblocks.api.entities.PetBlock;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.business.bukkit.nms.helper.PetBlockHelper;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigPet;
import com.github.shynixn.petblocks.business.logic.persistence.entity.PetData;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
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
    private CustomEntity rabbit;
    private int counter;

    private boolean isDieing;
    private double health = 20.0;

    private boolean hitflor;

    public CustomGroundArmorstand(World world) {
        super(world);
    }

    public CustomGroundArmorstand(Location location, PetMeta meta) {
        super(((CraftWorld) location.getWorld()).getHandle());
        this.isSpecial = true;
        this.petMeta = (PetData) meta;
        this.owner = petMeta.getOwner();
        if (petMeta.getMovementType() == Movement.HOPPING)
            this.rabbit = new CustomRabbit(this.owner, meta);
        else if (petMeta.getMovementType() == Movement.CRAWLING)
            this.rabbit = new CustomZombie(this.owner, meta);
        this.spawn(location);
    }

    public CustomGroundArmorstand(World world, double d0, double d1, double d2) {
        super(world, d0, d1, d2);
    }

    private boolean isJumping() {
        final Field jump;
        try {
            jump = EntityLiving.class.getDeclaredField("bd");
            jump.setAccessible(true);
            return !this.passengers.isEmpty() && jump.getBoolean(this.passengers.get(0));
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
            Bukkit.getLogger().log(Level.WARNING, "EntityNMS exception.", e1);
        }
        return false;
    }

    private EntityHuman hasHumanPassenger() {
        if (this.passengers != null) {
            for (final Entity entity : this.passengers) {
                if (entity instanceof EntityHuman)
                    return (EntityHuman) entity;
            }
        }
        return null;
    }

    @Override
    protected void doTick() {
        if (this.isSpecial) {
            this.counter = PetBlockHelper.doTick(this.counter, this, location -> {
                CustomGroundArmorstand.this.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
                final PacketPlayOutEntityTeleport animation = new PacketPlayOutEntityTeleport(CustomGroundArmorstand.this);
                for (final Player player : CustomGroundArmorstand.this.getArmorStand().getWorld().getPlayers()) {
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(animation);
                }
            });
        }
        super.doTick();
    }

    @Override
    public void g(float sideMot, float forMot) {
        if (this.isSpecial) {
            if (this.hasHumanPassenger() != null) {
                if (this.petMeta.getMoveType() == MoveType.WALKING) {
                    this.lastYaw = (this.yaw = this.hasHumanPassenger().yaw);
                    this.pitch = (this.hasHumanPassenger().pitch * 0.5F);
                    this.setYawPitch(this.yaw, this.pitch);
                    this.aP = (this.aN = this.yaw);
                    sideMot = this.hasHumanPassenger().be * 0.5F;
                    forMot = this.hasHumanPassenger().bf;
                    if (forMot <= 0.0F) {
                        forMot *= 0.25F;
                    }
                    if (this.onGround && this.isJumping()) {
                        this.motY = 0.5D;
                    }
                    this.P = (float) ConfigPet.getInstance().getModifier_petclimbing();
                    this.aR = (this.cq() * 0.1F);
                    if (!this.world.isClientSide) {
                        this.l(0.35F);
                        super.g(sideMot * (float) ConfigPet.getInstance().getModifier_petriding(), forMot * (float) ConfigPet.getInstance().getModifier_petriding());
                    }
                    this.aF = this.aG;
                    final double d0 = this.locX - this.lastX;
                    final double d1 = this.locZ - this.lastZ;
                    float f4 = MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0F;

                    if (f4 > 1.0F) {
                        f4 = 1.0F;
                    }

                    this.aG += (f4 - this.aG) * 0.4F;
                    this.aH += this.aG;
                } else {
                    final float side = this.hasHumanPassenger().be * 0.5F;
                    final float forw = this.hasHumanPassenger().bf;
                    final Vector v = new Vector();
                    final Location l = new Location(this.world.getWorld(), this.locX, this.locY, this.locZ);
                    if (side < 0.0F) {
                        l.setYaw(this.hasHumanPassenger().yaw - 90);
                        v.add(l.getDirection().normalize().multiply(-0.5));
                    } else if (side > 0.0F) {
                        l.setYaw(this.hasHumanPassenger().yaw + 90);
                        v.add(l.getDirection().normalize().multiply(-0.5));
                    }

                    if (forw < 0.0F) {
                        l.setYaw(this.hasHumanPassenger().yaw);
                        v.add(l.getDirection().normalize().multiply(0.5));
                    } else if (forw > 0.0F) {
                        l.setYaw(this.hasHumanPassenger().yaw);
                        v.add(l.getDirection().normalize().multiply(0.5));
                    }

                    this.lastYaw = this.yaw = this.hasHumanPassenger().yaw - 180;
                    this.pitch = this.hasHumanPassenger().pitch * 0.5F;
                    this.lastYaw = (this.yaw = this.hasHumanPassenger().yaw);
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
            final World mcWorld = ((CraftWorld) location.getWorld()).getHandle();
            this.setPosition(location.getX(), location.getY(), location.getZ());
            mcWorld.addEntity(this, SpawnReason.CUSTOM);
            final NBTTagCompound compound = new NBTTagCompound();
            compound.setBoolean("invulnerable", true);
            compound.setBoolean("Invisible", true);
            compound.setBoolean("PersistenceRequired", true);
            compound.setBoolean("ShowArms", true);
            compound.setBoolean("NoBasePlate", true);
            this.a(compound);
            this.getArmorStand().setBodyPose(new EulerAngle(0, 0, 2878));
            this.getArmorStand().setLeftArmPose(new EulerAngle(2878, 0, 0));
            this.getArmorStand().setMetadata("keep", this.getKeepField());
            NMSRegistry.rollbackWorldGuardSpawn(location);
            this.getArmorStand().setCustomNameVisible(true);
            this.getArmorStand().setCustomName(this.petMeta.getDisplayName());
            this.getArmorStand().setRemoveWhenFarAway(false);
            this.rabbit.getSpigotEntity().setRemoveWhenFarAway(false);
            this.health = ConfigPet.getInstance().getCombat_health();
            if (this.petMeta == null)
                return;
            PetBlockHelper.setItemConsideringAge(this);
        }
    }

    @Override
    public void teleportWithOwner(Location location) {
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
                for (final Player player : CustomGroundArmorstand.this.getArmorStand().getWorld().getPlayers()) {
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
    public LivingEntity getMovementEntity() {
        if (this.rabbit == null)
            return null;
        return this.rabbit.getSpigotEntity();
    }

    @Override
    public void setDieing() {
        this.isDieing = PetBlockHelper.setDieing(this);
    }

    @Override
    public void refreshHeadMeta() {
        PetBlockHelper.refreshHeadItemMeta(this, this.getArmorStand().getHelmet());
    }

    @Override
    public boolean isDieing() {
        return this.isDieing;
    }

    @Override
    public void setSkin(String skin) {
        PetBlockHelper.setSkin(this, skin);
    }

    @Override
    public void setSkin(org.bukkit.Material material, byte data) {
        PetBlockHelper.setSkin(this, material, data);
    }

    @Override
    public void remove() {
        PetBlockHelper.remove(this);
    }

    @Override
    public void ride(Player player) {
        PetBlockHelper.setRiding(this, player);
    }

    @Override
    public boolean isDead() {
        return PetBlockHelper.isDead(this);
    }

    @Override
    public void teleport(Location location) {
        PetBlockHelper.teleport(this, location);
    }

    @Override
    public void teleport(org.bukkit.entity.Entity entity) {
        this.teleport(entity.getLocation());
    }

    @Override
    public void jump() {
        PetBlockHelper.jump(this);
    }

    @Override
    public void launch(Vector vector) {
        PetBlockHelper.launch(this, vector);
    }

    @Override
    public void wear(final Player player) {
        final NBTTagCompound compound = new NBTTagCompound();
        this.b(compound);
        compound.setBoolean("Marker", true);
        this.a(compound);
        this.setCustomNameVisible(false);
        PetBlockHelper.wear(this, player, location -> {
            final PacketPlayOutMount animation = new PacketPlayOutMount(((CraftPlayer) player).getHandle());
            for (final Player player2 : CustomGroundArmorstand.this.getArmorStand().getWorld().getPlayers()) {
                ((CraftPlayer) player2).getHandle().playerConnection.sendPacket(animation);
            }
        });
    }

    @Override
    public void setDisplayName(String name) {
        PetBlockHelper.setDisplayName(this, name);
    }

    @Override
    public ArmorStand getArmorStand() {
        return (ArmorStand) this.getBukkitEntity();
    }

    @Override
    public void eject(final Player player) {
        final NBTTagCompound compound = new NBTTagCompound();
        this.b(compound);
        compound.setBoolean("Marker", false);
        this.a(compound);
        this.setCustomNameVisible(true);
        PetBlockHelper.eject(this, player, location -> {
            final PacketPlayOutMount animation = new PacketPlayOutMount(((CraftPlayer) player).getHandle());
            for (final Player player2 : CustomGroundArmorstand.this.getArmorStand().getWorld().getPlayers()) {
                ((CraftPlayer) player2).getHandle().playerConnection.sendPacket(animation);
            }
        });
    }

    @Override
    public Player getOwner() {
        return this.owner;
    }

    @Override
    public String getDisplayName() {
        return this.getArmorStand().getCustomName();
    }

    @Override
    public Location getLocation() {
        return this.getArmorStand().getLocation();
    }

    @Override
    public PetMeta getPetMeta() {
        return this.petMeta;
    }

    private FixedMetadataValue getKeepField() {
        return new FixedMetadataValue(Bukkit.getPluginManager().getPlugin("PetBlocks"), true);
    }
}
