package com.github.shynixn.petblocks.business.bukkit.nms.v1_8_R3;

import com.github.shynixn.petblocks.api.entities.CustomEntity;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.business.bukkit.nms.helper.PetBlockHelper;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigPet;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.logging.Level;

public final class CustomRabbit extends EntityRabbit implements CustomEntity {
    private Player player;
    private PetMeta petData;

    private long playedMovingSound = 100000;

    public CustomRabbit(World world) {
        super(world);
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
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.30000001192092896D * ConfigPet.getInstance().getModifier_petwalking());

            this.goalSelector.a(0, new PathfinderGoalFloat(this));
            this.goalSelector.a(1, new OwnerPathfinder(this, player));
        } catch (final Exception exc) {
            Bukkit.getLogger().log(Level.WARNING, "EntityNMS exception.", exc);
        }
        this.player = player;
        this.petData = meta;
        this.S = (float) ConfigPet.getInstance().getModifier_petclimbing();
    }

    @Override
    protected String cm() {
        this.playedMovingSound = PetBlockHelper.executeMovingSound(this.getBukkitEntity(), this.player, this.petData, this.playedMovingSound);
        return "mob.rabbit.hop";
    }

    @Override
    public void spawn(Location location) {
        final net.minecraft.server.v1_8_R3.World mcWorld = ((CraftWorld) location.getWorld()).getHandle();
        this.setPosition(location.getX(), location.getY(), location.getZ());
        mcWorld.addEntity(this, SpawnReason.CUSTOM);
        this.getSpigotEntity().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 9999999, 1));
        this.getSpigotEntity().setMetadata("keep", this.getKeepField());
        this.getSpigotEntity().setCustomNameVisible(false);
        this.getSpigotEntity().setCustomName("PetBlockIdentifier");
    }

    @Override
    public Rabbit getSpigotEntity() {
        return (Rabbit) this.getBukkitEntity();
    }

    private FixedMetadataValue getKeepField() {
        return new FixedMetadataValue(Bukkit.getPluginManager().getPlugin("PetBlocks"), true);
    }
}
