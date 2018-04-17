package com.github.shynixn.petblocks.sponge.nms.v1_12_R1;

import com.flowpowered.math.vector.Vector3d;
import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.business.entity.PetBlockPartEntity;
import com.github.shynixn.petblocks.api.business.enumeration.RideType;
import com.github.shynixn.petblocks.api.persistence.entity.IPosition;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.api.sponge.event.PetBlockMoveEvent;
import com.github.shynixn.petblocks.api.sponge.event.PetBlockSpawnEvent;
import com.github.shynixn.petblocks.sponge.PetBlocksPlugin;
import com.github.shynixn.petblocks.sponge.logic.business.helper.ExtensionMethodsKt;
import com.github.shynixn.petblocks.sponge.logic.persistence.configuration.Config;
import com.github.shynixn.petblocks.sponge.logic.persistence.entity.SpongeLocationBuilder;
import com.github.shynixn.petblocks.sponge.nms.helper.PetBlockPartWrapper;
import com.github.shynixn.petblocks.sponge.nms.helper.PetBlockWrapper;
import net.minecraft.anchor.v1_12_mcpR1.entity.Entity;
import net.minecraft.anchor.v1_12_mcpR1.entity.EntityLivingBase;
import net.minecraft.anchor.v1_12_mcpR1.entity.SharedMonsterAttributes;
import net.minecraft.anchor.v1_12_mcpR1.entity.item.EntityArmorStand;
import net.minecraft.anchor.v1_12_mcpR1.entity.player.EntityPlayer;
import net.minecraft.anchor.v1_12_mcpR1.entity.player.EntityPlayerMP;
import net.minecraft.anchor.v1_12_mcpR1.nbt.NBTTagCompound;
import net.minecraft.anchor.v1_12_mcpR1.network.play.server.SPacketEntityTeleport;
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

final class CustomGroundArmorstand extends EntityArmorStand {

    private PetBlockPartEntity rabbit;
    private PetBlockWrapper wrapper;

    private boolean isSpecial;
    private boolean isGround;
    private boolean firstRide = true;
    private int counter;
    private Vector3d bumper;

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
    protected void updateEntityActionState() {
        if (this.isSpecial) {
            this.getArmorstand().getHealthData().health().set(this.getArmorstand().getHealthData().maxHealth().getMaxValue());
            final PetMeta petData = this.wrapper.getMeta();
            final ArmorStand armorStand = this.getArmorstand();
            final Living engine = (Living) this.rabbit.getEntity();

            if (!armorStand.isRemoved() && armorStand.getPassengers().isEmpty() && engine != null && !armorStand.getVehicle().isPresent()) {
                IPosition location = null;
                if (petData.getAge() >= Config.INSTANCE.getAge_largeticks()) {
                    location = new SpongeLocationBuilder(engine.getLocation().getExtent().getName(), engine.getLocation().getX(), engine.getLocation().getY(), engine.getLocation().getZ(), engine.getTransform().getYaw(), engine.getTransform().getPitch());
                    location.setY(location.getY() - 1.2);
                } else if (petData.getAge() <= Config.INSTANCE.getAge_smallticks())
                    location = new SpongeLocationBuilder(engine.getLocation().getExtent().getName(), engine.getLocation().getX(), engine.getLocation().getY() - 0.7, engine.getLocation().getZ(), engine.getTransform().getYaw(), engine.getTransform().getPitch());
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
                        this.wrapper.getEffectPipeline().playSound(this.wrapper.getLocation(), this.wrapper.getMeta().getEngine().getAmbientSound());
                    }
                    this.counter = 20 * random.nextInt(20) + 1;
                }
                if (engine.isRemoved()) {
                    this.wrapper.remove();
                    PetBlocksApi.getDefaultPetBlockController().remove((PetBlock<Object, Object>) (Object) wrapper);
                }
                if (petData.getParticleEffectMeta() != null) {
                    this.wrapper.getEffectPipeline().playParticleEffect(armorStand.getTransform().add(new Transform<>(armorStand.getTransform().getExtent(), new Vector3d(0, 1, 0))), petData.getParticleEffectMeta());
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
                ex.printStackTrace();
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
                EntityLivingBase entityLiving = (EntityLivingBase) this.hasHumanPassenger();
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
                double d1 = this.posX - this.prevPosX;
                double d0 = this.posZ - this.prevPosZ;
                float f2 = MathHelper.sqrt(d1 * d1 + d0 * d0) * 4.0F;

                if (f2 > 1.0F) {
                    f2 = 1.0F;
                }

                this.limbSwingAmount += (f2 - this.limbSwingAmount) * 0.4F;
                this.limbSwing += this.limbSwingAmount;
            } else {
                EntityLivingBase entityLiving = (EntityLivingBase) this.hasHumanPassenger();
                this.rotationYaw = this.prevRotationYaw = entityLiving.rotationYaw;
                this.rotationPitch = entityLiving.rotationPitch * 0.5F;
                this.setRotation(this.rotationYaw, this.rotationPitch);
                this.rotationYawHead = this.renderYawOffset = this.rotationYaw;

                final float side = entityLiving.moveStrafing * 0.5F;
                final float forw = entityLiving.moveForward;
                Vector3d v = new Vector3d();
                org.spongepowered.api.world.World world = (org.spongepowered.api.world.World) this.world;
                final SpongeLocationBuilder l = new SpongeLocationBuilder(world.getName(), this.posX, this.posY, this.posZ, 0, 0);

                if (side < 0.0F) {
                    l.setYaw(entityLiving.rotationYaw - 90);
                    v = v.add(l.getDirection().normalize().mul(-0.5));
                } else if (side > 0.0F) {
                    l.setYaw(entityLiving.rotationYaw + 90);
                    v = v.add(l.getDirection().normalize().mul(-0.5));
                }

                if (forw < 0.0F) {
                    l.setYaw(entityLiving.rotationYaw);
                    v = v.add(l.getDirection().normalize().mul(0.5));
                } else if (forw > 0.0F) {
                    l.setYaw(entityLiving.rotationYaw);
                    v = v.add(l.getDirection().normalize().mul(0.5));
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
                    l.addCoordinates(v.getX(), v.getY(), v.getZ());
                    this.setPosition(l.getX(), l.getY(), l.getZ());
                } else {
                    v = v.mul(2.25).mul(Config.INSTANCE.getModifier_petriding());
                    l.addCoordinates(v.getX(), v.getY(), v.getZ());
                    this.setPosition(l.getX(), l.getY(), l.getZ());
                }
                final Vec3d vec3d = new Vec3d(this.posX, this.posY, this.posZ);
                final Vec3d vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
                final RayTraceResult movingobjectposition = this.world.rayTraceBlocks(vec3d, vec3d1);
                if (movingobjectposition == null) {
                    this.bumper = l.toVector();
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
        final PetBlockSpawnEvent event = new PetBlockSpawnEvent(wrapper);
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
            ((PetBlockWrapper) (Object) this.wrapper).setHealth(Config.INSTANCE.getCombat_health());
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
