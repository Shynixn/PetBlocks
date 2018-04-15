package com.github.shynixn.petblocks.sponge.nms.v1_12_R1;

import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.sponge.logic.persistence.configuration.Config;
import com.google.common.collect.Sets;
import net.minecraft.anchor.v1_12_mcpR1.entity.SharedMonsterAttributes;
import net.minecraft.anchor.v1_12_mcpR1.entity.ai.EntityAISwimming;
import net.minecraft.anchor.v1_12_mcpR1.entity.passive.EntityRabbit;
import net.minecraft.anchor.v1_12_mcpR1.util.SoundEvent;
import net.minecraft.anchor.v1_12_mcpR1.world.World;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;

public final class CustomRabbit extends EntityRabbit {
    private PetBlock petBlock;
    private long playedMovingSound = 100000;

    public CustomRabbit(World worldIn) {
        super(worldIn);
    }

    public CustomRabbit(Location<org.spongepowered.api.world.World> location, PetBlock petBlock) {
        super((World) (location).getExtent());
        this.setSilent(true);
        try {
            this.tasks.taskEntries = Sets.newLinkedHashSet();
            this.tasks.executingTaskEntries = Sets.newLinkedHashSet();
            this.targetTasks.taskEntries = Sets.newLinkedHashSet();
            this.targetTasks.executingTaskEntries = Sets.newLinkedHashSet();
            this.tasks.addTask(0, new EntityAISwimming(this));
            this.tasks.addTask(1, new OwnerPathfinder(this, petBlock));
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                    .setBaseValue(0.30000001192092896D * Config.INSTANCE.getModifier_petwalking());
        } catch (final Exception exc) {
            exc.printStackTrace();
        }
        this.petBlock = petBlock;
        this.stepHeight = (float) Config.INSTANCE.getModifier_petclimbing();
    }

    @Override
    protected SoundEvent getJumpSound() {
        final long milli = System.currentTimeMillis();
        if (milli - this.playedMovingSound > 500) {
            this.petBlock.getEffectPipeline().playSound(this.petBlock.getLocation(), this.petBlock.getMeta().getEngine().getWalkingSound());
            this.playedMovingSound = milli;
        }
        return super.getJumpSound();
    }

    /**
     * Spawns the entity at the given location.
     *
     * @param location location
     */
    public void spawn(Transform<org.spongepowered.api.world.World> location) {
        final Living entity = (Living) (Object) this;
        final World mcWorld = (World) location.getExtent();
        this.setPosition(location.getPosition().getX(), location.getPosition().getY(), location.getPosition().getZ());
        mcWorld.spawnEntity(this);

        this.enablePersistence();
        final PotionEffect effect = PotionEffect.builder()
                .potionType(PotionEffectTypes.INVISIBILITY)
                .duration(9999999).amplifier(1).build();
        final PotionEffectData effects = entity.getOrCreate(PotionEffectData.class).get();
        effects.addElement(effect);
        entity.offer(effects);

        entity.offer(Keys.DISPLAY_NAME, Text.of("PetBlockIdentifier"));
        entity.offer(Keys.CUSTOM_NAME_VISIBLE, false);
    }
}
