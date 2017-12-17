package com.github.shynixn.petblocks.bukkit.nms.v1_10_R1;

import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.business.entity.PetBlockPartEntity;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.logic.business.configuration.ConfigPet;
import com.github.shynixn.petblocks.bukkit.nms.helper.PetBlockHelper;
import com.google.common.collect.Sets;
import net.minecraft.server.v1_10_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Level;

public final class CustomZombie extends EntityZombie implements PetBlockPartEntity {
    private long playedMovingSound = 100000;
    private PetBlock petBlock;

    public CustomZombie(World world) {
        super(world);
    }

    public CustomZombie(Player player, PetBlock petBlock) {
        super(((CraftWorld) player.getWorld()).getHandle());
        this.setSilent(true);
        try {
            final Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
            final Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
            this.ignoreFinalField(bField);
            this.ignoreFinalField(cField);
            cField.setAccessible(true);
            bField.set(this.goalSelector, Sets.newLinkedHashSet());
            bField.set(this.targetSelector, Sets.newLinkedHashSet());
            cField.set(this.goalSelector, Sets.newLinkedHashSet());
            cField.set(this.targetSelector, Sets.newLinkedHashSet());
            this.goalSelector.a(0, new PathfinderGoalFloat(this));
            this.goalSelector.a(1, new OwnerPathfinder(this, petBlock));
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.30000001192092896D * ConfigPet.getInstance().getModifier_petwalking());
        } catch (final Exception exc) {
            PetBlocksPlugin.logger().log(Level.WARNING, "EntityNMS exception.", exc);
        }
        this.petBlock = petBlock;
        this.P = (float) ConfigPet.getInstance().getModifier_petclimbing();
    }

    @Override
    protected void a(BlockPosition blockposition, Block block) {
        this.playedMovingSound = PetBlockHelper.executeMovingSound(this.petBlock, this.playedMovingSound);
        super.a(blockposition, block);
    }

    /**
     * Returns the entity hidden by this object
     *
     * @return spigotEntity
     */
    @Override
    public Object getEntity() {
        return this.getBukkitEntity();
    }

    /**
     * Spawns the entity at the given location
     *
     * @param mLocation location
     */
    @Override
    public void spawn(Object mLocation) {
        final Location location = (Location) mLocation;
        final LivingEntity entity = (LivingEntity) this.getEntity();
        final net.minecraft.server.v1_10_R1.World mcWorld = ((CraftWorld) location.getWorld()).getHandle();
        this.setPosition(location.getX(), location.getY(), location.getZ());
        mcWorld.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 9999999, 1));
        entity.setMetadata("keep", this.getKeepField());
        entity.setCustomNameVisible(false);
        entity.setCustomName("PetBlockIdentifier");
    }

    /**
     * Removes the entity from the world
     */
    @Override
    public void remove() {
        ((LivingEntity) this.getEntity()).remove();
    }

    /**
     * Returns the keepField
     *
     * @return keepField
     */
    private FixedMetadataValue getKeepField() {
        return new FixedMetadataValue(Bukkit.getPluginManager().getPlugin("PetBlocks"), true);
    }

    /**
     * Ignores any final field value
     *
     * @param field field
     * @throws NoSuchFieldException     exception
     * @throws SecurityException        exception
     * @throws IllegalArgumentException exception
     * @throws IllegalAccessException   exception
     */
    private void ignoreFinalField(Field field) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        field.setAccessible(true);
        final Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }
}
