package com.github.shynixn.petblocks.sponge.nms.v1_12_R1;

import com.flowpowered.math.vector.Vector3d;
import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.business.entity.PetBlockPartEntity;
import com.github.shynixn.petblocks.api.business.enumeration.RideType;
import com.github.shynixn.petblocks.api.business.service.ParticleService;
import com.github.shynixn.petblocks.api.business.service.SoundService;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.api.persistence.entity.Position;
import com.github.shynixn.petblocks.api.sponge.event.PetBlockMoveEvent;
import com.github.shynixn.petblocks.api.sponge.event.PetBlockSpawnEvent;
import com.github.shynixn.petblocks.core.logic.persistence.entity.PositionEntity;
import com.github.shynixn.petblocks.sponge.logic.business.helper.ExtensionMethodsKt;
import com.github.shynixn.petblocks.sponge.logic.persistence.configuration.Config;
import com.github.shynixn.petblocks.sponge.nms.helper.PetBlockPartWrapper;
import com.github.shynixn.petblocks.sponge.nms.helper.PetBlockWrapper;
import net.minecraft.anchor.v1_12_mcpR1.entity.Entity;
import net.minecraft.anchor.v1_12_mcpR1.entity.EntityLivingBase;
import net.minecraft.anchor.v1_12_mcpR1.entity.MoverType;
import net.minecraft.anchor.v1_12_mcpR1.entity.SharedMonsterAttributes;
import net.minecraft.anchor.v1_12_mcpR1.entity.item.EntityArmorStand;
import net.minecraft.anchor.v1_12_mcpR1.entity.player.EntityPlayer;
import net.minecraft.anchor.v1_12_mcpR1.entity.player.EntityPlayerMP;
import net.minecraft.anchor.v1_12_mcpR1.nbt.NBTTagCompound;
import net.minecraft.anchor.v1_12_mcpR1.network.play.server.SPacketEntityTeleport;
import net.minecraft.anchor.v1_12_mcpR1.util.math.AxisAlignedBB;
import net.minecraft.anchor.v1_12_mcpR1.util.math.MathHelper;
import net.minecraft.anchor.v1_12_mcpR1.util.math.RayTraceResult;
import net.minecraft.anchor.v1_12_mcpR1.util.math.Vec3d;
import net.minecraft.anchor.v1_12_mcpR1.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CustomGroundArmorstand extends EntityArmorStand {

    private PetBlockPartEntity rabbit;
    private PetBlockWrapper wrapper;

    private boolean isSpecial;
    private boolean isGround;
    private boolean firstRide = true;
    private int counter;
    private Vector3d bumper;
    private boolean isGroundRiding;

    private final SoundService soundService = PetBlocksApi.INSTANCE.resolve(SoundService.class);
    private final ParticleService particleService = PetBlocksApi.INSTANCE.resolve(ParticleService.class);

    /**
     * Default necessary constructor.
     *
     * @param world world
     */
    public CustomGroundArmorstand(World world) {
        super(world);
    }

    /**
     * Default necessary constructor.
     *
     * @param world world
     * @param d0    d0
     * @param d1    d1
     * @param d2    d2
     */
    public CustomGroundArmorstand(World world, double d0, double d1, double d2) {
        super(world, d0, d1, d2);
    }

    /**
     * Custom constructor to spawn the petblock pet.
     *
     * @param blockWrapper wrapper
     * @param location     location
     */
    public CustomGroundArmorstand(Transform<org.spongepowered.api.world.World> location, PetBlockWrapper blockWrapper) {
        super((World) location.getExtent());
        this.isSpecial = true;
        this.wrapper = blockWrapper;

        this.spawn(location);
    }

    @Override
    public void move(MoverType type, double x, double y, double z) {
        super.move(type, x, y, z);
        this.recalculatePosition();
    }

    private void recalculatePosition() {
        if (this.hasHumanPassenger() != null) {
            final AxisAlignedBB localAxisAlignedBB = this.getEntityBoundingBox();
            this.posX = ((localAxisAlignedBB.minX + localAxisAlignedBB.maxX) / 2.0D);
            this.posZ = ((localAxisAlignedBB.minZ + localAxisAlignedBB.maxZ) / 2.0D);
            this.posY = (localAxisAlignedBB.minY + Config.INSTANCE.getHitBoxYAxeModification());
            this.isGroundRiding = true;
        }
    }

    @Override
    protected void updateEntityActionState() {
        if (this.isSpecial) {
            if (this.isGroundRiding && this.hasHumanPassenger() == null) {
                ((Living) this.rabbit.getEntity()).setLocation(((Living) this.rabbit.getEntity()).getLocation().add(0, 2, 0));
                this.isGroundRiding = false;
            }

            this.getArmorstand().getHealthData().health().set(this.getArmorstand().getHealthData().maxHealth().getMaxValue());
            final PetMeta petData = this.wrapper.getMeta();
            final ArmorStand armorStand = this.getArmorstand();
            final Living engine = (Living) this.rabbit.getEntity();

            if (!armorStand.isRemoved() && armorStand.getPassengers().isEmpty() && engine != null && !armorStand.getVehicle().isPresent()) {
                Position location = null;
                if (petData.getAge() >= Config.INSTANCE.getAge_largeticks()) {
                    location = new PositionEntity();
                    location.setWorldName(engine.getLocation().getExtent().getName());
                    location.setX(engine.getLocation().getX());
                    location.setY(engine.getLocation().getY());
                    location.setZ(engine.getLocation().getZ());
                    location.setYaw(engine.getTransform().getYaw());
                    location.setPitch(engine.getTransform().getPitch());
                    location.setY(location.getY() - 1.2);
                } else if (petData.getAge() <= Config.INSTANCE.getAge_smallticks()) {
                    location = new PositionEntity();
                    location.setWorldName(engine.getLocation().getExtent().getName());
                    location.setX(engine.getLocation().getX());
                    location.setY(engine.getLocation().getY());
                    location.setZ(engine.getLocation().getZ());
                    location.setYaw(engine.getTransform().getYaw());
                    location.setPitch(engine.getTransform().getPitch());
                    location.setY(location.getY() - 0.7);
                }
                if (location != null) {
                    this.setLocationAndAngles(location.getX(), location.getY() + 0.2, location.getZ(), (float) location.getYaw(), (float) location.getPitch());
                    final SPacketEntityTeleport animation = new SPacketEntityTeleport(this);
                    for (final Player player : this.getArmorstand().getWorld().getPlayers()) {
                        ((EntityPlayerMP) player).connection.sendPacket(animation);
                    }
                }
                if (this.counter <= 0) {
                    final Random random = new Random();
                    if (!engine.isOnGround() || petData.getEngine().getEntityType().equalsIgnoreCase("ZOMBIE")) {
                        if (petData.isSoundEnabled()) {
                            this.soundService.playSound(this.wrapper.getLocation().getLocation(), this.wrapper.getMeta().getEngine().getAmbientSound(), (Player) petData.getPlayerMeta().getPlayer());
                        }
                    }
                    this.counter = 20 * random.nextInt(20) + 1;
                }
                if (engine.isRemoved()) {
                    return;
                }
                if (petData.getParticleEffectMeta() != null) {
                    this.particleService.<Location, Player>playParticle(armorStand.getLocation().add(0, 1, 0), petData.getParticleEffectMeta(), petData.getPlayerMeta().<Player>getPlayer());
                }
                this.counter--;
            } else if (engine != null) {
                engine.setLocation(armorStand.getLocation());
            }
            try {
                if (petData.getAge() >= Config.INSTANCE.getAge_maxticks()) {
                    if (Config.INSTANCE.isAge_deathOnMaxTicks() && !this.wrapper.isDieing()) {
                        this.wrapper.setDieing();
                    }
                } else {
                    boolean respawn = false;
                    if (petData.getAge() < Config.INSTANCE.getAge_largeticks()) {
                        respawn = true;
                    }
                    petData.setAge(petData.getAge() + 1);
                    if (petData.getAge() >= Config.INSTANCE.getAge_largeticks() && respawn) {
                        this.wrapper.respawn();
                    }
                }
            } catch (final Exception ex) {
                Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING, "Failed to manage aging.", ex);
            }
            armorStand.offer(Keys.FIRE_TICKS, 0);
            if (engine != null) {
                engine.offer(Keys.FIRE_TICKS, 0);
            }
            Sponge.getEventManager().post(new PetBlockMoveEvent(this.wrapper));
        }
        super.updateEntityActionState();
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.hasHumanPassenger() != null) {
            if (this.wrapper.getMeta().getEngine().getRideType() == RideType.RUNNING) {
                final EntityLivingBase entityLiving = (EntityLivingBase) this.hasHumanPassenger();
                this.rotationYaw = this.prevRotationYaw = entityLiving.rotationYaw;
                this.rotationPitch = entityLiving.rotationPitch * 0.5F;
                this.setRotation(this.rotationYaw, this.rotationPitch);
                this.rotationYawHead = this.renderYawOffset = this.rotationYaw;

                strafe = entityLiving.moveStrafing * 0.5F;
                forward = entityLiving.moveForward;

                if (forward <= 0.0F) {
                    forward *= 0.25F;
                }
                if (this.onGround && this.isJumping()) {
                    this.motionY = 0.5D;
                }

                this.stepHeight = (float) Config.INSTANCE.getModifier_petclimbing();
                this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;
                if (!this.world.isRemote) {
                    this.setAIMoveSpeed((float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                    super.travel(strafe * (float) Config.INSTANCE.getModifier_petriding() * 0.75F, vertical, forward * (float) Config.INSTANCE.getModifier_petriding() * 0.75F);
                }

                this.prevLimbSwingAmount = this.limbSwingAmount;
                final double d1 = this.posX - this.prevPosX;
                final double d0 = this.posZ - this.prevPosZ;
                float f2 = MathHelper.sqrt(d1 * d1 + d0 * d0) * 4.0F;

                if (f2 > 1.0F) {
                    f2 = 1.0F;
                }

                this.limbSwingAmount += (f2 - this.limbSwingAmount) * 0.4F;
                this.limbSwing += this.limbSwingAmount;
            } else {
                final EntityLivingBase entityLiving = (EntityLivingBase) this.hasHumanPassenger();
                this.rotationYaw = this.prevRotationYaw = entityLiving.rotationYaw;
                this.rotationPitch = entityLiving.rotationPitch * 0.5F;
                this.setRotation(this.rotationYaw, this.rotationPitch);
                this.rotationYawHead = this.renderYawOffset = this.rotationYaw;

                final float side = entityLiving.moveStrafing * 0.5F;
                final float forw = entityLiving.moveForward;
                Vector3d v = new Vector3d();
                final org.spongepowered.api.world.World world = (org.spongepowered.api.world.World) this.world;
                final Position l = new PositionEntity();
                l.setWorldName(world.getName());
                l.setX(this.posX);
                l.setY(this.posY);
                l.setZ(this.posZ);

                if (side < 0.0F) {
                    l.setYaw(entityLiving.rotationYaw - 90);
                    v = v.add(ExtensionMethodsKt.getDirection(l).normalize().mul(-0.5));
                } else if (side > 0.0F) {
                    l.setYaw(entityLiving.rotationYaw + 90);
                    v = v.add(ExtensionMethodsKt.getDirection(l).normalize().mul(-0.5));
                }

                if (forw < 0.0F) {
                    l.setYaw(entityLiving.rotationYaw);
                    v = v.add(ExtensionMethodsKt.getDirection(l).normalize().mul(0.5));
                } else if (forw > 0.0F) {
                    l.setYaw(entityLiving.rotationYaw);
                    v = v.add(ExtensionMethodsKt.getDirection(l).normalize().mul(0.5));
                }
                if (this.firstRide) {
                    this.firstRide = false;
                    v = new Vector3d(v.getX(), 1, v.getZ());
                }
                if (this.isJumping()) {
                    v = new Vector3d(v.getX(), 0.5F, v.getZ());
                    this.isGround = true;
                    this.wrapper.setHitflor(false);
                } else if (this.isGround) {
                    v = new Vector3d(v.getX(), -0.2F, v.getZ());
                }
                if (this.wrapper.getHitflor()) {
                    v = new Vector3d(v.getX(), 0, v.getZ());
                    v = v.mul(2.25).mul(Config.INSTANCE.getModifier_petriding());
                    l.setX(l.getX() + v.getX());
                    l.setY(l.getY() + v.getY());
                    l.setZ(l.getZ() + v.getZ());
                    this.setPosition(l.getX(), l.getY(), l.getZ());
                } else {
                    v = v.mul(2.25).mul(Config.INSTANCE.getModifier_petriding());
                    l.setX(l.getX() + v.getX());
                    l.setY(l.getY() + v.getY());
                    l.setZ(l.getZ() + v.getZ());
                    this.setPosition(l.getX(), l.getY(), l.getZ());
                }
                final Vec3d vec3d = new Vec3d(this.posX, this.posY, this.posZ);
                final Vec3d vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
                final RayTraceResult movingobjectposition = this.world.rayTraceBlocks(vec3d, vec3d1);
                if (movingobjectposition == null) {
                    this.bumper = new Vector3d(l.getX(), l.getY(), l.getZ());
                } else {
                    if (this.bumper != null && Config.INSTANCE.isFollow_wallcolliding())
                        this.setPosition(this.bumper.getX(), this.bumper.getY(), this.bumper.getZ());
                }
            }
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    public void spawn(Transform<org.spongepowered.api.world.World> location) {
        final PetBlockSpawnEvent event = new PetBlockSpawnEvent(this.wrapper);
        Sponge.getEventManager().post(event);
        if (!event.isCancelled()) {
            if (this.wrapper.getMeta().getEngine().getEntityType().equalsIgnoreCase("RABBIT")) {
                this.rabbit = new PetBlockPartWrapper((Living) (Object) new CustomRabbit(location.getLocation(), this.wrapper));

            } else if (this.wrapper.getMeta().getEngine().getEntityType().equalsIgnoreCase("ZOMBIE")) {
                this.rabbit = new PetBlockPartWrapper((Living) (Object) new CustomZombie(location.getLocation(), this.wrapper));
            }

            this.dead = false;
            this.isDead = false;

            this.rabbit.spawn(location.add(new Transform<>(location.getExtent(), new Vector3d(0.0, 1.2, 0.0))));
            final World mcWorld = (World) location.getExtent();
            this.setPosition(location.getLocation().getX(), location.getLocation().getY(), location.getLocation().getZ());
            mcWorld.spawnEntity(this);
            final NBTTagCompound compound = new NBTTagCompound();
            compound.setBoolean("invulnerable", true);
            compound.setBoolean("Invisible", true);
            compound.setBoolean("PersistenceRequired", true);
            compound.setBoolean("ShowArms", true);
            compound.setBoolean("NoBasePlate", true);
            this.readEntityFromNBT(compound);
            this.getArmorstand().gravity().set(false);
            this.getArmorstand().getBodyPartRotationalData().bodyRotation().set(new Vector3d(0, 0, 2878));
            this.getArmorstand().getBodyPartRotationalData().leftArmDirection().set(new Vector3d(2878, 0, 0));
            this.getArmorstand().offer(Keys.CUSTOM_NAME_VISIBLE, true);
            this.getArmorstand().offer(Keys.DISPLAY_NAME, ExtensionMethodsKt.translateToText(this.wrapper.getMeta().getPetDisplayName()));
            this.wrapper.setHealth(Config.INSTANCE.getCombat_health());
            this.getArmorstand().setHelmet((ItemStack) this.wrapper.getMeta().getHeadItemStack());
            if (this.wrapper.getMeta().getAge() >= Config.INSTANCE.getAge_largeticks()) {
                this.getArmorstand().small().set(false);
            } else {
                this.getArmorstand().small().set(true);
            }
        }
    }

    private ArmorStand getArmorstand() {
        return (ArmorStand) (Object) this;
    }

    private boolean isJumping() {
        return !this.getPassengers().isEmpty() && ((EntityLivingBase) this.getPassengers().get(0)).isJumping;
    }

    private Player hasHumanPassenger() {
        for (final Entity entity : this.getPassengers()) {
            if (entity instanceof EntityPlayer) {
                return (Player) entity;
            }
        }
        return null;
    }
}
