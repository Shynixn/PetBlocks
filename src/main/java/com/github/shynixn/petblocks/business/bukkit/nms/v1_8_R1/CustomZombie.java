package com.github.shynixn.petblocks.business.bukkit.nms.v1_8_R1;

import com.github.shynixn.petblocks.api.entities.PetMeta;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigPet;
import com.github.shynixn.petblocks.api.entities.CustomEntity;
import com.github.shynixn.petblocks.business.bukkit.nms.helper.PetBlockHelper;
import net.minecraft.server.v1_8_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.util.UnsafeList;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;

/**
 * Created by Shynixn
 */
public final class CustomZombie extends EntityZombie implements CustomEntity {
    private long playedMovingSound = 100000;
    private PetMeta petMeta;
    private Player player;

    public CustomZombie(World world) {
        super(world);
    }

    public CustomZombie(Player player, PetMeta meta) {
        super(((CraftWorld) player.getWorld()).getHandle());
        this.b(true);
        try {
            Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
            bField.setAccessible(true);
            Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
            cField.setAccessible(true);
            bField.set(this.goalSelector, new UnsafeList<PathfinderGoalSelector>());
            bField.set(this.targetSelector, new UnsafeList<PathfinderGoalSelector>());
            cField.set(this.goalSelector, new UnsafeList<PathfinderGoalSelector>());
            cField.set(this.targetSelector, new UnsafeList<PathfinderGoalSelector>());
            this.getAttributeInstance(GenericAttributes.d).setValue(0.30000001192092896D * ConfigPet.getInstance().getModifier_petwalking());
            this.goalSelector.a(0, new PathfinderGoalFloat(this));
            this.goalSelector.a(1, new OwnerPathfinder(this, player));
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        this.player = player;
        this.petMeta = meta;
        this.S = (float) ConfigPet.getInstance().getModifier_petclimbing();
    }

    @Override
    public void spawn(Location location) {
        net.minecraft.server.v1_8_R1.World mcWorld = ((CraftWorld) location.getWorld()).getHandle();
        this.setPosition(location.getX(), location.getY(), location.getZ());
        mcWorld.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        this.getSpigotEntity().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 9999999, 1));
        this.getSpigotEntity().setMetadata("keep", this.getKeepField());
        this.getSpigotEntity().setCustomNameVisible(false);
        this.getSpigotEntity().setCustomName("PetBlockIdentifier");
    }

    @Override
    protected void a(BlockPosition blockposition, Block block) {
        this.playedMovingSound = PetBlockHelper.executeMovingSound(this.getBukkitEntity(), this.player, this.petMeta, this.playedMovingSound);
        super.a(blockposition, block);
    }

    @Override
    public LivingEntity getSpigotEntity() {
        return (LivingEntity) this.getBukkitEntity();
    }

    private FixedMetadataValue getKeepField() {
        return new FixedMetadataValue(Bukkit.getPluginManager().getPlugin("PetBlocks"), true);
    }
}
