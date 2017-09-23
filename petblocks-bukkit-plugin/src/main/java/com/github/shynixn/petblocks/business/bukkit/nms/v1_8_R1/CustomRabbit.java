package com.github.shynixn.petblocks.business.bukkit.nms.v1_8_R1;

import com.github.shynixn.petblocks.api.business.entity.PetBlockPartEntity;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.business.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.business.bukkit.nms.helper.PetBlockHelper;
import com.github.shynixn.petblocks.business.logic.business.configuration.ConfigPet;
import net.minecraft.server.v1_8_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.util.UnsafeList;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.logging.Level;

public final class CustomRabbit extends EntityRabbit implements PetBlockPartEntity {
    private long playedMovingSound = 100000;
    private PetMeta petMeta;
    private Player player;

    public CustomRabbit(World world) {
        super(world);
    }

    @Override
    public EntityAgeable createChild(EntityAgeable entityAgeable) {
        return null;
    }

    public CustomRabbit(Player player, PetMeta meta) {
        super(((CraftWorld) player.getWorld()).getHandle());
        this.b(true);
        try {
            final Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
            bField.setAccessible(true);
            final Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
            cField.setAccessible(true);
            bField.set(this.goalSelector, new UnsafeList<PathfinderGoalSelector>());
            bField.set(this.targetSelector, new UnsafeList<PathfinderGoalSelector>());
            cField.set(this.goalSelector, new UnsafeList<PathfinderGoalSelector>());
            cField.set(this.targetSelector, new UnsafeList<PathfinderGoalSelector>());
            this.getAttributeInstance(GenericAttributes.d).setValue(0.30000001192092896D * ConfigPet.getInstance().getModifier_petwalking());
            this.goalSelector.a(0, new PathfinderGoalFloat(this));
            this.goalSelector.a(1, new OwnerPathfinder(this, player));
        } catch (final Exception exc) {
            PetBlocksPlugin.logger().log(Level.WARNING, "EntityNMS exception.", exc);
        }
        this.player = player;
        this.petMeta = meta;
        this.S = (float) ConfigPet.getInstance().getModifier_petclimbing();
    }

    @Override
    protected String ck() {
        this.playedMovingSound = PetBlockHelper.executeMovingSound(this.getBukkitEntity(), this.player, this.petMeta, this.playedMovingSound);
        return "mob.rabbit.hop";
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
        final net.minecraft.server.v1_8_R1.World mcWorld = ((CraftWorld) location.getWorld()).getHandle();
        this.setPosition(location.getX(), location.getY(), location.getZ());
        mcWorld.addEntity(this, SpawnReason.CUSTOM);
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
}
