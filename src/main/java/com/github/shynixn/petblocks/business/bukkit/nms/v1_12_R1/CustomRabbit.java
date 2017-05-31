package com.github.shynixn.petblocks.business.bukkit.nms.v1_12_R1;

import com.github.shynixn.petblocks.api.entities.CustomEntity;
import com.github.shynixn.petblocks.api.entities.PetMeta;
import com.github.shynixn.petblocks.business.bukkit.nms.helper.PetBlockHelper;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigPet;
import com.google.common.collect.Sets;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
            this.goalSelector.a(1, new OwnerPathfinder(this, player));
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.30000001192092896D * ConfigPet.getInstance().getModifier_petwalking());
        } catch (final Exception exc) {
            Bukkit.getLogger().log(Level.WARNING, "EntityNMS exception.", exc);
        }
        this.player = player;
        this.petData = meta;
        this.P = (float) ConfigPet.getInstance().getModifier_petclimbing();
    }

    private void ignoreFinalField(Field field) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        field.setAccessible(true);
        final Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }

    @Override
    protected SoundEffect dk() {
        this.playedMovingSound = PetBlockHelper.executeMovingSound(this.getBukkitEntity(), this.player, this.petData, this.playedMovingSound);
        return super.dk();
    }

    @Override
    public void spawn(Location location) {
        final World mcWorld = ((CraftWorld) location.getWorld()).getHandle();
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
